package com.example.uniforlibrary.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utilitário para corrigir URLs duplicadas do Cloudinary no Firestore
 *
 * URLs antigas (com duplicação):
 * https://res.cloudinary.com/.../producoes/documentos/producoes/documentos/ID.pdf
 *
 * URLs corretas:
 * https://res.cloudinary.com/.../producoes/documentos/ID.pdf
 */
class CloudinaryUrlFixer {

    companion object {
        private const val TAG = "CloudinaryUrlFixer"

        /**
         * Corrige todas as URLs de produção que têm o path duplicado
         */
        suspend fun fixDuplicatedUrls(): Result<Int> {
            return try {
                val firestore = FirebaseFirestore.getInstance()
                val producoesRef = firestore.collection("producoes")

                // Buscar todas as produções
                val snapshot = producoesRef.get().await()
                var fixedCount = 0

                snapshot.documents.forEach { doc ->
                    val arquivoUrl = doc.getString("arquivo_url") ?: return@forEach

                    // Verificar se a URL tem duplicação
                    if (arquivoUrl.contains("/producoes/documentos/producoes/documentos/")) {
                        // Corrigir URL removendo a duplicação
                        val fixedUrl = arquivoUrl.replace(
                            "/producoes/documentos/producoes/documentos/",
                            "/producoes/documentos/"
                        )

                        // Atualizar no Firestore
                        doc.reference.update("arquivo_url", fixedUrl).await()

                        Log.d(TAG, "URL corrigida para produção ${doc.id}")
                        Log.d(TAG, "  Antiga: $arquivoUrl")
                        Log.d(TAG, "  Nova: $fixedUrl")

                        fixedCount++
                    }
                }

                Log.d(TAG, "Total de URLs corrigidas: $fixedCount")
                Result.success(fixedCount)

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao corrigir URLs", e)
                Result.failure(e)
            }
        }
    }
}

