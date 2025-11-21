package com.example.uniforlibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Producao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ExposicoesUiState {
    object Loading : ExposicoesUiState()
    data class Success(val producoes: List<Producao>) : ExposicoesUiState()
    data class Error(val message: String) : ExposicoesUiState()
}

class ExposicoesViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ExposicoesUiState>(ExposicoesUiState.Loading)
    val uiState: StateFlow<ExposicoesUiState> = _uiState.asStateFlow()

    init {
        loadApprovedProducoes()
    }

    fun loadApprovedProducoes(
        categoria: String = "",
        searchQuery: String = ""
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = ExposicoesUiState.Loading

                // Base query - apenas status aprovado
                var query: Query = firestore.collection("producoes")
                    .whereEqualTo("status", "aprovado")

                // Se tiver categoria, adiciona o filtro
                if (categoria.isNotEmpty()) {
                    query = query.whereEqualTo("categoria", categoria)
                }

                // Buscar documentos
                val snapshot = query.get().await()
                var producoes = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Producao::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("ExposicoesVM", "Erro ao converter produção: ${doc.id}", e)
                        null
                    }
                }

                // Ordenar por data no cliente (evita índice composto)
                producoes = producoes.sortedByDescending { it.createdAt }

                // Filtrar por busca (título ou autor) - feito no cliente
                if (searchQuery.isNotEmpty()) {
                    producoes = producoes.filter { producao ->
                        producao.titulo.contains(searchQuery, ignoreCase = true) ||
                                producao.usuarioNome.contains(searchQuery, ignoreCase = true)
                    }
                }

                _uiState.value = ExposicoesUiState.Success(producoes)

            } catch (e: Exception) {
                android.util.Log.e("ExposicoesVM", "Erro ao carregar exposições", e)
                _uiState.value = ExposicoesUiState.Error("Erro ao carregar exposições: ${e.message}")
            }
        }
    }
}

