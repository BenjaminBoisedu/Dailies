package com.example.helloworld.presentations.addedit


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.data.source.StoriesDao
import com.example.helloworld.presentations.list.StoryVM
import com.example.helloworld.presentations.list.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEditStoryViewModel(val dao: StoriesDao, storyId: Int = -1) : ViewModel() {
    private val _story = mutableStateOf(StoryVM())
    val story : State<StoryVM> = _story

    private val _eventFlow = MutableSharedFlow<AddEditStoryUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            val storyEntity = withContext(Dispatchers.IO) {
                dao.getStory(storyId)
            }
            storyEntity?.let {
                _story.value = StoryVM.fromEntity(it)
            }
        }
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
                viewModelScope.launch {
                    if(story.value.title.isEmpty() || story.value.description?.isEmpty() != false ||
                        story.value.date.isEmpty() || story.value.time.isEmpty() ||
                        story.value.priority == null) {
                        _eventFlow.emit(AddEditStoryUiEvent.ShowMessage("Unable to save story"))
                        return@launch
                    }

                    story.value.toEntity()?.let { entity ->
                        withContext(Dispatchers.IO) {
                            dao.upsertStory(entity)
                        }
                        _eventFlow.emit(AddEditStoryUiEvent.SavedStory)
                    }
                }
            }
            is AddEditStoryEvent.PrioritySelected -> {
                _story.value = _story.value.copy(priority = event.priority)
            }
        }
    }
}

sealed interface AddEditStoryUiEvent {
    data class ShowMessage(val message: String) : AddEditStoryUiEvent
    data object SavedStory : AddEditStoryUiEvent
}