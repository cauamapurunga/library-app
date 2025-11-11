package com.example.uniforlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de dados para Empréstimo
 * Representa um livro que foi retirado pelo aluno após aprovação da reserva
 */
data class Loan(
    var id: String = "",

    @get:PropertyName("reservation_id")
    @set:PropertyName("reservation_id")
    var reservationId: String = "", // Referência à reserva original

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

    var status: String = "Ativo", // Ativo, Atrasado, Devolvido

    @get:PropertyName("withdrawal_date")
    @set:PropertyName("withdrawal_date")
    var withdrawalDate: Timestamp = Timestamp.now(), // Data que retirou

    @get:PropertyName("due_date")
    @set:PropertyName("due_date")
    var dueDate: Timestamp? = null, // Data de devolução (14 dias após retirada)

    @get:PropertyName("return_date")
    @set:PropertyName("return_date")
    var returnDate: Timestamp? = null, // Data que devolveu (se devolvido)

    @get:PropertyName("renewal_count")
    @set:PropertyName("renewal_count")
    var renewalCount: Int = 0, // Quantas vezes foi renovado

    @get:PropertyName("max_renewals")
    @set:PropertyName("max_renewals")
    var maxRenewals: Int = 2, // Máximo de renovações permitidas

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp = Timestamp.now(),

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Timestamp = Timestamp.now()
) {
    /**
     * Verifica se o empréstimo está atrasado
     */
    fun isLate(): Boolean {
        if (status == "Devolvido") return false
        val now = Timestamp.now()
        return dueDate?.let { now > it } ?: false
    }

    /**
     * Calcula a data de devolução (14 dias após retirada)
     */
    fun calculateDueDate(): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = withdrawalDate.toDate()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 14)
        return Timestamp(calendar.time)
    }

    /**
     * Verifica se pode renovar
     */
    fun canRenew(): Boolean {
        return renewalCount < maxRenewals && status == "Ativo" && !isLate()
    }

    /**
     * Calcula dias restantes para devolução
     */
    fun daysUntilDue(): Long {
        if (dueDate == null) return 0
        val now = System.currentTimeMillis()
        val dueTime = dueDate!!.toDate().time
        val diff = dueTime - now
        return diff / (1000 * 60 * 60 * 24)
    }

    /**
     * Converte para Map para salvar no Firestore
     */
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "reservation_id" to reservationId,
            "book_id" to bookId,
            "book_title" to bookTitle,
            "book_author" to bookAuthor,
            "book_cover_url" to bookCoverUrl,
            "user_id" to userId,
            "user_name" to userName,
            "user_matricula" to userMatricula,
            "user_email" to userEmail,
            "status" to status,
            "withdrawal_date" to withdrawalDate,
            "due_date" to dueDate,
            "return_date" to returnDate,
            "renewal_count" to renewalCount,
            "max_renewals" to maxRenewals,
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}

enum class LoanStatus(val value: String) {
    ATIVO("Ativo"),
    ATRASADO("Atrasado"),
    DEVOLVIDO("Devolvido");

    companion object {
        fun fromString(status: String): LoanStatus {
            return values().find { it.value == status } ?: ATIVO
        }
    }
}

