package com.example.uniforlibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Producao
import com.example.uniforlibrary.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ExposicaoDetailUiState {
    object Loading : ExposicaoDetailUiState()
    data class Success(val producao: Producao) : ExposicaoDetailUiState()
    data class Error(val message: String) : ExposicaoDetailUiState()
}

class ExposicaoDetailAdmViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepo = NotificationRepository()

    private val _uiState = MutableStateFlow<ExposicaoDetailUiState>(ExposicaoDetailUiState.Loading)
    val uiState: StateFlow<ExposicaoDetailUiState> = _uiState.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    fun loadProducao(producaoId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ExposicaoDetailUiState.Loading

                val doc = firestore.collection("producoes")
                    .document(producaoId)
                    .get()
                    .await()

                val producao = doc.toObject(Producao::class.java)?.copy(id = doc.id)
                    ?: throw Exception("Produção não encontrada")

                _uiState.value = ExposicaoDetailUiState.Success(producao)

            } catch (e: Exception) {
                android.util.Log.e("ExposicaoDetailVM", "Erro ao carregar produção", e)
                _uiState.value = ExposicaoDetailUiState.Error("Erro ao carregar produção: ${e.message}")
            }
        }
    }

    fun aprovarProducao(producaoId: String, motivo: String = "") {
        viewModelScope.launch {
            try {
                // Buscar dados da produção antes de aprovar
                val doc = firestore.collection("producoes")
                    .document(producaoId)
                    .get()
                    .await()

                val producao = doc.toObject(Producao::class.java)?.copy(id = doc.id)

                firestore.collection("producoes")
                    .document(producaoId)
                    .update(
                        mapOf(
                            "status" to "aprovado",
                            "data_avaliacao" to com.google.firebase.Timestamp.now(),
                            "motivo_avaliacao" to motivo,
                            "updated_at" to com.google.firebase.Timestamp.now()
                        )
                    ).await()

                // NOTIFICAÇÃO: Notificar usuário sobre aprovação
                producao?.let {
                    try {
                        notificationRepo.notifyProductionApproved(
                            userId = it.usuarioId,
                            productionTitle = it.titulo,
                            productionId = producaoId
                        )
                        android.util.Log.d("ExposicaoDetailVM", "Notificação de aprovação enviada")
                    } catch (e: Exception) {
                        android.util.Log.e("ExposicaoDetailVM", "Erro ao enviar notificação", e)
                    }
                }

                _actionResult.value = "Produção aprovada com sucesso!"
                // Recarregar produção para atualizar status na tela
                loadProducao(producaoId)

            } catch (e: Exception) {
                android.util.Log.e("ExposicaoDetailVM", "Erro ao aprovar produção", e)
                _actionResult.value = "Erro ao aprovar produção: ${e.message}"
            }
        }
    }

    fun reprovarProducao(producaoId: String, motivo: String) {
        viewModelScope.launch {
            try {
                if (motivo.isBlank()) {
                    _actionResult.value = "Informe o motivo da reprovação"
                    return@launch
                }

                // Buscar dados da produção antes de reprovar
                val doc = firestore.collection("producoes")
                    .document(producaoId)
                    .get()
                    .await()

                val producao = doc.toObject(Producao::class.java)?.copy(id = doc.id)

                firestore.collection("producoes")
                    .document(producaoId)
                    .update(
                        mapOf(
                            "status" to "reprovado",
                            "data_avaliacao" to com.google.firebase.Timestamp.now(),
                            "motivo_avaliacao" to motivo,
                            "updated_at" to com.google.firebase.Timestamp.now()
                        )
                    ).await()

                // NOTIFICAÇÃO: Notificar usuário sobre reprovação
                producao?.let {
                    try {
                        notificationRepo.notifyProductionRejected(
                            userId = it.usuarioId,
                            productionTitle = it.titulo,
                            productionId = producaoId,
                            reason = motivo
                        )
                        android.util.Log.d("ExposicaoDetailVM", "Notificação de reprovação enviada")
                    } catch (e: Exception) {
                        android.util.Log.e("ExposicaoDetailVM", "Erro ao enviar notificação", e)
                    }
                }

                _actionResult.value = "Produção reprovada."
                // Recarregar produção para atualizar status na tela
                loadProducao(producaoId)

            } catch (e: Exception) {
                android.util.Log.e("ExposicaoDetailVM", "Erro ao reprovar produção", e)
                _actionResult.value = "Erro ao reprovar produção: ${e.message}"
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}