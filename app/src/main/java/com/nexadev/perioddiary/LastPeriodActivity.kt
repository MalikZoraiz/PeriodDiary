package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import com.nexadev.perioddiary.databinding.ActivityLastPeriodBinding
import java.util.Calendar

class LastPeriodActivity : BaseCalendarActivity() {
    private lateinit var binding: ActivityLastPeriodBinding
    private val selectedDates = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastPeriodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowLastPeriod.setOnClickListener { finish() }

        binding.confirmButtonLastPeriod.isEnabled = false

        setupCalendar(binding.monthsRecyclerView, selectedDates, true, false) { date -> // Predictions disabled
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
            binding.monthsRecyclerView.adapter?.notifyDataSetChanged()
            binding.confirmButtonLastPeriod.isEnabled = true
        }

        binding.confirmButtonLastPeriod.setOnClickListener {
            val datesInMillis = selectedDates.map { it.timeInMillis }.toLongArray()
            val intent = Intent(this, LogMorePeriodsActivity::class.java)
            intent.putExtra("selectedDates", datesInMillis)
            startActivity(intent)
        }
    }
}