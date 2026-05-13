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
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsFragment : Fragment() {

    // 1. El _binding es el que realmente guarda la referencia y es nuleable
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    // Referencias Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/").reference

    // NUEVO: Variables para guardar los listeners y las referencias de los nodos
    private var statsListener: ValueEventListener? = null
    private var diarioListener: ValueEventListener? = null
    private var statsRef: DatabaseReference? = null
    private var diarioRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leerEstadisticasFirebase()
        calcularRachaYEntradas()
        mostrarFraseAleatoria()

        binding.cardTotalEntries.setOnClickListener {
            findNavController().navigate(R.id.action_statisticsFragment_to_entriesFragment)
        }
    }

    private fun leerEstadisticasFirebase(){
        val uid = auth.currentUser?.uid ?: return

        // Guardamos la referencia en la variable de clase
        statsRef = database.child("usuarios").child(uid).child("estadisticas")

        // Guardamos el listener en la variable de clase para poder removerlo luego
        statsListener = statsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Validación de seguridad para el binding
                _binding?.let {
                    if(snapshot.exists()){
                        val veryHappy = snapshot.child("emocion_1").getValue(Int::class.java) ?: 0
                        val happy = snapshot.child("emocion_2").getValue(Int::class.java) ?: 0
                        val neutral = snapshot.child("emocion_3").getValue(Int::class.java) ?: 0
                        val sad = snapshot.child("emocion_4").getValue(Int::class.java) ?: 0
                        val angry = snapshot.child("emocion_5").getValue(Int::class.java) ?: 0

                        actualizarGrafico(veryHappy, happy, neutral, sad, angry)
                        generarAnalisisEmocional(veryHappy, happy, neutral, sad, angry)
                    } else {
                        actualizarGrafico(0, 0, 0, 0, 0)
                        generarAnalisisEmocional(0, 0, 0, 0, 0)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // CAMBIO: Uso de safe call (?.) para evitar el crash al cerrar sesión
                _binding?.let {
                    Toast.makeText(context, getString(R.string.loading_error), LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun actualizarGrafico(v1: Int, v2: Int, v3: Int, v4: Int, v5: Int) {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .title(getString(R.string.stats_chart_title))
            .backgroundColor("#F5F9FF")
            .dataLabelsEnabled(true)
            .colorsTheme(arrayOf("#4CAF50", "#FFEB3B", "#9E9E9E", "#FF9800", "#F44336"))
            .series(arrayOf(
                AASeriesElement()
                    .name(getString(R.string.stats_label_entries))
                    .data(arrayOf(
                        arrayOf(getString(R.string.emotion_very_happy), v1),
                        arrayOf(getString(R.string.emotion_happy), v2),
                        arrayOf(getString(R.string.emotion_neutral), v3),
                        arrayOf(getString(R.string.emotion_sad), v4),
                        arrayOf(getString(R.string.emotion_angry), v5)
                    ))
            ))

        _binding?.aaChartView?.aa_drawChartWithChartModel(aaChartModel)
    }

    private fun calcularRachaYEntradas(){
        val uid = auth.currentUser?.uid ?: return
        diarioRef = database.child("usuarios").child(uid).child("diario")

        diarioListener = diarioRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _binding?.let { safeBinding ->
                    val total = snapshot.childrenCount
                    safeBinding.tvTotalEntries.text = total.toString()

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val fechasUnicas = mutableSetOf<String>()

                    for (data in snapshot.children) {
                        try {
                            val fechaMillis = data.child("fecha").getValue(Long::class.java)
                            if (fechaMillis != null) {
                                val fechaLimpia = sdf.format(Date(fechaMillis))
                                fechasUnicas.add(fechaLimpia)
                            }
                        } catch (e: Exception) { }
                    }

                    val fechasOrdenadas = fechasUnicas.map { sdf.parse(it)!! }.sortedDescending()
                    var rachaActual = 0

                    if(fechasOrdenadas.isNotEmpty()){
                        val hoyStr = sdf.format(Date())
                        val calendarioAyer = Calendar.getInstance()
                        calendarioAyer.add(Calendar.DAY_OF_YEAR, -1)
                        val ayerStr = sdf.format(calendarioAyer.time)
                        val fechaMasRescientStr = sdf.format(fechasOrdenadas[0])

                        if(fechaMasRescientStr == hoyStr || fechaMasRescientStr == ayerStr){
                            rachaActual = 1
                            val calendarioEvaluador = Calendar.getInstance()
                            calendarioEvaluador.time = fechasOrdenadas[0]

                            for( i in 1 until fechasOrdenadas.size){
                                val diaEsperado = calendarioEvaluador.clone() as Calendar
                                diaEsperado.add(Calendar.DAY_OF_YEAR, -1)
                                val esperadoStr = sdf.format(diaEsperado.time)
                                val fechaAComprobarStr = sdf.format(fechasOrdenadas[i])

                                if(fechaAComprobarStr == esperadoStr){
                                    rachaActual++
                                    calendarioEvaluador.add(Calendar.DAY_OF_YEAR, -1)
                                } else {
                                    break
                                }
                            }
                        }
                    }
                    safeBinding.tvStreakDays.text = rachaActual.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // CAMBIO: Lógica corregida para evitar NullPointerException
                _binding?.let { safeBinding ->
                    safeBinding.tvTotalEntries.text = "-"
                    safeBinding.tvStreakDays.text = "0"
                }
            }
        })
    }

    private fun generarAnalisisEmocional(v1: Int, v2: Int, v3: Int, v4: Int, v5: Int){
        val emociones = mapOf(
            getString(R.string.emotion_very_happy) to v1,
            getString(R.string.emotion_happy) to v2,
            getString(R.string.emotion_neutral) to v3,
            getString(R.string.emotion_sad) to v4,
            getString(R.string.emotion_angry) to v5
        )

        val maxValor = emociones.values.maxOrNull() ?: 0

        if(maxValor == 0){
            binding.tvEmotionTitle.text = getString(R.string.stats_analyzing_title)
            binding.tvAnalysisText.text = getString(R.string.stats_analyzing_desc)
            return
        }

        val emocionesDominantes = emociones.filter { it.value == maxValor }.keys.toList()
        val emocionPrincipal = emocionesDominantes.first()
        val titulo: String
        val analisis: String

        when (emocionPrincipal) {
            getString(R.string.emotion_very_happy), getString(R.string.emotion_happy) -> {
                titulo = getString(R.string.stats_title_happy)
                analisis = getString(R.string.stats_desc_happy, emocionPrincipal)
            }
            getString(R.string.emotion_neutral) -> {
                titulo = getString(R.string.stats_title_neutral)
                analisis = getString(R.string.stats_desc_neutral, emocionPrincipal)
            }
            getString(R.string.emotion_sad) -> {
                titulo = getString(R.string.stats_title_sad)
                analisis = getString(R.string.stats_desc_sad)
            }
            getString(R.string.emotion_angry) -> {
                titulo = getString(R.string.stats_title_angry)
                analisis = getString(R.string.stats_desc_angry)
            }
            else -> {
                titulo = getString(R.string.stats_analyzing_title)
                analisis = getString(R.string.stats_analyzing_desc)
            }
        }

        val iconRes = when (emocionPrincipal) {
            getString(R.string.emotion_very_happy) -> R.drawable.very_happy_icon
            getString(R.string.emotion_happy) -> R.drawable.happy_icon
            getString(R.string.emotion_neutral) -> R.drawable.neutral_icon
            getString(R.string.emotion_sad) -> R.drawable.sad_icon
            getString(R.string.emotion_angry) -> R.drawable.angry_icon
            else -> R.drawable.neutral_icon
        }

        _binding?.let {
            it.ivDominantEmotion.setImageResource(iconRes)
            it.tvEmotionTitle.text = titulo
            it.tvAnalysisText.text = analisis
        }
    }

    private fun mostrarFraseAleatoria(){
        val frases = listOf(
            getString(R.string.quote_1), getString(R.string.quote_2),
            getString(R.string.quote_3), getString(R.string.quote_4),
            getString(R.string.quote_5), getString(R.string.quote_6)
        )
        _binding?.tvQuote?.text = frases.random()
    }

    override fun onDestroyView() {
        // CAMBIO: Limpiamos los listeners de Firebase antes de destruir la vista
        statsListener?.let { statsRef?.removeEventListener(it) }
        diarioListener?.let { diarioRef?.removeEventListener(it) }

        super.onDestroyView()
        _binding = null
    }
}