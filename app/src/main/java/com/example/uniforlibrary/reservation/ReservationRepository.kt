package com.example.uniforlibrary.reservation

import com.example.uniforlibrary.acervo.BookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class ReservationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val bookRepository = BookRepository()
    private val reservationsCollection = firestore.collection("reservations")

    suspend fun createReservation(bookId: String, pickupDate: Date, durationDays: Int, observations: String): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            // Verificar se o livro está disponível
            val bookResult = bookRepository.getBookById(bookId)
            if (bookResult.isFailure) {
                return Result.failure(bookResult.exceptionOrNull() ?: Exception("Erro ao buscar livro"))
            }

            val book = bookResult.getOrNull()!!
            if (book.availableCopies <= 0) {
                return Result.failure(Exception("Não há cópias disponíveis"))
            }

            // Criar a reserva
            val reservation = Reservation(
                bookId = bookId,
                userId = userId,
                pickupDate = pickupDate,
                durationDays = durationDays,
                observations = observations
            )

            // Salvar no Firestore e atualizar cópias disponíveis
            firestore.runTransaction { transaction ->
                val reservationRef = reservationsCollection.document()
                transaction.set(reservationRef, reservation)

                // Decrementar cópias disponíveis
                val bookRef = firestore.collection("books").document(bookId)
                val bookSnapshot = transaction.get(bookRef)
                val currentCopies = bookSnapshot.getLong("availableCopies")?.toInt() ?: 0
                if (currentCopies <= 0) {
                    throw Exception("Não há cópias disponíveis")
                }
                transaction.update(bookRef, "availableCopies", currentCopies - 1)

                reservationRef.id
            }.await()

            Result.success("Reserva criada com sucesso")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserReservations(): Result<List<Reservation>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val snapshot = reservationsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val reservations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reservation::class.java)?.copy(id = doc.id)
            }
            Result.success(reservations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReservationById(reservationId: String): Result<Reservation> {
        return try {
            val doc = reservationsCollection.document(reservationId).get().await()
            val reservation = doc.toObject(Reservation::class.java)?.copy(id = doc.id)
                ?: throw Exception("Reserva não encontrada")
            Result.success(reservation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReservationStatus(reservationId: String, newStatus: ReservationStatus): Result<Unit> {
        return try {
            val reservation = getReservationById(reservationId).getOrNull()
                ?: return Result.failure(Exception("Reserva não encontrada"))

            // Se a reserva está sendo cancelada ou completada, devolver a cópia para o acervo
            if ((newStatus == ReservationStatus.CANCELLED || newStatus == ReservationStatus.COMPLETED) &&
                (reservation.status != ReservationStatus.CANCELLED && reservation.status != ReservationStatus.COMPLETED)) {

                bookRepository.updateAvailableCopies(reservation.bookId, 1)
            }

            reservationsCollection.document(reservationId)
                .update("status", newStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReservation(reservationId: String): Result<Unit> {
        return try {
            val reservation = getReservationById(reservationId).getOrNull()
                ?: return Result.failure(Exception("Reserva não encontrada"))

            // Se a reserva não estava cancelada ou completada, devolver a cópia para o acervo
            if (reservation.status != ReservationStatus.CANCELLED &&
                reservation.status != ReservationStatus.COMPLETED) {
                bookRepository.updateAvailableCopies(reservation.bookId, 1)
            }

            reservationsCollection.document(reservationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
