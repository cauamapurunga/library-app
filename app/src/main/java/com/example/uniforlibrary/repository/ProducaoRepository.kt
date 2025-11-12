package com.example.uniforlibrary.repository

import android.content.Context
import android.net.Uri
import com.example.uniforlibrary.model.Producao
import com.example.uniforlibrary.service.CloudinaryService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProducaoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val producaoCollection = db.collection("producoes")
    private val auth = FirebaseAuth.getInstance()

    // Criar nova produção
    suspend fun addProducao(producao: Producao): Result<String> {
        return try {
            val docRef = producaoCollection.document()
            val producaoWithId = producao.copy(
                id = docRef.id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            docRef.set(producaoWithId.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload de foto da produção
    suspend fun uploadFotoProducao(context: Context, producaoId: String, imageUri: Uri): Result<String> {
        return try {
            android.util.Log.d("ProducaoRepository", "Iniciando upload de foto para produção: $producaoId")

            // Fazer upload para Cloudinary na pasta "producoes"
            val uploadResult = CloudinaryService.uploadImage(context, imageUri, "producoes", producaoId)

            uploadResult.onSuccess { imageUrl ->
                // Atualizar URL da foto no Firestore
                producaoCollection.document(producaoId)
                    .update(
                        mapOf(
                            "foto_url" to imageUrl,
                            "updated_at" to Timestamp.now()
                        )
                    )
                    .await()

                android.util.Log.d("ProducaoRepository", "Foto atualizada com sucesso: $imageUrl")
            }

            uploadResult
        } catch (e: Exception) {
            android.util.Log.e("ProducaoRepository", "Erro ao fazer upload da foto", e)
            Result.failure(e)
        }
    }

    // Upload de arquivo da produção (PDF/DOC)
    suspend fun uploadArquivoProducao(context: Context, producaoId: String, fileUri: Uri): Result<String> {
        return try {
            android.util.Log.d("ProducaoRepository", "Iniciando upload de arquivo para produção: $producaoId")

            // Fazer upload para Cloudinary na pasta "producoes/documentos"
            val uploadResult = CloudinaryService.uploadDocument(context, fileUri, "producoes/documentos", producaoId)

            uploadResult.onSuccess { fileUrl ->
                // Atualizar URL do arquivo no Firestore
                producaoCollection.document(producaoId)
                    .update(
                        mapOf(
                            "arquivo_url" to fileUrl,
                            "updated_at" to Timestamp.now()
                        )
                    )
                    .await()

                android.util.Log.d("ProducaoRepository", "Arquivo atualizado com sucesso: $fileUrl")
            }

            uploadResult
        } catch (e: Exception) {
            android.util.Log.e("ProducaoRepository", "Erro ao fazer upload do arquivo", e)
            Result.failure(e)
        }
    }

    // Obter produções do usuário atual
    suspend fun getMinhasProducoes(): Result<List<Producao>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuário não autenticado")

            val snapshot = producaoCollection
                .whereEqualTo("usuario_id", userId)
                .get()
                .await()

            val producoes = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Producao::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    android.util.Log.e("ProducaoRepository", "Erro ao converter produção: ${doc.id}", e)
                    null
                }
            }

            Result.success(producoes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

