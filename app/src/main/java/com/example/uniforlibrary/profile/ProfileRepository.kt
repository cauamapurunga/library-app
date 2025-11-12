package com.example.uniforlibrary.profile

import android.content.Context
import android.net.Uri
import com.example.uniforlibrary.service.CloudinaryService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val nome: String = "",
    val matricula: String = "",
    val email: String = "",
    val telefone: String = "",
    val curso: String = "",
    val tipo: String = "usuario",
    val fotoUrl: String = ""
)

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    // Buscar dados do perfil do usuário
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val userId = currentUser?.uid ?: throw Exception("Usuário não autenticado")

            // Sincronizar email do Auth com Firestore (caso tenha sido atualizado via link)
            syncEmailFromAuthToFirestore()

            val doc = firestore.collection("usuarios").document(userId).get().await()

            if (!doc.exists()) {
                throw Exception("Perfil não encontrado")
            }

            val profile = UserProfile(
                nome = doc.getString("nome") ?: "",
                matricula = doc.getString("matricula") ?: "",
                email = doc.getString("email") ?: "",
                telefone = doc.getString("telefone") ?: "",
                curso = doc.getString("curso") ?: "",
                tipo = doc.getString("tipo") ?: "usuario",
                fotoUrl = doc.getString("fotoUrl") ?: ""
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sincronizar email do Firebase Auth com Firestore
    private suspend fun syncEmailFromAuthToFirestore() {
        try {
            val user = currentUser ?: return
            val authEmail = user.email ?: return
            val userId = user.uid

            // Buscar email atual no Firestore
            val doc = firestore.collection("usuarios").document(userId).get().await()
            val firestoreEmail = doc.getString("email") ?: ""

            // Se o email no Auth for diferente do Firestore, atualizar Firestore
            if (authEmail != firestoreEmail && authEmail.isNotBlank()) {
                firestore.collection("usuarios").document(userId)
                    .update("email", authEmail)
                    .await()
            }
        } catch (e: Exception) {
            // Erro na sincronização não deve quebrar o carregamento do perfil
            e.printStackTrace()
        }
    }

    // Atualizar email no Firebase Auth e Firestore
    suspend fun updateEmail(newEmail: String, currentPassword: String): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("Usuário não autenticado")
            val currentEmail = user.email ?: throw Exception("Email atual não encontrado")

            if (currentEmail == newEmail) {
                throw Exception("O novo email deve ser diferente do atual")
            }

            val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            user.reauthenticate(credential).await()

            val existingUsers = firestore.collection("usuarios")
                .whereEqualTo("email", newEmail)
                .get()
                .await()

            if (!existingUsers.isEmpty && existingUsers.documents[0].id != user.uid) {
                throw Exception("Este email já está em uso por outro usuário")
            }

            // Enviar o e-mail de verificação para o novo endereço
            // O email SÓ será atualizado no Firebase Auth DEPOIS que o usuário clicar no link
            // NÃO atualizamos no Firestore ainda - será atualizado quando o usuário fizer login novamente
            user.verifyBeforeUpdateEmail(newEmail).await()


            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Atualizar telefone
    suspend fun updatePhone(phone: String): Result<Unit> {
        return try {
            val userId = currentUser?.uid ?: throw Exception("Usuário não autenticado")

            firestore.collection("usuarios").document(userId)
                .update("telefone", phone)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Atualizar foto de perfil (URL da imagem)
    suspend fun updateProfilePhoto(photoUrl: String): Result<Unit> {
        return try {
            val userId = currentUser?.uid ?: throw Exception("Usuário não autenticado")

            firestore.collection("usuarios").document(userId)
                .update("fotoUrl", photoUrl)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload de foto de perfil usando Cloudinary
    suspend fun uploadProfilePhoto(context: Context, imageUri: Uri): Result<String> {
        return try {
            val userId = currentUser?.uid ?: throw Exception("Usuário não autenticado")

            android.util.Log.d("ProfileRepository", "Iniciando upload de foto de perfil para usuário: $userId")

            // Fazer upload para Cloudinary
            val uploadResult = CloudinaryService.uploadProfileImage(context, imageUri, userId)

            uploadResult.onSuccess { imageUrl ->
                // Atualizar URL da foto no Firestore
                updateProfilePhoto(imageUrl)
                android.util.Log.d("ProfileRepository", "Foto de perfil atualizada com sucesso: $imageUrl")
            }

            uploadResult
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepository", "Erro ao fazer upload da foto de perfil", e)
            Result.failure(e)
        }
    }

    // Logout
    fun logout() {
        auth.signOut()
    }
}

