package com.example.uniforlibrary.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.profile.ProfileRepository
import com.example.uniforlibrary.profile.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String = "Sucesso!") : ProfileState()
    data class Error(val message: String) : ProfileState()
    data class ProfileLoaded(val profile: UserProfile) : ProfileState()
}

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    // Carregar perfil do usuário
    private fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val result = repository.getUserProfile()
            result.fold(
                onSuccess = { profile ->
                    _userProfile.value = profile
                    _profileState.value = ProfileState.ProfileLoaded(profile)
                },
                onFailure = { exception ->
                    _profileState.value = ProfileState.Error(
                        exception.message ?: "Erro ao carregar perfil"
                    )
                }
            )
        }
    }
    // Atualizar email
    fun updateEmail(newEmail: String, currentPassword: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            if (newEmail.isBlank()) {
                _profileState.value = ProfileState.Error("Digite um email válido")
                return@launch
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                _profileState.value = ProfileState.Error("Email inválido")
                return@launch
            }

            if (currentPassword.isBlank()) {
                _profileState.value = ProfileState.Error("Digite sua senha atual para confirmar")
                return@launch
            }

            val result = repository.updateEmail(newEmail, currentPassword)
            if (result.isSuccess) {
                _profileState.value = ProfileState.Success(
                    "📧 Email de verificação enviado para $newEmail!\n\n" +
                    "Verifique sua caixa de entrada e clique no link para confirmar.\n\n" +
                    "⚠️ Seu email será atualizado após a confirmação."
                )
                // NÃO recarregar perfil - email não mudou ainda
            } else {
                _profileState.value = ProfileState.Error(getErrorMessage(result.exceptionOrNull()))
            }
        }
    }

    // Atualizar telefone
    fun updatePhone(phone: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            // Validar telefone (pode estar vazio, pois é opcional)
            if (phone.isNotBlank() && phone.length < 10) {
                _profileState.value = ProfileState.Error("Telefone deve ter pelo menos 10 dígitos")
                return@launch
            }

            val result = repository.updatePhone(phone)
            if (result.isSuccess) {
                _profileState.value = ProfileState.Success("Telefone atualizado com sucesso!")
                loadUserProfile() // Recarregar perfil
            } else {
                _profileState.value = ProfileState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao atualizar telefone"
                )
            }
        }
    }

    // Upload de foto de perfil
    fun uploadProfilePhoto(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            val result = repository.uploadProfilePhoto(context, imageUri)
            if (result.isSuccess) {
                _profileState.value = ProfileState.Success("Foto de perfil atualizada com sucesso!")
                loadUserProfile() // Recarregar perfil
            } else {
                _profileState.value = ProfileState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao atualizar foto de perfil"
                )
            }
        }
    }

    // Fazer logout
    fun logout() {
        repository.logout()
        _profileState.value = ProfileState.Idle
        _userProfile.value = null
    }

    // Resetar estado
    fun resetState() {
        _profileState.value = ProfileState.Idle
    }

    // Mensagens de erro amigáveis
    private fun getErrorMessage(exception: Throwable?): String {
        val errorMsg = exception?.message ?: ""
        return when {
            errorMsg.contains("The email address is already in use by another account") ->
                "Este email já está em uso"
            errorMsg.contains("The password is invalid") || errorMsg.contains("INVALID_PASSWORD") ->
                "Senha atual incorreta"
            errorMsg.contains("The email address is badly formatted") ->
                "Email inválido"
            errorMsg.contains("A network error") ->
                "Erro de conexão. Verifique sua internet"
            errorMsg.contains("Este email já está em uso por outro usuário") ->
                "Este email já está em uso"
            errorMsg.contains("Usuário não autenticado") ->
                "Sessão expirada. Faça login novamente"
            errorMsg.contains("This operation is not allowed") || errorMsg.contains("OPERATION_NOT_ALLOWED") ->
                "Operação não permitida. Verifique as configurações do Firebase Authentication"
            errorMsg.contains("The user's credential is no longer valid") ->
                "Sessão expirada. Faça login novamente"
            errorMsg.contains("An internal error has occurred") ->
                "Erro interno. Tente novamente mais tarde"
            errorMsg.contains("TOO_MANY_ATTEMPTS_TRY_LATER") ->
                "Muitas tentativas. Tente novamente mais tarde"
            errorMsg.contains("novo email deve ser diferente") ->
                "O novo email deve ser diferente do atual"
            else -> errorMsg.ifBlank { "Erro desconhecido ao atualizar email" }
        }
    }
}

