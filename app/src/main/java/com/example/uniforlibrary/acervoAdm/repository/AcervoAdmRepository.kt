package com.example.uniforlibrary.acervoAdm.repository

import com.example.uniforlibrary.acervoAdm.model.AcervoAdm
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcervoAdmRepository {
    private val db = FirebaseFirestore.getInstance()
    private val acervoCollection = db.collection("acervos")

    suspend fun addAcervo(acervo: AcervoAdm): Result<String> = withContext(Dispatchers.IO) {
        try {
            val document = acervoCollection.add(acervo).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAcervo(id: String, acervo: AcervoAdm): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            acervoCollection.document(id).set(acervo.copy(
                updatedAt = System.currentTimeMillis()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAcervo(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            acervoCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllAcervos(): Flow<Result<List<AcervoAdm>>> = flow {
        try {
            val snapshot = acervoCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val acervos = snapshot.toObjects(AcervoAdm::class.java)
            emit(Result.success(acervos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun filterAcervos(
        searchQuery: String = "",
        categoria: String? = null,
        disponibilidade: String? = null
    ): Flow<Result<List<AcervoAdm>>> = flow {
        try {
            var query = acervoCollection.orderBy("createdAt", Query.Direction.DESCENDING)

            // Aplicar filtros no Firestore quando possível
            if (categoria != null && categoria != "Todas") {
                query = query.whereEqualTo("category", categoria)
            }

            val snapshot = query.get().await()
            var acervos = snapshot.toObjects(AcervoAdm::class.java)

            // Aplicar filtros adicionais em memória
            if (searchQuery.isNotBlank()) {
                acervos = acervos.filter { acervo ->
                    acervo.title.contains(searchQuery, ignoreCase = true) ||
                    acervo.author.contains(searchQuery, ignoreCase = true)
                }
            }

            if (disponibilidade != null && disponibilidade != "Todas") {
                acervos = acervos.filter { it.disponibilidade == disponibilidade }
            }

            emit(Result.success(acervos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
