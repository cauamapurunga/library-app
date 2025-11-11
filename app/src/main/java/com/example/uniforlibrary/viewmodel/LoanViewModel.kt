package com.example.uniforlibrary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Loan
import com.example.uniforlibrary.repository.LoanRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar empréstimos na UI
 */
class LoanViewModel : ViewModel() {
    private val repository = LoanRepository()
    private val auth = FirebaseAuth.getInstance()

    // Estado da UI (Loading, Success, Empty, Error)
    private val _uiState = MutableStateFlow<LoanUiState>(LoanUiState.Loading)
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    // Lista de empréstimos do usuário
    private val _userLoans = MutableStateFlow<List<Loan>>(emptyList())
    val userLoans: StateFlow<List<Loan>> = _userLoans.asStateFlow()

    // Mensagens de feedback para o usuário
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    companion object {
        private const val TAG = "LoanViewModel"
    }

    init {
        loadUserLoans()
    }

    /**
     * Carrega os empréstimos do usuário logado
     * Usa Flow para atualizar em tempo real quando houver mudanças no Firebase
     */
    fun loadUserLoans() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = LoanUiState.Error("Usuário não autenticado")
                    return@launch
                }

                _uiState.value = LoanUiState.Loading

                // Escuta mudanças em tempo real
                repository.getUserLoans(userId).collect { loans ->
                    _userLoans.value = loans
                    _uiState.value = if (loans.isEmpty()) {
                        LoanUiState.Empty
                    } else {
                        LoanUiState.Success(loans)
                    }
                    Log.d(TAG, "✅ Empréstimos carregados: ${loans.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar empréstimos", e)
                _uiState.value = LoanUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    /**
     * Filtra empréstimos por status (para as tabs)
     */
    fun getLoansByStatus(status: String): List<Loan> {
        return when (status) {
            "Todos" -> _userLoans.value
            "Ativos" -> _userLoans.value.filter {
                it.status == "Ativo"
            }
            "Atrasados" -> _userLoans.value.filter {
                it.status == "Atrasado" || it.isLate()
            }
            "Devolvidos" -> _userLoans.value.filter {
                it.status == "Devolvido"
            }
            else -> _userLoans.value
        }
    }

    /**
     * Renovar empréstimo (adiciona 7 dias)
     */
    fun renewLoan(
        loanId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = repository.renewLoan(loanId)

                result.onSuccess {
                    _feedbackMessage.value = "✅ Empréstimo renovado com sucesso! +7 dias"
                    Log.d(TAG, "Empréstimo renovado: $loanId")
                    onSuccess()

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }

                result.onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao renovar empréstimo"
                    _feedbackMessage.value = "❌ $errorMsg"
                    Log.e(TAG, "Erro ao renovar empréstimo", error)
                    onError(errorMsg)

                    kotlinx.coroutines.delay(3000)
                    _feedbackMessage.value = null
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido"
                _feedbackMessage.value = "❌ $errorMsg"
                Log.e(TAG, "Exceção ao renovar empréstimo", e)
                onError(errorMsg)

                kotlinx.coroutines.delay(3000)
                _feedbackMessage.value = null
            }
        }
    }

    /**
     * Verificar empréstimos atrasados
     */
    fun checkLateLoans() {
        viewModelScope.launch {
            try {
                repository.checkAndUpdateLateLoans()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar empréstimos atrasados", e)
            }
        }
    }
}

/**
 * Estados possíveis da tela de empréstimos
 */
sealed class LoanUiState {
    object Loading : LoanUiState()
    object Empty : LoanUiState()
    data class Success(val loans: List<Loan>) : LoanUiState()
    data class Error(val message: String) : LoanUiState()
}

