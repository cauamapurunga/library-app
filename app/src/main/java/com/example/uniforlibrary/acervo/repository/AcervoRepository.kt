package com.example.uniforlibrary.acervo.repository

import com.example.uniforlibrary.acervo.model.Acervo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcervoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val acervoCollection = db.collection("acervos")

    suspend fun addAcervo(acervo: Acervo): Result<String> = withContext(Dispatchers.IO) {
        try {
            val document = acervoCollection.add(acervo).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAcervo(id: String, acervo: Acervo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            acervoCollection.document(id).set(acervo.copy(
                dataAtualizacao = System.currentTimeMillis()
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

    suspend fun getAcervo(id: String): Result<Acervo?> = withContext(Dispatchers.IO) {
        try {
            val document = acervoCollection.document(id).get().await()
            Result.success(document.toObject(Acervo::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllAcervos(): Flow<Result<List<Acervo>>> = flow {
        try {
            val snapshot = acervoCollection
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
                .get()
                .await()
            val acervos = snapshot.toObjects(Acervo::class.java)
            emit(Result.success(acervos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun searchAcervos(query: String): Flow<Result<List<Acervo>>> = flow {
        try {
            // Busca por título, autor ou ISBN
            val queryLower = query.lowercase()
            val snapshot = acervoCollection.get().await()
            val acervos = snapshot.toObjects(Acervo::class.java)
                .filter { acervo ->
                    acervo.titulo.lowercase().contains(queryLower) ||
                    acervo.autor.lowercase().contains(queryLower)
                }
            emit(Result.success(acervos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun filterAcervos(
        categoria: String? = null,
        disponivel: Boolean? = null,
        digital: Boolean? = null,
        fisico: Boolean? = null
    ): Flow<Result<List<Acervo>>> = flow {
        try {
            var query = acervoCollection.orderBy("dataCriacao", Query.Direction.DESCENDING)

            if (categoria != null) {
                query = query.whereEqualTo("categoria", categoria)
            }
            if (disponivel != null) {
                query = query.whereEqualTo("disponivel", disponivel)
            }
            if (digital != null) {
                query = query.whereEqualTo("digital", digital)
            }
            if (fisico != null) {
                query = query.whereEqualTo("fisico", fisico)
            }

            val snapshot = query.get().await()
            val acervos = snapshot.toObjects(Acervo::class.java)
            emit(Result.success(acervos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun updateDisponibilidade(
        id: String,
        exemplaresDisponiveis: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            acervoCollection.document(id).update(
                mapOf(
                    "exemplaresDisponiveis" to exemplaresDisponiveis,
                    "disponivel" to (exemplaresDisponiveis > 0),
                    "dataAtualizacao" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
