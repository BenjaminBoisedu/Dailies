package com.example.helloworld.presentations.addedit


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.helloworld.presentations.list.StoryVM
import com.example.helloworld.utils.addOrUpdateStory
import com.example.helloworld.utils.findStory

class AddEditStoryViewModel(storyId: Int = -1) : ViewModel() {

    private val _story = mutableStateOf(StoryVM())
    val story : State<StoryVM> = _story

    init {
        _story.value = findStory(storyId) ?: StoryVM()
    }

    fun onEvent(event: AddEditStoryEvent) {
        when (event) {
            is AddEditStoryEvent.EnteredTitle -> {
                _story.value = _story.value.copy(title = event.title)
            }

            is AddEditStoryEvent.EnteredDescription -> {
                _story.value = _story.value.copy(description = event.description)
            }
            is AddEditStoryEvent.TimeSelected -> {
                _story.value = _story.value.copy(time = event.time)
            }
            is AddEditStoryEvent.DateSelected -> {
                _story.value = _story.value.copy(date = event.date)
            }
            AddEditStoryEvent.StoryDone ->
                _story.value = _story.value.copy(done = !_story.value.done)
            AddEditStoryEvent.SaveStory -> {
                addOrUpdateStory(_story.value)
                println("Story saved")

            }
            is AddEditStoryEvent.PrioritySelected -> {
                _story.value = _story.value.copy(priority = event.priority)
            }
        }
    }
}