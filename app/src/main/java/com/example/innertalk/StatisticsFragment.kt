package com.example.innertalk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
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

        // 3. Aquí ya puedes usar 'binding' para configurar tu gráfico
        //configurarGrafico()
        //Habra que borrar est llamada y sustituarla por leerEstadisticas
        leerEstadisticasFirebase()


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

                    //vamos a hacer un mapa con clave valo r
                }else{
                    Toast.makeText(requireContext(), "No hay datos en Firebase aún", Toast.LENGTH_SHORT).show()
                    // Dibujamos uno vacío para que al menos se vea el título
                    actualizarGrafico(0, 0, 0, 0, 0)
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

    override fun onDestroyView() {
        super.onDestroyView()
        // 4. Importantísimo: limpiar el binding para evitar fugas de memoria
        _binding = null
    }
}