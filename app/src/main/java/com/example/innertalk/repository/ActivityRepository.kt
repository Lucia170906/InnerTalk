package com.example.innertalk.repository

import android.content.Context
import com.example.innertalk.R
import com.example.innertalk.model.ActivityFrequency
import com.example.innertalk.model.ActivityModel
import java.util.Calendar

class ActivityRepository (private val context : Context){

    fun getSuggestedActivities(): List<ActivityModel> {
        //1. Todas las actividades sin filtrar
        val allActivities = listOf(
            // ==========================================
            // RESPIRACIÓN Y MINDFULNESS (1-10)
            // ==========================================
            ActivityModel("1", context.getString(R.string.act_title_1), context.getString(R.string.act_desc_1), "10 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=inpok4MKVLM", ActivityFrequency.DAILY),
            ActivityModel("2", context.getString(R.string.act_title_2), context.getString(R.string.act_desc_2), "30 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("3", context.getString(R.string.act_title_3), context.getString(R.string.act_desc_3), "3 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=S6CmxoIkHd4", ActivityFrequency.DAILY),
            ActivityModel("4", context.getString(R.string.act_title_4), context.getString(R.string.act_desc_4), "15 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=3toPuPbDnuw", ActivityFrequency.DAILY),
            ActivityModel("5", context.getString(R.string.act_title_5), context.getString(R.string.act_desc_5), "5 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=aIno6T8UMQk", ActivityFrequency.RANDOM),
            ActivityModel("6", context.getString(R.string.act_title_6), context.getString(R.string.act_desc_6), "10 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("7", context.getString(R.string.act_title_7), context.getString(R.string.act_desc_7), "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("8", context.getString(R.string.act_title_8), context.getString(R.string.act_desc_8), "20 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=N73UWEsXKeU", ActivityFrequency.RANDOM),
            ActivityModel("9", context.getString(R.string.act_title_9), context.getString(R.string.act_desc_9), "5 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=AXsBog5Q9BU", ActivityFrequency.DAILY),
            ActivityModel("10", context.getString(R.string.act_title_10), context.getString(R.string.act_desc_10), "5 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),

            // CONEXIÓN FÍSICA Y CUERPO
            ActivityModel("11", context.getString(R.string.act_title_11), context.getString(R.string.act_desc_11), "5 min", R.drawable.ic_walk, false, false, "https://www.youtube.com/watch?v=pF46ZFaR7Ag", ActivityFrequency.RANDOM),
            ActivityModel("12", context.getString(R.string.act_title_12), context.getString(R.string.act_desc_12), "15 min", R.drawable.ic_walk, false, false, "https://www.youtube.com/watch?v=qMzk83G5JgY", ActivityFrequency.RANDOM),
            ActivityModel("13", context.getString(R.string.act_title_13), context.getString(R.string.act_desc_13), "5 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("14", context.getString(R.string.act_title_14), context.getString(R.string.act_desc_14), "3 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=e6DXrocKXI4", ActivityFrequency.RANDOM),
            ActivityModel("15", context.getString(R.string.act_title_15), context.getString(R.string.act_desc_15), "10 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("16", context.getString(R.string.act_title_16), context.getString(R.string.act_desc_16), "2 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("17", context.getString(R.string.act_title_17), context.getString(R.string.act_desc_17), "15 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=vAaRM_wV5W8", ActivityFrequency.SUNDAY),
            ActivityModel("18", context.getString(R.string.act_title_18), context.getString(R.string.act_desc_18), "20 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("19", context.getString(R.string.act_title_19), context.getString(R.string.act_desc_19), "5 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=230lLzfLw0k", ActivityFrequency.RANDOM),
            ActivityModel("20", context.getString(R.string.act_title_20), context.getString(R.string.act_desc_20), "2 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),

            // ESCRITURA Y REFLEXIÓN
            ActivityModel("21", context.getString(R.string.act_title_21), context.getString(R.string.act_desc_21), "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("22", context.getString(R.string.act_title_22), context.getString(R.string.act_desc_22), "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("23", context.getString(R.string.act_title_23), context.getString(R.string.act_desc_23), "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("24", context.getString(R.string.act_title_24), context.getString(R.string.act_desc_24), "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("25", context.getString(R.string.act_title_25), context.getString(R.string.act_desc_25), "15 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("26", context.getString(R.string.act_title_26), context.getString(R.string.act_desc_26), "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("27", context.getString(R.string.act_title_27), context.getString(R.string.act_desc_27), "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("28", context.getString(R.string.act_title_28), context.getString(R.string.act_desc_28), "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("29", context.getString(R.string.act_title_29), context.getString(R.string.act_desc_29), "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("30", context.getString(R.string.act_title_30), context.getString(R.string.act_desc_30), "15 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),

            // CREATIVIDAD Y DESCONEXIÓN
            ActivityModel("31", context.getString(R.string.act_title_31), context.getString(R.string.act_desc_31), "1 hora", R.drawable.ic_relax, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("32", context.getString(R.string.act_title_32), context.getString(R.string.act_desc_32), "20 min", R.drawable.ic_creativity, false, false, "https://www.youtube.com/watch?v=CCZlD_dIXSc", ActivityFrequency.RANDOM),
            ActivityModel("33", context.getString(R.string.act_title_33), context.getString(R.string.act_desc_33), "30 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=CFGLoQIhmow", ActivityFrequency.RANDOM),
            ActivityModel("34", context.getString(R.string.act_title_34), context.getString(R.string.act_desc_34), "10 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("35", context.getString(R.string.act_title_35), context.getString(R.string.act_desc_35), "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("36", context.getString(R.string.act_title_36), context.getString(R.string.act_desc_36), "10 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("37", context.getString(R.string.act_title_37), context.getString(R.string.act_desc_37), "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("38", context.getString(R.string.act_title_38), context.getString(R.string.act_desc_38), "15 min", R.drawable.ic_creativity, false, false, "https://www.youtube.com/watch?v=y1uzBncUsQQ", ActivityFrequency.RANDOM),
            ActivityModel("39", context.getString(R.string.act_title_39), context.getString(R.string.act_desc_39), "40 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("40", context.getString(R.string.act_title_40), context.getString(R.string.act_desc_40), "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),

            // CUIDADO PERSONAL Y SOCIAL
            ActivityModel("41", context.getString(R.string.act_title_41), context.getString(R.string.act_desc_41), "15 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("42", context.getString(R.string.act_title_42), context.getString(R.string.act_desc_42), "5 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("43", context.getString(R.string.act_title_43), context.getString(R.string.act_desc_43), "2 horas", R.drawable.ic_relax, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("44", context.getString(R.string.act_title_44), context.getString(R.string.act_desc_44), "10 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("45", context.getString(R.string.act_title_45), context.getString(R.string.act_desc_45), "20 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("46", context.getString(R.string.act_title_46), context.getString(R.string.act_desc_46), "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("47", context.getString(R.string.act_title_47), context.getString(R.string.act_desc_47), "5 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("48", context.getString(R.string.act_title_48), context.getString(R.string.act_desc_48), "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("49", context.getString(R.string.act_title_49), context.getString(R.string.act_desc_49), "2 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("50", context.getString(R.string.act_title_50), context.getString(R.string.act_desc_50), "5 min", R.drawable.ic_meditation, false, false, null, ActivityFrequency.RANDOM)
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