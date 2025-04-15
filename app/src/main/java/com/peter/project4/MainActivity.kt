package com.peter.project4

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.peter.project4.ui.EventAdapter
import com.peter.project4.data.Event
import com.peter.project4.event.EventProvider
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: EventAdapter
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dateInput: DatePicker
    private lateinit var timeInput: TimePicker
    private lateinit var addButton: Button

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

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(fetchEventsFromProvider())
        recyclerView.adapter = adapter

        // Set up Add button click listener
        addButton.setOnClickListener {
            addEventToProvider()
        }
    }

    private fun addEventToProvider() {
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

            // Refresh the RecyclerView
            adapter.updateData(fetchEventsFromProvider())

            // Clear input fields
            titleInput.text.clear()
            descriptionInput.text.clear()

            // Reset DatePicker to the current date
            val calendar = java.util.Calendar.getInstance()
            dateInput.updateDate(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

            // Reset TimePicker to the current time
            timeInput.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            timeInput.minute = calendar.get(java.util.Calendar.MINUTE)
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
}