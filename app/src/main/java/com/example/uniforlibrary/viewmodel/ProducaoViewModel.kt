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

                // Obter dados do usuário
                val userId = auth.currentUser?.uid ?: throw Exception("Usuário não autenticado")
                val userDoc = firestore.collection("usuarios").document(userId).get().await()
                val userName = userDoc.getString("nome") ?: "Usuário"

                // Criar produção no Firestore
                val producao = Producao(
                    titulo = titulo,
                    categoria = categoria,
                    usuarioId = userId,
                    usuarioNome = userName,
                    status = "pendente"
                )

                val result = repository.addProducao(producao)

                result.onSuccess { producaoId ->
                    _producaoId.value = producaoId

                    // Upload da foto (opcional)
                    if (fotoUri != null) {
                        val fotoResult = repository.uploadFotoProducao(context, producaoId, fotoUri)
                        fotoResult.onFailure { e ->
                            android.util.Log.e("ProducaoViewModel", "Erro ao fazer upload da foto", e)
                        }
                    }

                    // Upload do arquivo (obrigatório)
                    val arquivoResult = repository.uploadArquivoProducao(context, producaoId, arquivoUri)

                    arquivoResult.onSuccess {
                        _uiState.value = ProducaoUiState.Success(
                            "Produção enviada com sucesso! Aguarde a avaliação do comitê."
                        )
                    }.onFailure { e ->
                        _uiState.value = ProducaoUiState.Error(
                            "Erro ao fazer upload do arquivo: ${e.message}"
                        )
                    }

                }.onFailure { e ->
                    _uiState.value = ProducaoUiState.Error("Erro ao criar produção: ${e.message}")
                }

            } catch (e: Exception) {
                _uiState.value = ProducaoUiState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProducaoUiState.Idle
        _producaoId.value = null
    }
}

