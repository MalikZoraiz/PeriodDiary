package com.nexadev.perioddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.nexadev.perioddiary.adapter.DashboardMonthAdapter
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var monthAdapter: DashboardMonthAdapter
    private val months = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val periodEntries = AppDatabase.getDatabase(applicationContext).periodEntryDao().getAllPeriodEntriesList()
            val loggedPeriodDates = periodEntries.flatMap {
                val dates = mutableListOf<Calendar>()
                val current = it.startDate.clone() as Calendar
                while (current.before(it.endDate) || current.isSameDay(it.endDate)) {
                    dates.add(current.clone() as Calendar)
                    current.add(Calendar.DAY_OF_MONTH, 1)
                }
                dates
            }

            // --- Prediction and Fertile Window Logic ---
            val predictedPeriodDates = mutableListOf<Calendar>()
            val fertileDates = mutableListOf<Calendar>()
            val ovulationDates = mutableListOf<Calendar>()

            if (periodEntries.isNotEmpty()) {
                val lastPeriod = periodEntries.first() // most recent one
                val cycleLength = 28
                val periodLength = 5

                // Predict next 6 periods
                var nextPeriodStart = lastPeriod.startDate.clone() as Calendar
                for (i in 0 until 6) {
                    nextPeriodStart.add(Calendar.DAY_OF_MONTH, cycleLength)
                    for (j in 0 until periodLength) {
                        val predictedDate = nextPeriodStart.clone() as Calendar
                        predictedDate.add(Calendar.DAY_OF_MONTH, j)
                        predictedPeriodDates.add(predictedDate)
                    }
                }

                // Calculate fertile window and ovulation for the next cycle
                val nextCycleStart = lastPeriod.startDate.clone() as Calendar
                nextCycleStart.add(Calendar.DAY_OF_MONTH, cycleLength)
                val ovulationDay = nextCycleStart.clone() as Calendar
                ovulationDay.add(Calendar.DAY_OF_MONTH, -14)
                ovulationDates.add(ovulationDay)

                for (i in -3..2) { // Fertile window is typically 6 days
                    val fertileDay = ovulationDay.clone() as Calendar
                    fertileDay.add(Calendar.DAY_OF_MONTH, i)
                    fertileDates.add(fertileDay)
                }

                // --- Countdown Logic ---
                val today = Calendar.getInstance()
                val nextUpcomingPeriod = predictedPeriodDates.firstOrNull { !it.before(today) }
                if (nextUpcomingPeriod != null) {
                    val diff = nextUpcomingPeriod.timeInMillis - today.timeInMillis
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    binding.countdownText.text = days.toString()
                }
            }

            // --- Calendar Setup ---
            val earliestLoggedMonth = periodEntries.minByOrNull { it.startDate.timeInMillis }?.startDate
            val startCalendar = earliestLoggedMonth ?: Calendar.getInstance()
            startCalendar.set(Calendar.DAY_OF_MONTH, 1)

            val endCalendar = Calendar.getInstance().apply { set(2030, Calendar.DECEMBER, 31) }

            val current = startCalendar.clone() as Calendar
            while (current.before(endCalendar)) {
                months.add(current.clone() as Calendar)
                current.add(Calendar.MONTH, 1)
            }

            monthAdapter = DashboardMonthAdapter(this@DashboardActivity, months, loggedPeriodDates, predictedPeriodDates, fertileDates, ovulationDates)
            binding.dashboardCalendarViewPager.adapter = monthAdapter

            // Scroll to the current month
            val currentMonthIndex = months.indexOfFirst { it.isSameMonth(Calendar.getInstance()) }
            if (currentMonthIndex != -1) {
                binding.dashboardCalendarViewPager.setCurrentItem(currentMonthIndex, false)
                updateMonthYearText(months[currentMonthIndex])
            }

            setupNavigation()
        }
    }

    private fun setupNavigation() {
        binding.dashboardCalendarViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateMonthYearText(months[position])
            }
        })

        binding.prevMonthButton.setOnClickListener {
            binding.dashboardCalendarViewPager.currentItem = binding.dashboardCalendarViewPager.currentItem - 1
        }

        binding.nextMonthButton.setOnClickListener {
            binding.dashboardCalendarViewPager.currentItem = binding.dashboardCalendarViewPager.currentItem + 1
        }
    }

    private fun updateMonthYearText(calendar: Calendar) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthYearText.text = sdf.format(calendar.time)
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
               this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }

    private fun Calendar.isSameMonth(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
               this.get(Calendar.MONTH) == other.get(Calendar.MONTH)
    }
}