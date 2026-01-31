package com.example.innertalk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.innertalk.R
import com.example.innertalk.model.Message

class ChatAdapter(private val messagesList: MutableList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //etiquetas para pintar cada burbuja
    private val TYPE_USER = 1
    private val TYPE_AI = 2


    //funcion para determinar si el mensaje es del usuario o de la IA y pintarlo correctamente
    override fun getItemViewType(position: Int): Int {
        // si es user es typer user, sino type ia
        return if (messagesList[position].isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // si es el mensaje es type user inflará el layout de dicho tipo
        return if (viewType == TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_user, parent, false)
            UserViewHolder(view)
        } else {
            // del contrario inflrá el alyout de mensaje de la ia
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_ai, parent, false)
            AIViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //cogemos el texto de dentro del mensaje y lo pintamos dentro del text view correspondiente (ia o user)
        val message = messagesList[position]
        if (holder is UserViewHolder) {
            holder.bindingText.text = message.text
        } else if (holder is AIViewHolder) {
            holder.bindingText.text = message.text
        }
    }

    override fun getItemCount() = messagesList.size
//funcion para añadir los mensajes
    fun addMessage(message: Message) {
        messagesList.add(message)
        notifyItemInserted(messagesList.size - 1)
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingText: TextView = view.findViewById(R.id.tvMessageUser)
    }

    class AIViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingText: TextView = view.findViewById(R.id.tvMessageAi)
    }

}