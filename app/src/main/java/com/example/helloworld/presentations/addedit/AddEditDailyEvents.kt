package com.example.helloworld.presentations.addedit

import com.example.helloworld.presentations.PriorityType


sealed interface AddEditDailyEvent {
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
    data class
}
