package com.example.uniforlibrary.acervoAdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.acervoAdm.model.AcervoAdm
import com.example.uniforlibrary.acervoAdm.repository.AcervoAdmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AcervoAdmViewModel : ViewModel() {
    private val repository = AcervoAdmRepository()

    private val _acervos = MutableStateFlow<List<AcervoAdm>>(emptyList())
    val acervos: StateFlow<List<AcervoAdm>> = _acervos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadAcervos()
    }

    fun loadAcervos() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllAcervos()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { acervos ->
                            _acervos.value = acervos
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
                    _isLoading.value = false
                }
        }
    }

    fun filterAcervos(
        searchQuery: String = "",
        categoria: String? = null,
        disponibilidade: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.filterAcervos(searchQuery, categoria, disponibilidade)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { acervos ->
                            _acervos.value = acervos
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
                    _isLoading.value = false
                }
        }
    }

    fun addAcervo(acervo: AcervoAdm) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addAcervo(acervo)
                .fold(
                    onSuccess = {
                        loadAcervos()
                        _error.value = null
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            _isLoading.value = false
        }
    }

    fun updateAcervo(id: String, acervo: AcervoAdm) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateAcervo(id, acervo)
                .fold(
                    onSuccess = {
                        loadAcervos()
                        _error.value = null
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            _isLoading.value = false
        }
    }

    fun removeAcervo(book: Book?) {
        if (book == null) return

        viewModelScope.launch {
            _isLoading.value = true
            // Primeiro precisamos encontrar o acervo correspondente ao book
            val acervo = _acervos.value.find { it.titulo == book.title }

            if (acervo != null) {
                repository.deleteAcervo(acervo.id)
                    .fold(
                        onSuccess = {
                            loadAcervos()
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
            } else {
                _error.value = "Livro não encontrado"
            }
            _isLoading.value = false
        }
    }
}
