package com.example.uniforlibrary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Notification
import com.example.uniforlibrary.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    companion object {
        private const val TAG = "NotificationViewModel"
    }

    init {
        loadNotifications()
    }

    /**
     * Carregar notificações do usuário
     */
    fun loadNotifications() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Log.w(TAG, "Usuário não autenticado")
            _isLoading.value = false
            _notifications.value = emptyList()
            _unreadCount.value = 0
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.getUserNotifications(currentUserId)
                    .onSuccess { notificationsList ->
                        _notifications.value = notificationsList
                        _unreadCount.value = notificationsList.count { !it.isRead }
                        Log.d(TAG, "Notificações carregadas: ${notificationsList.size}, Não lidas: ${_unreadCount.value}")
                    }
                    .onFailure { exception ->
                        _errorMessage.value = "Erro ao carregar notificações: ${exception.message}"
                        _notifications.value = emptyList()
                        _unreadCount.value = 0
                        Log.e(TAG, "Erro ao carregar notificações", exception)
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Erro inesperado: ${e.message}"
                _notifications.value = emptyList()
                _unreadCount.value = 0
                Log.e(TAG, "Erro inesperado ao carregar notificações", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Atualizar contagem de não lidas
     */
    fun updateUnreadCount() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            _unreadCount.value = 0
            return
        }

        viewModelScope.launch {
            try {
                repository.getUnreadCount(currentUserId)
                    .onSuccess { count ->
                        _unreadCount.value = count
                        Log.d(TAG, "Contagem atualizada: $count não lidas")
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Erro ao atualizar contagem", exception)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Erro inesperado ao atualizar contagem", e)
            }
        }
    }

    /**
     * Marcar notificação como lida
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
                .onSuccess {
                    // Atualizar lista local
                    _notifications.value = _notifications.value.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    updateUnreadCount()
                }
                .onFailure { exception ->
                    Log.e(TAG, "Erro ao marcar como lida", exception)
                }
        }
    }

    /**
     * Marcar todas como lidas
     */
    fun markAllAsRead() {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            repository.markAllAsRead(currentUserId)
                .onSuccess {
                    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                    _unreadCount.value = 0
                }
                .onFailure { exception ->
                    _errorMessage.value = "Erro ao marcar todas como lidas"
                    Log.e(TAG, "Erro ao marcar todas como lidas", exception)
                }
        }
    }

    /**
     * Deletar notificação
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
                .onSuccess {
                    _notifications.value = _notifications.value.filter { it.id != notificationId }
                    updateUnreadCount()
                }
                .onFailure { exception ->
                    _errorMessage.value = "Erro ao deletar notificação"
                    Log.e(TAG, "Erro ao deletar notificação", exception)
                }
        }
    }

    /**
     * Deletar todas as notificações
     */
    fun deleteAllNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            repository.deleteAllNotifications(currentUserId)
                .onSuccess {
                    _notifications.value = emptyList()
                    _unreadCount.value = 0
                }
                .onFailure { exception ->
                    _errorMessage.value = "Erro ao deletar notificações"
                    Log.e(TAG, "Erro ao deletar todas", exception)
                }
        }
    }

    /**
     * Limpar mensagem de erro
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
