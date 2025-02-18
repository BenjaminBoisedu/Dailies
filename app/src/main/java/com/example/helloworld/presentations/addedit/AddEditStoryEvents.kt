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
}
