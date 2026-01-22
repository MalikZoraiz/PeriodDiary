package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.nexadev.perioddiary.adapter.CycleInfo
import com.nexadev.perioddiary.adapter.CycleInfoAdapter
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
    private val snapHelper = PagerSnapHelper()

    private val editPeriodResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            months.clear()
            loadDataAndSetupUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadDataAndSetupUI()

        binding.editPeriodButton.setOnClickListener {
            val intent = Intent(this, EditPeriodActivity::class.java)
            editPeriodResultLauncher.launch(intent)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDataAndSetupUI() {
        lifecycleScope.launch {
            val periodEntries = AppDatabase.getDatabase(applicationContext).periodEntryDao().getAllPeriodEntriesList()

            // --- Calendar Date Range & Empty State UI ---
            val startCalendar: Calendar
            if (periodEntries.isEmpty()) {
                binding.cycleInfoViewPager.visibility = View.GONE
                binding.cycleInfoDots.visibility = View.GONE
                startCalendar = Calendar.getInstance().apply { set(2020, Calendar.JANUARY, 1) }
            } else {
                binding.cycleInfoViewPager.visibility = View.VISIBLE
                binding.cycleInfoDots.visibility = View.VISIBLE
                startCalendar = periodEntries.minByOrNull { it.startDate.timeInMillis }!!.startDate.clone() as Calendar
            }
            startCalendar.set(Calendar.DAY_OF_MONTH, 1)

            val loggedPeriodDates = periodEntries.flatMap {
                val dates = mutableListOf<Calendar>()
                val current = it.startDate.clone() as Calendar
                while (current.before(it.endDate) || current.isSameDay(it.endDate)) {
                    dates.add(current.clone() as Calendar)
                    current.add(Calendar.DAY_OF_MONTH, 1)
                }
                dates
            }

            // --- Prediction, Fertile Window, and Countdown Logic ---
            val allPeriodDates = loggedPeriodDates.toMutableList()
            val fertileDates = mutableListOf<Calendar>()
            val ovulationDates = mutableListOf<Calendar>()
            var cycleDay = ""
            var daysUntil = ""

            if (periodEntries.isNotEmpty()) {
                val lastPeriod = periodEntries.first()
                val cycleLength = 28
                val periodLength = 5

                var nextPeriodStart = lastPeriod.startDate.clone() as Calendar
                for (i in 0 until 6) {
                    nextPeriodStart.add(Calendar.DAY_OF_MONTH, cycleLength)
                    for (j in 0 until periodLength) {
                        val predictedDate = nextPeriodStart.clone() as Calendar
                        predictedDate.add(Calendar.DAY_OF_MONTH, j)
                        allPeriodDates.add(predictedDate)
                    }
                }

                val nextCycleStart = lastPeriod.startDate.clone() as Calendar
                nextCycleStart.add(Calendar.DAY_OF_MONTH, cycleLength)
                val ovulationDay = nextCycleStart.clone() as Calendar
                ovulationDay.add(Calendar.DAY_OF_MONTH, -14)
                ovulationDates.add(ovulationDay)

                for (i in -3..2) {
                    val fertileDay = ovulationDay.clone() as Calendar
                    fertileDay.add(Calendar.DAY_OF_MONTH, i)
                    fertileDates.add(fertileDay)
                }

                val today = Calendar.getInstance()
                val currentPeriod = periodEntries.find { today.after(it.startDate) && today.before(it.endDate) || today.isSameDay(it.startDate) || today.isSameDay(it.endDate) }

                if (currentPeriod != null) {
                    val dayOfPeriod = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - currentPeriod.startDate.timeInMillis) + 1
                    daysUntil = dayOfPeriod.toString()
                    cycleDay = when (dayOfPeriod.toInt()) {
                        1 -> "st day of period"
                        2 -> "nd day of period"
                        3 -> "rd day of period"
                        else -> "th day of period"
                    }
                } else {
                    val nextUpcomingPeriod = allPeriodDates.firstOrNull { !it.before(today) }
                    if (nextUpcomingPeriod != null) {
                        val diff = nextUpcomingPeriod.timeInMillis - today.timeInMillis
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        daysUntil = days.toString()
                    }
                }

                val cycleStart = periodEntries.first().startDate
                val diff = today.timeInMillis - cycleStart.timeInMillis
                cycleDay = "Day ${TimeUnit.MILLISECONDS.toDays(diff) + 1} of $cycleLength"
            }
            
            val cycleInfoItems = mutableListOf<CycleInfo>()
            if (daysUntil.isNotEmpty()) {
                 cycleInfoItems.add(CycleInfo(daysUntil, "days until next period"))
            }
            if(cycleDay.isNotEmpty()) {
                cycleInfoItems.add(CycleInfo(cycleDay.split(" ").first(), cycleDay.substringAfter(" ")))
            }

            binding.cycleInfoViewPager.adapter = CycleInfoAdapter(cycleInfoItems)
            TabLayoutMediator(binding.cycleInfoDots, binding.cycleInfoViewPager) { _, _ -> }.attach()


            // --- Calendar Setup ---
            val endCalendar = Calendar.getInstance().apply { set(2030, Calendar.DECEMBER, 31) }
            val current = startCalendar.clone() as Calendar
            while (current.before(endCalendar)) {
                months.add(current.clone() as Calendar)
                current.add(Calendar.MONTH, 1)
            }

            monthAdapter = DashboardMonthAdapter(this@DashboardActivity, months, loggedPeriodDates, allPeriodDates, fertileDates, ovulationDates)
            binding.dashboardCalendarRecyclerView.adapter = monthAdapter
            binding.dashboardCalendarRecyclerView.layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            if (binding.dashboardCalendarRecyclerView.onFlingListener == null) {
                snapHelper.attachToRecyclerView(binding.dashboardCalendarRecyclerView)
            }

            val currentMonthIndex = months.indexOfFirst { it.isSameMonth(Calendar.getInstance()) }
            if (currentMonthIndex != -1) {
                (binding.dashboardCalendarRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(currentMonthIndex, 0)
                updateMonthYearText(months[currentMonthIndex])
            }

            setupNavigation()
        }
    }

    private fun setupNavigation() {
        binding.dashboardCalendarRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val snapView = snapHelper.findSnapView(layoutManager)
                    val snapPosition = snapView?.let { layoutManager.getPosition(it) } ?: RecyclerView.NO_POSITION
                    if (snapPosition != RecyclerView.NO_POSITION) {
                        updateMonthYearText(months[snapPosition])
                    }
                }
            }
        })

        binding.prevMonthButton.setOnClickListener {
            val layoutManager = binding.dashboardCalendarRecyclerView.layoutManager as LinearLayoutManager
            val snapView = snapHelper.findSnapView(layoutManager)
            val currentPosition = snapView?.let { layoutManager.getPosition(it) } ?: return@setOnClickListener
            if (currentPosition > 0) {
                binding.dashboardCalendarRecyclerView.smoothScrollToPosition(currentPosition - 1)
            }
        }

        binding.nextMonthButton.setOnClickListener {
            val layoutManager = binding.dashboardCalendarRecyclerView.layoutManager as LinearLayoutManager
            val snapView = snapHelper.findSnapView(layoutManager)
            val currentPosition = snapView?.let { layoutManager.getPosition(it) } ?: return@setOnClickListener
            if (currentPosition < monthAdapter.itemCount - 1) {
                binding.dashboardCalendarRecyclerView.smoothScrollToPosition(currentPosition + 1)
            }
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
