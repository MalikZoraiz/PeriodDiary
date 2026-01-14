package com.nexadev.perioddiary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nexadev.perioddiary.R
import java.util.Calendar

class DashboardMonthAdapter(
    private val context: Context,
    private val months: List<Calendar>,
    private val loggedPeriodDates: List<Calendar>,
    private val predictedPeriodDates: List<Calendar>,
    private val fertileDates: List<Calendar>,
    private val ovulationDates: List<Calendar>
) : RecyclerView.Adapter<DashboardMonthAdapter.MonthViewHolder>() {

    inner class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvDays: RecyclerView = view.findViewById(R.id.rvDays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_month_horizontal, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthCalendar = months[position]

        holder.rvDays.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.rvDays.adapter = DashboardDayAdapter(
            context,
            monthCalendar,
            loggedPeriodDates,
            predictedPeriodDates,
            fertileDates,
            ovulationDates
        )
        holder.rvDays.isNestedScrollingEnabled = false
    }

    override fun getItemCount() = months.size
}