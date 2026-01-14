package com.nexadev.perioddiary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nexadev.perioddiary.R
import java.util.Calendar
import java.util.Locale

class MonthAdapter(
    private val context: Context,
    private val months: List<Calendar>,
    private val selectedDates: List<Calendar>,
    private val selectionEnabled: Boolean,
    private val showPredictions: Boolean,
    private val isHorizontal: Boolean,
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    inner class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonthName: TextView = view.findViewById(R.id.tvMonthName)
        val rvDays: RecyclerView = view.findViewById(R.id.rvDays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = if (isHorizontal) {
            inflater.inflate(R.layout.item_calendar_month_horizontal, parent, false)
        } else {
            inflater.inflate(R.layout.item_calendar_month, parent, false)
        }
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthCalendar = months[position]
        val monthName = monthCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val year = monthCalendar.get(Calendar.YEAR)
        holder.tvMonthName.text = "$monthName $year"

        holder.rvDays.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.rvDays.adapter = DayAdapter(context, monthCalendar, selectedDates, selectionEnabled, showPredictions, isHorizontal, onDateSelected)

        holder.rvDays.isNestedScrollingEnabled = false
    }

    override fun getItemCount() = months.size
}