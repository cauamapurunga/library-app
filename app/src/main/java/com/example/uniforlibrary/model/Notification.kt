package com.example.uniforlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de Notificação
 * Tipos: RESERVA_APROVADA, RESERVA_REJEITADA, NOVA_RESERVA, PRODUCAO_SUBMETIDA
 */
data class Notification(
    var id: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "", // Tipo da notificação

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("message")
    @set:PropertyName("message")
    var message: String = "",

    @get:PropertyName("reference_id")
    @set:PropertyName("reference_id")
    var referenceId: String = "", // ID da reserva, produção, etc

    @get:PropertyName("reference_type")
    @set:PropertyName("reference_type")
    var referenceType: String = "", // "reservation", "production"

    @get:PropertyName("is_read")
    @set:PropertyName("is_read")
    var isRead: Boolean = false,

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp = Timestamp.now(),

    @get:PropertyName("priority")
    @set:PropertyName("priority")
    var priority: String = "NORMAL" // HIGH, NORMAL, LOW
)

// Tipos de notificação implementados
object NotificationType {
    // Para usuários
    const val RESERVA_APROVADA = "RESERVA_APROVADA"
    const val RESERVA_REJEITADA = "RESERVA_REJEITADA"
    const val PRODUCAO_APROVADA = "PRODUCAO_APROVADA"
    const val PRODUCAO_REPROVADA = "PRODUCAO_REPROVADA"
    const val LIVRO_ATRASADO = "LIVRO_ATRASADO"

    // Para admins
    const val NOVA_RESERVA = "NOVA_RESERVA"
    const val PRODUCAO_SUBMETIDA = "PRODUCAO_SUBMETIDA"
}

// Prioridades
object NotificationPriority {
    const val HIGH = "HIGH"
    const val NORMAL = "NORMAL"
    const val LOW = "LOW"
}
