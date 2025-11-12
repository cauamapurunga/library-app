package com.example.uniforlibrary.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.model.Book
import com.example.uniforlibrary.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookUiState {
    object Loading : BookUiState()
    data class Success(val books: List<Book>) : BookUiState()
    data class Error(val message: String) : BookUiState()
}

class BookViewModel : ViewModel() {
    private val repository = BookRepository()

    private val _uiState = MutableStateFlow<BookUiState>(BookUiState.Loading)
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults.asStateFlow()

    private val _operationResult = MutableStateFlow<Result<String>?>(null)
    val operationResult: StateFlow<Result<String>?> = _operationResult.asStateFlow()

    init {
        android.util.Log.d("BookViewModel", "ViewModel inicializado, carregando livros...")
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            android.util.Log.d("BookViewModel", "loadBooks() chamado")
            _uiState.value = BookUiState.Loading
            try {
                repository.getAllBooks().collect { books ->
                    android.util.Log.d("BookViewModel", "Livros coletados no ViewModel: ${books.size}")
                    books.forEachIndexed { index, book ->
                        android.util.Log.d("BookViewModel", "Livro $index: ${book.title} - ${book.author}")
                    }
                    _uiState.value = BookUiState.Success(books)
                }
            } catch (e: Exception) {
                android.util.Log.e("BookViewModel", "Erro ao carregar livros", e)
                _uiState.value = BookUiState.Error(e.message ?: "Erro ao carregar livros")
            }
        }
    }

    fun searchBooks(query: String) {
        android.util.Log.d("BookViewModel", "searchBooks chamado com query: '$query'")

        if (query.isBlank()) {
            android.util.Log.d("BookViewModel", "Query vazia, limpando resultados")
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                android.util.Log.d("BookViewModel", "Iniciando pesquisa no repository...")
                val result = repository.searchBooks(query)
                result.onSuccess { books ->
                    android.util.Log.d("BookViewModel", "Pesquisa bem-sucedida: ${books.size} livros encontrados")
                    books.forEach { book ->
                        android.util.Log.d("BookViewModel", "Livro encontrado: ${book.title} (ID: ${book.id})")
                    }
                    _searchResults.value = books
                }.onFailure { e ->
                    android.util.Log.e("BookViewModel", "Erro na pesquisa", e)
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookViewModel", "Exceção durante pesquisa", e)
                _searchResults.value = emptyList()
            }
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            try {
                val result = repository.addBook(book)
                _operationResult.value = result.map { "Livro adicionado com sucesso!" }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            try {
                val result = repository.updateBook(book)
                _operationResult.value = result.map { "Livro atualizado com sucesso!" }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteBook(bookId)
                _operationResult.value = result.map { "Livro removido com sucesso!" }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun getBooksByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = BookUiState.Loading
            try {
                repository.getBooksByCategory(category).collect { books ->
                    _uiState.value = BookUiState.Success(books)
                }
            } catch (e: Exception) {
                _uiState.value = BookUiState.Error(e.message ?: "Erro ao carregar livros")
            }
        }
    }

    fun getAvailableBooks() {
        viewModelScope.launch {
            _uiState.value = BookUiState.Loading
            try {
                repository.getAvailableBooks().collect { books ->
                    _uiState.value = BookUiState.Success(books)
                }
            } catch (e: Exception) {
                _uiState.value = BookUiState.Error(e.message ?: "Erro ao carregar livros")
            }
        }
    }

    fun borrowBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = repository.borrowBook(bookId)
                _operationResult.value = result.map { "Empréstimo realizado com sucesso!" }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun returnBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = repository.returnBook(bookId)
                _operationResult.value = result.map { "Devolução realizada com sucesso!" }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun uploadBookCover(context: Context, bookId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val result = repository.uploadBookCover(context, bookId, imageUri)
                _operationResult.value = result.map { "Capa do livro atualizada com sucesso!" }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }
}
