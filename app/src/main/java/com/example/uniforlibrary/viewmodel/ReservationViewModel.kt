package com.example.uniforlibrary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.model.ReservationStatus
import com.example.uniforlibrary.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar o estado e lógica de negócio das Reservas (Admin)
 */
class ReservationViewModel : ViewModel() {
    private val repository = ReservationRepository()

    // Estado da UI
    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Loading)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    // Lista de todas as reservas
    private val _allReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val allReservations: StateFlow<List<Reservation>> = _allReservations.asStateFlow()

    // Estatísticas
    private val _stats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stats: StateFlow<Map<String, Int>> = _stats.asStateFlow()

    // Mensagens de feedback
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    companion object {
        private const val TAG = "ReservationViewModel"
    }

    init {
        loadAllReservations()
        loadStats()
    }

    /**
     * Carrega todas as reservas em tempo real
     */
    fun loadAllReservations() {
        viewModelScope.launch {
            try {
                _uiState.value = ReservationUiState.Loading

                repository.getAllReservations().collect { reservations ->
                    _allReservations.value = reservations
                    _uiState.value = if (reservations.isEmpty()) {
                        ReservationUiState.Empty
                    } else {
                        ReservationUiState.Success(reservations)
                    }
                    Log.d(TAG, "Reservas carregadas: ${reservations.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar reservas", e)
                _uiState.value = ReservationUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    /**
     * Filtra reservas por status
     */
    fun getReservationsByStatus(status: String): List<Reservation> {
        return when (status) {
            "Todos" -> _allReservations.value
            "Pendentes" -> _allReservations.value.filter {
                it.status == ReservationStatus.PENDENTE.value
            }
            "Aprovados" -> _allReservations.value.filter {
                it.status == ReservationStatus.APROVADA.value ||
                it.status == ReservationStatus.REJEITADA.value
            }
            "Retirados" -> _allReservations.value.filter {
                it.status == ReservationStatus.RETIRADO.value ||
                it.status == ReservationStatus.EXPIRADA.value
            }
            else -> _allReservations.value
        }
    }

    /**
     * Aprova uma reserva
     */
    fun approveReservation(
        reservationId: String,
        adminId: String? = null,
        adminNotes: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.approveReservation(reservationId, adminId, adminNotes)

                result.onSuccess {
                    _feedbackMessage.value = "Reserva aprovada com sucesso!"
                    Log.d(TAG, "Reserva aprovada: $reservationId")
                    onSuccess()

                    // Limpar mensagem após 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao aprovar reserva"
                    _feedbackMessage.value = errorMsg
                    Log.e(TAG, "Erro ao aprovar reserva", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = errorMsg
                Log.e(TAG, "Exceção ao aprovar reserva", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }

    /**
     * Rejeita uma reserva
     */
    fun rejectReservation(
        reservationId: String,
        reason: String? = null,
        adminId: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.rejectReservation(reservationId, reason, adminId)

                result.onSuccess {
                    _feedbackMessage.value = "Reserva rejeitada com sucesso!"
                    Log.d(TAG, "Reserva rejeitada: $reservationId")
                    onSuccess()

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao rejeitar reserva"
                    _feedbackMessage.value = errorMsg
                    Log.e(TAG, "Erro ao rejeitar reserva", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = errorMsg
                Log.e(TAG, "Exceção ao rejeitar reserva", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }

    /**
     * Marca uma reserva como retirada
     */
    fun markAsWithdrawn(
        reservationId: String,
        adminId: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.markAsWithdrawn(reservationId, adminId)

                result.onSuccess {
                    _feedbackMessage.value = "Reserva marcada como retirada!"
                    Log.d(TAG, "Reserva marcada como retirada: $reservationId")
                    onSuccess()

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao marcar como retirada"
                    _feedbackMessage.value = errorMsg
                    Log.e(TAG, "Erro ao marcar como retirada", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = errorMsg
                Log.e(TAG, "Exceção ao marcar como retirada", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }

    /**
     * Marca uma reserva como expirada
     */
    fun markAsExpired(
        reservationId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.markAsExpired(reservationId)

                result.onSuccess {
                    _feedbackMessage.value = "Reserva marcada como expirada!"
                    Log.d(TAG, "Reserva marcada como expirada: $reservationId")
                    onSuccess()

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao marcar como expirada"
                    _feedbackMessage.value = errorMsg
                    Log.e(TAG, "Erro ao marcar como expirada", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = errorMsg
                Log.e(TAG, "Exceção ao marcar como expirada", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }

    /**
     * Verifica e expira reservas automaticamente
     */
    fun checkAndExpireReservations() {
        viewModelScope.launch {
            try {
                val result = repository.checkAndExpireReservations()

                result.onSuccess { count ->
                    if (count > 0) {
                        _feedbackMessage.value = "$count reserva(s) expirada(s) automaticamente"
                        Log.d(TAG, "$count reservas expiradas")

                        kotlinx.coroutines.delay(3000)
                        _feedbackMessage.value = null
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Erro ao verificar reservas expiradas", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao verificar reservas expiradas", e)
            }
        }
    }

    /**
     * Carrega estatísticas das reservas
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                val result = repository.getReservationStats()

                result.onSuccess { stats ->
                    _stats.value = stats
                    Log.d(TAG, "Estatísticas carregadas: $stats")
                }

                result.onFailure { error ->
                    Log.e(TAG, "Erro ao carregar estatísticas", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao carregar estatísticas", e)
            }
        }
    }

    /**
     * Busca uma reserva específica
     */
    fun getReservationById(reservationId: String): Reservation? {
        return _allReservations.value.find { it.id == reservationId }
    }

    /**
     * Limpa a mensagem de feedback
     */
    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }
}

/**
 * Estados possíveis da UI
 */
sealed class ReservationUiState {
    object Loading : ReservationUiState()
    object Empty : ReservationUiState()
    data class Success(val reservations: List<Reservation>) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}

