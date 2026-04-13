package com.example.innertalk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.navigation.fragment.findNavController
import com.example.innertalk.databinding.FragmentStatisticsBinding
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class StatisticsFragment : Fragment() {

    // 1. El _binding es el que realmente guarda la referencia y es nuleable
    private var _binding: FragmentStatisticsBinding? = null

    private val binding get() = _binding!! // para no pones ? en todo el codigo

    //Referencias  Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/").reference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializamos el _binding
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leerEstadisticasFirebase()//para poner las estadisticas desde primer momento
        contarEntradasTotales() // para mostar el numero de enrdas dsde el principio
        mostrarFraseAleatoria() // frase motivadora

        binding.cardTotalEntries.setOnClickListener {

            findNavController().navigate(R.id.action_statisticsFragment_to_entriesFragment)


        }

    }

    private fun  leerEstadisticasFirebase(){
        val uid = auth.currentUser?.uid ?: return

        //apuntamos al nodo de estadisticas dentro de Firebase
        val statsRef = database.child("usuarios").child(uid).child("estadisticas")

        //Vamos a  usar addValueEventListener para que el grafico pueda actualizarse en tiempo rea
        statsRef.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange (snapshot: DataSnapshot){
                // Extraemos los valores, si aún no existen (el usuario no ha hecho ninguna entrada) ponemos 0
                if(snapshot.exists()){
                    val veryHappy = snapshot.child("emocion_1").getValue(Int::class.java)?:0
                    val happy = snapshot.child("emocion_2").getValue(Int::class.java) ?: 0
                    val neutral = snapshot.child("emocion_3").getValue(Int::class.java) ?: 0
                    val sad = snapshot.child("emocion_4").getValue(Int::class.java) ?: 0
                    val angry = snapshot.child("emocion_5").getValue(Int::class.java) ?: 0



                    //llamamos a la funcion para actualizar los datos del grafico
                    actualizarGrafico(veryHappy, happy, neutral, sad, angry)

                    generarAnalisisEmocional(veryHappy, happy, neutral, sad, angry)
                }else{
                    Toast.makeText(requireContext(), "No hay datos en Firebase aún", Toast.LENGTH_SHORT).show()
                    // Dibujamos uno vacío para que al menos se vea el título
                    actualizarGrafico(0, 0, 0, 0, 0)

                    generarAnalisisEmocional(0, 0, 0, 0, 0)
                }


            }

            // si hay un error al obtener los datos de la base

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, "Error al cargar los datos", LENGTH_SHORT).show()
            }


        })
    }

    private fun actualizarGrafico(v1: Int, v2: Int, v3: Int, v4: Int, v5: Int) {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .title("Análisis de Emociones")
            .backgroundColor("#F5F9FF")
            .dataLabelsEnabled(true)
            .colorsTheme(arrayOf("#4CAF50", "#FFEB3B", "#9E9E9E", "#FF9800", "#F44336")) //para añadir colores deseados
            .series(arrayOf(
                AASeriesElement()
                    .name("Frecuencia")
                    .data(arrayOf(
                        arrayOf("Muy Feliz", v1),
                        arrayOf("Contento", v2),
                        arrayOf("Apático", v3),
                        arrayOf("Triste", v4),
                        arrayOf("Enfadado",v5)
                    ))
            ))


        // Acceso directo gracias al binding
        binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

    private fun contarEntradasTotales(){
        val uid = auth.currentUser?.uid ?: return

        //Apuntamo al nodo donde guardamos los diarios del currenrUser

        val diarioRef = database.child("usuarios").child(uid).child("diario")

        diarioRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //.childrenCount nos da el número de elementos en la lista
                val total = snapshot.childrenCount
                binding.tvTotalEntries.text = total.toString()
            }

            override fun onCancelled(p0: DatabaseError) {
                binding.tvTotalEntries.text="-" // si da error ponemos un guión por estética
            }
        })


    }

    private fun generarAnalisisEmocional (v1: Int, v2: Int, v3: Int, v4: Int, v5: Int){
        //1. Guardamos los valores en un mapa para saber a que emoción correcponde cada número
        val emociones = mapOf(
            "Muy Feliz" to v1,
            "Contento" to v2,
            "Apático" to v3,
            "Triste" to v4,
            "Enfadado" to v5
        )

        //2.Buscamos que emoción tiene el valor máximo
        val maxValor = emociones.values.maxOrNull() ?:0

        //Si todas las emociones son 0 (el usuario no tiene registro)
        //mostramos un mensaje de bienvenida para el usuario
        if(maxValor==0){
            binding.tvEmotionTitle.text="¡Empieza tu viaje!"
            binding.tvAnalysisText.text="Registra como te sientes hoy en tu diario para comenzar tu análisi emocional"
            return
        }

        //3. Obtenemos las emociones que tiene el valor máximo (puede haber empates)
        val emocionesDominantes = emociones.filter { it.value == maxValor }.keys.toList() //el .keys extrae el nombre de las emociones

        //4. Generamos el título y el análisi dependiendo de que emoción es más frecuente
        //Si hay un empate cogeremos la primera de la lista
        val emocionPrincipal = emocionesDominantes.first()
        val titulo : String
        val analisis : String

        when (emocionPrincipal) {
            "Muy Feliz", "Contento" -> {
                titulo = "¡Estás en una buena racha!  ☀\uFE0F"
                analisis =
                    "Tu emoción predominante es '$emocionPrincipal'. Parece que últimamente estás encontrando motivos para sonreír. ¡Sigue así! Recuerda guardar estos buenos momentos en tu Diario de Gratitud."
            }

            "Apático" -> {
                titulo = "Días de calma plana ☁☁\uFE0F"
                analisis =
                    "Tu emoción más frecuente es '$emocionPrincipal'. Es normal tener temporadas donde todo da un poco igual. Intenta probar alguna actividad nueva, como 'Caminar sin Rumbo', para despertar un poco tus sentidos."
            }

            "Triste" -> {
                titulo = "Abrazo virtual en camino \uD83D\uDC9A"
                analisis =
                    "He notado que la tristeza ha estado muy presente. No pasa nada por sentirse así. Apóyate en el chat de InnerTalk si necesitas desahogarte, o prueba una meditación guiada."
            }

            "Enfadado" -> {
                titulo = "Tensión acumulada 🌩\uD83C\uDF29\uFE0F"
                analisis =
                    "El enfado es tu emoción principal ahora mismo. Quizás estás pasando por mucho estrés. Intenta hacer la actividad 'Estiramiento Exprés' o la 'Respiración 4-7-8' para soltar esa carga."
            }

            else -> {
                titulo = "Analizando tus emociones..."
                analisis = "Sigue registrando tu estado de ánimo para obtener un informe detallado."
            }

        }
        //5. Actualizamos los textos en la pantalla
        binding.tvEmotionTitle.text = titulo
        binding.tvAnalysisText.text = analisis
    }

    //sisema de frase provisional, tal vez se evolucione a sistema de rachas

    private fun mostrarFraseAleatoria(){
        val frases = listOf(
                "Está bien no estar bien siempre.",
                "Tu progreso no tiene que ser perfecto.",
                "Respira. Has superado el 100% de tus días malos.",
                "Hoy es un buen día para cuidarte.",
                "Tus emociones son válidas.",
                "Un paso a la vez, no hay prisa."
        )

        binding.tvQuote.text= frases.random()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        // 4. Importantísimo: limpiar el binding para evitar fugas de memoria
        _binding = null
    }
}