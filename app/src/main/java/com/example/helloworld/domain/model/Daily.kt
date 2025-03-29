package com.example.helloworld.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dailies")
data class Daily(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val done: Boolean,
    val priority: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val recurringType: String = "",
    val recurringDays: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val notificationTime: String = "30",  // Default 30 minutes before
) {
}