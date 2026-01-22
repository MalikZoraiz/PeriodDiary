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
    private val showPredictions: Boolean, // This can be used later for predictions
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
        holder.rvDays.adapter = DayAdapter(context, monthCalendar, selectedDates, selectionEnabled, onDateSelected)

        holder.rvDays.isNestedScrollingEnabled = false
    }

    override fun getItemCount() = months.size

    // --- Inner Day Adapter --- //
    private inner class DayAdapter(
        private val context: Context,
        private val calendar: Calendar,
        private val selectedDates: List<Calendar>,
        private val selectionEnabled: Boolean,
        private val onDateSelected: (Calendar) -> Unit
    ) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

        private val days = mutableListOf<Calendar?>()

        init {
            val monthCalendar = calendar.clone() as Calendar
            monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (i in 0 until firstDayOfMonth) {
                days.add(null)
            }
            for (i in 1..daysInMonth) {
                val dayCalendar = monthCalendar.clone() as Calendar
                dayCalendar.set(Calendar.DAY_OF_MONTH, i)
                days.add(dayCalendar)
            }
        }

        inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dayText: TextView = view.findViewById(R.id.day_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.day_item_layout, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            val date = days[position]
            if (date != null) {
                holder.dayText.text = date.get(Calendar.DAY_OF_MONTH).toString()
                holder.itemView.visibility = View.VISIBLE

                holder.itemView.isSelected = selectedDates.any { it.isSameDay(date) }

                if (selectionEnabled) {
                    holder.itemView.setOnClickListener {
                        onDateSelected(date)
                    }
                }
            } else {
                holder.dayText.text = ""
                holder.itemView.visibility = View.INVISIBLE
            }
        }

        override fun getItemCount() = days.size

        private fun Calendar.isSameDay(other: Calendar): Boolean {
            return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                   this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
        }
    }
}