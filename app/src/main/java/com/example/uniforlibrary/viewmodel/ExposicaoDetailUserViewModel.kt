package com.example.uniforlibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Producao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProducaoDetailUiState {
    object Loading : ProducaoDetailUiState()
    data class Success(val producao: Producao) : ProducaoDetailUiState()
    data class Error(val message: String) : ProducaoDetailUiState()
}

class ExposicaoDetailUserViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ProducaoDetailUiState>(ProducaoDetailUiState.Loading)
    val uiState: StateFlow<ProducaoDetailUiState> = _uiState

    fun loadProducao(producaoId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ProducaoDetailUiState.Loading

                val document = firestore.collection("producoes")
                    .document(producaoId)
                    .get()
                    .await()

                if (document.exists()) {
                    val producao = document.toObject(Producao::class.java)

                    if (producao != null) {
                        // Verifica se a produção está aprovada
                        if (producao.status == "aprovado") {
                            _uiState.value = ProducaoDetailUiState.Success(producao)
                        } else {
                            _uiState.value = ProducaoDetailUiState.Error(
                                "Esta produção não está disponível para visualização."
                            )
                        }
                    } else {
                        _uiState.value = ProducaoDetailUiState.Error(
                            "Erro ao carregar os dados da produção."
                        )
                    }
                } else {
                    _uiState.value = ProducaoDetailUiState.Error(
                        "Produção não encontrada."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ProducaoDetailUiState.Error(
                    "Erro ao carregar produção: ${e.message}"
                )
            }
        }
    }
}