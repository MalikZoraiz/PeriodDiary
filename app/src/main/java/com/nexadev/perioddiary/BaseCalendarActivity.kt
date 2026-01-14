package com.nexadev.perioddiary

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nexadev.perioddiary.adapter.MonthAdapter
import java.util.Calendar

abstract class BaseCalendarActivity : AppCompatActivity() {

    protected fun setupCalendar(
        recyclerView: RecyclerView,
        selectedDates: List<Calendar>,
        selectionEnabled: Boolean,
        showPredictions: Boolean,
        isHorizontal: Boolean, // This flag is now correctly used
        onDateSelected: (Calendar) -> Unit
    ) {
        val months = mutableListOf<Calendar>()
        val startCalendar = Calendar.getInstance().apply { set(2000, Calendar.JANUARY, 1) }
        val endCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }

        val calendar = startCalendar.clone() as Calendar
        while (calendar.before(endCalendar)) {
            months.add(calendar.clone() as Calendar)
            calendar.add(Calendar.MONTH, 1)
        }

        val adapter = MonthAdapter(this, months, selectedDates, selectionEnabled, showPredictions, isHorizontal, onDateSelected)
        
        val layoutManager = if (isHorizontal) {
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        } else {
            LinearLayoutManager(this)
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        if (!isHorizontal) {
            recyclerView.scrollToPosition(months.size - 1)
        }
    }
}