package com.example.helloworld.presentations.list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.domain.model.Story
import com.example.helloworld.domain.useCases.StoriesUseCases
import com.example.helloworld.presentations.PriorityType
import com.example.helloworld.presentations.StandardPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ListStoriesViewModel @Inject constructor(
    private val storiesUseCases: StoriesUseCases
) : ViewModel() {
    private val _stories: MutableState<List<StoryVM>> = mutableStateOf(emptyList())
    var stories: State<List<StoryVM>> = _stories
    private var job: Job? = null

    init {
       loadStories()
    }

    private fun loadStories() {
        job?.cancel()
        job = storiesUseCases.getStories().onEach { stories ->
            _stories.value = stories.map { StoryVM.fromEntity(it) }
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
    val time: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val isRecurring: Boolean = false,
    val recurringType: String = "", // "daily", "weekly", "monthly"
    val recurringInterval: Int = 0, // 0 for daily, 1 for weekly, 2 for monthly
    val notificationTime: String = "30" // Default 30 minutes before

){
    companion object {
        fun fromEntity(entity: Story): StoryVM {
            return StoryVM(
                id = entity.id!!,
                title = entity.title,
                description = entity.description,
                done = entity.done,
                priority = PriorityType.fromInt(entity.priority),
                date = entity.date,
                time = entity.time,
                latitude = entity.latitude,
                longitude = entity.longitude,
                locationName = entity.locationName,
                isRecurring = entity.isRecurring,
                recurringType = entity.recurringType,
                recurringInterval = entity.recurringInterval,
                notificationTime = entity.notificationTime
            )
        }
    }
}

fun StoryVM.toEntity() : Story? {
    val id = if (this.id == -1) null else this.id
    return this.description?.let {
        priority?.let { it1 ->
            Story(
                id = id,
                title = this.title,
                description = it,
                done = this.done,
                priority = it1.toInt(),
                date = this.date,
                time = this.time,
                latitude = this.latitude,
                longitude = this.longitude,
                locationName = this.locationName,
                recurringType = this.recurringType,
                recurringInterval = this.recurringInterval,
                isRecurring = this.isRecurring,
                notificationTime = this.notificationTime

            )
        }
    }
}