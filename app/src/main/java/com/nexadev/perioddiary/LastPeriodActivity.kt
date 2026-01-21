package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.contentValuesOf
import androidx.core.os.bundleOf // KTX for bundles
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

        binding.notSureButton.setOnClickListener {
            // Navigate to the SymptomsActivity, skipping the LogMorePeriodsActivity
            val intent = Intent(this, SymptomsActivity::class.java)
            startActivity(intent)
            finish() // Finish this activity to prevent going back to it
        }

        setupCalendar(
            recyclerView = binding.monthsRecyclerView,
            selectedDates = selectedDates,
            selectionEnabled = true,
            showPredictions = false,
            isHorizontal = false
        ) { date ->
            val today = Calendar.getInstance()

            if (date.after(today)) return@setupCalendar

            selectedDates.clear()
            val startOfPeriod = date.clone() as Calendar

            // KTX/Kotlin logic: Using 'repeat' instead of standard 'for' loops
            repeat(5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) return@repeat

                selectedDates.add(dayToAdd)
                startOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }

            binding.monthsRecyclerView.adapter?.notifyDataSetChanged()
            binding.confirmButtonLastPeriod.isEnabled = true
        }

        binding.confirmButtonLastPeriod.setOnClickListener {
            saveAndNavigate()
        }
    }

    private fun saveAndNavigate() {
        if (selectedDates.isNotEmpty()) {
            lifecycleScope.launch {
                val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
                val startDate = selectedDates.first()
                val endDate = selectedDates.last()
                periodEntryDao.insertPeriodEntry(PeriodEntry(startDate = startDate, endDate = endDate))
            }
        }

        // KTX Optimization: Using Intent constructor more cleanly
        val datesInMillis = selectedDates.map { it.timeInMillis }.toLongArray()
        val intent = Intent(this, LogMorePeriodsActivity::class.java).apply {
            putExtra("selectedDates", datesInMillis)
        }
        startActivity(intent)
        finish()
    }
}