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
import androidx.fragment.app.activityViewModels
import com.example.innertalk.viewModel.ChatViewModel


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    //Usamos el activity viewmodel para que este vinculadoa la activity
    //y asi si el fragment muere la conversacion se mantiene
    private  val viewModel : ChatViewModel by activityViewModels()

    // 1. LA APIKEY
    private val apiKey by lazy { leerApiKey() }

    // 2. EL MODELO
    private val modeloGroq = "llama-3.1-8b-instant"

    //3. Datos del usuario
    private var nombreUsuario: String = ""
    private var interesesUsuario: List<String> = listOf()

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

        // Inicializamos el nombre por defecto desde strings
        nombreUsuario = getString(R.string.chat_default_username)

        if(!viewModel.datosCargados){
            //Cargamos los datos del usuario solo si no se han cargado antes
            cargarDatosPerfilFirestore()
        }
        //Usamos la lista del viewModel para el adapter
        chatAdapter = ChatAdapter(viewModel.messageList)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter

            //Si ya habia mensajes hacemos scroll hasta el final
            if(chatAdapter.itemCount >0){
                scrollToPosition(chatAdapter.itemCount -1 )
            }
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
                    viewModel.nombreUsuario = document.getString("name") ?: getString(R.string.chat_default_username)
                    viewModel.interesesUsuario = document.get("intereses") as? List<String> ?: listOf()
                    viewModel.datosCargados = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", getString(R.string.chat_log_firestore_error, e.message))
            }
    }

    private fun enviarMensaje(texto: String) {
        val userMsg = Message(texto, isUser = true)

        //Guardams en el viewModel y notificamos al adapter
        viewModel.addMessag(userMsg)
        chatAdapter.notifyItemInserted(viewModel.messageList.size -1)
        binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)

        // Hacemos la llamada a la red en un hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Llamamos a nuestra propia función de conexión a Groq
                val respuestaTexto = llamarApiGroq()

                // Volvemos al hilo principal para actualizar la pantalla
                withContext(Dispatchers.Main) {
                    val aiMsg = Message(respuestaTexto, isUser = false)

                    //Guardamos en el viewModel
                    viewModel.addMessag(aiMsg)
                    chatAdapter.notifyItemInserted(viewModel.messageList.size -1)
                    binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GroqError", "Error técnico: ", e)
                    chatAdapter.addMessage(Message(getString(R.string.chat_error_connection, e.message), false))
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
            jsonBody.put("temperature", 0.6)

            val messagesArray = JSONArray()

            // A. EL SYSTEM PROMPT (Usando el recurso de strings con formato)
            val interesesStr = interesesUsuario.joinToString(", ")
            val systemMsg = JSONObject()
            systemMsg.put("role", "system")
            systemMsg.put("content", getString(R.string.chat_system_prompt, interesesStr, nombreUsuario))
            messagesArray.put(systemMsg)

            // B. EL HISTORIAL DE CHAT (Para que tenga memoria)
            for (msg in viewModel.messageList) {
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

                val jsonResponse = JSONObject(responseString)
                val choices = jsonResponse.getJSONArray("choices")
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                return message.getString("content")
            } else {
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
            val inputStream = requireContext().assets.open("config.txt")
            val size = inputStream.available()
            val buffer = ByteArray(size)

            inputStream.read(buffer)
            inputStream.close()

            String(buffer).trim()
        }catch (e : Exception){
            Log.e("ApiKeyError", getString(R.string.chat_log_api_error, e.message))
            ""
        }
    }

    private fun mostrarSugerencia(mensaje: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.chat_dialog_care_title))
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.chat_dialog_btn_activities)) { _, _ ->
                findNavController().navigate(R.id.action_chatFragment_to_startFragment)
            }
            .setNegativeButton(getString(R.string.chat_dialog_btn_later), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}