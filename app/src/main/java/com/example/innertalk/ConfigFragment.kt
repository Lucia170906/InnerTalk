package com.example.innertalk

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.innertalk.databinding.FragmentConfigBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.concurrent.TimeUnit


class ConfigFragment : Fragment() {

    private var _binding : FragmentConfigBinding ?= null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperamos el estado previo del Switch desde SharedPreferences
        // "config_prefs" es el nombre del archivo de preferencias
        val preferencia = requireContext().getSharedPreferences("config_prefs", 0)
        binding.switchNotificaciones.isChecked = preferencia.getBoolean("notif_active", false)

        // Escuchador para detectar cambios en el Switch de notificaciones
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si el usuario activa el switch, abrimos el selector de hora
                abrirRelojYProgramar()
            } else {
                // Si lo desactiva, cancelamos la tarea programada y actualizamos preferencias
                cancelarNotificacion()
                preferencia.edit().putBoolean("notif_active", false).apply()
            }
        }
        binding.btnCerrarSesion.setOnClickListener {
            // 1. Cerramos sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            //2. Limpiamos las shared preferences de las notificaciones
            val prefs = requireContext().getSharedPreferences("config_prefs", 0)
            prefs.edit().clear().apply()

            // 3. Mandamos al usuario de vuelta al Login (LogInActivity)
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


    //  Muestra un selector de hora (MaterialTimePicker) y gestiona la respuesta del usuario.

    private fun abrirRelojYProgramar() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H) // Formato de 24 horas
            .setHour(20) // Hora por defecto: 20:00
            .setMinute(0)
            .setTitleText("¿A qué hora quieres el recordatorio?")
            .build()

        // Mostramos el selector de hora
        picker.show(childFragmentManager, "TIME_PICKER")

        // Acción al pulsar "Aceptar" en el selector
        picker.addOnPositiveButtonClickListener {
            // Programamos la notificación con la hora y minutos seleccionados
            programarNotificacion(picker.hour, picker.minute)

            // Guardamos en persistencia que las notificaciones están activas
            requireContext().getSharedPreferences("config_prefs", 0)
                .edit().putBoolean("notif_active", true).apply()

            Toast.makeText(context, "Recordatorio programado a las ${picker.hour}:${picker.minute}", Toast.LENGTH_SHORT).show()
        }
    }


     // Calcula el retraso inicial y programa una tarea periódica de 24 horas usando WorkManager.

    private fun programarNotificacion(hora: Int, minuto: Int) {
        // Configuramos un calendario con la hora deseada
        val customTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }

        val currentTime = Calendar.getInstance()

        // Si la hora seleccionada ya pasó hoy (ej. son las 10:00 y eliges las 08:00),
        // sumamos un día para que empiece mañana.
        if (customTime.before(currentTime)) {
            customTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Calculamos cuánto tiempo falta (en milisegundos) desde ahora hasta la hora elegida
        val delay = customTime.timeInMillis - currentTime.timeInMillis

        // Configuramos la petición de trabajo periódico (cada 24 horas)
        // NotificationWorker es la clase que contiene la lógica de qué mostrar en la notificación
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) // Espera el tiempo calculado antes de la primera ejecución
            .addTag("notificacion_diaria") // Etiqueta para identificar y cancelar la tarea después
            .build()

        // Encolamos el trabajo de forma única.
        // UPDATE asegura que si ya existía uno, se actualice con la nueva hora.
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "notificacion_diaria",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }


    //  Cancela cualquier tarea de WorkManager asociada a la etiqueta de notificación.

    private fun cancelarNotificacion() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("notificacion_diaria")
        Toast.makeText(context, "Notificación cancelada", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}