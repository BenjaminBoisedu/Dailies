package com.example.daily.data.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>): String? {
        return if (list.isEmpty()) null else list.joinToString(",")
    }
}