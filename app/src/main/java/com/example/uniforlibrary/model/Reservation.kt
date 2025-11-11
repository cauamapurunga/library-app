package com.example.uniforlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de dados para Reserva
 * Estados possíveis: Pendente -> Aprovada/Rejeitada -> Retirado/Expirada
 */
data class Reservation(
    var id: String = "",

    @get:PropertyName("book_id")
    @set:PropertyName("book_id")
    var bookId: String = "",

    @get:PropertyName("book_title")
    @set:PropertyName("book_title")
    var bookTitle: String = "",

    @get:PropertyName("book_author")
    @set:PropertyName("book_author")
    var bookAuthor: String = "",

    @get:PropertyName("book_cover_url")
    @set:PropertyName("book_cover_url")
    var bookCoverUrl: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("user_name")
    @set:PropertyName("user_name")
    var userName: String = "",

    @get:PropertyName("user_matricula")
    @set:PropertyName("user_matricula")
    var userMatricula: String = "",

    @get:PropertyName("user_email")
    @set:PropertyName("user_email")
    var userEmail: String = "",

    /**
     * Status da reserva:
     * - "Pendente": Aguardando aprovação do administrador
     * - "Aprovada": Aprovada pelo admin, aguardando retirada
     * - "Rejeitada": Rejeitada pelo admin
     * - "Retirado": Livro retirado pelo estudante
     * - "Expirada": Prazo de retirada expirado
     * - "Cancelada": Cancelada pelo usuário
     */
    var status: String = "Pendente",

    @get:PropertyName("request_date")
    @set:PropertyName("request_date")
    var requestDate: Timestamp = Timestamp.now(),

    @get:PropertyName("approval_date")
    @set:PropertyName("approval_date")
    var approvalDate: Timestamp? = null,

    @get:PropertyName("withdrawal_date")
    @set:PropertyName("withdrawal_date")
    var withdrawalDate: Timestamp? = null,

    @get:PropertyName("expiration_date")
    @set:PropertyName("expiration_date")
    var expirationDate: Timestamp? = null,

    @get:PropertyName("approved_by")
    @set:PropertyName("approved_by")
    var approvedBy: String? = null, // ID do admin que aprovou

    @get:PropertyName("rejection_reason")
    @set:PropertyName("rejection_reason")
    var rejectionReason: String? = null,

    @get:PropertyName("admin_notes")
    @set:PropertyName("admin_notes")
    var adminNotes: String? = null,

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp = Timestamp.now(),

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Timestamp = Timestamp.now()
) {
    /**
     * Converte o objeto para um Map para salvar no Firestore
     */
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "book_id" to bookId,
            "book_title" to bookTitle,
            "book_author" to bookAuthor,
            "book_cover_url" to bookCoverUrl,
            "user_id" to userId,
            "user_name" to userName,
            "user_matricula" to userMatricula,
            "user_email" to userEmail,
            "status" to status,
            "request_date" to requestDate,
            "approval_date" to approvalDate,
            "withdrawal_date" to withdrawalDate,
            "expiration_date" to expirationDate,
            "approved_by" to approvedBy,
            "rejection_reason" to rejectionReason,
            "admin_notes" to adminNotes,
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }

    /**
     * Verifica se a reserva está expirada
     * Considera expirada se passou 7 dias da aprovação sem retirada
     */
    fun isExpired(): Boolean {
        if (status != "Aprovada" || expirationDate == null) return false
        return Timestamp.now() > expirationDate!!
    }

    /**
     * Calcula a data de expiração (7 dias após aprovação)
     */
    fun calculateExpirationDate(): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = approvalDate?.toDate() ?: java.util.Date()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 7)
        return Timestamp(calendar.time)
    }
}

/**
 * Enumeração dos status possíveis de uma reserva
 */
enum class ReservationStatus(val value: String) {
    PENDENTE("Pendente"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada"),
    RETIRADO("Retirado"),
    EXPIRADA("Expirada"),
    CANCELADA("Cancelada");

    companion object {
        fun fromString(status: String): ReservationStatus {
            return values().find { it.value == status } ?: PENDENTE
        }
    }
}

