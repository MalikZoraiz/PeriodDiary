package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import com.nexadev.perioddiary.databinding.ActivityLogMorePeriodsBinding
import java.util.Calendar

class LogMorePeriodsActivity : BaseCalendarActivity() {
    private lateinit var binding: ActivityLogMorePeriodsBinding
    private val selectedDates = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogMorePeriodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowLogMore.setOnClickListener { finish() }

        binding.confirmButtonLogMore.isEnabled = false

        val datesInMillis = intent.getLongArrayExtra("selectedDates")
        if (datesInMillis != null) {
            for (millis in datesInMillis) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = millis
                selectedDates.add(calendar)
            }
        }

        setupCalendar(binding.monthsRecyclerViewLogMore, selectedDates, true, true) { date ->
            val today = Calendar.getInstance()

            if (date.after(today)) {
                // Don't allow selecting future dates
                return@setupCalendar
            }

            val startOfPeriod = date.clone() as Calendar
            for (i in 0 until 5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) {
                    break // Stop if we reach a future day
                }

                // Add only if not already selected to avoid duplicates
                if (selectedDates.none { it.get(Calendar.YEAR) == dayToAdd.get(Calendar.YEAR) && it.get(Calendar.DAY_OF_YEAR) == dayToAdd.get(Calendar.DAY_OF_YEAR) }) {
                    selectedDates.add(dayToAdd)
                }

                startOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Notify the adapter that the underlying data has changed
            binding.monthsRecyclerViewLogMore.adapter?.notifyDataSetChanged()
            binding.confirmButtonLogMore.isEnabled = true
        }

        binding.confirmButtonLogMore.setOnClickListener {
            val intent = Intent(this, SymptomsActivity::class.java)
            startActivity(intent)
        }
    }
}