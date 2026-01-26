package com.nexadev.perioddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityEditPeriodBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class EditPeriodActivity : BaseCalendarActivity() {

    private lateinit var binding: ActivityEditPeriodBinding
    private val selectedDates = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPeriodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            // 1. Correctly get all period entries
            val periodEntries = AppDatabase.getDatabase(applicationContext).periodEntryDao().getAllPeriodEntries()
            
            // 2. Convert to a list of Calendar objects (works with the new PeriodEntry class)
            val allDates = periodEntries
                .filter { it.type == "PERIOD_DAY" } // We only edit period days
                .map { it.date.toCalendar() }
            
            selectedDates.addAll(allDates)
            setupEditCalendar()
        }

        binding.closeButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { saveAndFinish() }
    }

    private fun setupEditCalendar() {
        setupCalendar(
            recyclerView = binding.editPeriodCalendarRecyclerView,
            selectedDates = selectedDates,
            selectionEnabled = true,
            showPredictions = false,
            isHorizontal = false,
            startYear = 2024,
            startMonth = Calendar.JANUARY
        ) { date ->
            val isSelected = selectedDates.any { it.isSameDay(date) }

            if (isSelected) {
                val datesToRemove = mutableListOf<Calendar>()
                val processingQueue = mutableListOf(date)
                val processed = mutableSetOf<Calendar>()

                while (processingQueue.isNotEmpty()) {
                    val currentDay = processingQueue.removeAt(0)
                    if (selectedDates.any { it.isSameDay(currentDay) } && !processed.any { it.isSameDay(currentDay) }) {
                        datesToRemove.add(currentDay)
                        processed.add(currentDay)

                        val dayBefore = currentDay.clone() as Calendar
                        dayBefore.add(Calendar.DAY_OF_MONTH, -1)
                        processingQueue.add(dayBefore)

                        val dayAfter = currentDay.clone() as Calendar
                        dayAfter.add(Calendar.DAY_OF_MONTH, 1)
                        processingQueue.add(dayAfter)
                    }
                }
                selectedDates.removeAll { toRemove -> datesToRemove.any { it.isSameDay(toRemove) } }

            } else {
                val today = Calendar.getInstance()
                if (date.after(today)) return@setupCalendar
                
                for (i in 0 until 5) {
                    val dayToAdd = date.clone() as Calendar
                    dayToAdd.add(Calendar.DAY_OF_MONTH, i)
                    if (dayToAdd.after(today)) continue
                    if (!selectedDates.any { it.isSameDay(dayToAdd) }) {
                        selectedDates.add(dayToAdd)
                    }
                }
            }
            binding.editPeriodCalendarRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun saveAndFinish() {
        lifecycleScope.launch {
            val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
            
            // 1. Delete all old period entries
            periodEntryDao.deleteAllPeriodEntries()

            // 2. Create new entries from the selected dates (works with the new PeriodEntry class)
            val newPeriodEntries = selectedDates.map { 
                PeriodEntry(date = it.time, type = "PERIOD_DAY") 
            }
            periodEntryDao.insertAll(newPeriodEntries)
            
            setResult(RESULT_OK) // Set the result to indicate data has changed
            finish()
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
               this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun Date.toCalendar(): Calendar {
        return Calendar.getInstance().apply { time = this@toCalendar }
    }
}