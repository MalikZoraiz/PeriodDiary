package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityDashboardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var monthAdapter: DashboardMonthAdapter
    private val months = mutableListOf<Calendar>()
    private val snapHelper = PagerSnapHelper()
    private var welcomeToastShown = false

    private val editPeriodResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // The Flow will automatically update the UI
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observePeriodData()

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

    private fun observePeriodData() {
        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()

        lifecycleScope.launch {
            userDao.getUserFlow().collectLatest { user ->
                periodEntryDao.getAllPeriodEntriesFlow().collectLatest { periodEntries ->
                    if (!welcomeToastShown) {
                        if (periodEntries.isNotEmpty()) {
                            val name = user?.name ?: ""
                            if (name.isNotEmpty()) {
                                Toast.makeText(this@DashboardActivity, "Welcome, $name!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@DashboardActivity, "Welcome!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@DashboardActivity, "Welcome! Add your period to get started", Toast.LENGTH_LONG).show()
                        }
                        welcomeToastShown = true
                    }
                    updateUiWithData(periodEntries)
                }
            }
        }
    }

    private fun updateUiWithData(periodEntries: List<PeriodEntry>) {
        // --- Calendar Date Range & Empty State UI ---
        val startCalendar: Calendar
        if (periodEntries.isEmpty()) {
            binding.cycleInfoViewPager.visibility = View.GONE
            binding.cycleInfoDots.visibility = View.GONE
            startCalendar = Calendar.getInstance().apply { set(2020, Calendar.JANUARY, 1) }
        } else {
            binding.cycleInfoViewPager.visibility = View.VISIBLE
            binding.cycleInfoDots.visibility = View.VISIBLE
            startCalendar = periodEntries.minByOrNull { it.date.time }!!.date.toCalendar()
        }
        startCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val loggedPeriodDates = periodEntries.map { it.date.toCalendar() }

        // --- Prediction, Fertile Window, and Countdown Logic ---
        val allPeriodDates = loggedPeriodDates.toMutableList()
        val fertileDates = mutableListOf<Calendar>()
        val ovulationDates = mutableListOf<Calendar>()
        var cycleDay = ""
        var daysUntil = ""

        if (periodEntries.isNotEmpty()) {
            val lastPeriodDate = periodEntries.maxByOrNull { it.date.time }!!.date
            val cycleLength = 28
            val periodLength = 5

            var nextPeriodStart = lastPeriodDate.toCalendar()
            for (i in 0 until 6) {
                nextPeriodStart.add(Calendar.DAY_OF_MONTH, cycleLength)
                for (j in 0 until periodLength) {
                    val predictedDate = nextPeriodStart.clone() as Calendar
                    predictedDate.add(Calendar.DAY_OF_MONTH, j)
                    allPeriodDates.add(predictedDate)
                }
            }

            val nextCycleStart = lastPeriodDate.toCalendar()
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
            val currentPeriodEntry = periodEntries.find { it.date.toCalendar().isSameDay(today) && it.type == "PERIOD_DAY" }

            if (currentPeriodEntry != null) {
                val periodStartDate = periodEntries.filter { it.type == "PERIOD_DAY" && it.date.time <= currentPeriodEntry.date.time }.minByOrNull { it.date.time }?.date
                if (periodStartDate != null) {
                    val dayOfPeriod = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - periodStartDate.time) + 1
                    daysUntil = dayOfPeriod.toString()
                    cycleDay = when (dayOfPeriod.toInt()) {
                        1 -> "st day of period"
                        2 -> "nd day of period"
                        3 -> "rd day of period"
                        else -> "th day of period"
                    }
                }
            } else {
                val nextUpcomingPeriod = allPeriodDates.firstOrNull { !it.before(today) }
                if (nextUpcomingPeriod != null) {
                    val diff = nextUpcomingPeriod.timeInMillis - today.timeInMillis
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    daysUntil = days.toString()
                }
            }

            val cycleStart = periodEntries.minByOrNull { it.date.time }?.date?.toCalendar()
            if (cycleStart != null) {
                val diff = today.timeInMillis - cycleStart.timeInMillis
                cycleDay = "Day ${TimeUnit.MILLISECONDS.toDays(diff) + 1} of $cycleLength"
            }
        }

        val cycleInfoItems = mutableListOf<CycleInfo>()
        if (daysUntil.isNotEmpty()) {
            cycleInfoItems.add(CycleInfo(daysUntil, "days until next period"))
        }
        if (cycleDay.isNotEmpty()) {
            cycleInfoItems.add(CycleInfo(cycleDay.split(" ").first(), cycleDay.substringAfter(" ")))
        }

        binding.cycleInfoViewPager.adapter = CycleInfoAdapter(cycleInfoItems)
        TabLayoutMediator(binding.cycleInfoDots, binding.cycleInfoViewPager) { _, _ -> }.attach()


        // --- Calendar Setup ---
        months.clear()
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

    private fun setupNavigation() {
        binding.dashboardCalendarRecyclerView.clearOnScrollListeners()
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

    private fun Date.toCalendar(): Calendar {
        return Calendar.getInstance().apply { time = this@toCalendar }
    }
}
