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
         * IMPORTANTE: A ordem alfabética dos parâmetros é CRUCIAL!
         */
        private fun generateSignature(params: Map<String, String>): String {
            // Ordenar alfabeticamente (OBRIGATÓRIO pelo Cloudinary)
            val sortedParams = params.toSortedMap()

            // Construir string no formato: key1=value1&key2=value2&...&API_SECRET
            val stringToSign = sortedParams
                .map { "${it.key}=${it.value}" }
                .joinToString("&") + API_SECRET

            Log.d("CloudinaryService", "String para assinar: $stringToSign")

            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(stringToSign.toByteArray())
            val signature = hash.joinToString("") { "%02x".format(it) }

            Log.d("CloudinaryService", "Signature gerada: $signature")
            return signature
        }

        /**
         * Gera URL assinada para acesso a arquivo privado
         * URLs assinadas permitem acesso temporário a recursos privados
         */
        fun generateSignedUrl(publicId: String, resourceType: String = "raw"): String {
            // Timestamp de expiração (1 hora a partir de agora)
            val timestamp = (System.currentTimeMillis() / 1000) + 3600

            // String para assinar: formato específico do Cloudinary
            val stringToSign = "timestamp=${timestamp}&${publicId}${API_SECRET}"

            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(stringToSign.toByteArray())
            val signature = hash.joinToString("") { "%02x".format(it) }

            // Construir URL assinada
            val signedUrl = "https://res.cloudinary.com/$CLOUD_NAME/$resourceType/upload/" +
                    "s--${signature}--/v1/$publicId"

            Log.d("CloudinaryService", "URL assinada gerada para: $publicId")
            Log.d("CloudinaryService", "Expira em: ${java.util.Date(timestamp * 1000)}")

            return signedUrl
        }

        /**
         * Extrai o public_id de uma URL do Cloudinary
         */
        fun extractPublicId(url: String): String? {
            return try {
                // Padrão: https://res.cloudinary.com/CLOUD_NAME/TYPE/upload/vXXXX/FOLDER/FILE.ext
                val regex = Regex("/upload/(?:v\\d+/)?(.+?)(?:\\.[^.]+)?$")
                regex.find(url)?.groupValues?.get(1)
            } catch (e: Exception) {
                Log.e("CloudinaryService", "Erro ao extrair public_id: ${e.message}")
                null
            }
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
                // NÃO incluir folder no public_id! Será enviado separadamente
                val finalPublicId = publicId ?: UUID.randomUUID().toString()

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
                Log.d("CloudinaryService", "✅ USANDO UPLOAD ASSINADO (mais seguro)")

                // Ler bytes do arquivo
                val fileBytes = context.contentResolver.openInputStream(fileUri)?.use { input ->
                    input.readBytes()
                } ?: throw Exception("Não foi possível ler o arquivo")

                Log.d("CloudinaryService", "Arquivo lido: ${fileBytes.size} bytes")

                val timestamp = (System.currentTimeMillis() / 1000).toString()
                // NÃO incluir folder no public_id! Será enviado separadamente
                val finalPublicId = publicId ?: UUID.randomUUID().toString()

                // Gerar assinatura para upload seguro
                // IMPORTANTE: Só incluir parâmetros que serão ENVIADOS no upload
                val paramsForSignature = mutableMapOf(
                    "folder" to folder,
                    "public_id" to finalPublicId,
                    "timestamp" to timestamp
                )

                // Ordenar para debug
                val sortedDebug = paramsForSignature.toSortedMap()
                Log.d("CloudinaryService", "Parâmetros para assinatura:")
                sortedDebug.forEach { (key, value) ->
                    Log.d("CloudinaryService", "  $key = $value")
                }

                val signature = generateSignature(paramsForSignature)
                Log.d("CloudinaryService", "Signature gerada para documento")

                // Criar conexão HTTP (signed upload - COM assinatura)
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
                    Log.d("CloudinaryService", "═══════════════════════════════════")
                    Log.d("CloudinaryService", "Response completa do Cloudinary:")
                    Log.d("CloudinaryService", response)
                    Log.d("CloudinaryService", "═══════════════════════════════════")

                    // Extrair URL da resposta JSON
                    val urlPattern = "\"secure_url\":\"([^\"]+)\"".toRegex()
                    val matchResult = urlPattern.find(response)
                    val fileUrl = matchResult?.groupValues?.get(1)
                        ?: throw Exception("URL não encontrada na resposta")

                    Log.d("CloudinaryService", "URL original retornada: $fileUrl")

                    // VALIDAÇÃO: Verificar se a URL está acessível
                    try {
                        val testConnection = URL(fileUrl).openConnection() as HttpURLConnection
                        testConnection.requestMethod = "HEAD"
                        testConnection.connectTimeout = 5000
                        testConnection.connect()
                        val testCode = testConnection.responseCode
                        testConnection.disconnect()

                        Log.d("CloudinaryService", "Validação da URL: HTTP $testCode")

                        if (testCode == 404) {
                            Log.w("CloudinaryService", "⚠️ ATENÇÃO: URL retornou 404!")
                            Log.w("CloudinaryService", "URL problemática: $fileUrl")

                            // Tentar construir URL alternativa
                            val alternativeUrl = fileUrl.replace("/raw/upload/", "/image/upload/")
                            Log.d("CloudinaryService", "Tentando URL alternativa: $alternativeUrl")

                            val altConnection = URL(alternativeUrl).openConnection() as HttpURLConnection
                            altConnection.requestMethod = "HEAD"
                            altConnection.connectTimeout = 5000
                            altConnection.connect()
                            val altCode = altConnection.responseCode
                            altConnection.disconnect()

                            if (altCode == 200) {
                                Log.d("CloudinaryService", "✅ URL alternativa funciona! Usando: $alternativeUrl")
                                return@withContext Result.success(alternativeUrl)
                            }
                        } else if (testCode == 200) {
                            Log.d("CloudinaryService", "✅ URL validada com sucesso!")
                        }
                    } catch (e: Exception) {
                        Log.w("CloudinaryService", "Erro ao validar URL: ${e.message}")
                    }

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

