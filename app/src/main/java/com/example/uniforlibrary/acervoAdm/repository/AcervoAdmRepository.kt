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
            acervoCollection.document(id).set(acervo).await()
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
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
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
            var query = acervoCollection.orderBy("dataCriacao", Query.Direction.DESCENDING)

            // Aplicar filtros no Firestore quando possível
            if (categoria != null && categoria != "Todas") {
                query = query.whereEqualTo("categoria", categoria)
            }

            val snapshot = query.get().await()
            var acervos = snapshot.toObjects(AcervoAdm::class.java)

            // Aplicar filtros adicionais em memória
            if (searchQuery.isNotBlank()) {
                acervos = acervos.filter { acervo ->
                    acervo.titulo.contains(searchQuery, ignoreCase = true) ||
                            acervo.autor.contains(searchQuery, ignoreCase = true)
                }
            }

            if (disponibilidade != null && disponibilidade != "Todas") {
                when (disponibilidade) {
                    "Disponível" -> acervos = acervos.filter { it.disponivel && it.exemplaresDisponiveis > 0 }
                    "Indisponível" -> acervos = acervos.filter { !it.disponivel || it.exemplaresDisponiveis == 0 }
                    "Digital" -> acervos = acervos.filter { it.digital }
                }
            }

            emit(Result.success(acervos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}