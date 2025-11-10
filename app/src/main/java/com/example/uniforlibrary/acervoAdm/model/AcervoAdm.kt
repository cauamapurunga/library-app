package com.example.uniforlibrary.acervoAdm.model

data class AcervoAdm(
    val id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val ano: Int = 0,
    val categoria: String = "",
    val descricao: String = "",
    val classificacao: Double = 0.0,
    val disponivel: Boolean = true,
    val exemplaresTotais: Int = 0,
    val exemplaresDisponiveis: Int = 0,
    val digital: Boolean = false,
    val fisico: Boolean = true,
    val imagemUrl: String = "",
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualização: Long = System.currentTimeMillis()
)