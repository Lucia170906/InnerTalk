package com.example.innertalk.repository

import com.example.innertalk.R
import com.example.innertalk.model.ActivityModel

class ActivityRepository {

    fun getSuggestedActivities(): List<ActivityModel> {
        return listOf(
            ActivityModel(1, "Meditación", "Relaja tu mente", "10 min", R.drawable.ic_meditation, false),
            ActivityModel(2, "Caminar", "Sal a dar una vuelta", "30 min", R.drawable.ic_walk, false),
            ActivityModel(3, "Diario", "Escribe tus reflexiones", "5 min", R.drawable.ic_journal, false)
        )


    }



}