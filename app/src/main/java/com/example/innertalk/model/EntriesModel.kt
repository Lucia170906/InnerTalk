package com.example.innertalk.model

data class EntriesModel(
    val id: String = "",
    val texto: String = "",
    val emocion: Int = 1, // Del 1 al 5 según la cara elegida
    val fecha: String = "",
    val fotoBase64: String? = null
)