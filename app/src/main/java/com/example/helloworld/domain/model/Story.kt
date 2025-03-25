package com.example.helloworld.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class Story (
    @PrimaryKey(autoGenerate = true) val id : Int? = null,
    val title : String,
    val description : String,
    val done : Boolean,
    val priority : Int,
    val date : String,
    val time : String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
)