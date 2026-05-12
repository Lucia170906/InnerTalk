package com.example.innertalk.repository

import com.example.innertalk.R
import com.example.innertalk.model.ActivityFrequency
import com.example.innertalk.model.ActivityModel
import java.util.Calendar

class ActivityRepository {

    fun getSuggestedActivities(): List<ActivityModel> {
        //1. Todas las actividades sin filtrar
        val allActivities = listOf(
            // ==========================================
            // RESPIRACIÓN Y MINDFULNESS (1-10)
            // ==========================================
            ActivityModel("1", "Meditación Guiada", "Reduce el cortisol y calma la ansiedad mediante una guía profesional que aquieta tus pensamientos.", "10 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=inpok4MKVLM", ActivityFrequency.DAILY),
            ActivityModel("2", "Caminar Consciente", "Mejora la conexión mente-cuerpo y reduce la rumiación mental al enfocarte en tus sensaciones físicas.", "30 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("3", "Respiración 4-7-8", "Activa el sistema nervioso parasimpático para un alivio inmediato del estrés y ayuda a conciliar el sueño.", "3 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=S6CmxoIkHd4", ActivityFrequency.DAILY),
            ActivityModel("4", "Escaneo Corporal", "Identifica y libera tensiones acumuladas en los músculos, promoviendo una relajación profunda antes de dormir.", "15 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=3toPuPbDnuw", ActivityFrequency.DAILY),
            ActivityModel("5", "Técnica 5-4-3-2-1", "Detiene ataques de pánico o ansiedad intensa mediante el anclaje sensorial en el momento presente.", "5 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=aIno6T8UMQk", ActivityFrequency.RANDOM),
            ActivityModel("6", "Comer con Atención", "Mejora la digestión y la relación con la comida al reducir la impulsividad y aumentar el placer sensorial.", "10 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("7", "Mirar las Estrellas", "Fomenta la sensación de 'asombro' (awe), lo que reduce el ego y los problemas personales percibidos.", "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("8", "Baño de Sonido", "Utiliza frecuencias para sincronizar las ondas cerebrales, facilitando estados de meditación profunda sin esfuerzo.", "20 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=N73UWEsXKeU&list=RDN73UWEsXKeU&start_radio=1", ActivityFrequency.RANDOM),
            ActivityModel("9", "Respiración de Caja", "Técnica usada por los Navy SEALs para mantener la calma y la claridad mental bajo presión extrema.", "5 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=AXsBog5Q9BU", ActivityFrequency.DAILY),
            ActivityModel("10", "Escucha Activa", "Entrena la capacidad de concentración y reduce el ruido mental al enfocarte en el entorno externo.", "5 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            // ️ CONEXIÓN FÍSICA Y CUERPO (11-20)
            // ==========================================
            ActivityModel("11", "Estiramiento Exprés", "Mejora la circulación y oxigenación cerebral, eliminando la rigidez causada por el sedentarismo.", "5 min", R.drawable.ic_walk, false, false, "https://www.youtube.com/watch?v=pF46ZFaR7Ag", ActivityFrequency.RANDOM),
            ActivityModel("12", "Yoga Suave", "Aumenta la flexibilidad y libera bloqueos emocionales almacenados físicamente en las articulaciones.", "15 min", R.drawable.ic_walk, false, false, "https://www.youtube.com/watch?v=qMzk83G5JgY", ActivityFrequency.RANDOM),
            ActivityModel("13", "Baile Libre", "Libera endorfinas y dopamina, mejorando el estado de ánimo de forma casi instantánea.", "5 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("14", "Abrazo de Mariposa", "Estimulación bilateral que ayuda al cerebro a procesar emociones difíciles y recuperar la calma.", "3 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=e6DXrocKXI4", ActivityFrequency.RANDOM),
            ActivityModel("15", "Pies Descalzos", "Reduce la inflamación corporal y mejora el equilibrio gracias al contacto directo con la tierra.", "10 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("16", "Agua Fría", "Estimula el nervio vago, bajando las pulsaciones y reiniciando el sistema nervioso ante el estrés.", "2 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("17", "Relajación Progresiva", "Enseña al cerebro a reconocer la diferencia entre tensión y relajación, reduciendo el insomnio.", "15 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=vAaRM_wV5W8", ActivityFrequency.SUNDAY),
            ActivityModel("18", "Caminar sin Rumbo", "Estimula la creatividad y el pensamiento lateral al quitar la presión de un objetivo o destino.", "20 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("19", "Automasaje", "Reduce los niveles de tensión en los tejidos blandos y mejora el flujo linfático en zonas críticas.", "5 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=230lLzfLw0k", ActivityFrequency.RANDOM),
            ActivityModel("20", "Postura de Poder", "Cambia la química hormonal aumentando la testosterona y bajando el cortisol para mayor confianza.", "2 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            //  ESCRITURA Y REFLEXIÓN (21-30)
            // ==========================================
            ActivityModel("21", "Diario de Gratitud", "Entrena al cerebro para detectar lo positivo, aumentando la felicidad a largo plazo según la ciencia.", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("22", "Carta a mi Yo Futuro", "Ayuda a clarificar metas y valores, creando una visión de vida motivadora y coherente.", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("23", "Registro de Logros", "Combate el síndrome del impostor y fortalece la autoeficacia al reconocer tus pequeñas victorias.", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("24", "Desahogo Libre", "Drenaje emocional que permite exteriorizar traumas o frustraciones sin juicio externo.", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("25", "Tiempo de Preocupación", "Evita que la ansiedad tome control de todo tu día al asignarle un horario y espacio limitado.", "15 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("26", "Reformular Pensamientos", "Técnica de Terapia Cognitivo Conductual para romper ciclos de autocrítica y pensamientos irracionales.", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("27", "Lista de Perdón", "Libera la carga emocional del pasado y mejora la autoestima al aceptar nuestra humanidad.", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("28", "Afirmaciones 'Yo Soy'", "Reprograma creencias limitantes en el subconsciente mediante la repetición de verdades positivas.", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("29", "El Frasco de lo Bueno", "Crea un banco de memoria positiva que sirve como apoyo emocional en momentos de crisis futura.", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("30", "Carta a un Ser Querido", "Fortalece los vínculos emocionales y la empatía al procesar tus sentimientos hacia los demás.", "15 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            //  CREATIVIDAD Y DESCONEXIÓN (31-40)
            // ==========================================
            ActivityModel("31", "Desconexión Digital", "Permite que el cerebro descanse de la sobreestimulación de dopamina, mejorando la atención.", "1 hora", R.drawable.ic_relax, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("32", "Dibujar Mandalas", "Induce un estado de flujo (flow) similar al de la meditación, equilibrando ambos hemisferios cerebrales.", "20 min", R.drawable.ic_creativity, false, false, "https://www.youtube.com/watch?v=CCZlD_dIXSc&list=PL8bWmx3r1IoZBuzNXEMLnnkxCtUp6gKO8", ActivityFrequency.RANDOM),
            ActivityModel("33", "Musicoterapia Lofi", "Crea una atmósfera de calma que reduce la ansiedad ambiental y mejora la concentración.", "30 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=CFGLoQIhmow&list=RDCFGLoQIhmow&start_radio=1", ActivityFrequency.RANDOM),
            ActivityModel("34", "Escribir un Haiku", "Obliga a la mente a sintetizar emociones en pocas palabras, fomentando la presencia mental.", "10 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("35", "Ordenar un Espacio", "El orden externo genera orden interno, reduciendo el estrés visual y aumentando la calma.", "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("36", "Fotografía Consciente", "Entrena el ojo para buscar la belleza en lo cotidiano, cambiando tu perspectiva del día.", "10 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("37", "Aprender algo Nuevo", "Estimula la neuroplasticidad cerebral, manteniendo la mente ágil y joven.", "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("38", "Origami", "Mejora la coordinación mano-ojo y la paciencia mediante una actividad repetitiva y artística.", "15 min", R.drawable.ic_creativity, false, false, "https://www.youtube.com/watch?v=y1uzBncUsQQ&list=RDy1uzBncUsQQ&start_radio=1", ActivityFrequency.RANDOM),
            ActivityModel("39", "Cocina Consciente", "Transforma una tarea rutinaria en un acto de autocuidado y nutrición con todos los sentidos.", "40 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("40", "Rompecabezas Mental", "Previene el deterioro cognitivo y mejora la capacidad de resolución de problemas bajo calma.", "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            //  CUIDADO PERSONAL Y SOCIAL (41-50)
            // ==========================================
            ActivityModel("41", "Contacto Social", "Reduce la sensación de soledad y libera oxitocina, la hormona del vínculo y la confianza.", "15 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("42", "Mensaje de Gratitud", "Mejora no solo tu bienestar, sino el de otra persona, creando un círculo de afecto positivo.", "5 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("43", "Cita Contigo Mismo", "Refuerza la independencia emocional y el autoconocimiento al disfrutar de tu propia compañía.", "2 horas", R.drawable.ic_relax, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("44", "Rutina de Skincare", "Un acto de amor propio que marca el inicio o fin del día con un cuidado físico consciente.", "10 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("45", "Planear una Ilusión", "Genera dopamina anticipatoria, lo cual ayuda a superar semanas difíciles con optimismo.", "20 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("46", "Ducha Consciente", "Limpia simbólicamente las cargas del día mientras relajas los músculos con el agua.", "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("47", "Elogiar a Alguien", "Fomenta la amabilidad y mejora el clima social a tu alrededor de forma inmediata.", "5 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("48", "Mirar Fotos Antiguas", "Conecta con tu historia personal y revive momentos de alegría para mejorar el humor.", "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("49", "Vaso de Agua", "Mejora la función cognitiva y la energía, ya que una leve deshidratación causa fatiga y confusión.", "2 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("50", "Pausa Total", "Reset mental necesario para evitar el agotamiento emocional y recuperar la presencia.", "5 min", R.drawable.ic_meditation, false, false, null, ActivityFrequency.RANDOM)
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