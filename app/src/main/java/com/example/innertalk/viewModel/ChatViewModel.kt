package com.example.innertalk.viewModel

import androidx.lifecycle.ViewModel
import com.example.innertalk.model.Message

class ChatViewModel : ViewModel(){

    //Lista para los mensajes
    val messageList = mutableListOf<Message>()

    //Gardanos los datos del perfil para no pedirselos trodo el rato a firebase cada vez que se entre al chat
    var nombreUsuario: String = ""
    var interesesUsuario: List<String> = listOf()
    var datosCargados: Boolean = false

    fun addMessag(message : Message){
        messageList.add(message)
    }
}