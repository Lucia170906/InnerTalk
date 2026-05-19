package com.example.innertalk

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.innertalk.databinding.FragmentNewEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class EditEntryFragment : Fragment() {
    //Usamos el binding de new entry
    private var _binding: FragmentNewEntryBinding? = null
    private val binding get() = _binding!!

    //Variables de firebase
    private val database =
        FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val auth = FirebaseAuth.getInstance()

    //Variables para mostrar la informacion de las notas
    private var idNotaActual: String = ""
    private var textoNota: String = ""

    private var emocionSeleccionada: Int = 0

    //Variable para la imagen
    private var nuevaImagenUri: Uri? = null

    //Guardamos la foto vieja por si el usuario no la cambia
    private var fotoBase64Actual: String? = null


    //Selector de imagen
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            nuevaImagenUri = uri // Guardamos la nueva Uri
            binding.cardPreview.visibility = View.VISIBLE

            // Mostramos preview
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivPreview.setImageBitmap(bitmap)
            } catch (e: Exception) {
                binding.ivPreview.setImageURI(uri)
            }
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

        // Sustituimos texto del botón por string
        binding.btnSave.text = getString(R.string.edit_entry_btn_update)

        //Configuramos los clicks para las emociones
        setupEmotions()

        // Recibimos los datos de la nota
        arguments?.let { bundle ->
            idNotaActual = bundle.getString("id_nota", "")
            textoNota = bundle.getString("texto_nota", "")
            emocionSeleccionada = bundle.getInt("emocion_nota", 0)
            fotoBase64Actual = bundle.getString("foto_nota")
            // Escribimos el texto que ya había en la base de datos
            binding.editTextNote.setText(textoNota)

            // Disparamos animación para marcar la carita correcta al entrar
            val listaCaritas =
                listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5)
            actualizarDisenoCaritas(listaCaritas, emocionSeleccionada)

            //Cargamo la imagen
            if (!fotoBase64Actual.isNullOrEmpty()) {
                cargarFotoActualEnPreview(fotoBase64Actual!!)
            }
        }

        binding.btnAdd.setOnClickListener {
            pickMedia.launch("image/*")
        }
        binding.ivPreview.setOnLongClickListener {
            if (fotoBase64Actual != null || nuevaImagenUri != null) {
                mostrarDialogoEliminarFoto()
            }
            true
        }

        //Preparamos el boton para guaradar
        binding.btnSave.setOnClickListener {
            actualizarEnFirebase()
        }


    }

    private fun setupEmotions() {
        val listaCaritas =
            listOf(binding.face1, binding.face2, binding.face3, binding.face4, binding.face5)
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

    private fun cargarFotoActualEnPreview(base64String: String) {
        try {
            //Convertimos el texto en Base 64 de Firebadr  vuelta a bytes
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            //ConVERTIMOS BTES A BITMAP
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            //Mostramos el contenedor y la foto
            binding.cardPreview.visibility = View.VISIBLE
            binding.ivPreview.setImageBitmap(decodedImage)
        } catch (e: Exception) {
            Log.e("EditEntry", getString(R.string.edit_entry_log_photo_error, e.message))
            binding.cardPreview.visibility = View.GONE
        }
    }

    private fun actualizarDisenoCaritas(
        caritas: List<android.widget.ImageButton>,
        seleccionada: Int
    ) {
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

    fun actualizarEnFirebase() {
        val nuevoTexto = binding.editTextNote.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        var fotoFinalBase64: String? = when{
            nuevaImagenUri != null -> comprimirImagen(nuevaImagenUri!!)//foto nueva
            fotoBase64Actual != null -> fotoBase64Actual//mantener foto vieja
            else -> null//no hay foto
        }

//        //el usuario ha elegido una foto nueva ==
//        if (nuevaImagenUri != null) {
//            //si hay foto nueva la comprimimos
//            fotoFinalBase64 = comprimirImagen(nuevaImagenUri!!)
//
//            if (fotoFinalBase64 == null) {
//                Toast.makeText(
//                    requireContext(),
//                    getString(R.string.edit_entry_error_image),
//                    Toast.LENGTH_SHORT
//                ).show()
//                return
//            }
//        }

        if (idNotaActual.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.edit_entry_error_id),
                Toast.LENGTH_LONG
            ).show()
            return // Cortamos la ejecución aquí, ¡no guardamos nada!
        }

        // Solo actualizamos texto y emoción. La fecha y la foto se mantienen como estaban.
        val actualizaciones = mapOf(
            "texto" to nuevoTexto,
            "emocion" to emocionSeleccionada,
            "fotoBase64" to fotoFinalBase64 // Esta será la vieja o la nueva procesada

        )

        //Accedemos a la ruta exacta de esta nota y usamos updateChildren
        database.child("usuarios").child(uid).child("diario").child(idNotaActual)
            .updateChildren(actualizaciones)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.edit_entry_success),
                    Toast.LENGTH_SHORT
                ).show()

                // 5. Volvemos atrás automáticamente al terminar
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.edit_entry_db_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    //Funiando para la compresion
    private fun comprimirImagen(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            // Usamos tus mismos valores de compresión
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("EditEntry", "Error comprimiendo: ${e.message}")
            null
        }
    }

    //Funciones para eliminar la imagen

    private fun mostrarDialogoEliminarFoto() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_entry_delete_photo_btn))
            .setMessage(getString(R.string.edit_entry_delete_photo_confirm))
            .setPositiveButton(getString(R.string.edit_entry_delete_photo_btn)) { _, _ ->
                eliminarFotoDeLaVista()
            }
            .setNegativeButton(getString(R.string.config_dialog_cancel), null)
            .show()
    }

    private fun eliminarFotoDeLaVista(){
        //Limpiamos la referencia
        fotoBase64Actual = null
        nuevaImagenUri = null
        //Ovultamos la UI
        // 2. Ocultamos la UI
        binding.ivPreview.setImageDrawable(null)
        binding.cardPreview.visibility = View.GONE

        Toast.makeText(requireContext(), "Foto marcada para eliminar", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}