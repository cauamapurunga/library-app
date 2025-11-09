package com.example.uniforlibrary.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Registrar novo usuário com matrícula
    suspend fun register(nome: String, matricula: String, email: String, senha: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, senha).await()
            val user = result.user ?: throw Exception("Erro ao criar usuário")

            // Verificar se é admin baseado no email
            val tipo = if (email.endsWith("@adm.unifor.br")) "ADMIN" else "usuario"

            // Salvar dados adicionais no Firestore incluindo matrícula
            val userData = hashMapOf(
                "nome" to nome,
                "matricula" to matricula,
                "email" to email,
                "tipo" to tipo,
                "criadoEm" to System.currentTimeMillis()
            )
            firestore.collection("usuarios").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fazer login com matrícula ou email
    suspend fun login(emailOuMatricula: String, senha: String): Result<FirebaseUser> {
        return try {
            // Verificar se é email ou matrícula
            val email = if (emailOuMatricula.contains("@")) {
                emailOuMatricula
            } else {
                // Buscar email pela matrícula no Firestore
                getEmailByMatricula(emailOuMatricula)
            }

            val result = auth.signInWithEmailAndPassword(email, senha).await()
            val user = result.user ?: throw Exception("Erro ao fazer login")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar email pela matrícula
    private suspend fun getEmailByMatricula(matricula: String): String {
        val querySnapshot = firestore.collection("usuarios")
            .whereEqualTo("matricula", matricula)
            .get()
            .await()

        if (querySnapshot.documents.isEmpty()) {
            throw Exception("Matrícula não encontrada")
        }

        return querySnapshot.documents[0].getString("email")
            ?: throw Exception("Email não encontrado")
    }

    // Fazer logout
    fun logout() {
        auth.signOut()
    }

    // Resetar senha (pode usar email ou matrícula)
    suspend fun resetPassword(emailOuMatricula: String): Result<Unit> {
        return try {
            val email = if (emailOuMatricula.contains("@")) {
                emailOuMatricula
            } else {
                getEmailByMatricula(emailOuMatricula)
            }

            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar se é administrador
    suspend fun isAdmin(): Boolean {
        return try {
            val userId = currentUser?.uid ?: return false
            val doc = firestore.collection("usuarios").document(userId).get().await()
            val tipo = doc.getString("tipo")
            tipo == "admin" || tipo == "ADMIN"
        } catch (e: Exception) {
            false
        }
    }
}
