package com.example.uniforlibrary.acervo

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val booksCollection = firestore.collection("books")

    suspend fun getAllBooks(): Result<List<Book>> {
        return try {
            val snapshot = booksCollection.get().await()
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookById(bookId: String): Result<Book> {
        return try {
            val doc = booksCollection.document(bookId).get().await()
            val book = doc.toObject(Book::class.java)?.copy(id = doc.id)
                ?: throw Exception("Livro não encontrado")
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val snapshot = booksCollection
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + '\uf8ff')
                .get()
                .await()

            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBook(book: Book): Result<String> {
        return try {
            val docRef = booksCollection.add(book).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBook(bookId: String, book: Book): Result<Unit> {
        return try {
            booksCollection.document(bookId).set(book).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            booksCollection.document(bookId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvailableCopies(bookId: String, change: Int): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = booksCollection.document(bookId)
                val snapshot = transaction.get(docRef)
                val currentCopies = snapshot.getLong("availableCopies")?.toInt()
                    ?: throw Exception("Campo availableCopies não encontrado")

                val newCopies = currentCopies + change
                if (newCopies < 0) {
                    throw Exception("Não há cópias disponíveis suficientes")
                }

                transaction.update(docRef, "availableCopies", newCopies)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
