package br.edu.ufabc.reciclabc

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    private val NOTIFICATIONID = 1

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            var builder = NotificationCompat.Builder(it, "GarbageReminder")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(it.getString(R.string.broadcast_receiver_notification_title))
                .setContentText(it.getString(R.string.broadcast_receiver_notification_content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            var notificationManager = NotificationManagerCompat.from(it)

            notificationManager.notify(NOTIFICATIONID, builder.build())
        }
    }

    fun setAlarm(context: Context, scheduledDateInMillis: Long, pendingIntent: PendingIntent) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            scheduledDateInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent
        )
    }

    fun cancelAlarm(context: Context, pendingIntent: PendingIntent) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "GarbageReminder"
                val descriptionText = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("GarbageReminder", name, importance).apply {
                    description = descriptionText
                }

                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun calculateNextTriggerDateInMillis(weekday: Int, hour: Int, minutes: Int): Long {
            val scheduledDate = Calendar.getInstance() //Today
            if (scheduledDate.get(Calendar.DAY_OF_WEEK) != weekday) {
                scheduledDate.add(
                    Calendar.DAY_OF_MONTH,
                    (weekday + 7 - scheduledDate.get(Calendar.DAY_OF_WEEK)) % 7
                )
            } else {
                val minOfDay =
                    scheduledDate.get(Calendar.HOUR_OF_DAY) * 60 + scheduledDate.get(Calendar.MINUTE)
                if (minOfDay >= (hour + 3) * 60 + minutes) scheduledDate.add(
                    Calendar.DAY_OF_MONTH,
                    7
                ) //Next week
            }
            scheduledDate.set(Calendar.HOUR_OF_DAY, hour + 3) //To local time
            scheduledDate.set(Calendar.MINUTE, minutes)

            return scheduledDate.timeInMillis
        }
    }

}