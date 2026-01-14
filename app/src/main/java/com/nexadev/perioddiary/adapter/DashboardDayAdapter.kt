package com.nexadev.perioddiary.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nexadev.perioddiary.R
import java.util.Calendar

class DashboardDayAdapter(
    private val context: Context,
    private val monthCalendar: Calendar,
    private val loggedPeriodDates: List<Calendar>,
    private val predictedPeriodDates: List<Calendar>,
    private val fertileDates: List<Calendar>,
    private val ovulationDates: List<Calendar>
) : RecyclerView.Adapter<DashboardDayAdapter.DayViewHolder>() {

    private val days = mutableListOf<String?>()
    private val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

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
    }

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val tv = TextView(parent.context)
        val displayMetrics = DisplayMetrics()
        (parent.context as android.app.Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val size = displayMetrics.widthPixels / 7 // Use full width
        tv.layoutParams = ViewGroup.LayoutParams(size, size)
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
            val dayCalendar = monthCalendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, dayInt)

            val isLoggedPeriod = loggedPeriodDates.any { it.isSameDay(dayCalendar) }
            val isPredictedPeriod = predictedPeriodDates.any { it.isSameDay(dayCalendar) }
            val isFertile = fertileDates.any { it.isSameDay(dayCalendar) }
            val isOvulation = ovulationDates.any { it.isSameDay(dayCalendar) }

            holder.tvDay.background = null
            holder.tvDay.setTextColor(Color.BLACK)
            holder.tvDay.typeface = Typeface.DEFAULT

            when {
                isLoggedPeriod -> {
                    holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background)
                    holder.tvDay.isSelected = true
                    holder.tvDay.setTextColor(Color.WHITE)
                }
                isPredictedPeriod -> {
                    holder.tvDay.setBackgroundResource(R.drawable.predicted_day_background)
                }
                isOvulation -> {
                    holder.tvDay.setBackgroundResource(R.drawable.ovulation_day_background)
                }
                isFertile -> {
                    holder.tvDay.setBackgroundResource(R.drawable.fertile_day_background)
                }
                monthCalendar.get(Calendar.YEAR) == currentYear && monthCalendar.get(Calendar.MONTH) == currentMonth && dayInt == currentDay -> {
                    holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.pink))
                    holder.tvDay.typeface = Typeface.DEFAULT_BOLD
                }
                else -> {
                    holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background)
                    holder.tvDay.isSelected = false
                }
            }
        } else {
            holder.tvDay.background = null
        }
    }

    override fun getItemCount() = days.size

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
               this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}