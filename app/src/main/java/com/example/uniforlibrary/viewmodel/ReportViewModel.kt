package com.example.uniforlibrary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.repository.PopularBook
import com.example.uniforlibrary.repository.ReportRepository
import com.example.uniforlibrary.repository.ReportStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

sealed class ReportUiState {
    object Loading : ReportUiState()
    data class Success(val stats: ReportStats, val popularBooks: List<PopularBook>) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

class ReportViewModel : ViewModel() {
    private val repository = ReportRepository()

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val _startDate = MutableStateFlow<Date?>(null)
    val startDate: StateFlow<Date?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Date?>(null)
    val endDate: StateFlow<Date?> = _endDate.asStateFlow()

    companion object {
        private const val TAG = "ReportViewModel"
    }

    init {
        loadReports()
    }

    fun setDateRange(start: Date?, end: Date?) {
        _startDate.value = start
        _endDate.value = end
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            try {
                _uiState.value = ReportUiState.Loading

                // Buscar estatísticas
                val statsResult = repository.getStats(_startDate.value, _endDate.value)
                if (statsResult.isFailure) {
                    _uiState.value = ReportUiState.Error(
                        statsResult.exceptionOrNull()?.message ?: "Erro ao carregar estatísticas"
                    )
                    return@launch
                }

                // Buscar livros populares
                val booksResult = repository.getPopularBooks(_startDate.value, _endDate.value, 10)
                if (booksResult.isFailure) {
                    _uiState.value = ReportUiState.Error(
                        booksResult.exceptionOrNull()?.message ?: "Erro ao carregar livros populares"
                    )
                    return@launch
                }

                val stats = statsResult.getOrNull()!!
                val books = booksResult.getOrNull()!!

                _uiState.value = ReportUiState.Success(stats, books)
                Log.d(TAG, "Relatórios carregados com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar relatórios", e)
                _uiState.value = ReportUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun calculatePercentageChange(current: Int, previous: Int): String {
        if (previous == 0) {
            return if (current > 0) "+100%" else "0%"
        }
        val change = ((current - previous).toDouble() / previous.toDouble()) * 100
        return if (change >= 0) {
            "+%.0f%%".format(change)
        } else {
            "%.0f%%".format(change)
        }
    }

    fun calculatePercentageChange(current: Double, previous: Double): String {
        if (previous == 0.0) {
            return if (current > 0) "+100%" else "0%"
        }
        val change = ((current - previous) / previous) * 100
        return if (change >= 0) {
            "+%.0f%%".format(change)
        } else {
            "%.0f%%".format(change)
        }
    }
}

