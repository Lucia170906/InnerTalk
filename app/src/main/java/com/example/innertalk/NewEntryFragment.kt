package com.example.innertalk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.innertalk.databinding.FragmentNewEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.io.ByteArrayOutputStream

class NewEntryFragment : Fragment() {

    private var _binding: FragmentNewEntryBinding? = null
    private val binding get() = _binding!!

    // Cambiamos Firestore por Realtime Database
    private val database = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val auth = FirebaseAuth.getInstance()
    private var imageUri: Uri? = null
    private var emocionSeleccionada: Int = 0

    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivPreview.setImageURI(uri)
            binding.cardPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEmotions()

        binding.btnAdd.setOnClickListener {
            pickMedia.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            ejecutarGuardado()
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

    private fun ejecutarGuardado() {
        val texto = binding.editTextNote.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (texto.isEmpty() || emocionSeleccionada == 0) {
            Toast.makeText(requireContext(), "Escribe algo y elige un emoji", Toast.LENGTH_SHORT).show()
            return
        }

        val fotoBase64 = imageUri?.let { comprimirImagen(it) }

        // 1. Crear un ID único para la entrada
        val entradaId = database.child("usuarios").child(uid).child("diario").push().key ?: return

        // 2. Crear el mapa de datos
        val entrada = hashMapOf(
            "texto" to texto,
            "emocion" to emocionSeleccionada,
            "fotoBase64" to fotoBase64,
            "fecha" to ServerValue.TIMESTAMP // Marca de tiempo de Realtime DB
        )

        // 3. Guardar en Realtime Database
        database.child("usuarios").child(uid).child("diario").child(entradaId).setValue(entrada)
            .addOnSuccessListener {
                actualizarEstadisticas(uid)
                Toast.makeText(requireContext(), "¡Entrada guardada!", Toast.LENGTH_SHORT).show()
                limpiarPantalla()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    //Esta funcion comprime la imagen para poder meter la imagen en la base de datos sin que sin acaben cobrando
    private fun comprimirImagen(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun actualizarEstadisticas(uid: String) {
        // En Realtime DB, el incremento se hace accediendo al valor y sumando
        val statsRef = database.child("usuarios").child(uid).child("estadisticas").child("emocion_$emocionSeleccionada")

        statsRef.get().addOnSuccessListener { snapshot ->
            val actual = snapshot.getValue(Int::class.java) ?: 0
            statsRef.setValue(actual + 1)
        }
    }

    private fun limpiarPantalla() {
        binding.editTextNote.setText("")
        binding.cardPreview.visibility = View.GONE
        imageUri = null
        emocionSeleccionada = 0
        actualizarDisenoCaritas(listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5), 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}