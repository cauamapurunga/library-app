package com.example.uniforlibrary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.model.ReservationStatus
import com.example.uniforlibrary.repository.ReservationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar as reservas do usuário logado
 */
class UserReservationViewModel : ViewModel() {
    private val repository = ReservationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UserReservationUiState>(UserReservationUiState.Loading)
    val uiState: StateFlow<UserReservationUiState> = _uiState.asStateFlow()


    private val _userReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val userReservations: StateFlow<List<Reservation>> = _userReservations.asStateFlow()


    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    companion object {
        private const val TAG = "UserReservationViewModel"
    }

    init {
        loadUserReservations()
    }


    fun loadUserReservations() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = UserReservationUiState.Error("Usuário não autenticado")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UserReservationUiState.Loading

                repository.getUserReservations(currentUser.uid).collect { reservations ->
                    _userReservations.value = reservations
                    _uiState.value = if (reservations.isEmpty()) {
                        UserReservationUiState.Empty
                    } else {
                        UserReservationUiState.Success(reservations)
                    }
                    Log.d(TAG, "Reservas do usuário carregadas: ${reservations.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar reservas do usuário", e)
                _uiState.value = UserReservationUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }


    fun getReservationsByDisplayStatus(displayStatus: String): List<Reservation> {
        return when (displayStatus) {
            "Todos" -> _userReservations.value
            "Disponíveis" -> _userReservations.value.filter {
                it.status == ReservationStatus.APROVADA.value // Aprovado = disponível para retirar
            }
            "Aguardando" -> _userReservations.value.filter {
                it.status == ReservationStatus.PENDENTE.value // Aguardando aprovação do admin
            }
            "Devolvidos" -> _userReservations.value.filter {
                it.status == ReservationStatus.RETIRADO.value // Já foi retirado
            }
            else -> _userReservations.value
        }
    }


    fun confirmWithdrawal(
        reservationId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.confirmWithdrawalByUser(reservationId)

                result.onSuccess {
                    _feedbackMessage.value = "Retirada confirmada! Vá buscar o livro na biblioteca."
                    Log.d(TAG, "Retirada confirmada: $reservationId")
                    onSuccess()

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao confirmar retirada"
                    _feedbackMessage.value = errorMsg
                    Log.e(TAG, "Erro ao confirmar retirada", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = errorMsg
                Log.e(TAG, "Exceção ao confirmar retirada", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }


    fun cancelReservation(
        reservationId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.cancelReservation(reservationId)

                result.onSuccess {
                    _feedbackMessage.value = "Reserva cancelada com sucesso!"
                    Log.d(TAG, "Reserva cancelada: $reservationId")
                    onSuccess()

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao cancelar reserva"
                    _feedbackMessage.value = errorMsg
                    Log.e(TAG, "Erro ao cancelar reserva", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = errorMsg
                Log.e(TAG, "Exceção ao cancelar reserva", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }


    fun getDisplayStatus(reservation: Reservation): String {
        return when (reservation.status) {
            ReservationStatus.PENDENTE.value -> "Aguardando Aprovação"
            ReservationStatus.APROVADA.value -> "Disponível para Retirada"
            ReservationStatus.AGUARDANDO_RETIRADA.value -> "Aguardando Retirada"
            ReservationStatus.REJEITADA.value -> "Rejeitada"
            ReservationStatus.RETIRADO.value -> "Retirado"
            ReservationStatus.EXPIRADA.value -> "Expirada"
            ReservationStatus.CANCELADA.value -> "Cancelada"
            else -> reservation.status
        }
    }


    fun canCancelReservation(reservation: Reservation): Boolean {
        return reservation.status == ReservationStatus.PENDENTE.value ||
               reservation.status == ReservationStatus.APROVADA.value
    }


    fun canConfirmWithdrawal(reservation: Reservation): Boolean {
        return reservation.status == ReservationStatus.APROVADA.value
    }


    fun isWaitingForAdminPickup(reservation: Reservation): Boolean {
        return reservation.status == ReservationStatus.AGUARDANDO_RETIRADA.value
    }

    /**
     * Limpa a mensagem de feedback
     */
    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }
}

/**
 * Estados possíveis da UI para reservas do usuário
 */
sealed class UserReservationUiState {
    object Loading : UserReservationUiState()
    object Empty : UserReservationUiState()
    data class Success(val reservations: List<Reservation>) : UserReservationUiState()
    data class Error(val message: String) : UserReservationUiState()
}

