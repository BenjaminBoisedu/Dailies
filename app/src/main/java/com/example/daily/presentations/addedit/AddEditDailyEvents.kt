package com.example.daily.presentations.addedit

import com.example.daily.presentations.PriorityType


sealed interface AddEditDailyEvent {
    fun joinToString(): List<String>  = emptyList()

    data class EnteredTitle(val title: String) :
        AddEditDailyEvent

    data class EnteredDescription(val description: String) :
        AddEditDailyEvent

    data object DailyDone : AddEditDailyEvent
    data class TimeSelected(val time: String) : AddEditDailyEvent
    data class DateSelected(val date: String) : AddEditDailyEvent
    data object SaveDaily : AddEditDailyEvent
    data class PrioritySelected(val priority: PriorityType) : AddEditDailyEvent
    data class DailyRecurringChanged(val isRecurring: Boolean) : AddEditDailyEvent
    data class RecurringTypeSelected(val type: String) : AddEditDailyEvent
    class NotificationTimeSelected(val time: String) : AddEditDailyEvent
    data class LocationSelected(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    ) : AddEditDailyEvent
    data class RecurringDaysSelected(val days: String) : AddEditDailyEvent
    data class RecurringDaysChanged(val day: String) : AddEditDailyEvent
}
