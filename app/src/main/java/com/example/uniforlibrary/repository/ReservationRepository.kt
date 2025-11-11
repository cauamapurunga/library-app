package com.example.uniforlibrary.repository

import android.util.Log
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.model.ReservationStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository para gerenciar operações de Reservas no Firebase Firestore
 * Implementa todas as operações CRUD e transições de estado
 */
class ReservationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reservationsCollection = db.collection("reservations")
    private val booksCollection = db.collection("books")
    private val usersCollection = db.collection("usuarios")

    companion object {
        private const val TAG = "ReservationRepository"
        private const val EXPIRATION_DAYS = 7 // Dias para expirar após aprovação
    }

    /**
     * CREATE - Criar nova reserva
     * Valida disponibilidade do livro e dados do usuário
     */
    suspend fun createReservation(
        bookId: String,
        userId: String
    ): Result<String> {
        return try {
            // Buscar informações do livro
            val bookDoc = booksCollection.document(bookId).get().await()
            if (!bookDoc.exists()) {
                return Result.failure(Exception("Livro não encontrado"))
            }

            val availableCopies = bookDoc.getLong("available_copies")?.toInt() ?: 0
            if (availableCopies <= 0) {
                return Result.failure(Exception("Livro indisponível no momento"))
            }

            // Verificar se o usuário já tem reserva ativa para este livro
            val existingReservation = reservationsCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("book_id", bookId)
                .whereIn("status", listOf("Pendente", "Aprovada"))
                .get()
                .await()

            if (!existingReservation.isEmpty) {
                return Result.failure(Exception("Você já possui uma reserva ativa para este livro"))
            }

            // Buscar informações do usuário
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Usuário não encontrado"))
            }

            // Criar documento de reserva
            val docRef = reservationsCollection.document()
            val reservation = Reservation(
                id = docRef.id,
                bookId = bookId,
                bookTitle = bookDoc.getString("title") ?: "",
                bookAuthor = bookDoc.getString("author") ?: "",
                bookCoverUrl = bookDoc.getString("cover_image_url") ?: "",
                userId = userId,
                userName = userDoc.getString("nome") ?: "",
                userMatricula = userDoc.getString("matricula") ?: "",
                userEmail = userDoc.getString("email") ?: "",
                status = ReservationStatus.PENDENTE.value,
                requestDate = Timestamp.now(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            docRef.set(reservation.toMap()).await()
            Log.d(TAG, "Reserva criada com sucesso: ${docRef.id}")

            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar reserva", e)
            Result.failure(e)
        }
    }

    /**
     * READ - Buscar todas as reservas em tempo real (para Admin)
     * Retorna Flow para atualizações automáticas
     */
    fun getAllReservations(): Flow<List<Reservation>> = callbackFlow {
        Log.d(TAG, "Iniciando escuta de todas as reservas...")

        val subscription = reservationsCollection
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao buscar reservas", error)
                    close(error)
                    return@addSnapshotListener
                }

                val reservations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Reservation::class.java)?.apply {
                            id = doc.id
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter reserva: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Reservas recebidas: ${reservations.size}")
                trySend(reservations)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * READ - Buscar reservas por status
     */
    fun getReservationsByStatus(status: String): Flow<List<Reservation>> = callbackFlow {
        Log.d(TAG, "Iniciando escuta de reservas com status: $status")

        val subscription = reservationsCollection
            .whereEqualTo("status", status)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao buscar reservas por status", error)
                    close(error)
                    return@addSnapshotListener
                }

                val reservations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Reservation::class.java)?.apply {
                            id = doc.id
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter reserva: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(reservations)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * READ - Buscar reservas de um usuário específico
     */
    fun getUserReservations(userId: String): Flow<List<Reservation>> = callbackFlow {
        Log.d(TAG, "Iniciando escuta de reservas do usuário: $userId")

        val subscription = reservationsCollection
            .whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao buscar reservas do usuário", error)
                    close(error)
                    return@addSnapshotListener
                }

                val reservations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Reservation::class.java)?.apply {
                            id = doc.id
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter reserva: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(reservations)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * READ - Buscar uma reserva específica
     */
    suspend fun getReservationById(reservationId: String): Result<Reservation> {
        return try {
            val doc = reservationsCollection.document(reservationId).get().await()

            if (!doc.exists()) {
                return Result.failure(Exception("Reserva não encontrada"))
            }

            val reservation = doc.toObject(Reservation::class.java)?.apply {
                id = doc.id
            } ?: throw Exception("Erro ao converter dados da reserva")

            Result.success(reservation)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar reserva", e)
            Result.failure(e)
        }
    }

    /**
     * UPDATE - Aprovar reserva (Admin)
     * Transição: Pendente -> Aprovada
     */
    suspend fun approveReservation(
        reservationId: String,
        adminId: String? = null,
        adminNotes: String? = null
    ): Result<Unit> {
        return try {
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                return Result.failure(Exception("Reserva não encontrada"))
            }

            val currentStatus = reservationDoc.getString("status")
            if (currentStatus != ReservationStatus.PENDENTE.value) {
                return Result.failure(Exception("Apenas reservas pendentes podem ser aprovadas"))
            }

            // Verificar disponibilidade do livro
            val bookId = reservationDoc.getString("book_id") ?: ""
            val bookDoc = booksCollection.document(bookId).get().await()
            val availableCopies = bookDoc.getLong("available_copies")?.toInt() ?: 0

            if (availableCopies <= 0) {
                return Result.failure(Exception("Livro não está mais disponível"))
            }

            // Calcular data de expiração (7 dias)
            val approvalDate = Timestamp.now()
            val calendar = java.util.Calendar.getInstance()
            calendar.time = approvalDate.toDate()
            calendar.add(java.util.Calendar.DAY_OF_MONTH, EXPIRATION_DAYS)
            val expirationDate = Timestamp(calendar.time)

            // Atualizar reserva
            val updates = hashMapOf<String, Any>(
                "status" to ReservationStatus.APROVADA.value,
                "approval_date" to approvalDate,
                "expiration_date" to expirationDate,
                "approved_by" to (adminId ?: auth.currentUser?.uid ?: ""),
                "admin_notes" to (adminNotes ?: ""),
                "updated_at" to Timestamp.now()
            )

            // Decrementar cópias disponíveis do livro
            db.runTransaction { transaction ->
                transaction.update(reservationsCollection.document(reservationId), updates)
                transaction.update(
                    booksCollection.document(bookId),
                    "available_copies",
                    availableCopies - 1
                )
            }.await()

            Log.d(TAG, "Reserva aprovada com sucesso: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao aprovar reserva", e)
            Result.failure(e)
        }
    }

    /**
     * UPDATE - Rejeitar reserva (Admin)
     * Transição: Pendente -> Rejeitada
     */
    suspend fun rejectReservation(
        reservationId: String,
        reason: String? = null,
        adminId: String? = null
    ): Result<Unit> {
        return try {
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                return Result.failure(Exception("Reserva não encontrada"))
            }

            val currentStatus = reservationDoc.getString("status")
            if (currentStatus != ReservationStatus.PENDENTE.value) {
                return Result.failure(Exception("Apenas reservas pendentes podem ser rejeitadas"))
            }

            val updates = hashMapOf<String, Any>(
                "status" to ReservationStatus.REJEITADA.value,
                "rejection_reason" to (reason ?: "Não especificado"),
                "approved_by" to (adminId ?: auth.currentUser?.uid ?: ""),
                "updated_at" to Timestamp.now()
            )

            reservationsCollection.document(reservationId).update(updates).await()

            Log.d(TAG, "Reserva rejeitada com sucesso: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao rejeitar reserva", e)
            Result.failure(e)
        }
    }

    /**
     * UPDATE - Marcar como retirada (Admin)
     * Transição: Aprovada -> Retirado
     * IMPORTANTE: Cria automaticamente um empréstimo quando marcado como retirado
     */
    suspend fun markAsWithdrawn(
        reservationId: String,
        adminId: String? = null
    ): Result<Unit> {
        return try {
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                return Result.failure(Exception("Reserva não encontrada"))
            }

            val currentStatus = reservationDoc.getString("status")
            if (currentStatus != ReservationStatus.APROVADA.value) {
                return Result.failure(Exception("Apenas reservas aprovadas podem ser marcadas como retiradas"))
            }

            val withdrawalDate = Timestamp.now()

            val updates = hashMapOf<String, Any>(
                "status" to ReservationStatus.RETIRADO.value,
                "withdrawal_date" to withdrawalDate,
                "approved_by" to (adminId ?: auth.currentUser?.uid ?: ""),
                "updated_at" to Timestamp.now()
            )

            reservationsCollection.document(reservationId).update(updates).await()

            // CRIAR EMPRÉSTIMO AUTOMATICAMENTE
            createLoanFromReservation(reservationId, withdrawalDate, reservationDoc)

            Log.d(TAG, "Reserva marcada como retirada e empréstimo criado: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar como retirada", e)
            Result.failure(e)
        }
    }

    /**
     * Cria um empréstimo automaticamente a partir de uma reserva retirada
     * Este método é chamado internamente quando o admin marca como retirado
     */
    private suspend fun createLoanFromReservation(
        reservationId: String,
        withdrawalDate: Timestamp,
        reservationDoc: com.google.firebase.firestore.DocumentSnapshot
    ) {
        try {
            val loansCollection = db.collection("loans")

            // Verificar se já existe empréstimo para esta reserva
            val existingLoan = loansCollection
                .whereEqualTo("reservation_id", reservationId)
                .get()
                .await()

            if (!existingLoan.isEmpty) {
                Log.d(TAG, "Empréstimo já existe para esta reserva")
                return
            }

            // Calcular data de devolução (14 dias após retirada)
            val calendar = java.util.Calendar.getInstance()
            calendar.time = withdrawalDate.toDate()
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 14)
            val dueDate = Timestamp(calendar.time)

            // Criar documento de empréstimo
            val loanDoc = loansCollection.document()
            val loanData = hashMapOf(
                "id" to loanDoc.id,
                "reservation_id" to reservationId,
                "book_id" to (reservationDoc.getString("book_id") ?: ""),
                "book_title" to (reservationDoc.getString("book_title") ?: ""),
                "book_author" to (reservationDoc.getString("book_author") ?: ""),
                "book_cover_url" to (reservationDoc.getString("book_cover_url") ?: ""),
                "user_id" to (reservationDoc.getString("user_id") ?: ""),
                "user_name" to (reservationDoc.getString("user_name") ?: ""),
                "user_matricula" to (reservationDoc.getString("user_matricula") ?: ""),
                "user_email" to (reservationDoc.getString("user_email") ?: ""),
                "status" to "Ativo",
                "withdrawal_date" to withdrawalDate,
                "due_date" to dueDate,
                "return_date" to null,
                "renewal_count" to 0,
                "max_renewals" to 2,
                "created_at" to Timestamp.now(),
                "updated_at" to Timestamp.now()
            )

            loanDoc.set(loanData).await()
            Log.d(TAG, "✅ Empréstimo criado automaticamente: ${loanDoc.id} para reserva: $reservationId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao criar empréstimo automático", e)
            // Não falhar a operação principal se o empréstimo não for criado
        }
    }

    /**
     * UPDATE - Marcar como expirada (Sistema automático ou Admin)
     * Transição: Aprovada -> Expirada
     */
    suspend fun markAsExpired(reservationId: String): Result<Unit> {
        return try {
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                return Result.failure(Exception("Reserva não encontrada"))
            }

            val currentStatus = reservationDoc.getString("status")
            val bookId = reservationDoc.getString("book_id") ?: ""

            if (currentStatus != ReservationStatus.APROVADA.value) {
                return Result.failure(Exception("Apenas reservas aprovadas podem expirar"))
            }

            val updates = hashMapOf<String, Any>(
                "status" to ReservationStatus.EXPIRADA.value,
                "updated_at" to Timestamp.now()
            )

            // Devolver cópia disponível ao livro
            db.runTransaction { transaction ->
                val bookDoc = transaction.get(booksCollection.document(bookId))
                val currentCopies = bookDoc.getLong("available_copies")?.toInt() ?: 0

                transaction.update(reservationsCollection.document(reservationId), updates)
                transaction.update(
                    booksCollection.document(bookId),
                    "available_copies",
                    currentCopies + 1
                )
            }.await()

            Log.d(TAG, "Reserva marcada como expirada: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar como expirada", e)
            Result.failure(e)
        }
    }

    /**
     * UPDATE - Cancelar reserva (Usuário)
     * Transição: Pendente/Aprovada -> Cancelada
     */
    suspend fun cancelReservation(reservationId: String): Result<Unit> {
        return try {
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                return Result.failure(Exception("Reserva não encontrada"))
            }

            val currentStatus = reservationDoc.getString("status")
            val bookId = reservationDoc.getString("book_id") ?: ""

            // Só pode cancelar se estiver Pendente ou Aprovada
            if (currentStatus != ReservationStatus.PENDENTE.value &&
                currentStatus != ReservationStatus.APROVADA.value) {
                return Result.failure(Exception("Esta reserva não pode ser cancelada"))
            }

            val updates = hashMapOf<String, Any>(
                "status" to ReservationStatus.CANCELADA.value,
                "updated_at" to Timestamp.now()
            )

            // Se estava aprovada, devolver cópia disponível
            if (currentStatus == ReservationStatus.APROVADA.value) {
                db.runTransaction { transaction ->
                    val bookDoc = transaction.get(booksCollection.document(bookId))
                    val currentCopies = bookDoc.getLong("available_copies")?.toInt() ?: 0

                    transaction.update(reservationsCollection.document(reservationId), updates)
                    transaction.update(
                        booksCollection.document(bookId),
                        "available_copies",
                        currentCopies + 1
                    )
                }.await()
            } else {
                reservationsCollection.document(reservationId).update(updates).await()
            }

            Log.d(TAG, "Reserva cancelada: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar reserva", e)
            Result.failure(e)
        }
    }

    /**
     * DELETE - Excluir reserva (Admin - apenas para limpeza)
     */
    suspend fun deleteReservation(reservationId: String): Result<Unit> {
        return try {
            reservationsCollection.document(reservationId).delete().await()
            Log.d(TAG, "Reserva excluída: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir reserva", e)
            Result.failure(e)
        }
    }

    /**
     * Verificar e marcar reservas expiradas automaticamente
     * Deve ser chamado periodicamente
     */
    suspend fun checkAndExpireReservations(): Result<Int> {
        return try {
            val now = Timestamp.now()
            val expiredReservations = reservationsCollection
                .whereEqualTo("status", ReservationStatus.APROVADA.value)
                .whereLessThan("expiration_date", now)
                .get()
                .await()

            var count = 0
            expiredReservations.documents.forEach { doc ->
                markAsExpired(doc.id)
                count++
            }

            Log.d(TAG, "Reservas expiradas marcadas: $count")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar reservas expiradas", e)
            Result.failure(e)
        }
    }

    /**
     * Obter estatísticas de reservas
     */
    suspend fun getReservationStats(): Result<Map<String, Int>> {
        return try {
            val allReservations = reservationsCollection.get().await()

            val stats = mutableMapOf(
                "total" to allReservations.size(),
                "pendentes" to 0,
                "aprovadas" to 0,
                "rejeitadas" to 0,
                "retiradas" to 0,
                "expiradas" to 0,
                "canceladas" to 0
            )

            allReservations.documents.forEach { doc ->
                when (doc.getString("status")) {
                    ReservationStatus.PENDENTE.value -> stats["pendentes"] = stats["pendentes"]!! + 1
                    ReservationStatus.APROVADA.value -> stats["aprovadas"] = stats["aprovadas"]!! + 1
                    ReservationStatus.REJEITADA.value -> stats["rejeitadas"] = stats["rejeitadas"]!! + 1
                    ReservationStatus.RETIRADO.value -> stats["retiradas"] = stats["retiradas"]!! + 1
                    ReservationStatus.EXPIRADA.value -> stats["expiradas"] = stats["expiradas"]!! + 1
                    ReservationStatus.CANCELADA.value -> stats["canceladas"] = stats["canceladas"]!! + 1
                }
            }

            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter estatísticas", e)
            Result.failure(e)
        }
    }
}

