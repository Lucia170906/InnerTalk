import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.innertalk.model.ActivityModel

import com.example.innertalk.databinding.ViewholderActividadesBinding

//  Recibimos una "función" (callback) que se activará cuando cliquemos el checkbox.
//  Añadimos un segundo callback para la acción del botón Play.
class ActivitiesAdapter(
    private val onPlayClicked: (String) -> Unit,
    private val onCheckClicked: (Int) -> Unit
) : ListAdapter<ActivityModel, ActivitiesAdapter.ViewHolder>(ActivityDiffCallback) {

    //  El ViewHolder es el "contenedor" de la vista.
    // Usamos Binding para acceder a los IDs sin hacer findViewById.
    inner class ViewHolder(val binding: ViewholderActividadesBinding) : RecyclerView.ViewHolder(binding.root)

    // 3. Este método crea la tarjeta (el XML) la primera vez.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderActividadesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // 4. Este método "rellena" la tarjeta con los datos de cada actividad.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = getItem(position) // Obtenemos la actividad de la posición actual

        holder.binding.apply {
            // rellenamos  textos
            tvTitle.text = activity.title
            tvDuration.text = activity.duration
            ivIcon.setImageResource(activity.iconRes)

            // Lógica para el botón Play: Solo aparece si hay una URL
            if (!activity.url.isNullOrEmpty()) {
                btnPlay.visibility = View.VISIBLE
                btnPlay.setOnClickListener {
                    onPlayClicked(activity.url) // Abre el video sin marcar la actividad
                }
            } else {
                btnPlay.visibility = View.GONE // Si no hay video, el botón no ocupa espacio
            }

            // Pnemos el checkbox como esté en el modelo (true o false)
            cbDone.isChecked = activity.isCompleted

            //  Configuramos el click del checkbox
            cbDone.setOnClickListener {
                // Avisamos al ViewModel pasándole el ID de esta actividad
                onCheckClicked(activity.id)
            }
        }
    }

    //Compara la lista vieja con la nueva.
    // Esto hace que si solo cambias un checkbox, no se refresque toda la lista, solo ese item.
    object ActivityDiffCallback : DiffUtil.ItemCallback<ActivityModel>() {
        override fun areItemsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
            return oldItem.id == newItem.id // ES la misma actividad?
        }

        override fun areContentsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
            return oldItem == newItem //Ha cambiado algo
        }
    }
}