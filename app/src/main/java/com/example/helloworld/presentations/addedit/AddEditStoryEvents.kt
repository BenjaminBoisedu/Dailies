package com.example.helloworld.presentations.addedit

import com.example.helloworld.presentations.PriorityType


sealed interface AddEditStoryEvent {
    data class EnteredTitle(val title: String):
        AddEditStoryEvent
    data class EnteredDescription(val description: String):
        AddEditStoryEvent
    data object StoryDone: AddEditStoryEvent
    data class TimeSelected(val time: String): AddEditStoryEvent
    data class DateSelected(val date: String): AddEditStoryEvent
    data object SaveStory: AddEditStoryEvent
    data class PrioritySelected(val priority: PriorityType): AddEditStoryEvent
    data class StoryRecurringChanged(val isRecurring: Boolean) : AddEditStoryEvent
    data class RecurringTypeSelected(val type: String) : AddEditStoryEvent
    data class RecurringIntervalChanged(val interval: Int) : AddEditStoryEvent
    class NotificationTimeSelected(val time: Int) : AddEditStoryEvent
    data class LocationSelected(val latitude: Double, val longitude: Double, val locationName: String) : AddEditStoryEvent
}
