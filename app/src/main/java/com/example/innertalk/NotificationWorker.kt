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
        // Sustituimos los textos por recursos del sistema strings.xml
        val name = applicationContext.getString(R.string.notif_channel_name)
        val descriptionText = applicationContext.getString(R.string.notif_channel_desc)
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
            .setContentTitle(applicationContext.getString(R.string.notif_title))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentText(applicationContext.getString(R.string.notif_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}