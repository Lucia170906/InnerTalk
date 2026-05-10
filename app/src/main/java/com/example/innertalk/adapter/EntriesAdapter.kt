package com.example.innertalk.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.innertalk.R
import com.example.innertalk.databinding.ItemsEntriesCardBinding
import com.example.innertalk.model.EntryModel

// 1. CORRECCIÓN: var en lugar de val, y List<EntryModel>
class EntriesAdapter(private var entries: List<EntryModel>,
    private val onItemClick : (EntryModel) -> Unit) :
    RecyclerView.Adapter<EntriesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemsEntriesCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemsEntriesCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // 2. CORRECCIÓN: Esta función es VITAL para que el calendario pueda cambiar la lista
    fun updateList(newEntries: List<EntryModel>) {
        entries = newEntries
        notifyDataSetChanged() // Le avisa al RecyclerView que hay datos nuevos y debe repintarse
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]

        // 3. CORRECCIÓN: Usamos entry.timestamp (que es un Long de verdad)
        if (entry.fecha > 0L) {
            // Formateamos el milisegundo a algo que una persona entienda (dd/MM/yyyy)
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            holder.binding.tvHistoryDate.text = sdf.format(java.util.Date(entry.fecha))
        }
        holder.binding.apply {
            // Ponemos el texto de la nota del diario
            tvHistoryNote.text = entry.texto

            // Nuestra lista de colores para que el historial sea visual (Verde -> Rojo)
            val colores = listOf("#4CAF50", "#FFEB3B", "#9E9E9E", "#FF9800", "#F44336")

            // Pillamos el índice (emoción 1-5) y nos aseguramos de no salirnos del array con coerceIn
            val indiceColor = (entry.emocion - 1).coerceIn(0, 4)
            val colorActual = android.graphics.Color.parseColor(colores[indiceColor])

            // Elegimos el icono que toca según el número guardado
            val iconRes = when (entry.emocion) {
                1 -> R.drawable.very_happy_icon
                2 -> R.drawable.happy_icon
                3 -> R.drawable.neutral_icon
                4 -> R.drawable.sad_icon
                else -> R.drawable.angry_icon
            }
            ivHistoryEmoji.setImageResource(iconRes)

            // Pintamos el icono con el color de la lista para que quede chulo
            ivHistoryEmoji.setColorFilter(colorActual)

            // --- LÓGICA DE LA FOTO (BASE64) ---
            if (!entry.fotoBase64.isNullOrEmpty()) {
                try {
                    // Decodificamos el churro de texto Base64 a bytes
                    val imageBytes = Base64.decode(entry.fotoBase64, Base64.DEFAULT)

                    // Hacemos visible el Card de la imagen
                    cardHistoryImage.visibility = View.VISIBLE

                    // Usamos Glide para cargar los bytes, recortar al centro y que no se deforme
                    Glide.with(ivHistoryImage.context)
                        .asBitmap()
                        .load(imageBytes)
                        .centerCrop()
                        .into(ivHistoryImage)

                } catch (e: Exception) {
                    // Si el Base64 da error o está corrupto, escondemos la imagen
                    cardHistoryImage.visibility = View.GONE
                }
            } else {
                // Si no hay foto en esta entrada, el CardView no debe ocupar espacio
                cardHistoryImage.visibility = View.GONE
            }
            //Le pasamos la nota a editar entera al fragment
            root.setOnClickListener {
                onItemClick(entry)
            }
        }
    }

    // El tamaño de la lista de entradas que traemos de Firebase
    override fun getItemCount() = entries.size
}