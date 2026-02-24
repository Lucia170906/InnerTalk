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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream

class NewEntryFragment : Fragment() {





    private var _binding:FragmentNewEntryBinding? = null
    private val binding get() = _binding!!

    // Herramientas de Firebase y variables de estado
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var imageUri: Uri? = null
    private var emocionSeleccionada: Int = 0


    // 1. EL LANZADOR DE GALERÍA
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivPreview.setImageURI(uri) // Asegúrate de tener este ImageView en tu XML
            binding.cardPreview.visibility = View.VISIBLE
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEmotions()

        binding.btnAdd.setOnClickListener {
            pickMedia.launch("image/*")

        }

        // Botón Guardar
        binding.btnSave.setOnClickListener {
            ejecutarGuardado()
        }
    }

    private fun setupEmotions() {
        val listaCaritas =
            listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5)

        listaCaritas.forEachIndexed { index, imageButton ->
            imageButton.setOnClickListener {
                val numeroEmocion = index + 1

                // Si pulsa la que ya estaba seleccionada, la desmarcamos (vuelve a negro)
                if (emocionSeleccionada == numeroEmocion) {
                    emocionSeleccionada = 0
                    actualizarDisenoCaritas(listaCaritas, 0)
                } else {
                    // Si pulsa una nueva, marcamos esa y limpiamos las demás
                    emocionSeleccionada = numeroEmocion
                    actualizarDisenoCaritas(listaCaritas, emocionSeleccionada)
                }
            }
        }
    }

        private fun actualizarDisenoCaritas(caritas: List<android.widget.ImageButton>, seleccionada: Int) {
            // Definimos los colores para cada carita (Índice 0 a 4)
            val colores = listOf(
                "#4CAF50", // 1. Muy Feliz -> Verde
                "#FFEB3B", // 2. Feliz -> Amarillo
                "#9E9E9E", // 3. Neutral -> Gris
                "#FF9800", // 4. Triste -> Naranja
                "#F44336"  // 5. Cabreado -> Rojo
            )

            caritas.forEachIndexed { index, button ->
                val numeroBoton = index + 1
                if (numeroBoton == seleccionada) {
                    // Aplicamos el color específico de la lista de colores
                    button.setColorFilter(android.graphics.Color.parseColor(colores[index]))
                    button.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start()
                } else {
                    // Si no está seleccionada, vuelve a negro o gris oscuro
                    button.setColorFilter(android.graphics.Color.BLACK)
                    button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
            }
        }


    // 3. PROCESO DE GUARDADO
    private fun ejecutarGuardado() {
        val texto = binding.editTextNote.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (texto.isEmpty() || emocionSeleccionada == 0) {
            Toast.makeText(requireContext(), "Escribe algo y elige un emoji", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertimos la imagen a Base64 (Texto)
        val fotoBase64 = imageUri?.let { comprimirImagen(it) }

        val entrada = hashMapOf(
            "texto" to texto,
            "emocion" to emocionSeleccionada,
            "fotoBase64" to fotoBase64,
            "fecha" to FieldValue.serverTimestamp() // para coger la fecha actual
        )

        // Guardar en la subcolección "diario" del usuario
        db.collection("users").document(uid).collection("diario")
            .add(entrada)
            .addOnSuccessListener {
                actualizarEstadisticas(uid)
                Toast.makeText(requireContext(), "¡Entrada guardada!", Toast.LENGTH_SHORT).show()
                limpiarPantalla()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    // 4. CONVERTIR IMAGEN A TEXTO (Para evitar el Storage de pago)
    private fun comprimirImagen(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // La hacemos pequeña para no superar el límite de 1MB de Firestore
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT) // Para pasar la foto a Array de Bytes
    }

    // 5. ACTUALIZAR CONTADORES PARA GRÁFICOS
    private fun actualizarEstadisticas(uid: String) {
        val statsRef = db.collection("stats").document(uid)
        statsRef.update("emocion_$emocionSeleccionada", FieldValue.increment(1))
            .addOnFailureListener {
                statsRef.set(mapOf("emocion_$emocionSeleccionada" to 1), SetOptions.merge())
            }
    }

    private fun limpiarPantalla() {
        binding.editTextNote.setText("")
        binding.ivPreview.visibility = View.GONE
        imageUri = null
        emocionSeleccionada = 0
        actualizarDisenoCaritas(listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5), 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}