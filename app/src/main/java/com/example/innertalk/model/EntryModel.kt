package com.example.innertalk.model

data class EntryModel(
    val id: String ="",
    val texto: String = "",
    val emocion: Int = 0,
    val fotoBase64: String? = null, // La foto irá  como texto
    val fecha: Long = System.currentTimeMillis()
)