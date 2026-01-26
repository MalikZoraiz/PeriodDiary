package com.nexadev.perioddiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "user") // Corrected table name to singular
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val backupEmail: String? = null,

    // This field seems to be causing issues with the Room TypeConverter.
    // For now, let's store it as a Long (timestamp) which Room can handle natively.
    val lastBackupTimestamp: Long? = null
)
