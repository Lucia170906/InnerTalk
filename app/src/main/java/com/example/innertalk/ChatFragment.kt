package com.example.innertalk.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innertalk.adapter.ChatAdapter
import com.example.innertalk.databinding.FragmentChatBinding
import com.example.innertalk.model.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.type.generationConfig

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    // CONFIGURACIÓN DE GEMINI
    private val generativeModel by lazy {
        GenerativeModel(
            // Cambiado a la versión preview específica
            modelName = "gemini-1.5-pro-preview-0409",
            apiKey = "AIzaSyDeBbLMIBnLrkMe8MCwqTfAy6zvWIMcXsA",
            generationConfig = generationConfig {
                temperature = 0.8f
            }
        )
    }
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

        // 1. Configurar el RecyclerView usando Binding
        chatAdapter = ChatAdapter(messageList)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        // 2. Lógica del botón enviar usando Binding
        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                binding.etMessage.setText("")
                enviarMensaje(userText)
            }
        }
    }

    private fun enviarMensaje(texto: String) {
        val userMsg = Message(texto, isUser = true)
        chatAdapter.addMessage(userMsg)

        // Scroll al final
        binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)

        lifecycleScope.launch {
            try {
                // Llamada a la API
                val response = generativeModel.generateContent(texto)
                val respuestaTexto = response.text ?: "La IA no pudo generar una respuesta."

                val aiMsg = Message(respuestaTexto, isUser = false)
                chatAdapter.addMessage(aiMsg)

                binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)

            } catch (e: Exception) {
                Log.e("GeminiError", "Error: ${e.message}")
                chatAdapter.addMessage(Message("Error de API: ${e.message}", false))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Importante para evitar fugas de memoria
    }
}