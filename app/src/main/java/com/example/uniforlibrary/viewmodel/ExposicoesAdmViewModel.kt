package com.example.uniforlibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Producao
import com.example.uniforlibrary.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProducaoAdminUiState {
    object Loading : ProducaoAdminUiState()
    data class Success(val producoes: List<Producao>) : ProducaoAdminUiState()
    data class Error(val message: String) : ProducaoAdminUiState()
}

class ProducaoAdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepo = NotificationRepository()

    private val _uiState = MutableStateFlow<ProducaoAdminUiState>(ProducaoAdminUiState.Loading)
    val uiState: StateFlow<ProducaoAdminUiState> = _uiState.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    init {
        loadProducoes()
    }

    fun loadProducoes(
        categoria: String = "",
        status: String = "",
        searchQuery: String = ""
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = ProducaoAdminUiState.Loading

                // Base query
                var query: Query = firestore.collection("producoes")

                // Filtrar por categoria
                if (categoria.isNotEmpty()) {
                    query = query.whereEqualTo("categoria", categoria)
                }

                // Filtrar por status
                if (status.isNotEmpty()) {
                    val statusFiltro = when (status) {
                        "Pendente" -> "pendente"
                        "Aprovado" -> "aprovado"
                        "Reprovado" -> "reprovado"
                        else -> ""
                    }
                    if (statusFiltro.isNotEmpty()) {
                        query = query.whereEqualTo("status", statusFiltro)
                    }
                }

                val snapshot = query.get().await()
                var producoes = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Producao::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("ProducaoAdminVM", "Erro ao converter produção: ${doc.id}", e)
                        null
                    }
                }

                // Ordenar por data no cliente (evita índice composto no Firebase)
                producoes = producoes.sortedByDescending { it.createdAt }

                // Filtrar por busca (título ou autor) - feito no cliente
                if (searchQuery.isNotEmpty()) {
                    producoes = producoes.filter { producao ->
                        producao.titulo.contains(searchQuery, ignoreCase = true) ||
                                producao.usuarioNome.contains(searchQuery, ignoreCase = true)
                    }
                }

                _uiState.value = ProducaoAdminUiState.Success(producoes)

            } catch (e: Exception) {
                android.util.Log.e("ProducaoAdminVM", "Erro ao carregar produções", e)
                _uiState.value = ProducaoAdminUiState.Error("Erro ao carregar produções: ${e.message}")
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
                            "dataAvaliacao" to com.google.firebase.Timestamp.now(),
                            "motivoAvaliacao" to motivo
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
                        android.util.Log.d("ProducaoAdminVM", "Notificação de aprovação enviada")
                    } catch (e: Exception) {
                        android.util.Log.e("ProducaoAdminVM", "Erro ao enviar notificação", e)
                    }
                }

                _actionResult.value = "Produção aprovada com sucesso!"
                loadProducoes() // Recarregar lista

            } catch (e: Exception) {
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
                            "dataAvaliacao" to com.google.firebase.Timestamp.now(),
                            "motivoAvaliacao" to motivo
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
                        android.util.Log.d("ProducaoAdminVM", "Notificação de reprovação enviada")
                    } catch (e: Exception) {
                        android.util.Log.e("ProducaoAdminVM", "Erro ao enviar notificação", e)
                    }
                }

                _actionResult.value = "Produção reprovada."
                loadProducoes() // Recarregar lista

            } catch (e: Exception) {
                _actionResult.value = "Erro ao reprovar produção: ${e.message}"
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}