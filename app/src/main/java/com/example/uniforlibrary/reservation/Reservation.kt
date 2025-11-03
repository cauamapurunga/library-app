package com.example.uniforlibrary.reservation

import java.util.Date

data class Reservation(
    val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val pickupDate: Date? = null,
    val durationDays: Int = 7,
    val observations: String = "",
    val status: ReservationStatus = ReservationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReservationStatus {
    PENDING,    // Aguardando aprovação
    APPROVED,   // Aprovada, aguardando retirada
    ACTIVE,     // Livro retirado
    COMPLETED,  // Livro devolvido
    CANCELLED,  // Reserva cancelada
    EXPIRED     // Prazo de retirada expirou
}
