package com.example.innertalk.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innertalk.R
import com.example.innertalk.adapter.ChatAdapter
import com.example.innertalk.databinding.FragmentChatBinding
import com.example.innertalk.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    // 1. LA APIKEY
    private val apiKey by lazy { leerApiKey() }

    // 2. EL MODELO
    private val modeloGroq = "llama-3.1-8b-instant"

    //3. Datos del usuario
    private var nombreUsuario: String = "Usuario"
    private var interesesUsuario: List<String> = listOf()
    //private var generoUsuario: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Cargamos los datos del usuario:
        cargarDatosPerfilFirestore()

        chatAdapter = ChatAdapter(messageList)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                binding.etMessage.setText("")
                enviarMensaje(userText)
            }
        }
    }
    private fun cargarDatosPerfilFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nombreUsuario = document.getString("name") ?: "Usuario"
                    //generoUsuario = document.getString("gender") ?: "" // de momento no voy a usar el género, pero asi sería la lína para la ampliación en el futuro
                    // Extraemos la lista de intereses
                    interesesUsuario = document.get("intereses") as? List<String> ?: listOf()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al leer perfil: ${e.message}")
            }
    }

    private fun enviarMensaje(texto: String) {
        val userMsg = Message(texto, isUser = true)
        chatAdapter.addMessage(userMsg)
        binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)

        // Hacemos la llamada a la red en un hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Llamamos a nuestra propia función de conexión a Groq
                val respuestaTexto = llamarApiGroq()

                // Volvemos al hilo principal para actualizar la pantalla
                withContext(Dispatchers.Main) {
                    val aiMsg = Message(respuestaTexto, isUser = false)
                    chatAdapter.addMessage(aiMsg)
                   //analizarSentimientosYRecomendar(respuestaTexto)
                    binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GroqError", "Error técnico detallado: ", e)
                    chatAdapter.addMessage(Message("Error de conexión: ${e.message}", false))
                }
            }
        }
    }

    // 3.CONEXIÓN NATIVA A LA API DE GROQ
    private fun llamarApiGroq(): String {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Configuramos la petición
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Creamos el JSON con los datos que espera Groq
            val jsonBody = JSONObject()
            jsonBody.put("model", modeloGroq)
            jsonBody.put("temperature", 0.6) // 0.6 para equilibrar empatía y coherencia

            val messagesArray = JSONArray()

            // A. EL SYSTEM PROMPT (La personalidad del psicólogo)
            val systemMsg = JSONObject()
            systemMsg.put("role", "system")
            systemMsg.put("content", "Eres un asistente virtual especializado en " +
                    "apoyo emocional y bienestar psicológico. Tu tono debe ser cálido," +
                    " empático, validante y libre de juicios. " +
                    "Usa respuestas conversacionales. " +
                    "No diagnostiques condiciones médicas, sugiere buscar ayuda profesional si detectas peligro grave." +
                    "El usuario ha indicado que sus principales preocupaciones son: ${interesesUsuario.joinToString {(", ")  }}." +
                     "Por favor, adapta tus consejos a estas condiciones." +
                    "Estás hablando con $nombreUsuario")
            messagesArray.put(systemMsg)

            // B. EL HISTORIAL DE CHAT (Para que tenga memoria)
            for (msg in messageList) {
                val role = if (msg.isUser) "user" else "assistant"
                val msgJson = JSONObject()
                msgJson.put("role", role)
                msgJson.put("content", msg.text)
                messagesArray.put(msgJson)
            }

            jsonBody.put("messages", messagesArray)

            // Enviamos los datos
            val outputStream = OutputStreamWriter(connection.outputStream)
            outputStream.write(jsonBody.toString())
            outputStream.flush()
            outputStream.close()

            // Leemos la respuesta
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.readText()
                reader.close()

                // Extraemos el texto exacto que dijo la IA del JSON de respuesta
                val jsonResponse = JSONObject(responseString)
                val choices = jsonResponse.getJSONArray("choices")
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                return message.getString("content")
            } else {
                // Si falla (por ejemplo, cuota excedida), leemos el error
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream))
                val errorString = errorReader.readText()
                errorReader.close()
                throw Exception("Código $responseCode - Detalle: $errorString")
            }
        } finally {
            connection.disconnect()
        }
    }

    //FUNCIÓN PARA LEER EL TXT CON LA APIKEY
    private fun leerApiKey():String{
        return try{
            //abrimos el archivo que esta en assets
            val inputStream = requireContext().assets.open("config.txt")
            val size = inputStream.available()
            val buffer = ByteArray(size)

            inputStream.read(buffer)
            inputStream.close()

            //Convertimos los bytes del Array a un String y quitamos posibles espacios
            String(buffer).trim()
        }catch (e : Exception){
            Log.e("ApiKeyError", "No se puede leer la API Key: ${e.message}")
            "" //Devolvemos vacío si falla
        }
    }

//    private fun analizarSentimientosYRecomendar (respuestaIA : String){
//        val respuestaMinusculas = respuestaIA.lowercase()
//
//        //1.Definimos los trigegrs (disparadores)
//         val esTriste = respuestaMinusculas.contains("triste")|| respuestaMinusculas.contains("ánimo") || respuestaMinusculas.contains("llorar") || respuestaMinusculas.contains("frustración")
//         val esAnsioso = respuestaMinusculas.contains("respira") || respuestaMinusculas.contains("calma") || respuestaMinusculas.contains("ansiedad")
//
//        //2.Lógica de recomendación visual
//        when{
//            esTriste->{
//                mostrarSugerencia("Parece que necesitas un abrazo virtual. ¿Qué tal si escribes en tu Diario de Gratitud?")
//            }
//            esAnsioso -> {
//                mostrarSugerencia("He notado algo de inquietud. Te recomiendo 5 minutos de Meditación ahora mismo.")
//            }
//        }
//    }

    private fun mostrarSugerencia(mensaje: String) {
        // Aquí puedes usar un Toast, un SnackBar o un cuadro de diálogo bonito
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("InnerTalk te cuida 🌿")
            .setMessage(mensaje)
            .setPositiveButton("Ir a Actividades") { _, _ ->

                findNavController().navigate(R.id.action_chatFragment_to_startFragment)
            }
            .setNegativeButton("Luego", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}