package com.example.uniforlibrary.repository

import com.example.uniforlibrary.model.ReadingProgress
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReadingProgressRepository {
    private val db = FirebaseFirestore.getInstance()
    private val progressCollection = db.collection("reading_progress")

    suspend fun markAsCompleted(producaoId: String, userId: String): Result<Unit> {
        return try {
            // Verificar se já existe registro
            val existingDoc = progressCollection
                .whereEqualTo("producao_id", producaoId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (existingDoc.isEmpty) {
                // Criar novo registro
                val progress = ReadingProgress(
                    producaoId = producaoId,
                    userId = userId,
                    completed = true,
                    completedAt = Timestamp.now()
                )
                progressCollection.add(progress).await()
            } else {
                // Atualizar registro existente
                val docId = existingDoc.documents[0].id
                progressCollection.document(docId).update(
                    mapOf(
                        "completed" to true,
                        "completed_at" to Timestamp.now()
                    )
                ).await()
            }

            android.util.Log.d("ReadingProgress", "✅ Leitura completa marcada: producaoId=$producaoId, userId=$userId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ReadingProgress", "❌ Erro ao marcar leitura completa: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun hasCompletedReading(producaoId: String, userId: String): Result<Boolean> {
        return try {
            val snapshot = progressCollection
                .whereEqualTo("producao_id", producaoId)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("completed", true)
                .get()
                .await()

            val completed = !snapshot.isEmpty
            android.util.Log.d("ReadingProgress", "🔍 Verificar leitura completa: producaoId=$producaoId, userId=$userId, completed=$completed")
            Result.success(completed)
        } catch (e: Exception) {
            android.util.Log.e("ReadingProgress", "❌ Erro ao verificar leitura: ${e.message}", e)
            Result.failure(e)
        }
    }
}

