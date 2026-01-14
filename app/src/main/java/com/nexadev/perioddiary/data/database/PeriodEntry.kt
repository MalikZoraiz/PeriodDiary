package com.nexadev.perioddiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "period_entries")
data class PeriodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Calendar,
    val endDate: Calendar
)