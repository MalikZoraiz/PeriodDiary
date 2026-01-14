package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityLastPeriodBinding
import kotlinx.coroutines.launch
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

        setupCalendar(binding.monthsRecyclerView, selectedDates, true, false, false) { date -> // Predictions disabled, not horizontal
            val today = Calendar.getInstance()

            if (date.after(today)) {
                // Don't allow selecting future dates
                return@setupCalendar
            }

            selectedDates.clear()
            val startOfPeriod = date.clone() as Calendar
            for (i in 0 until 5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) {
                    break // Stop if we reach a future day
                }
                selectedDates.add(dayToAdd)
                startOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Notify the adapter that the underlying data has changed
            binding.monthsRecyclerView.adapter?.notifyDataSetChanged()
            binding.confirmButtonLastPeriod.isEnabled = true
        }

        binding.confirmButtonLastPeriod.setOnClickListener {
            if (selectedDates.isNotEmpty()) {
                lifecycleScope.launch {
                    val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
                    val startDate = selectedDates.first()
                    val endDate = selectedDates.last()
                    periodEntryDao.insertPeriodEntry(PeriodEntry(startDate = startDate, endDate = endDate))
                }
            }

            val datesInMillis = selectedDates.map { it.timeInMillis }.toLongArray()
            val intent = Intent(this, LogMorePeriodsActivity::class.java)
            intent.putExtra("selectedDates", datesInMillis)
            startActivity(intent)
        }
    }
}