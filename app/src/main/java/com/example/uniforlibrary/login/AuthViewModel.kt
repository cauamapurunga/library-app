package com.example.uniforlibrary.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String = "Sucesso!", val isAdmin: Boolean = false) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isLoggedIn: Boolean
        get() = repository.currentUser != null

    // Login (aceita email ou matrícula)
    fun login(emailOuMatricula: String, senha: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (emailOuMatricula.isBlank() || senha.isBlank()) {
                _authState.value = AuthState.Error("Preencha todos os campos")
                return@launch
            }

            val result = repository.login(emailOuMatricula, senha)
            _authState.value = if (result.isSuccess) {
                val isAdmin = repository.isAdmin()
                AuthState.Success("Login realizado com sucesso!", isAdmin)
            } else {
                AuthState.Error(getErrorMessage(result.exceptionOrNull()))
            }
        }
    }

    // Registrar com matrícula
    fun register(nome: String, matricula: String, email: String, senha: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (nome.isBlank() || matricula.isBlank() || email.isBlank() || senha.isBlank()) {
                _authState.value = AuthState.Error("Preencha todos os campos")
                return@launch
            }

            if (senha.length < 6) {
                _authState.value = AuthState.Error("A senha deve ter no mínimo 6 caracteres")
                return@launch
            }

            val result = repository.register(nome, matricula, email, senha)
            _authState.value = if (result.isSuccess) {
                val isAdmin = repository.isAdmin()
                AuthState.Success("Cadastro realizado com sucesso!", isAdmin)
            } else {
                AuthState.Error(getErrorMessage(result.exceptionOrNull()))
            }
        }
    }

    // Resetar senha (aceita email ou matrícula)
    fun resetPassword(emailOuMatricula: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (emailOuMatricula.isBlank()) {
                _authState.value = AuthState.Error("Digite seu email ou matrícula")
                return@launch
            }

            val result = repository.resetPassword(emailOuMatricula)
            _authState.value = if (result.isSuccess) {
                AuthState.Success("Link de recuperação enviado para seu email!")
            } else {
                AuthState.Error(getErrorMessage(result.exceptionOrNull()))
            }
        }
    }

    // Logout
    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    // Verificar se é admin
    suspend fun isAdmin(): Boolean {
        return repository.isAdmin()
    }

    // Resetar estado
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Converter erros do Firebase em mensagens amigáveis
    private fun getErrorMessage(exception: Throwable?): String {
        val message = exception?.message ?: ""
        return when {
            message.contains("Matrícula não encontrada") -> "Matrícula não encontrada. Verifique se você digitou corretamente."
            message.contains("Email não encontrado") -> "Email não encontrado para esta matrícula"
            message.contains("badly formatted") -> "Email inválido. Verifique o formato."
            message.contains("password is invalid") || message.contains("incorrect") -> "Email ou senha incorretos. Verifique suas credenciais."
            message.contains("no user record") || message.contains("user not found") -> "Usuário não encontrado. Verifique o email/matrícula."
            message.contains("already in use") -> "Este email já está cadastrado"
            message.contains("Password should be at least") -> "A senha deve ter no mínimo 6 caracteres"
            message.contains("expired") -> "Credenciais inválidas ou expiradas. Tente novamente."
            message.contains("INVALID_LOGIN_CREDENTIALS") -> "Email ou senha incorretos. Verifique seus dados."
            else -> "Erro ao fazer login: $message"
        }
    }
}
