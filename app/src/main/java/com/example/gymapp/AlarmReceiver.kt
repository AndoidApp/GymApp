package com.example.gymapp

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * AlarmReceiver, subclass of [BroadcastReceiver].
 * Define how to handle alarms
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ONE_DAY_IN_MILLIS = 24*60*60*1000
        const val CHANNEL_ID = "gym_app"
        const val CONTENT_TITLE = "Gym App"
        const val CONTENT_TEXT = "Time to train!"
        const val NOTIFICATION_CHANNEL_NAME = "gym_appReminderChannel"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Channel for Gym App"
        const val TIME_PICKER_TEXT = "Select alarm time"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.charles_leclerc)
            .setContentTitle(CONTENT_TITLE)
            .setContentText(CONTENT_TEXT)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(123, builder.build())
    }
}