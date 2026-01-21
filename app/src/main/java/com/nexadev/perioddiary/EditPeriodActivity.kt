package com.nexadev.perioddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityEditPeriodBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class EditPeriodActivity : BaseCalendarActivity() {

    private lateinit var binding: ActivityEditPeriodBinding
    private val selectedDates = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPeriodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val periodEntries = AppDatabase.getDatabase(applicationContext).periodEntryDao().getAllPeriodEntriesList()
            val allDates = periodEntries.flatMap {
                val dates = mutableListOf<Calendar>()
                val current = it.startDate.clone() as Calendar
                while (current.before(it.endDate) || current.isSameDay(it.endDate)) {
                    dates.add(current.clone() as Calendar)
                    current.add(Calendar.DAY_OF_MONTH, 1)
                }
                dates
            }
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
                selectedDates.removeAll { it.isSameDay(date) }
            } else {
                selectedDates.add(date)
            }
            binding.editPeriodCalendarRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun saveAndFinish() {
        lifecycleScope.launch {
            val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
            periodEntryDao.deleteAllPeriodEntries()

            // Logic to group consecutive dates into period entries
            val sortedDates = selectedDates.sortedBy { it.timeInMillis }
            if (sortedDates.isNotEmpty()) {
                var currentEntryStart = sortedDates.first()
                for (i in 1 until sortedDates.size) {
                    val diff = sortedDates[i].timeInMillis - sortedDates[i - 1].timeInMillis
                    val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
                    if (days > 1) {
                        periodEntryDao.insertPeriodEntry(PeriodEntry(startDate = currentEntryStart, endDate = sortedDates[i - 1]))
                        currentEntryStart = sortedDates[i]
                    }
                }
                periodEntryDao.insertPeriodEntry(PeriodEntry(startDate = currentEntryStart, endDate = sortedDates.last()))
            }
            setResult(RESULT_OK) // Set the result to indicate data has changed
            finish()
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
               this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}