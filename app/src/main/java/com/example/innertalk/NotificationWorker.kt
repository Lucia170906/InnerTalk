package com.example.innertalk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.innertalk.R
class NotificationWorker (context :Context, params : WorkerParameters ): Worker(context, params){

    override fun doWork(): Result {
        lanzarNotificacion()
        return Result.success()
    }

    private fun lanzarNotificacion(){
        val name ="InnerTalk"
        val descriptionText = "¡Recuerda registrar como te sientes!"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId = "RECORDATORIO_DIARIO"

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Creamos el canal si es andorid 8.0 o superior
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, name, importance).apply {
                description=descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("¿Cómo te sientes hoy? :)")
            .setContentText("Es un buen momento para escribir en tu diario")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}