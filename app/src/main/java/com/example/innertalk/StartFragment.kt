package com.example.innertalk

import ActivitiesAdapter
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innertalk.databinding.FragmentStartBinding
import com.example.innertalk.model.ActivityModel
import com.example.innertalk.viewModel.ActivityViewModel
import android.graphics.Color
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri

class StartFragment : Fragment() {
    private var _binding : FragmentStartBinding? = null
    private  val binding  get ()  =_binding!!

    private lateinit var  viewModel : ActivityViewModel
    private lateinit var  adapter: ActivitiesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)

        adapter = ActivitiesAdapter (
            onPlayClicked = {url ->
                //cogemos la url y entramos en youtube/ navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            },
            onCheckClicked = {id ->
                //marcamos la actividad como completada
                viewModel.activityCompletion(id)

            },
            onItemClicked = {activity ->
                mostrarModalActividad(activity)

            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StartFragment.adapter
            // Esto es vital cuando el Recycler está dentro de un Scroll
            isNestedScrollingEnabled = false
        }

        viewModel.activities.observe(viewLifecycleOwner) { lista ->
            lista?.let {
                adapter.submitList(it)
            }
        }

        viewModel.loadActivities()
    }

    private fun mostrarModalActividad(activity : ActivityModel){
        //1.Creamos el Dialog
        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_activity_detail) //conectamos con el layout

        //2.Hacemos el fondo tranparente para consegui las esquinas redondeadas
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //Hacemos que el cuadro ocupe casi tod el ancgho de la pantalla
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        //3. Buscamos las vistas dentro del dialogo
        val icon = dialog.findViewById<ImageView>(R.id.dialogIcon)
        val title = dialog.findViewById<TextView>(R.id.dialogTitle)
        val description = dialog.findViewById<TextView>(R.id.dialogDescription)
        val duration = dialog.findViewById<TextView>(R.id.dialogDuration)
        val btnPlay = dialog.findViewById<Button>(R.id.dialogBtnPlay)
        val checkBox = dialog.findViewById<CheckBox>(R.id.dialogCheckBox)

        //4. Rellenamos los datos con los de la actividad pulsada
        icon.setImageResource(activity.iconRes)
        title.text = activity.title
        description.text = activity.description
        duration.text = activity.duration
        checkBox.isChecked = activity.isCompleted

        //5.Configuramos el botón de play si este es pulsado desde el modal
        if(!activity.url.isNullOrEmpty()){
            btnPlay.visibility = View.VISIBLE
            btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.url))
                startActivity(intent)
                dialog.dismiss() //cerramos el modal al ir a youtube
            }
        }else{
            btnPlay.visibility=View.GONE

        }

        //6. Configuramos el check box
        checkBox.setOnClickListener {
            viewModel.activityCompletion(activity.id)
            dialog.dismiss() //cerramos al marcas como hecha
        }
        //7.Mostramos el modal en la pantalla
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}