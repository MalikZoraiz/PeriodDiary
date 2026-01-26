package com.nexadev.perioddiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "period_entry")
data class PeriodEntry(
    @PrimaryKey
    var date: Date = Date(),
    var type: String = ""
)
