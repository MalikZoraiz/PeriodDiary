package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat // KTX for Intent extras
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

        // KTX Optimization: Unpacking the Intent extras using functional mapping
        intent.getLongArrayExtra("selectedDates")?.forEach { millis ->
            selectedDates.add(Calendar.getInstance().apply { timeInMillis = millis })
        }

        setupCalendar(
            recyclerView = binding.monthsRecyclerViewLogMore,
            selectedDates = selectedDates,
            selectionEnabled = true,
            showPredictions = true,
            isHorizontal = false
        ) { date ->
            val today = Calendar.getInstance()

            if (date.after(today)) return@setupCalendar

            val startOfPeriod = date.clone() as Calendar
            val newPeriodDates = mutableListOf<Calendar>()

            // KTX Optimization: Using repeat instead of for-until
            repeat(5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) return@repeat

                // KTX Optimization: Simplified date comparison check
                val isAlreadySelected = selectedDates.any {it.isSameDay(dayToAdd) }

                if (!isAlreadySelected) {
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
            saveAndFinish()
        }
    }

    private fun saveAndFinish() {
        lifecycleScope.launch {
            val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
            // KTX Optimization: Direct iteration for background saving
            newPeriodsToSave.forEach { periodEntryDao.insertPeriodEntry(it) }
        }

        // KTX Optimization: Simple navigation
        startActivity(Intent(this, SymptomsActivity::class.java))
        finish()
    }

    // KTX Helper Extension: Keeps the logic inside setupCalendar clean
    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}