package com.example.innertalk.ui.theme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.innertalk.R


 // Clase que define el Worker que se ejecutará en segundo plano.
  //Extiende de Worker, lo que permite que WorkManager lo gestione incluso si la app está cerrada.

class NotificationWorker (context :Context, params : WorkerParameters ): Worker(context, params){


     // doWork() es el método que se ejecuta automáticamente cuando llega la hora programada.

    override fun doWork(): Result {
        // Ejecutamos la lógica de la notificación
        lanzarNotificacion()
        
        // Indicamos a WorkManager que el trabajo se completó con éxito
        return Result.success()
    }


    //  Configura y  la notificación en el sistema.

    private fun lanzarNotificacion(){
        // Datos básicos del canal y la notificación
        val name ="InnerTalk"
        val descriptionText = "Recordatorio diario"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId = "RECORDATORIO_DIARIO"

        // Obtenemos el servicio de notificaciones del sistema Android
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

         // A partir de Android 8.0, todas las notificaciones deben pertenecer a un canal.

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Registramos el canal en el sistema
            notificationManager.createNotificationChannel(channel)
        }


         // CONSTRUCCIÓN DE LA NOTIFICACIÓN
         // Aquí definimos el aspecto visual: título, texto, icono y comportamiento.

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener un icono válido aquí
            .setContentTitle("¿Cómo te sientes hoy? :)") // Título principal
            .setContentText("Es un buen momento para escribir en tu diario") // Cuerpo del mensaje
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad para Android 7.1 e inferiores
            .setAutoCancel(true) // La notificación desaparece cuando el usuario la toca

        // Manda la notificación
        notificationManager.notify(1, builder.build())
    }
}