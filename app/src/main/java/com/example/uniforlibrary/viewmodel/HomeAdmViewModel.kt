package com.example.uniforlibrary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.repository.HomeAdmRepository
import com.example.uniforlibrary.repository.HomeMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeAdmUiState {
    object Loading : HomeAdmUiState()
    data class Success(val metrics: HomeMetrics) : HomeAdmUiState()
    data class Error(val message: String) : HomeAdmUiState()
}

class HomeAdmViewModel : ViewModel() {
    private val repository = HomeAdmRepository()

    private val _uiState = MutableStateFlow<HomeAdmUiState>(HomeAdmUiState.Loading)
    val uiState: StateFlow<HomeAdmUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "HomeAdmViewModel"
    }

    init {
        loadMetrics()
    }

    fun loadMetrics() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeAdmUiState.Loading

                val result = repository.getHomeMetrics()

                if (result.isSuccess) {
                    val metrics = result.getOrNull()!!
                    _uiState.value = HomeAdmUiState.Success(metrics)
                    Log.d(TAG, "Métricas carregadas com sucesso")
                } else {
                    _uiState.value = HomeAdmUiState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao carregar métricas"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar métricas", e)
                _uiState.value = HomeAdmUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}

