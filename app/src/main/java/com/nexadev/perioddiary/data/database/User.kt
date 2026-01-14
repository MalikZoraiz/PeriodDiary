package com.nexadev.perioddiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val backupEmail: String? = null,
    val lastBackupDate: Calendar? = null // New field for last backup date
)