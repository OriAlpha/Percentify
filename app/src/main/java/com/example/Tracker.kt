package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackers")
data class Tracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val value: Int,
    val style: String,
    val color: String,
    val bgPath: String?,
    val timestamp: Long = System.currentTimeMillis()
)
