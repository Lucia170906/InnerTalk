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
            ActivityModel("1", "Meditación Guiada", "Relaja tu mente con este video", "10 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=inpok4MKVLM", ActivityFrequency.DAILY),
            ActivityModel("2", "Caminar Consciente", "Sal a dar una vuelta, respira y nota tu entorno", "30 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("3", "Respiración 4-7-8", "Técnica rápida para reducir la ansiedad", "3 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=1xN5-z2Btb4", ActivityFrequency.DAILY),
            ActivityModel("4", "Escaneo Corporal", "Relajación profunda ideal antes de dormir", "15 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=QS2yOmGitoQ", ActivityFrequency.DAILY),
            ActivityModel("5", "Técnica 5-4-3-2-1", "Encuentra 5 cosas que ver, 4 tocar, 3 oír, 2 oler, 1 saborear", "5 min", R.drawable.ic_meditation, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("6", "Comer con Atención", "Come un pequeño snack sin pantallas, saboreando cada bocado", "10 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("7", "Mirar las Estrellas", "Siéntate a observar el cielo o las nubes en silencio", "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("8", "Baño de Sonido", "Escucha frecuencias curativas con los ojos cerrados", "20 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=1zxj_z-U1gI", ActivityFrequency.RANDOM),
            ActivityModel("9", "Respiración de Caja", "Inhala 4s, sostén 4s, exhala 4s, sostén 4s", "5 min", R.drawable.ic_meditation, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("10", "Escucha Activa", "Cierra los ojos e intenta identificar 5 sonidos lejanos", "5 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            // ️ CONEXIÓN FÍSICA Y CUERPO (11-20)
            // ==========================================
            ActivityModel("11", "Estiramiento Exprés", "Libera la tensión acumulada en el cuerpo", "5 min", R.drawable.ic_walk, false, false, "https://www.youtube.com/watch?v=L_xrDAtykMI", ActivityFrequency.RANDOM),
            ActivityModel("12", "Yoga Suave", "Conecta con tu cuerpo mediante posturas fáciles", "15 min", R.drawable.ic_walk, false, false, "https://www.youtube.com/watch?v=v7AYKMP6rOE", ActivityFrequency.RANDOM),
            ActivityModel("13", "Baile Libre", "Pon tu canción favorita y baila sin que nadie te vea", "5 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("14", "Abrazo de Mariposa", "Cruza los brazos y date pequeños toques para calmarte", "3 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=ZggvK-lI8eE", ActivityFrequency.RANDOM),
            ActivityModel("15", "Pies Descalzos", "Camina descalzo sobre la hierba o por casa (Earthing)", "10 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("16", "Agua Fría", "Lávate la cara con agua fría para reiniciar tu sistema nervioso", "2 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("17", "Relajación Muscular Progresiva", "Tensa y relaja cada músculo de tu cuerpo", "15 min", R.drawable.ic_meditation, false, false, "https://www.youtube.com/watch?v=ihO02wUzgkc", ActivityFrequency.SUNDAY),
            ActivityModel("18", "Caminar sin Rumbo", "Sal a dar un paseo sin un destino fijo, solo por moverte", "20 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("19", "Automasaje", "Masajea tus sienes, cuello y hombros suavemente", "5 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("20", "Postura de Poder", "Ponte de pie como un superhéroe durante 2 minutos", "2 min", R.drawable.ic_walk, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            //  ESCRITURA Y REFLEXIÓN (21-30)
            // ==========================================
            ActivityModel("21", "Diario de Gratitud", "Escribe 3 cosas buenas de tu semana", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("22", "Carta a mi Yo Futuro", "Escribe qué esperas de ti en un año", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("23", "Registro de Logros", "Anota algo difícil que lograste superar hoy", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("24", "Desahogo Libre", "Escribe sin filtros todo lo que te frustra y bórralo", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("25", "Tiempo de Preocupación", "Dedica tiempo límite a escribir tus miedos y luego ciérralo", "15 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("26", "Reformular Pensamientos", "Escribe un pensamiento negativo y cámbialo a uno realista", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("27", "Lista de Perdon", "Escribe algo por lo que necesitas perdonarte a ti mismo", "10 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("28", "Afirmaciones 'Yo Soy'", "Escribe 5 frases positivas que empiecen por 'Yo soy...'", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("29", "El Frasco de lo Bueno", "Anota un buen recuerdo reciente para guardarlo", "5 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("30", "Carta a un Ser Querido", "Escribe lo que sientes por alguien (no tienes que enviarla)", "15 min", R.drawable.ic_journal, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            //  CREATIVIDAD Y DESCONEXIÓN (31-40)
            // ==========================================
            ActivityModel("31", "Desconexión Digital", "Pon el móvil en modo avión y lee o descansa", "1 hora", R.drawable.ic_relax, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("32", "Dibujar Mandalas", "Dibuja o colorea patrones para calmar la mente", "20 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("33", "Musicoterapia Lofi", "Escucha música sin letra para concentrarte o relajarte", "30 min", R.drawable.ic_relax, false, false, "https://www.youtube.com/watch?v=jfKfPfyJRdk", ActivityFrequency.RANDOM),
            ActivityModel("34", "Escribir un Haiku", "Crea un poema corto de 3 líneas sobre cómo te sientes", "10 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("35", "Ordenar un Espacio Pequeño", "Limpia solo tu escritorio o un cajón", "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("36", "Fotografía Consciente", "Haz 3 fotos a cosas bonitas o curiosas en tu casa", "10 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("37", "Aprender algo Nuevo", "Mira un mini-tutorial sobre algo que no sepas hacer", "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("38", "Origami", "Haz una grulla de papel o un barco siguiendo un video", "15 min", R.drawable.ic_creativity, false, false, "https://www.youtube.com/watch?v=KfnyopxdJXQ", ActivityFrequency.RANDOM),
            ActivityModel("39", "Cocina Consciente", "Prepara tu plato favorito con cariño y sin prisas", "40 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("40", "Rompecabezas Mental", "Haz un sudoku, palabras cruzadas o puzle visual", "15 min", R.drawable.ic_creativity, false, false, null, ActivityFrequency.RANDOM),

            // ==========================================
            //  CUIDADO PERSONAL Y SOCIAL (41-50)
            // ==========================================
            ActivityModel("41", "Contacto Social", "Llama (no escribas) a un familiar o amigo", "15 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("42", "Mensaje de Gratitud", "Envíale un texto a alguien dándole las gracias por algo", "5 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("43", "Cita Contigo Mismo", "Mira una película, pide tu comida favorita y mímate", "2 horas", R.drawable.ic_relax, false, false, null, ActivityFrequency.SUNDAY),
            ActivityModel("44", "Rutina de Skincare", "Lávate la cara e hidrátate la piel con calma", "10 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("45", "Planear una Ilusión", "Busca un viaje o evento para el futuro y guárdalo", "20 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("46", "Ducha Consciente", "Siente la temperatura del agua y el olor del jabón", "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("47", "Elogiar a Alguien", "Hazle un cumplido sincero a un desconocido o conocido", "5 min", R.drawable.ic_social, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("48", "Mirar Fotos Antiguas", "Repasa tu galería y busca recuerdos que te hagan sonreír", "15 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.RANDOM),
            ActivityModel("49", "Vaso de Agua", "Levántate y bebe un vaso grande de agua ahora mismo", "2 min", R.drawable.ic_relax, false, false, null, ActivityFrequency.DAILY),
            ActivityModel("50", "Cerrar los Ojos y Nada Más", "Simplemente siéntate y no hagas absolutamente nada", "5 min", R.drawable.ic_meditation, false, false, null, ActivityFrequency.RANDOM)
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