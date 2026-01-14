package com.nexadev.perioddiary.data.database

import androidx.room.TypeConverter
import java.util.Calendar

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            cal
        }
    }

    @TypeConverter
    fun dateToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }
}