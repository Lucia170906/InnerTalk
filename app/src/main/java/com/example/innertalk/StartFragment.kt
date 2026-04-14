package com.example.innertalk

import ActivitiesAdapter
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innertalk.databinding.FragmentStartBinding
import com.example.innertalk.model.ActivityModel
import com.example.innertalk.viewModel.ActivityViewModel
import android.graphics.Color
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.innertalk.model.EntryModel
import com.example.innertalk.adapter.EntriesAdapter

class StartFragment : Fragment() {
    // Variables para el ViewBinding (así no tengo que usar findViewById)
    private var _binding : FragmentStartBinding? = null
    private  val binding  get ()  =_binding!!

    // Mis "cerebros" y adaptadores de listas
    private lateinit var viewModel : ActivityViewModel
    private lateinit var adapter: ActivitiesAdapter // para las actividades
    private lateinit var notasAdapter: EntriesAdapter // Para las notas del diario, reciclamos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)

        // 1. RECYCLER DE NOTAS DEL DIARIO ---
        // Empiezo con una lista vacía para que no pete la app
        notasAdapter = EntriesAdapter(emptyList())

        // Configuro la lista de las notas
        binding.rvNotasCalendario.apply {
            adapter = notasAdapter
        }

        // 2. CONFIGURACIÓN DEL CALENDARIO
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            // Saco el día de HOY a las 00:00:00 exactas
            val hoy = java.util.Calendar.getInstance()
            hoy.set(java.util.Calendar.HOUR_OF_DAY, 0)
            hoy.set(java.util.Calendar.MINUTE, 0)
            hoy.set(java.util.Calendar.SECOND, 0)
            hoy.set(java.util.Calendar.MILLISECOND, 0)

            // Saco el día que el usuario ha pulsado también a las 00:00:00
            val pulsado = java.util.Calendar.getInstance()
            pulsado.set(year, month, dayOfMonth, 0, 0, 0)
            pulsado.set(java.util.Calendar.MILLISECOND, 0)

            // Formateo la fecha  para mandarla a Firebase con "-"
            val fechaFormateada = String.format("%02d-%02d-%d", dayOfMonth, month + 1, year)

            // Comparo las fechas limpias
            if (pulsado.before(hoy)) { // Si he pulsado un día anterior a hoy
                //  Muestro las actividades realizadas ese día
                binding.recyclerView.visibility = View.VISIBLE

                binding.tvSugeridasTitulo.text = "Actividades realizadas el $fechaFormateada"
                buscarSoloRealizadas(fechaFormateada)
                buscarNotaDiario(fechaFormateada)


            } else if(pulsado.after(hoy)){
                // He decicdo usar View GONE y cambio en la lista por tener un respaldo por si una falla

                binding.tvSugeridasTitulo.text= "No te preocupes por mañana, aún es pronto"
                binding.recyclerView.visibility = View.GONE // Escondemos la lista de actividades

                // Limpiamos el ViewModel para que no intente mostrar nada
                viewModel.actualizarListaMostrada(emptyList())
            }
            else {
                binding.recyclerView.visibility = View.VISIBLE
                // Quitamos las notas de días anteriores
                binding.tvTituloNotasDiario.visibility = View.GONE
                binding.rvNotasCalendario.visibility = View.GONE


                // Si es hoy , muestro todo el catálogo de sugerencias
                binding.tvSugeridasTitulo.text = "Actividades sugeridas"
                cargarActividadesDeHoy()
            }

        }

        // 3. RECYCLER DE ACTIVIDADES
        // Configuro qué pasa cuando toco los botones de cada actividad
        adapter = ActivitiesAdapter(
            onPlayClicked = { url ->
                //cogemos la url y entramos en youtube/ navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            },
            onCheckClicked = {id ->
                //marcamos la actividad como completada
                viewModel.activityCompletion(id)
            },
            onItemClicked = {activity ->
                mostrarModalActividad(activity)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StartFragment.adapter
            isNestedScrollingEnabled = false
        }

        viewModel.activities.observe(viewLifecycleOwner) { lista ->
            lista?.let {
                adapter.submitList(it)
            }
        }

        // Nada más abrir la app, cargo las actividades de hoy
        cargarActividadesDeHoy()
    }

    private fun mostrarModalActividad(activity : ActivityModel){
        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_activity_detail) //conectamos con el layout

        //2.Hacemos el fondo tranparente para consegui las esquinas redondeadas
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        //3. Buscamos las vistas dentro del dialogo
        val icon = dialog.findViewById<ImageView>(R.id.dialogIcon)
        val title = dialog.findViewById<TextView>(R.id.dialogTitle)
        val description = dialog.findViewById<TextView>(R.id.dialogDescription)
        val duration = dialog.findViewById<TextView>(R.id.dialogDuration)
        val btnPlay = dialog.findViewById<Button>(R.id.dialogBtnPlay)
        val checkBox = dialog.findViewById<CheckBox>(R.id.dialogCheckBox)

        //4. Rellenamos los datos con los de la actividad pulsada
        icon.setImageResource(activity.iconRes)
        title.text = activity.title
        description.text = activity.description
        duration.text = activity.duration
        checkBox.isChecked = activity.isCompleted

        //5.Configuramos el botón de play si este es pulsado desde el modal
        if(!activity.url.isNullOrEmpty()){
            btnPlay.visibility = View.VISIBLE
            btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.url))
                startActivity(intent)
                dialog.dismiss() //cerramos el modal al ir a youtube
            }
        }else{
            btnPlay.visibility = View.GONE
        }

        //6. Configuramos el check box
        checkBox.setOnClickListener {
            viewModel.activityCompletion(activity.id)
            dialog.dismiss() //cerramos al marcas como hecha
        }
        //7.Mostramos el modal en la pantalla
        dialog.show()
    }

    //  Muestra solo lo que hice (para el pasado)
    private fun buscarSoloRealizadas(fecha: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("usuarios").child(uid).child("actividades_diarias").child(fecha)

        // Toasts  para saber qué está haciendo por debajo
        android.widget.Toast.makeText(context, "Buscando historial de: $fecha", android.widget.Toast.LENGTH_SHORT).show()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // 1. Me bajo las IDs que guardé en Firebase y las limpio de espacios por si acaso
                val idsHechasFirebase = snapshot.children.mapNotNull {
                    it.key?.trim()?.lowercase() // Añadimos el lowercase por si acaso en el duuro usamos letras en los id
                }

                android.widget.Toast.makeText(context, "Se encontró: ${idsHechasFirebase.size} hechas", android.widget.Toast.LENGTH_SHORT).show()

                // 2. Le pido al ViewModel el catálogo entero limpio
                val catalogoCompleto = viewModel.getCatalogoLimpio()

                // 3. Me quedo solo con las actividades cuya ID esté en mi lista de Firebase
                val soloHechas = catalogoCompleto.filter { actividad ->
                    val idLocal = actividad.id.trim().lowercase()
                    idsHechasFirebase.contains(idLocal)
                }.map {
                    // Les fuerzo el check para que salgan como completadas visualmente
                    it.copy(isCompleted = true)
                }

                // 4. Se las paso al ViewModel para que las proteja y dispare el Observer
                viewModel.actualizarListaMostrada(soloHechas)

                // Mensajes de error por si algo falla en la comparación de las IDs
                if (idsHechasFirebase.isNotEmpty() && soloHechas.isEmpty()) {
                    android.widget.Toast.makeText(context, "Error: Las IDs no coinciden", android.widget.Toast.LENGTH_LONG).show()
                } else if (soloHechas.isEmpty()) {
                    android.widget.Toast.makeText(context, "Día sin actividades", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // FUNCIÓN PARA LAS NOTAS: Busca si escribó en el diario ese día
    private fun buscarNotaDiario(fecha: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("usuarios").child(uid).child("diario")

        try {
            // Como Firebase guarda las notas por "milisegundos", tengo que crear un rango:
            // Saco los milisegundos del principio del día (00:00:00) y del final (23:59:59)
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
            val date = sdf.parse(fecha.trim()) ?: return
            val calendar = java.util.Calendar.getInstance()

            calendar.time = date
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val inicioDia = calendar.timeInMillis

            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val finDia = calendar.timeInMillis

            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val listaNotasDelDia = mutableListOf<EntryModel>()

                        // Recorro todas mis notas...
                        for (data in snapshot.children) {
                            val entry = data.getValue(EntryModel::class.java)
                            if (entry != null) {
                                // ... y si alguna cae dentro del rango de horas de ese día, la saca
                                if (entry.fecha in inicioDia..finDia) { // el campo se llama fecha en Firebase
                                    listaNotasDelDia.add(entry)
                                }
                            }
                        }


                            if (listaNotasDelDia.isNotEmpty()) {
                                // Muestro la lista si hay notas
                                binding.tvTituloNotasDiario.visibility = View.VISIBLE
                                binding.rvNotasCalendario.visibility = View.VISIBLE
                                notasAdapter.updateList(listaNotasDelDia)
                            } else {
                                // Escondo todo si está vacío
                                binding.tvTituloNotasDiario.visibility = View.GONE
                                binding.rvNotasCalendario.visibility = View.GONE
                            }

                    } else {
                        // Si directamente no hay nada en Firebase
                        activity?.runOnUiThread {
                            binding.tvTituloNotasDiario.visibility = View.GONE
                            binding.rvNotasCalendario.visibility = View.GONE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {}
    }

    // FUNCIÓN PARA HOY: Muestra las activides guardando las que ya se han hecho
    private fun cargarActividadesDeHoy() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val fechaHoy = sdf.format(java.util.Date())

        val dbRef = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("usuarios").child(uid).child("actividades_diarias").child(fechaHoy)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. Obtenemos la lista de IDs que ya marqué hoy
                val idsHechasHoy = snapshot.children.mapNotNull { it.key }

                // 2. Le pido al ViewModel el catálogo entero limpio
                val catalogoCompleto = viewModel.getCatalogoLimpio()

                // 3.  Me devuelve la lista entera, pero con los checks bien puestos
                val listaListaParaMostrar = viewModel.sincronizarChecksDeHoy(catalogoCompleto, idsHechasHoy)

                // 4. Se la paso al ViewModel para que la proteja y la envíe al Adapter a través del Observer
                viewModel.actualizarListaMostrada(listaListaParaMostrar)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}