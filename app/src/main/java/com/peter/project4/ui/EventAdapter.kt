package com.peter.project4.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.peter.project4.R
import com.peter.project4.data.Event

class EventAdapter(private var events: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // ViewHolder class for binding views
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.eventTitle)
        val description: TextView = itemView.findViewById(R.id.eventDescription)
        val date: TextView = itemView.findViewById(R.id.eventDate)
        val time: TextView = itemView.findViewById(R.id.eventTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        holder.description.text = event.description
        holder.date.text = event.date // Binding the event date
        holder.time.text = event.time // Binding the event time
    }

    override fun getItemCount(): Int = events.size

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}
