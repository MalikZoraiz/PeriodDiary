package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityLogMorePeriodsBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class LogMorePeriodsActivity : BaseCalendarActivity() {
    private lateinit var binding: ActivityLogMorePeriodsBinding
    private val selectedDates = mutableListOf<Calendar>()
    private val newPeriodsToSave = mutableListOf<PeriodEntry>()

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

        setupCalendar(binding.monthsRecyclerViewLogMore, selectedDates, true, true, false) { date ->
            val today = Calendar.getInstance()

            if (date.after(today)) {
                return@setupCalendar
            }

            val startOfPeriod = date.clone() as Calendar
            val newPeriodDates = mutableListOf<Calendar>()
            for (i in 0 until 5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) {
                    break
                }
                if (selectedDates.none { it.get(Calendar.YEAR) == dayToAdd.get(Calendar.YEAR) && it.get(Calendar.DAY_OF_YEAR) == dayToAdd.get(Calendar.DAY_OF_YEAR) }) {
                    newPeriodDates.add(dayToAdd)
                }
                startOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }

            if (newPeriodDates.isNotEmpty()) {
                selectedDates.addAll(newPeriodDates)
                newPeriodsToSave.add(PeriodEntry(startDate = newPeriodDates.first(), endDate = newPeriodDates.last()))
                binding.monthsRecyclerViewLogMore.adapter?.notifyDataSetChanged()
                binding.confirmButtonLogMore.isEnabled = true
            }
        }

        binding.confirmButtonLogMore.setOnClickListener {
            lifecycleScope.launch {
                val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
                newPeriodsToSave.forEach { periodEntryDao.insertPeriodEntry(it) }
            }
            val intent = Intent(this, SymptomsActivity::class.java)
            startActivity(intent)
            finish() // Finish this activity to prevent going back to it
        }
    }
}