package com.example.uniforlibrary.repository

import com.example.uniforlibrary.model.Rating
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RatingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ratingsCollection = db.collection("ratings")
    private val booksCollection = db.collection("books")
    private val producoesCollection = db.collection("producoes")

    suspend fun createRating(
        bookId: String,
        userId: String,
        userName: String,
        stars: Int,
        comment: String
    ): Result<String> {
        return try {
            // Verificar se o usuário já avaliou este livro
            val existingRating = ratingsCollection
                .whereEqualTo("book_id", bookId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (!existingRating.isEmpty) {
                // Atualizar avaliação existente
                val ratingId = existingRating.documents[0].id
                ratingsCollection.document(ratingId).update(
                    mapOf(
                        "stars" to stars,
                        "comment" to comment,
                        "created_at" to Timestamp.now()
                    )
                ).await()

                // Atualizar média do livro
                updateBookAverageRating(bookId)

                Result.success(ratingId)
            } else {
                // Criar nova avaliação
                val rating = Rating(
                    bookId = bookId,
                    userId = userId,
                    userName = userName,
                    stars = stars,
                    comment = comment,
                    createdAt = Timestamp.now()
                )

                val docRef = ratingsCollection.add(rating).await()

                // Atualizar média do livro
                updateBookAverageRating(bookId)

                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NOVA FUNÇÃO: Atualiza a média de estrelas no documento do livro
    private suspend fun updateBookAverageRating(bookId: String) {
        try {
            val averageResult = getAverageRating(bookId)
            if (averageResult.isSuccess) {
                val average = averageResult.getOrNull() ?: 0f
                booksCollection.document(bookId).update(
                    "rating", average
                ).await()
                android.util.Log.d("RatingRepository", "Média do livro atualizada: $average")
            }
        } catch (e: Exception) {
            android.util.Log.e("RatingRepository", "Erro ao atualizar média do livro: ${e.message}")
        }
    }

    suspend fun getRatingsForBook(bookId: String): Result<List<Rating>> {
        return try {
            android.util.Log.d("RatingRepository", "Buscando avaliações para o livro: $bookId")

            val snapshot = ratingsCollection
                .whereEqualTo("book_id", bookId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()

            android.util.Log.d("RatingRepository", "Documentos encontrados: ${snapshot.documents.size}")

            val ratings = snapshot.documents.mapNotNull { doc ->
                android.util.Log.d("RatingRepository", "Documento: ${doc.id}, Dados: ${doc.data}")
                doc.toObject(Rating::class.java)?.apply {
                    id = doc.id
                }
            }

            android.util.Log.d("RatingRepository", "Avaliações convertidas: ${ratings.size}")
            ratings.forEach { rating ->
                android.util.Log.d("RatingRepository", "Rating carregado: user=${rating.userName}, stars=${rating.stars}, comment=${rating.comment}")
            }

            Result.success(ratings)
        } catch (e: Exception) {
            android.util.Log.e("RatingRepository", "Erro ao buscar avaliações: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAverageRating(bookId: String): Result<Float> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("book_id", bookId)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.getLong("stars")?.toInt()
            }

            val average = if (ratings.isNotEmpty()) {
                ratings.average().toFloat()
            } else {
                0f
            }

            Result.success(average)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatingCount(bookId: String): Result<Int> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("book_id", bookId)
                .get()
                .await()

            Result.success(snapshot.documents.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRating(bookId: String, userId: String): Result<Rating?> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("book_id", bookId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val rating = snapshot.documents.firstOrNull()?.toObject(Rating::class.java)?.apply {
                id = snapshot.documents.first().id
            }

            Result.success(rating)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRating(ratingId: String): Result<Unit> {
        return try {
            // Buscar o bookId antes de deletar
            val ratingDoc = ratingsCollection.document(ratingId).get().await()
            val bookId = ratingDoc.getString("book_id")

            // Deletar a avaliação
            ratingsCollection.document(ratingId).delete().await()

            // Atualizar média do livro
            bookId?.let { updateBookAverageRating(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Função utilitária para recalcular todas as médias de avaliações
    suspend fun recalculateAllBookRatings(): Result<Int> {
        return try {
            android.util.Log.d("RatingRepository", "Iniciando recálculo de todas as avaliações...")

            // Buscar todas as avaliações
            val allRatings = ratingsCollection.get().await()

            // Agrupar por bookId
            val bookIds = allRatings.documents
                .mapNotNull { it.getString("book_id") }
                .distinct()

            android.util.Log.d("RatingRepository", "Encontrados ${bookIds.size} livros com avaliações")

            // Recalcular média para cada livro
            bookIds.forEach { bookId ->
                updateBookAverageRating(bookId)
                android.util.Log.d("RatingRepository", "Média recalculada para livro: $bookId")
            }

            Result.success(bookIds.size)
        } catch (e: Exception) {
            android.util.Log.e("RatingRepository", "Erro ao recalcular avaliações: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== MÉTODOS PARA PRODUÇÕES ==========

    suspend fun createProducaoRating(
        producaoId: String,
        userId: String,
        userName: String,
        stars: Int,
        comment: String = ""
    ): Result<String> {
        return try {
            // Verificar se o usuário já avaliou esta produção
            val existingRating = ratingsCollection
                .whereEqualTo("producao_id", producaoId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (!existingRating.isEmpty) {
                // Atualizar avaliação existente
                val ratingId = existingRating.documents[0].id
                ratingsCollection.document(ratingId).update(
                    mapOf(
                        "stars" to stars,
                        "comment" to comment,
                        "created_at" to Timestamp.now()
                    )
                ).await()

                // Atualizar média da produção
                updateProducaoAverageRating(producaoId)

                Result.success(ratingId)
            } else {
                // Criar nova avaliação
                val rating = Rating(
                    producaoId = producaoId,
                    userId = userId,
                    userName = userName,
                    stars = stars,
                    comment = comment,
                    createdAt = Timestamp.now()
                )

                val docRef = ratingsCollection.add(rating).await()

                // Atualizar média da produção
                updateProducaoAverageRating(producaoId)

                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateProducaoAverageRating(producaoId: String) {
        try {
            val averageResult = getProducaoAverageRating(producaoId)
            if (averageResult.isSuccess) {
                val average = averageResult.getOrNull() ?: 0f
                producoesCollection.document(producaoId).update(
                    "rating", average
                ).await()
                android.util.Log.d("RatingRepository", "Média da produção atualizada: $average")
            }
        } catch (e: Exception) {
            android.util.Log.e("RatingRepository", "Erro ao atualizar média da produção: ${e.message}")
        }
    }

    suspend fun getRatingsForProducao(producaoId: String): Result<List<Rating>> {
        return try {
            android.util.Log.d("RatingRepository", "Buscando avaliações para produção: $producaoId")

            val snapshot = ratingsCollection
                .whereEqualTo("producao_id", producaoId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()

            android.util.Log.d("RatingRepository", "Documentos encontrados: ${snapshot.documents.size}")

            val ratings = snapshot.documents.mapNotNull { doc ->
                android.util.Log.d("RatingRepository", "Documento: ${doc.id}, Dados: ${doc.data}")
                doc.toObject(Rating::class.java)?.apply {
                    id = doc.id
                }
            }

            android.util.Log.d("RatingRepository", "Avaliações convertidas: ${ratings.size}")

            Result.success(ratings)
        } catch (e: Exception) {
            android.util.Log.e("RatingRepository", "Erro ao buscar avaliações da produção: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getProducaoAverageRating(producaoId: String): Result<Float> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("producao_id", producaoId)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.getLong("stars")?.toInt()
            }

            val average = if (ratings.isNotEmpty()) {
                ratings.average().toFloat()
            } else {
                0f
            }

            Result.success(average)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProducaoRating(producaoId: String, userId: String): Result<Rating?> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("producao_id", producaoId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val rating = snapshot.documents.firstOrNull()?.toObject(Rating::class.java)?.apply {
                id = snapshot.documents.first().id
            }

            Result.success(rating)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

