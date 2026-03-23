package com.example.innertalk.model

// etiquetaspara la frecuencia de las actividades
enum class ActivityFrequency{
    DAILY,
    SUNDAY,
    RANDOM
}

data class ActivityModel(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val iconRes: Int,
    val checkbox :Boolean,

    var isCompleted: Boolean = false,
    val url: String?  =null,
    val frequency : ActivityFrequency = ActivityFrequency.DAILY //por defecto todos los días



)