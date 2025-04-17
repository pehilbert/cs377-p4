package com.peter.project4

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.peter.project4.ui.EventAdapter
import com.peter.project4.data.Event
import com.peter.project4.event.EventProvider
import com.peter.project4.notification.AlarmReceiver
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: EventAdapter
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dateInput: DatePicker
    private lateinit var timeInput: TimePicker
    private lateinit var addButton: Button

    private val CHANNEL_ID = "event_notification_channel"
    private val ALARM_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        titleInput = findViewById(R.id.titleEditText)
        descriptionInput = findViewById(R.id.descriptionEditText)
        dateInput = findViewById(R.id.datePicker)
        timeInput = findViewById(R.id.timePicker)
        addButton = findViewById(R.id.submitButton)
        val recyclerView: RecyclerView = findViewById(R.id.eventsRecyclerView)

        // Create Notification Channel (For Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(fetchEventsFromProvider())
        recyclerView.adapter = adapter

        // Set up Add button click listener
        addButton.setOnClickListener {
            // Add event to ContentProvider and set alarm
            addEvent()
        }
    }

    private fun addEvent() {
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        // Get the selected date from DatePicker
        val year = dateInput.year
        val month = dateInput.month + 1
        val day = dateInput.dayOfMonth
        val date = "$year-$month-$day"

        // Get the selected time from TimePicker
        val hour = timeInput.hour
        val minute = timeInput.minute
        val time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        if (title.isNotBlank() && description.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
            val values = ContentValues().apply {
                put("title", title)
                put("description", description)
                put("date", date)
                put("time", time)
            }

            // Insert the event into the ContentProvider
            contentResolver.insert(EventProvider.EVENTS_URI, values)

            // Schedule notification through AlarmManager
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1) // Month is 0-based
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                } else {
                    setAlarm(title, description, calendar)
                }
            } else {
                setAlarm(title, description, calendar)
            }

            // Refresh the RecyclerView
            adapter.updateData(fetchEventsFromProvider())

            // Clear input fields
            titleInput.text.clear()
            descriptionInput.text.clear()

            // Reset DatePicker to the current date
            val currentCalendar = Calendar.getInstance()
            dateInput.updateDate(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH))

            // Reset TimePicker to the current time
            timeInput.hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            timeInput.minute = currentCalendar.get(Calendar.MINUTE)

            Toast.makeText(this, "Event '$title' added successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchEventsFromProvider(): List<Event> {
        val cursor = contentResolver.query(
            EventProvider.EVENTS_URI, null, null, null, null
        )
        val events = mutableListOf<Event>()
        cursor?.use {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
                events.add(Event(id, title, description, date, time))
            }
        }
        return events
    }

    // Sets an alarm to go off at the specified time by Calendar object for the title and description
    private fun setAlarm(title: String, description: String, calendar: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create an intent to trigger the BroadcastReceiver
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("event_title", title)
            putExtra("event_description", description)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use AlarmManager to set the alarm
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}