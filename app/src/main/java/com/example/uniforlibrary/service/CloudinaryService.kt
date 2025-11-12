package com.example.uniforlibrary.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.uniforlibrary.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID

class CloudinaryService {
    companion object {
        // Ler credenciais do BuildConfig (que vem do local.properties)
        private val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
        private val API_KEY = BuildConfig.CLOUDINARY_API_KEY
        private val API_SECRET = BuildConfig.CLOUDINARY_API_SECRET
        private val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

        /**
         * Gera a assinatura SHA-1 para upload assinado
         */
        private fun generateSignature(params: Map<String, String>): String {
            val sortedParams = params.toSortedMap()
            val stringToSign = sortedParams.map { "${it.key}=${it.value}" }.joinToString("&") + API_SECRET

            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(stringToSign.toByteArray())
            return hash.joinToString("") { "%02x".format(it) }
        }

        /**
         * Faz upload de uma imagem para o Cloudinary
         * @param context Contexto da aplicação
         * @param imageUri URI da imagem selecionada
         * @param folder Pasta no Cloudinary (ex: "books", "profiles")
         * @param publicId ID público opcional (se não fornecido, será gerado automaticamente)
         * @return URL da imagem no Cloudinary ou null em caso de erro
         */
        suspend fun uploadImage(
            context: Context,
            imageUri: Uri,
            folder: String,
            publicId: String? = null
        ): Result<String> = withContext(Dispatchers.IO) {
            try {
                Log.d("CloudinaryService", "Iniciando upload para pasta: $folder")

                // Ler bytes da imagem
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
                    input.readBytes()
                } ?: throw Exception("Não foi possível ler a imagem")

                Log.d("CloudinaryService", "Imagem lida: ${imageBytes.size} bytes")

                // Preparar parâmetros para assinatura
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val finalPublicId = "$folder/${publicId ?: UUID.randomUUID().toString()}"

                val paramsForSignature = mapOf(
                    "folder" to folder,
                    "public_id" to finalPublicId,
                    "timestamp" to timestamp
                )

                val signature = generateSignature(paramsForSignature)
                Log.d("CloudinaryService", "Signature gerada: $signature")

                // Criar conexão HTTP
                val url = URL(UPLOAD_URL)
                val boundary = "----CloudinaryBoundary${UUID.randomUUID()}"
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                }

                // Construir corpo da requisição multipart
                val outputStream = connection.outputStream
                val writer = outputStream.bufferedWriter()

                // Campo: file (imagem)
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n")
                writer.append("Content-Type: image/jpeg\r\n\r\n")
                writer.flush()
                outputStream.write(imageBytes)
                outputStream.flush()
                writer.append("\r\n")
                writer.flush()

                // Campo: api_key
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"api_key\"\r\n\r\n")
                writer.append("$API_KEY\r\n")
                writer.flush()

                // Campo: timestamp
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n")
                writer.append("$timestamp\r\n")
                writer.flush()

                // Campo: signature
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"signature\"\r\n\r\n")
                writer.append("$signature\r\n")
                writer.flush()

                // Campo: folder
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"folder\"\r\n\r\n")
                writer.append("$folder\r\n")
                writer.flush()

                // Campo: public_id
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"public_id\"\r\n\r\n")
                writer.append("$finalPublicId\r\n")
                writer.flush()

                // Finalizar multipart
                writer.append("--$boundary--\r\n")
                writer.flush()
                writer.close()

                // Ler resposta
                val responseCode = connection.responseCode
                Log.d("CloudinaryService", "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("CloudinaryService", "Response: $response")

                    // Extrair URL da resposta JSON
                    val urlPattern = "\"secure_url\":\"([^\"]+)\"".toRegex()
                    val matchResult = urlPattern.find(response)
                    val imageUrl = matchResult?.groupValues?.get(1)
                        ?: throw Exception("URL não encontrada na resposta")

                    Log.d("CloudinaryService", "Upload bem-sucedido: $imageUrl")
                    Result.success(imageUrl)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Erro desconhecido"
                    Log.e("CloudinaryService", "Erro no upload: $errorResponse")
                    Result.failure(Exception("Erro ao fazer upload: $errorResponse"))
                }
            } catch (e: Exception) {
                Log.e("CloudinaryService", "Exceção ao fazer upload", e)
                Result.failure(e)
            }
        }

        /**
         * Faz upload de foto de perfil
         */
        suspend fun uploadProfileImage(context: Context, imageUri: Uri, userId: String): Result<String> {
            return uploadImage(context, imageUri, "profiles", userId)
        }

        /**
         * Faz upload de capa de livro
         */
        suspend fun uploadBookCover(context: Context, imageUri: Uri, bookId: String): Result<String> {
            return uploadImage(context, imageUri, "books", bookId)
        }

        /**
         * Faz upload de documento (PDF, DOC, etc.)
         */
        suspend fun uploadDocument(
            context: Context,
            fileUri: Uri,
            folder: String,
            publicId: String? = null
        ): Result<String> = withContext(Dispatchers.IO) {
            try {
                Log.d("CloudinaryService", "Iniciando upload de documento para pasta: $folder")

                // Ler bytes do arquivo
                val fileBytes = context.contentResolver.openInputStream(fileUri)?.use { input ->
                    input.readBytes()
                } ?: throw Exception("Não foi possível ler o arquivo")

                Log.d("CloudinaryService", "Arquivo lido: ${fileBytes.size} bytes")

                // Preparar parâmetros para assinatura
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val finalPublicId = "$folder/${publicId ?: UUID.randomUUID().toString()}"

                val paramsForSignature = mapOf(
                    "folder" to folder,
                    "public_id" to finalPublicId,
                    "timestamp" to timestamp,
                    "resource_type" to "raw" // Importante para documentos
                )

                val signature = generateSignature(paramsForSignature)
                Log.d("CloudinaryService", "Signature gerada para documento: $signature")

                // Criar conexão HTTP (usar endpoint raw para documentos)
                val uploadUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/raw/upload"
                val url = URL(uploadUrl)
                val boundary = "----CloudinaryBoundary${UUID.randomUUID()}"
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                }

                // Construir corpo da requisição multipart
                val outputStream = connection.outputStream
                val writer = outputStream.bufferedWriter()

                // Campo: file
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"document.pdf\"\r\n")
                writer.append("Content-Type: application/octet-stream\r\n\r\n")
                writer.flush()
                outputStream.write(fileBytes)
                outputStream.flush()
                writer.append("\r\n")
                writer.flush()

                // Campo: api_key
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"api_key\"\r\n\r\n")
                writer.append("$API_KEY\r\n")
                writer.flush()

                // Campo: timestamp
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n")
                writer.append("$timestamp\r\n")
                writer.flush()

                // Campo: signature
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"signature\"\r\n\r\n")
                writer.append("$signature\r\n")
                writer.flush()

                // Campo: folder
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"folder\"\r\n\r\n")
                writer.append("$folder\r\n")
                writer.flush()

                // Campo: public_id
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"public_id\"\r\n\r\n")
                writer.append("$finalPublicId\r\n")
                writer.flush()

                // Campo: resource_type
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"resource_type\"\r\n\r\n")
                writer.append("raw\r\n")
                writer.flush()

                // Finalizar multipart
                writer.append("--$boundary--\r\n")
                writer.flush()
                writer.close()

                // Ler resposta
                val responseCode = connection.responseCode
                Log.d("CloudinaryService", "Response code (documento): $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("CloudinaryService", "Response: $response")

                    // Extrair URL da resposta JSON
                    val urlPattern = "\"secure_url\":\"([^\"]+)\"".toRegex()
                    val matchResult = urlPattern.find(response)
                    val fileUrl = matchResult?.groupValues?.get(1)
                        ?: throw Exception("URL não encontrada na resposta")

                    Log.d("CloudinaryService", "Upload de documento bem-sucedido: $fileUrl")
                    Result.success(fileUrl)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Erro desconhecido"
                    Log.e("CloudinaryService", "Erro no upload do documento: $errorResponse")
                    Result.failure(Exception("Erro ao fazer upload: $errorResponse"))
                }
            } catch (e: Exception) {
                Log.e("CloudinaryService", "Exceção ao fazer upload do documento", e)
                Result.failure(e)
            }
        }
    }
}

