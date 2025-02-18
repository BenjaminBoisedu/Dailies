package com.example.helloworld.presentations.list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.presentations.PriorityType
import com.example.helloworld.presentations.StandardPriority
import com.example.helloworld.utils.findStory
import com.example.helloworld.utils.getStories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.sql.Date
import kotlin.random.Random

class ListStoriesViewModel() : ViewModel()  {
    private val _stories : MutableState<List<StoryVM>> = mutableStateOf(emptyList())
    var stories: State<List<StoryVM>> = _stories

    init {
       loadStories()
    }

    private fun loadStories() {
        getStories().onEach { stories ->
            _stories.value = stories
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: StoryEvent) {
        when(event) {
            is StoryEvent.Delete ->
            { deleteStory(event.story) }

            is StoryEvent.Edit ->
            {
                _stories.value = _stories.value.map {
                    if (it.id == event.story.id) {
                        event.story
                    } else {
                        it
                    }
                }
            }
            is StoryEvent.Detail ->
            { detailStory(event.story) }

        }
    }

    fun deleteStory(story: StoryVM) {
        _stories.value = _stories.value.filter { it != story }
    }

    private fun detailStory(story: StoryVM) {
        _stories.value = _stories.value.filter { it != story }
    }
}

data class StoryVM(
    val id: Int = Random.nextInt(),
    val title: String = "",
    val description: String? = "",
    val done: Boolean = false,
    val priority: PriorityType ?= StandardPriority,
    val date: String = "",
    val time: String = ""

)