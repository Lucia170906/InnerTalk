package com.example.innertalk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.innertalk.databinding.FragmentStatisticsBinding
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement

class StatisticsFragment : Fragment() {

    // 1. El _binding es el que realmente guarda la referencia y es nuleable
    private var _binding: FragmentStatisticsBinding? = null

    // 2. Esta propiedad 'binding' solo es válida entre onCreateView y onDestroyView
    // El get() !! asegura que no tengas que poner '?' en todo tu código
    private val binding get() = _binding!!

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

        // 3. Aquí ya puedes usar 'binding' para configurar tu gráfico
        configurarGrafico()
    }

    private fun configurarGrafico() {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .title("Análisis de Emociones")
            .backgroundColor("#F5F9FF")
            .dataLabelsEnabled(true)
            //.colorsTheme() para añadir colores deseados
            .series(arrayOf(
                AASeriesElement()
                    .name("Frecuencia")
                    .data(arrayOf(
                        arrayOf("Felicidad", 10),
                        arrayOf("Calma", 7),
                        arrayOf("Apático", 3),
                        arrayOf("Triste", 2),
                        arrayOf("Enfado",4)
                    ))
            ))

        // Acceso directo gracias al binding
        binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 4. Importantísimo: limpiar el binding para evitar fugas de memoria
        _binding = null
    }
}