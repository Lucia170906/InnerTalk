package com.example.innertalk.repository

import com.example.innertalk.R
import com.example.innertalk.model.ActivityFrequency
import com.example.innertalk.model.ActivityModel
import java.util.Calendar

class ActivityRepository {

    fun getSuggestedActivities(): List<ActivityModel> {
        //1. Todas las actividades sin filtrar
        val allActivities =  listOf(
            ActivityModel(1, "Meditación Guiada", "Relaja tu mente con este video", "10 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=inpok4MKVLM", ActivityFrequency.DAILY),            ActivityModel(2, "Caminar", "Sal a dar una vuelta", "30 min", R.drawable.ic_walk, false),
            ActivityModel(2, "Caminar", "Sal a dar una vuelta y respira", "30 min", R.drawable.ic_walk, true, false, null, ActivityFrequency.DAILY),
            ActivityModel(3, "Diario de Gratitud", "Escribe 3 cosas buenas de tu semana", "5 min", R.drawable.ic_journal, true, false, null, ActivityFrequency.SUNDAY),
            ActivityModel(4, "Estiramiento Exprés", "Mueve un poco el cuerpo", "5 min", R.drawable.ic_walk, true, false, "https://www.youtube.com/watch?v=L_xrDAtykMI", ActivityFrequency.RANDOM)
        )

        //2. Obtenemos que día es hoy
        val calendar = Calendar.getInstance()
        val isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

        //3. Filtramos la lista de actividades según que día sea hoy
        val filteredList = allActivities.filter { activity ->
            when(activity.frequency){
                ActivityFrequency.DAILY-> true //actividades diarias se muestran siempre
                ActivityFrequency.SUNDAY-> isSunday // pasan si isSunday es true
                ActivityFrequency.RANDOM-> true //las dejamos, más tarde las mezclaremos
            }
        }.toMutableList()

        //4. Mezclamos las actividades aleatorias
        val randomActivities = filteredList.filter{it.frequency == ActivityFrequency.RANDOM }.shuffled()
        //cogemos "n" elementos de la lista de actividades aleatorias añadiendolas a las actividades fijas
        val finalActivities = filteredList.filter { it.frequency != ActivityFrequency.RANDOM }+ randomActivities.take(1)
        // devolvemos las actividades finales
        return finalActivities

    }



}