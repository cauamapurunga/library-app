package com.example.uniforlibrary.repository

import android.util.Log
import com.example.uniforlibrary.model.Notification
import com.example.uniforlibrary.model.NotificationPriority
import com.example.uniforlibrary.model.NotificationType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "NotificationRepository"
        private const val COLLECTION_NOTIFICATIONS = "notifications"
    }

    /**
     * Criar uma notificação
     */
    suspend fun createNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        referenceId: String = "",
        referenceType: String = "",
        priority: String = NotificationPriority.NORMAL
    ): Result<String> {
        return try {
            val notification = Notification(
                userId = userId,
                type = type,
                title = title,
                message = message,
                referenceId = referenceId,
                referenceType = referenceType,
                priority = priority,
                isRead = false,
                createdAt = Timestamp.now()
            )

            val docRef = db.collection(COLLECTION_NOTIFICATIONS).add(notification).await()

            // Atualizar com o ID do documento
            docRef.update("id", docRef.id).await()

            Log.d(TAG, "Notificação criada com sucesso: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar notificação", e)
            Result.failure(e)
        }
    }

    /**
     * Buscar notificações do usuário
     */
    suspend fun getUserNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val notifications = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.apply { id = doc.id }
            }

            Log.d(TAG, "Notificações recuperadas: ${notifications.size}")
            Result.success(notifications)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar notificações", e)
            Result.failure(e)
        }
    }

    /**
     * Contar notificações não lidas
     */
    suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("is_read", false)
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar notificações não lidas", e)
            Result.failure(e)
        }
    }

    /**
     * Marcar notificação como lida
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("is_read", true)
                .await()

            Log.d(TAG, "Notificação marcada como lida: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar notificação como lida", e)
            Result.failure(e)
        }
    }

    /**
     * Marcar todas as notificações como lidas
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("is_read", false)
                .get()
                .await()

            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "is_read", true)
            }
            batch.commit().await()

            Log.d(TAG, "Todas as notificações marcadas como lidas")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar todas como lidas", e)
            Result.failure(e)
        }
    }

    /**
     * Deletar notificação
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .await()

            Log.d(TAG, "Notificação deletada: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar notificação", e)
            Result.failure(e)
        }
    }

    /**
     * Deletar todas as notificações do usuário
     */
    suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        return try {
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Log.d(TAG, "Todas as notificações deletadas")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar todas as notificações", e)
            Result.failure(e)
        }
    }

    // ==================== NOTIFICAÇÕES ESPECÍFICAS ====================

    /**
     * Criar notificação de reserva aprovada
     */
    suspend fun notifyReservationApproved(
        userId: String,
        bookTitle: String,
        reservationId: String
    ): Result<String> {
        return createNotification(
            userId = userId,
            type = NotificationType.RESERVA_APROVADA,
            title = "Reserva Aprovada! 🎉",
            message = "Sua reserva do livro \"$bookTitle\" foi aprovada! Você tem 48h para retirar o livro na biblioteca.",
            referenceId = reservationId,
            referenceType = "reservation",
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Criar notificação de reserva rejeitada
     */
    suspend fun notifyReservationRejected(
        userId: String,
        bookTitle: String,
        reservationId: String,
        reason: String = ""
    ): Result<String> {
        val message = if (reason.isNotBlank()) {
            "Sua reserva do livro \"$bookTitle\" foi rejeitada. Motivo: $reason"
        } else {
            "Sua reserva do livro \"$bookTitle\" foi rejeitada."
        }

        return createNotification(
            userId = userId,
            type = NotificationType.RESERVA_REJEITADA,
            title = "Reserva Não Aprovada",
            message = message,
            referenceId = reservationId,
            referenceType = "reservation",
            priority = NotificationPriority.NORMAL
        )
    }

    /**
     * Criar notificação para admin de nova reserva
     */
    suspend fun notifyAdminNewReservation(
        adminId: String,
        userName: String,
        userMatricula: String,
        bookTitle: String,
        reservationId: String
    ): Result<String> {
        return createNotification(
            userId = adminId,
            type = NotificationType.NOVA_RESERVA,
            title = "Nova Reserva Pendente 📖",
            message = "$userName (Mat: $userMatricula) solicitou o livro \"$bookTitle\"",
            referenceId = reservationId,
            referenceType = "reservation",
            priority = NotificationPriority.NORMAL
        )
    }

    /**
     * Criar notificação para admin de nova produção acadêmica
     */
    suspend fun notifyAdminNewProduction(
        adminId: String,
        userName: String,
        productionTitle: String,
        productionId: String
    ): Result<String> {
        return createNotification(
            userId = adminId,
            type = NotificationType.PRODUCAO_SUBMETIDA,
            title = "Nova Produção Acadêmica 📝",
            message = "$userName submeteu a produção \"$productionTitle\" para revisão.",
            referenceId = productionId,
            referenceType = "production",
            priority = NotificationPriority.NORMAL
        )
    }

    /**
     * Criar notificação de produção aprovada
     */
    suspend fun notifyProductionApproved(
        userId: String,
        productionTitle: String,
        productionId: String
    ): Result<String> {
        return createNotification(
            userId = userId,
            type = NotificationType.PRODUCAO_APROVADA,
            title = "Produção Aprovada! 🎉",
            message = "Sua produção \"$productionTitle\" foi aprovada e será incluída no acervo da biblioteca!",
            referenceId = productionId,
            referenceType = "production",
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Criar notificação de produção reprovada
     */
    suspend fun notifyProductionRejected(
        userId: String,
        productionTitle: String,
        productionId: String,
        reason: String = ""
    ): Result<String> {
        val message = if (reason.isNotBlank()) {
            "Sua produção \"$productionTitle\" foi reprovada. Motivo: $reason"
        } else {
            "Sua produção \"$productionTitle\" foi reprovada."
        }

        return createNotification(
            userId = userId,
            type = NotificationType.PRODUCAO_REPROVADA,
            title = "Produção Não Aprovada",
            message = message,
            referenceId = productionId,
            referenceType = "production",
            priority = NotificationPriority.NORMAL
        )
    }

    /**
     * Criar notificação de livro atrasado
     */
    suspend fun notifyLateBook(
        userId: String,
        bookTitle: String,
        loanId: String,
        daysLate: Int
    ): Result<String> {
        val message = if (daysLate == 1) {
            "O livro \"$bookTitle\" está atrasado há $daysLate dia! Devolva o quanto antes para evitar multas."
        } else {
            "O livro \"$bookTitle\" está atrasado há $daysLate dias! Devolva o quanto antes para evitar multas."
        }

        return createNotification(
            userId = userId,
            type = NotificationType.LIVRO_ATRASADO,
            title = "Livro em Atraso! ⚠️",
            message = message,
            referenceId = loanId,
            referenceType = "loan",
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Verificar e notificar sobre empréstimos atrasados
     * Deve ser chamado periodicamente (ex: diariamente)
     */
    suspend fun checkAndNotifyLateLoans(): Result<Int> {
        return try {
            val now = Timestamp.now()
            val loansCollection = db.collection("loans")

            // Buscar empréstimos ativos com data de devolução vencida
            val lateLoans = loansCollection
                .whereEqualTo("status", "Ativo")
                .whereLessThan("due_date", now)
                .get()
                .await()

            var notificationsSent = 0

            lateLoans.documents.forEach { doc ->
                try {
                    val userId = doc.getString("user_id") ?: return@forEach
                    val bookTitle = doc.getString("book_title") ?: return@forEach
                    val dueDate = doc.getTimestamp("due_date") ?: return@forEach
                    val loanId = doc.id

                    // Calcular dias de atraso
                    val diffMillis = now.toDate().time - dueDate.toDate().time
                    val daysLate = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                    if (daysLate > 0) {
                        // Verificar se já enviou notificação hoje
                        val todayStart = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                        }.time

                        val existingNotification = db.collection(COLLECTION_NOTIFICATIONS)
                            .whereEqualTo("user_id", userId)
                            .whereEqualTo("reference_id", loanId)
                            .whereEqualTo("type", NotificationType.LIVRO_ATRASADO)
                            .whereGreaterThan("created_at", Timestamp(todayStart))
                            .get()
                            .await()

                        // Só enviar se não enviou hoje
                        if (existingNotification.isEmpty) {
                            notifyLateBook(userId, bookTitle, loanId, daysLate)
                            notificationsSent++
                            Log.d(TAG, "Notificação de atraso enviada: Livro '$bookTitle', $daysLate dias")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar empréstimo atrasado: ${doc.id}", e)
                }
            }

            Log.d(TAG, "Total de notificações de atraso enviadas: $notificationsSent")
            Result.success(notificationsSent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar empréstimos atrasados", e)
            Result.failure(e)
        }
    }
}
