package com.example.uniforlibrary.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Producao
import com.example.uniforlibrary.repository.ProducaoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProducaoUiState {
    object Idle : ProducaoUiState()
    object Loading : ProducaoUiState()
    data class Success(val message: String) : ProducaoUiState()
    data class Error(val message: String) : ProducaoUiState()
}

class ProducaoViewModel : ViewModel() {
    private val repository = ProducaoRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ProducaoUiState>(ProducaoUiState.Idle)
    val uiState: StateFlow<ProducaoUiState> = _uiState.asStateFlow()

    private val _producaoId = MutableStateFlow<String?>(null)
    val producaoId: StateFlow<String?> = _producaoId.asStateFlow()

    fun submitProducao(
        context: Context,
        titulo: String,
        categoria: String,
        fotoUri: Uri?,
        arquivoUri: Uri?
    ) {
        viewModelScope.launch {
            var tempProducaoId: String? = null
            try {
                _uiState.value = ProducaoUiState.Loading

                // Validações
                if (titulo.isBlank()) {
                    _uiState.value = ProducaoUiState.Error("O título é obrigatório")
                    return@launch
                }

                if (arquivoUri == null) {
                    _uiState.value = ProducaoUiState.Error("Selecione o arquivo da produção")
                    return@launch
                }

                // CORREÇÃO: Validar categoria para evitar inconsistências
                val categoriaFinal = categoria.trim().ifEmpty { "Cordel" }

                // Obter dados do usuário
                val userId = auth.currentUser?.uid ?: throw Exception("Usuário não autenticado")
                val userDoc = firestore.collection("usuarios").document(userId).get().await()
                val userName = userDoc.getString("nome") ?: "Usuário"

                // CORREÇÃO: Primeiro fazer upload do PDF ANTES de criar a produção
                // Gerar ID temporário para o upload
                val tempId = firestore.collection("producoes").document().id
                tempProducaoId = tempId

                android.util.Log.d("ProducaoViewModel", "Iniciando upload do PDF antes de criar produção...")

                // Upload do arquivo (obrigatório) - PRIMEIRO!
                val arquivoResult = repository.uploadArquivoProducao(context, tempId, arquivoUri)

                arquivoResult.onFailure { e ->
                    // Se o PDF falhar, não criar a produção
                    android.util.Log.e("ProducaoViewModel", "Falha no upload do PDF, abortando criação da produção", e)
                    _uiState.value = ProducaoUiState.Error(
                        "Erro ao fazer upload do arquivo: ${e.message}\n\nA produção não foi criada."
                    )
                    return@launch
                }

                // Se chegou aqui, o PDF foi enviado com sucesso
                val arquivoUrl = arquivoResult.getOrNull() ?: ""

                android.util.Log.d("ProducaoViewModel", "PDF enviado com sucesso! URL: $arquivoUrl")

                // Upload da foto (opcional)
                var fotoUrl = ""
                if (fotoUri != null) {
                    val fotoResult = repository.uploadFotoProducao(context, tempId, fotoUri)
                    fotoResult.onSuccess { url ->
                        fotoUrl = url
                    }.onFailure { e ->
                        android.util.Log.e("ProducaoViewModel", "Erro ao fazer upload da foto (continuando mesmo assim)", e)
                    }
                }

                // AGORA SIM criar produção no Firestore com as URLs já prontas
                val producao = Producao(
                    id = tempId,
                    titulo = titulo,
                    categoria = categoriaFinal,
                    usuarioId = userId,
                    usuarioNome = userName,
                    status = "pendente",
                    fotoUrl = fotoUrl,
                    arquivoUrl = arquivoUrl
                )

                val result = repository.addProducao(producao)

                result.onSuccess {
                    _producaoId.value = tempId
                    _uiState.value = ProducaoUiState.Success(
                        "Produção enviada com sucesso! Aguarde a avaliação do comitê."
                    )
                }.onFailure { e ->
                    // Se falhar ao criar no Firestore mas o PDF já foi enviado
                    android.util.Log.e("ProducaoViewModel", "Erro ao salvar no Firestore (mas PDF já foi enviado)", e)
                    _uiState.value = ProducaoUiState.Error("Erro ao salvar produção: ${e.message}")
                }

            } catch (e: Exception) {
                android.util.Log.e("ProducaoViewModel", "Erro inesperado", e)
                _uiState.value = ProducaoUiState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProducaoUiState.Idle
        _producaoId.value = null
    }
}

