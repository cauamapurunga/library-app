package com.example.uniforlibrary.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

data class ReportStats(
    val totalReservations: Int = 0,
    val activeUsers: Int = 0,
    val returnRate: Double = 0.0,
    val previousMonthReservations: Int = 0,
    val previousMonthUsers: Int = 0,
    val previousMonthReturnRate: Double = 0.0
)

data class PopularBook(
    val title: String = "",
    val author: String = "",
    val reservationCount: Int = 0
)

class ReportRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reservationsCollection = db.collection("reservations")
    private val usersCollection = db.collection("usuarios")

    companion object {
        private const val TAG = "ReportRepository"
    }

    /**
     * Buscar estatísticas do período especificado
     */
    suspend fun getStats(startDate: Date?, endDate: Date?): Result<ReportStats> {
        return try {
            val start = startDate ?: getMonthStart()
            val end = endDate ?: Date()

            // Buscar reservas do período atual
            val currentReservations = reservationsCollection
                .whereGreaterThanOrEqualTo("created_at", Timestamp(start))
                .whereLessThanOrEqualTo("created_at", Timestamp(end))
                .get()
                .await()

            val totalReservations = currentReservations.size()

            // Calcular usuários ativos (usuários únicos que fizeram reservas no período)
            val uniqueUserIds = currentReservations.documents
                .mapNotNull { it.getString("user_id") }
                .distinct()
            val activeUsers = uniqueUserIds.size

            // Calcular taxa de devolução
            val completedReservations = currentReservations.documents
                .filter {
                    val status = it.getString("status")
                    status == "Devolvido" || status == "Concluído"
                }
                .size

            val returnRate = if (totalReservations > 0) {
                (completedReservations.toDouble() / totalReservations.toDouble()) * 100
            } else {
                0.0
            }

            // Buscar estatísticas do mês anterior para comparação
            val previousStart = getPreviousMonthStart()
            val previousEnd = start

            val previousReservations = reservationsCollection
                .whereGreaterThanOrEqualTo("created_at", Timestamp(previousStart))
                .whereLessThan("created_at", Timestamp(previousEnd))
                .get()
                .await()

            val previousMonthReservations = previousReservations.size()

            val previousUniqueUserIds = previousReservations.documents
                .mapNotNull { it.getString("user_id") }
                .distinct()
            val previousMonthUsers = previousUniqueUserIds.size

            val previousCompletedReservations = previousReservations.documents
                .filter {
                    val status = it.getString("status")
                    status == "Devolvido" || status == "Concluído"
                }
                .size

            val previousMonthReturnRate = if (previousMonthReservations > 0) {
                (previousCompletedReservations.toDouble() / previousMonthReservations.toDouble()) * 100
            } else {
                0.0
            }

            Log.d(TAG, "Stats calculadas: $totalReservations reservas, $activeUsers usuários ativos")

            Result.success(
                ReportStats(
                    totalReservations = totalReservations,
                    activeUsers = activeUsers,
                    returnRate = returnRate,
                    previousMonthReservations = previousMonthReservations,
                    previousMonthUsers = previousMonthUsers,
                    previousMonthReturnRate = previousMonthReturnRate
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar estatísticas", e)
            Result.failure(e)
        }
    }

    /**
     * Buscar livros mais populares (mais reservados)
     */
    suspend fun getPopularBooks(startDate: Date?, endDate: Date?, limit: Int = 10): Result<List<PopularBook>> {
        return try {
            val start = startDate ?: getMonthStart()
            val end = endDate ?: Date()

            // Buscar todas as reservas do período
            val reservations = reservationsCollection
                .whereGreaterThanOrEqualTo("created_at", Timestamp(start))
                .whereLessThanOrEqualTo("created_at", Timestamp(end))
                .get()
                .await()

            // Agrupar por livro e contar
            val bookCounts = mutableMapOf<String, Pair<String, Int>>() // bookId -> (title, count)

            reservations.documents.forEach { doc ->
                val bookId = doc.getString("book_id") ?: return@forEach
                val bookTitle = doc.getString("book_title") ?: "Desconhecido"

                val current = bookCounts[bookId]
                if (current != null) {
                    bookCounts[bookId] = current.first to (current.second + 1)
                } else {
                    bookCounts[bookId] = bookTitle to 1
                }
            }

            // Buscar informações completas dos livros
            val popularBooks = bookCounts.entries
                .sortedByDescending { it.value.second }
                .take(limit)
                .map { entry ->
                    val bookDoc = db.collection("books").document(entry.key).get().await()
                    PopularBook(
                        title = entry.value.first,
                        author = bookDoc.getString("author") ?: "",
                        reservationCount = entry.value.second
                    )
                }

            Log.d(TAG, "Livros populares encontrados: ${popularBooks.size}")

            Result.success(popularBooks)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar livros populares", e)
            Result.failure(e)
        }
    }

    private fun getMonthStart(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getPreviousMonthStart(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}

