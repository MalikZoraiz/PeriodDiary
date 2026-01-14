package com.nexadev.perioddiary.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.nexadev.perioddiary.R
import java.util.Calendar

class HorizontalCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var monthName: TextView
    private var daysGrid: GridLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.item_calendar_month_horizontal, this, true)
        monthName = findViewById(R.id.tvMonthName)
        daysGrid = findViewById(R.id.rvDays)
    }

    fun setMonth(calendar: Calendar, selectedDates: List<Calendar>, predictedDates: List<Calendar>, fertileDates: List<Calendar>, ovulationDate: Calendar?) {
        monthName.text = String.format("%tB %tY", calendar, calendar)

        daysGrid.removeAllViews()

        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1 until firstDayOfWeek) {
            daysGrid.addView(TextView(context))
        }

        for (i in 1..daysInMonth) {
            val dayView = LayoutInflater.from(context).inflate(R.layout.day_item_layout, daysGrid, false) as TextView
            dayView.text = i.toString()

            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, i)

            val isSelected = selectedDates.any { it.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) && it.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR) }
            val isPredicted = predictedDates.any { it.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) && it.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR) }
            val isFertile = fertileDates.any { it.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) && it.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR) }
            val isOvulation = ovulationDate?.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) && ovulationDate.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR)

            when {
                isSelected -> dayView.setBackgroundResource(R.drawable.calendar_day_background)
                isPredicted -> dayView.setBackgroundResource(R.drawable.predicted_day_background)
                isOvulation -> dayView.setBackgroundResource(R.drawable.ovulation_day_background)
                isFertile -> dayView.setBackgroundResource(R.drawable.fertile_day_background)
                else -> dayView.setBackgroundResource(R.drawable.calendar_day_background) // Default
            }
            daysGrid.addView(dayView)
        }
    }
}