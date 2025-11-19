package com.example.uniforlibrary.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

data class HomeMetrics(
    val loansLast30Days: Int = 0,
    val activeReservations: Int = 0,
    val totalBooks: Int = 0,
    val activeUsers: Int = 0
)

class HomeAdmRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reservationsCollection = db.collection("reservations")
    private val booksCollection = db.collection("books")
    private val usersCollection = db.collection("usuarios")

    companion object {
        private const val TAG = "HomeAdmRepository"
    }

    /**
     * Buscar métricas para a home do administrador
     */
    suspend fun getHomeMetrics(): Result<HomeMetrics> {
        return try {
            // Calcular data de 30 dias atrás
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -30)
            val thirtyDaysAgo = Timestamp(calendar.time)

            // Buscar empréstimos dos últimos 30 dias (status Retirado ou Devolvido)
            val loansQuery = reservationsCollection
                .whereGreaterThanOrEqualTo("created_at", thirtyDaysAgo)
                .get()
                .await()

            val loansLast30Days = loansQuery.documents.filter { doc ->
                val status = doc.getString("status")
                status == "Retirado" || status == "Devolvido" || status == "Concluído"
            }.size

            // Buscar reservas ativas (Pendente ou Aprovada)
            val activeReservationsQuery = reservationsCollection
                .whereIn("status", listOf("Pendente", "Aprovada"))
                .get()
                .await()

            val activeReservations = activeReservationsQuery.size()

            // Buscar total de livros no acervo
            val booksQuery = booksCollection.get().await()
            val totalBooks = booksQuery.size()

            // Buscar total de usuários ativos (que fizeram reservas nos últimos 30 dias)
            val uniqueUserIds = loansQuery.documents
                .mapNotNull { it.getString("user_id") }
                .distinct()
            val activeUsers = uniqueUserIds.size

            Log.d(TAG, "Métricas: $loansLast30Days empréstimos, $activeReservations reservas ativas")

            Result.success(
                HomeMetrics(
                    loansLast30Days = loansLast30Days,
                    activeReservations = activeReservations,
                    totalBooks = totalBooks,
                    activeUsers = activeUsers
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar métricas da home", e)
            Result.failure(e)
        }
    }
}

