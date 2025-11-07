package com.example.uniforlibrary.acervo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniforlibrary.acervo.model.Acervo
import com.example.uniforlibrary.acervo.repository.AcervoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AcervoViewModel(
    private val repository: AcervoRepository = AcervoRepository()
) : ViewModel() {

    private val _acervos = MutableStateFlow<List<Acervo>>(emptyList())
    val acervos: StateFlow<List<Acervo>> = _acervos

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

    fun searchAcervos(query: String) {
        if (query.isBlank()) {
            loadAcervos()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.searchAcervos(query)
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
        categoria: String? = null,
        disponivel: Boolean? = null,
        digital: Boolean? = null,
        fisico: Boolean? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.filterAcervos(categoria, disponivel, digital, fisico)
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

    fun addAcervo(acervo: Acervo) {
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

    fun updateAcervo(id: String, acervo: Acervo) {
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

    fun deleteAcervo(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteAcervo(id)
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
}
