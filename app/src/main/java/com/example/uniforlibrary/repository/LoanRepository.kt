package com.example.uniforlibrary.repository

import android.util.Log
import com.example.uniforlibrary.model.Loan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositório para gerenciar empréstimos no Firestore
 */
class LoanRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val loansCollection = db.collection("loans")
    private val booksCollection = db.collection("books")

    companion object {
        private const val TAG = "LoanRepository"
    }

    /**
     * Buscar empréstimos do usuário em tempo real
     * Usa Flow para atualizar automaticamente a UI quando houver mudanças
     */
    fun getUserLoans(userId: String): Flow<List<Loan>> = callbackFlow {
        Log.d(TAG, "Iniciando escuta de empréstimos do usuário: $userId")

        val subscription = loansCollection
            .whereEqualTo("user_id", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao buscar empréstimos", error)
                    close(error)
                    return@addSnapshotListener
                }

                val loans = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Loan::class.java)?.apply {
                            id = doc.id
                            // Atualizar status se estiver atrasado
                            if (isLate() && status == "Ativo") {
                                status = "Atrasado"
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter empréstimo: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                // Ordenar no cliente por data de criação (mais recente primeiro)
                val sortedLoans = loans.sortedByDescending { it.createdAt.toDate().time }

                Log.d(TAG, "Empréstimos recebidos: ${sortedLoans.size}")
                trySend(sortedLoans)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Renovar empréstimo - adiciona 7 dias à data de devolução
     */
    suspend fun renewLoan(loanId: String): Result<Unit> {
        return try {
            val loanDoc = loansCollection.document(loanId).get().await()

            if (!loanDoc.exists()) {
                return Result.failure(Exception("Empréstimo não encontrado"))
            }

            val loan = loanDoc.toObject(Loan::class.java)?.apply { id = loanDoc.id }
                ?: return Result.failure(Exception("Erro ao ler empréstimo"))

            if (!loan.canRenew()) {
                return Result.failure(Exception("Este empréstimo não pode ser renovado"))
            }

            // Adicionar 7 dias à data de devolução
            val calendar = java.util.Calendar.getInstance()
            calendar.time = loan.dueDate?.toDate() ?: return Result.failure(Exception("Data inválida"))
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 7)
            val newDueDate = Timestamp(calendar.time)

            val updates = hashMapOf<String, Any>(
                "due_date" to newDueDate,
                "renewal_count" to (loan.renewalCount + 1),
                "status" to "Ativo",
                "updated_at" to Timestamp.now()
            )

            loansCollection.document(loanId).update(updates).await()
            Log.d(TAG, "Empréstimo renovado: $loanId")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao renovar empréstimo", e)
            Result.failure(e)
        }
    }

    /**
     * Verificar e atualizar empréstimos atrasados
     */
    suspend fun checkAndUpdateLateLoans(): Result<Int> {
        return try {
            val now = Timestamp.now()
            val activeLoans = loansCollection
                .whereEqualTo("status", "Ativo")
                .get()
                .await()

            var updatedCount = 0

            for (doc in activeLoans.documents) {
                val dueDate = doc.getTimestamp("due_date")
                if (dueDate != null && now > dueDate) {
                    doc.reference.update("status", "Atrasado").await()
                    updatedCount++
                }
            }

            Log.d(TAG, "Empréstimos atrasados atualizados: $updatedCount")
            Result.success(updatedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar empréstimos atrasados", e)
            Result.failure(e)
        }
    }
}

