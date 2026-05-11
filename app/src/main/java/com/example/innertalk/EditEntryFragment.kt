package com.example.innertalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.innertalk.databinding.FragmentNewEntryBinding // ¡Usamos el diseño que ya existe!
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditEntryFragment : Fragment() {
    //Usamos el binding de new entry
    private var _binding: FragmentNewEntryBinding? = null
    private val binding get() = _binding!!
    //Variables de firebase
    private val database = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val auth = FirebaseAuth.getInstance()

    //Variables para mostrar la informacion de las notas
    private  var  idNotaActual : String = ""
    private  var  textoNota : String = ""

    private  var emocionSeleccionada: Int = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.text = "Actualizar entrada"

        //Configuramos los clicks para las emociones
        setupEmotions()

        // Recibimos los datos de la nota
        arguments?.let { bundle ->
            idNotaActual = bundle.getString("id_nota", "")
            textoNota = bundle.getString("texto_nota", "")
            emocionSeleccionada = bundle.getInt("emocion_nota", 0)

            // Escribimos el texto que ya había en la base de datos
            binding.editTextNote.setText(textoNota)

            // Disparamos tu animación para marcar la carita correcta al entrar
            val listaCaritas = listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5)
            actualizarDisenoCaritas(listaCaritas, emocionSeleccionada)
        }

        //Preparamos el boton para guaradar
        binding.btnSave.setOnClickListener {
            actualizarEnFirebase()
        }


    }
    private fun setupEmotions() {
        val listaCaritas = listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5)
        listaCaritas.forEachIndexed { index, imageButton ->
            imageButton.setOnClickListener {
                val numeroEmocion = index + 1
                if (emocionSeleccionada == numeroEmocion) {
                    emocionSeleccionada = 0
                    actualizarDisenoCaritas(listaCaritas, 0)
                } else {
                    emocionSeleccionada = numeroEmocion
                    actualizarDisenoCaritas(listaCaritas, emocionSeleccionada)
                }
            }
        }
    }
    private fun actualizarDisenoCaritas(caritas: List<android.widget.ImageButton>, seleccionada: Int) {
        val colores = listOf("#4CAF50", "#FFEB3B", "#9E9E9E", "#FF9800", "#F44336")
        caritas.forEachIndexed { index, button ->
            val numeroBoton = index + 1
            if (numeroBoton == seleccionada) {
                button.setColorFilter(android.graphics.Color.parseColor(colores[index]))
                button.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start()
            } else {
                button.setColorFilter(android.graphics.Color.BLACK)
                button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
            }
        }
    }

    fun actualizarEnFirebase (){
        val nuevoTexto = binding.editTextNote.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (idNotaActual.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No se encontró el ID de la nota", Toast.LENGTH_LONG).show()
            return // Cortamos la ejecución aquí, ¡no guardamos nada!
        }

        // Solo actualizamos texto y emoción. La fecha y la foto se mantienen como estaban.
        val actualizaciones = mapOf(
            "texto" to nuevoTexto,
            "emocion" to emocionSeleccionada
        )

        //Accedemos a la ruta exacta de esta nota y usamos updateChildren
        database.child("usuarios").child(uid).child("diario").child(idNotaActual)
            .updateChildren(actualizaciones)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Nota actualizada!", Toast.LENGTH_SHORT).show()

                // 5. Volvemos atrás automáticamente al terminar
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}