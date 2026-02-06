package com.example.innertalk.model

data class ActivityModel(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val iconRes: Int,
    val checkbox :Boolean,

    var isCompleted: Boolean = false
)