package com.peter.project4.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.peter.project4.R

class AlarmReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "event_notification_channel"

    override fun onReceive(context: Context, intent: Intent?) {
        // Retrieve Event data from the Intent
        val eventTitle = intent?.getStringExtra("event_title") ?: "Event Reminder"
        val eventDescription = intent?.getStringExtra("event_description") ?: "You have an upcoming event."

        // Create the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(eventTitle) // Use the event title
            .setContentText(eventDescription) // Use the event description
            .setSmallIcon(R.drawable.baseline_calendar_month_24) // Replace with your own icon
            .setAutoCancel(true) // Remove the notification when clicked
            .build()

        // Get the NotificationManager system service
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Show the notification
        notificationManager.notify(0, notification)
    }
}