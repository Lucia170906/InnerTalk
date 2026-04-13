package com.example.innertalk.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Necesitarás la librería Glide para las imágenes
import com.example.innertalk.R
import com.example.innertalk.databinding.ItemsEntriesCardBinding
import com.example.innertalk.model.EntryModel


class EntriesAdapter(private val entries: MutableList<EntryModel>) :
    RecyclerView.Adapter<EntriesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemsEntriesCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemsEntriesCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        val fechaLong = entry.fecha as? Long ?: 0L
        if (fechaLong > 0) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val fechaLegible = sdf.format(java.util.Date(fechaLong))
            holder.binding.tvHistoryDate.text = fechaLegible
        } else {
            holder.binding.tvHistoryDate.text = entry.fecha.toString()
        }


        holder.binding.apply {
            tvHistoryNote.text = entry.texto

            // Asignar el emote según la emoción guardada
            val colores = listOf("#4CAF50", "#FFEB3B", "#9E9E9E", "#FF9800", "#F44336")
            val indiceColor = (entry.emocion - 1).coerceIn(0, 4)
            val colorActual = android.graphics.Color.parseColor(colores[indiceColor])

            // Asignamos el icono
            val iconRes = when (entry.emocion) {
                1 -> R.drawable.very_happy_icon
                2 -> R.drawable.happy_icon
                3 -> R.drawable.neutral_icon
                4 -> R.drawable.sad_icon
                else -> R.drawable.angry_icon
            }
            ivHistoryEmoji.setImageResource(iconRes)

            // Aplicamos el color al icono para que resalte
            ivHistoryEmoji.setColorFilter(colorActual)



            if (!entry.fotoBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(entry.fotoBase64, Base64.DEFAULT)

                    holder.binding.cardHistoryImage.visibility = View.VISIBLE

                    Glide.with(holder.binding.ivHistoryImage.context)
                        .asBitmap()
                        .load(imageBytes) // Glide acepta el ByteArray directamente
                      //  .override(Target.SIZE_ORIGINAL) // Lee el tamaño real
                        .centerCrop() // Recorta proporcionalmente
                        .into(holder.binding.ivHistoryImage)

                } catch (e: Exception) {
                    holder.binding.cardHistoryImage.visibility = View.GONE
                }
            } else {
                cardHistoryImage.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = entries.size
}