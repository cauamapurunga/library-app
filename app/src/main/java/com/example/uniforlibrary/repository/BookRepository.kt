package com.example.uniforlibrary.repository

import com.example.uniforlibrary.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BookRepository {
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    // CREATE - Adicionar novo livro
    suspend fun addBook(book: Book): Result<String> {
        return try {
            val docRef = booksCollection.document()
            val bookWithId = book.copy(id = docRef.id)
            docRef.set(bookWithId.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ - Buscar todos os livros (Flow para atualizações em tempo real)
    fun getAllBooks(): Flow<List<Book>> = callbackFlow {
        android.util.Log.d("BookRepository", "Iniciando escuta de livros...")
        val subscription = booksCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BookRepository", "Erro ao buscar livros", error)
                    close(error)
                    return@addSnapshotListener
                }

                android.util.Log.d("BookRepository", "Snapshot recebido: ${snapshot?.documents?.size} documentos")

                val books = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        android.util.Log.d("BookRepository", "Processando documento: ${doc.id}")
                        android.util.Log.d("BookRepository", "Dados do documento: ${doc.data}")

                        // Converte manualmente para lidar com tipos inconsistentes
                        val data = doc.data
                        if (data != null) {
                            val book = Book(
                                // SEMPRE usa o ID do documento do Firestore
                                id = doc.id,
                                title = data["title"] as? String ?: "",
                                author = data["author"] as? String ?: "",
                                publicationYear = when (val year = data["publication_year"]) {
                                    is Number -> year.toInt()
                                    is String -> year.toIntOrNull() ?: 0
                                    else -> 0
                                },
                                categoryId = when (val catId = data["category_id"]) {
                                    is Number -> catId.toInt()
                                    is String -> catId.toIntOrNull() ?: 0
                                    else -> 0
                                },
                                description = data["description"] as? String ?: "",
                                rating = when (val rat = data["rating"]) {
                                    is Number -> rat.toInt()
                                    is String -> rat.toIntOrNull() ?: 0
                                    else -> 0
                                },
                                isDigital = data["is_digital"] as? Boolean ?: false,
                                totalCopies = when (val total = data["total_copies"]) {
                                    is Number -> total.toInt()
                                    is String -> total.toIntOrNull() ?: 0
                                    else -> 0
                                },
                                availableCopies = when (val avail = data["available_copies"]) {
                                    is Number -> avail.toInt()
                                    is String -> avail.toIntOrNull() ?: 0
                                    else -> 0
                                },
                                coverImageUrl = data["cover_image_url"] as? String ?: "",
                                digitalContentUrl = data["digital_content_url"] as? String ?: "",
                                isbn = data["isbn"] as? String ?: "",
                                createdAt = data["created_at"] as? Timestamp,
                                updatedAt = data["updated_at"] as? Timestamp
                            )
                            android.util.Log.d("BookRepository", "Livro convertido: ${book.title} (ID Firestore: ${book.id})")
                            book
                        } else {
                            android.util.Log.e("BookRepository", "Documento sem dados: ${doc.id}")
                            null
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BookRepository", "Erro ao converter livro: ${doc.id}", e)
                        android.util.Log.e("BookRepository", "Stack trace:", e)
                        null
                    }
                } ?: emptyList()

                android.util.Log.d("BookRepository", "Total de livros carregados: ${books.size}")
                trySend(books)
            }

        awaitClose {
            android.util.Log.d("BookRepository", "Fechando listener de livros")
            subscription.remove()
        }
    }

    // READ - Buscar livro por ID
    suspend fun getBookById(bookId: String): Result<Book?> {
        return try {
            android.util.Log.d("BookRepository", "Buscando livro por ID: $bookId")
            val document = booksCollection.document(bookId).get().await()

            android.util.Log.d("BookRepository", "Documento existe: ${document.exists()}")

            if (!document.exists()) {
                android.util.Log.e("BookRepository", "Documento não encontrado: $bookId")
                return Result.success(null)
            }

            val book = try {
                // Converte manualmente para garantir consistência
                val data = document.data
                if (data != null) {
                    val book = Book(
                        // SEMPRE usa o ID do documento do Firestore
                        id = document.id,
                        title = data["title"] as? String ?: "",
                        author = data["author"] as? String ?: "",
                        publicationYear = when (val year = data["publication_year"]) {
                            is Number -> year.toInt()
                            is String -> year.toIntOrNull() ?: 0
                            else -> 0
                        },
                        categoryId = when (val catId = data["category_id"]) {
                            is Number -> catId.toInt()
                            is String -> catId.toIntOrNull() ?: 0
                            else -> 0
                        },
                        description = data["description"] as? String ?: "",
                        rating = when (val rat = data["rating"]) {
                            is Number -> rat.toInt()
                            is String -> rat.toIntOrNull() ?: 0
                            else -> 0
                        },
                        isDigital = data["is_digital"] as? Boolean ?: false,
                        totalCopies = when (val total = data["total_copies"]) {
                            is Number -> total.toInt()
                            is String -> total.toIntOrNull() ?: 0
                            else -> 0
                        },
                        availableCopies = when (val avail = data["available_copies"]) {
                            is Number -> avail.toInt()
                            is String -> avail.toIntOrNull() ?: 0
                            else -> 0
                        },
                        coverImageUrl = data["cover_image_url"] as? String ?: "",
                        digitalContentUrl = data["digital_content_url"] as? String ?: "",
                        isbn = data["isbn"] as? String ?: "",
                        createdAt = data["created_at"] as? Timestamp,
                        updatedAt = data["updated_at"] as? Timestamp
                    )
                    android.util.Log.d("BookRepository", "Livro carregado com sucesso: ${book.title} (ID: ${book.id})")
                    book
                } else {
                    android.util.Log.e("BookRepository", "Documento sem dados: $bookId")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("BookRepository", "Erro ao converter livro: $bookId", e)
                null
            }
            Result.success(book)
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "Erro ao buscar livro por ID: $bookId", e)
            Result.failure(e)
        }
    }

    // READ - Buscar livros por categoria
    fun getBooksByCategory(category: String): Flow<List<Book>> = callbackFlow {
        val subscription = booksCollection
            .whereEqualTo("category", category)
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val books = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Book::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("BookRepository", "Erro ao converter livro: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(books)
            }

        awaitClose { subscription.remove() }
    }

    // READ - Buscar livros disponíveis
    fun getAvailableBooks(): Flow<List<Book>> = callbackFlow {
        val subscription = booksCollection
            .whereGreaterThan("availableCopies", 0)
            .orderBy("availableCopies", Query.Direction.DESCENDING)
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val books = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Book::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("BookRepository", "Erro ao converter livro: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(books)
            }

        awaitClose { subscription.remove() }
    }

    // READ - Pesquisar livros por título, autor ou ISBN
    suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            android.util.Log.d("BookRepository", "Iniciando pesquisa com query: '$query'")
            val queryLower = query.lowercase()
            val allBooks = booksCollection.get().await()

            android.util.Log.d("BookRepository", "Total de documentos retornados: ${allBooks.documents.size}")

            val filteredBooks = allBooks.documents.mapNotNull { doc ->
                try {
                    android.util.Log.d("BookRepository", "Processando documento de pesquisa: ${doc.id}")

                    // Converte manualmente para garantir que o ID seja capturado
                    val data = doc.data
                    if (data != null) {
                        val book = Book(
                            // SEMPRE usa o ID do documento do Firestore
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            author = data["author"] as? String ?: "",
                            publicationYear = when (val year = data["publication_year"]) {
                                is Number -> year.toInt()
                                is String -> year.toIntOrNull() ?: 0
                                else -> 0
                            },
                            categoryId = when (val catId = data["category_id"]) {
                                is Number -> catId.toInt()
                                is String -> catId.toIntOrNull() ?: 0
                                else -> 0
                            },
                            description = data["description"] as? String ?: "",
                            rating = when (val rat = data["rating"]) {
                                is Number -> rat.toInt()
                                is String -> rat.toIntOrNull() ?: 0
                                else -> 0
                            },
                            isDigital = data["is_digital"] as? Boolean ?: false,
                            totalCopies = when (val total = data["total_copies"]) {
                                is Number -> total.toInt()
                                is String -> total.toIntOrNull() ?: 0
                                else -> 0
                            },
                            availableCopies = when (val avail = data["available_copies"]) {
                                is Number -> avail.toInt()
                                is String -> avail.toIntOrNull() ?: 0
                                else -> 0
                            },
                            coverImageUrl = data["cover_image_url"] as? String ?: "",
                            digitalContentUrl = data["digital_content_url"] as? String ?: "",
                            isbn = data["isbn"] as? String ?: "",
                            createdAt = data["created_at"] as? Timestamp,
                            updatedAt = data["updated_at"] as? Timestamp
                        )

                        android.util.Log.d("BookRepository", "Livro convertido: ID=${book.id}, Title=${book.title}")
                        book
                    } else {
                        android.util.Log.e("BookRepository", "Documento sem dados: ${doc.id}")
                        null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BookRepository", "Erro ao converter livro na pesquisa: ${doc.id}", e)
                    null
                }
            }.filter { book ->
                val matches = book.title.lowercase().contains(queryLower) ||
                        book.author.lowercase().contains(queryLower) ||
                        book.isbn.lowercase().contains(queryLower)

                if (matches) {
                    android.util.Log.d("BookRepository", "Livro matched: ${book.title}")
                }
                matches
            }

            android.util.Log.d("BookRepository", "Total de livros filtrados: ${filteredBooks.size}")
            filteredBooks.forEach { book ->
                android.util.Log.d("BookRepository", "Resultado: ${book.title} (ID: ${book.id})")
            }

            Result.success(filteredBooks)
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "Erro na pesquisa de livros", e)
            Result.failure(e)
        }
    }

    // UPDATE - Atualizar livro
    suspend fun updateBook(book: Book): Result<Unit> {
        return try {
            val updatedBook = book.copy(updatedAt = Timestamp.now())
            booksCollection.document(book.id).set(updatedBook.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE - Atualizar disponibilidade (para empréstimos/devoluções)
    suspend fun updateAvailability(bookId: String, availableCopies: Int): Result<Unit> {
        return try {
            booksCollection.document(bookId).update(
                mapOf(
                    "available_copies" to availableCopies,
                    "updated_at" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DELETE - Remover livro
    suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            booksCollection.document(bookId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Método auxiliar para decrementar cópias disponíveis (quando emprestar)
    suspend fun borrowBook(bookId: String): Result<Unit> {
        return try {
            val book = getBookById(bookId).getOrNull()
            if (book != null && book.availableCopies > 0) {
                updateAvailability(bookId, book.availableCopies - 1)
            } else {
                Result.failure(Exception("Livro não disponível para empréstimo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Método auxiliar para incrementar cópias disponíveis (quando devolver)
    suspend fun returnBook(bookId: String): Result<Unit> {
        return try {
            val book = getBookById(bookId).getOrNull()
            if (book != null && book.availableCopies < book.totalCopies) {
                updateAvailability(bookId, book.availableCopies + 1)
            } else {
                Result.failure(Exception("Erro ao devolver livro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
