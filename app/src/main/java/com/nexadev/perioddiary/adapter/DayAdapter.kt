package com.nexadev.perioddiary.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nexadev.perioddiary.R
import java.util.Calendar

class DayAdapter(
    private val context: Context,
    private val monthCalendar: Calendar,
    private val selectedDates: List<Calendar>,
    private val selectionEnabled: Boolean,
    private val showPredictions: Boolean, // New flag
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private val days = mutableListOf<String?>()
    private val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    private var selectedPositions = mutableListOf<Int>()
    private var predictedPositions = mutableListOf<Int>()

    init {
        val c = monthCalendar.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1
        val maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDayOfWeek) {
            days.add(null)
        }
        for (i in 1..maxDays) {
            days.add(i.toString())
        }

        // Handle selected dates
        selectedDates.forEach { selectedDate ->
            if (selectedDate.get(Calendar.MONTH) == monthCalendar.get(Calendar.MONTH) && selectedDate.get(Calendar.YEAR) == monthCalendar.get(Calendar.YEAR)) {
                val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                val index = days.indexOf(day.toString())
                if (index != -1) {
                    selectedPositions.add(index)
                }
            }
        }

        // Handle predicted dates based on the earliest selected date
        if (showPredictions && selectedDates.isNotEmpty()) {
            val earliestDate = selectedDates.minByOrNull { it.timeInMillis }!!
            val cycleLength = 28 // 23 days gap + 5 days period = 28
            var predictions = 0

            var predictedStartDate = earliestDate.clone() as Calendar
            predictedStartDate.add(Calendar.DAY_OF_MONTH, -cycleLength)

            while (predictedStartDate.after(Calendar.getInstance().apply { set(2000, 0, 1) }) && predictions < 4) {
                if (predictedStartDate.get(Calendar.MONTH) == monthCalendar.get(Calendar.MONTH) && predictedStartDate.get(Calendar.YEAR) == monthCalendar.get(Calendar.YEAR)) {
                    val startDay = predictedStartDate.get(Calendar.DAY_OF_MONTH)
                    val startIndex = days.indexOf(startDay.toString())
                    if (startIndex != -1) {
                        for (i in 0 until 5) {
                            val pos = startIndex + i
                            if (pos < days.size && days[pos] != null) {
                                predictedPositions.add(pos)
                            }
                        }
                    }
                }
                predictedStartDate.add(Calendar.DAY_OF_MONTH, -cycleLength)
                predictions++
            }
        }
    }

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val tv = TextView(parent.context)
        tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
        tv.gravity = android.view.Gravity.CENTER
        tv.setTextColor(Color.BLACK)
        tv.textSize = 14f
        tv.id = android.R.id.text1
        return DayViewHolder(tv)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.tvDay.text = day ?: ""

        if (day != null) {
            val dayInt = day.toInt()

            holder.tvDay.background = null
            holder.tvDay.setTextColor(Color.BLACK)
            holder.tvDay.typeface = Typeface.DEFAULT

            val thisMonth = monthCalendar.get(Calendar.MONTH)
            val thisYear = monthCalendar.get(Calendar.YEAR)

            when {
                position in selectedPositions -> {
                    holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background)
                    holder.tvDay.isSelected = true
                    holder.tvDay.setTextColor(Color.WHITE)
                }
                position in predictedPositions -> {
                    holder.tvDay.setBackgroundResource(R.drawable.predicted_day_background)
                }
                thisYear == currentYear && thisMonth == currentMonth && dayInt == currentDay -> {
                    holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.pink))
                    holder.tvDay.typeface = Typeface.DEFAULT_BOLD
                }
                else -> {
                    holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background)
                    holder.tvDay.isSelected = false
                }
            }

            if (selectionEnabled) {
                holder.itemView.setOnClickListener {
                    val clickedCalendar = monthCalendar.clone() as Calendar
                    clickedCalendar.set(Calendar.DAY_OF_MONTH, dayInt)
                    onDateSelected(clickedCalendar)
                }
            } else {
                holder.itemView.setOnClickListener(null)
            }
        } else {
            holder.tvDay.background = null
            holder.tvDay.setOnClickListener(null)
        }
    }

    override fun getItemCount() = days.size
}