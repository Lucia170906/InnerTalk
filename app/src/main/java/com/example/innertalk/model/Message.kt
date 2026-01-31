package com.example.innertalk.model

data class Message ( var text: String,
                     var isUser: Boolean, // True si lo envías tú, False si lo envía IA
                     val timestamp: Long = System.currentTimeMillis()){

}