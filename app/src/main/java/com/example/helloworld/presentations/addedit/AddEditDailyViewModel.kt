package com.example.helloworld.presentations.addedit


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.domain.useCases.DailiesUseCases
import com.example.helloworld.presentations.PriorityType
import com.example.helloworld.presentations.list.DailyVM
import com.example.helloworld.presentations.list.toEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditDailyViewModel @Inject constructor(
    private val dailiesUseCases: DailiesUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _daily = mutableStateOf(DailyVM())
    val daily: State<DailyVM> = _daily

    private val _eventFlow = MutableSharedFlow<AddEditDailyUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val dailyId = savedStateHandle.get<Int>("dailyId") ?: -1
        if (dailyId != -1) {
            viewModelScope.launch {
                dailiesUseCases.editDaily.getDaily(dailyId).collect { daily ->
                    _daily.value = DailyVM(
                        id = daily.id ?: -1,
                        title = daily.title,
                        description = daily.description,
                        date = daily.date,
                        time = daily.time,
                        priority = daily.priority.toPriorityType(),
                        done = daily.done
                    )
                }
            }
        }
    }

    fun onEvent(event: AddEditDailyEvent) {
        when (event) {
            is AddEditDailyEvent.EnteredTitle -> {
                _daily.value = _daily.value.copy(title = event.title)
            }
            is AddEditDailyEvent.EnteredDescription -> {
                _daily.value = _daily.value.copy(description = event.description)
            }
            is AddEditDailyEvent.TimeSelected -> {
                _daily.value = _daily.value.copy(time = event.time)
            }
            is AddEditDailyEvent.DateSelected -> {
                _daily.value = _daily.value.copy(date = event.date)
            }
            AddEditDailyEvent.DailyDone -> {
                _daily.value = _daily.value.copy(done = !_daily.value.done)
            }
            is AddEditDailyEvent.PrioritySelected -> {
                _daily.value = _daily.value.copy(priority = event.priority)
            }
            AddEditDailyEvent.SaveDaily -> {
                viewModelScope.launch {
                    if (daily.value.title.isEmpty() || daily.value.description?.isEmpty() != false ||
                        daily.value.date.isEmpty() || daily.value.time.isEmpty() ||
                        daily.value.priority == null
                    ) {
                        _eventFlow.emit(AddEditDailyUiEvent.ShowMessage("Unable to save daily"))
                        return@launch
                    }

                    daily.value.toEntity().let { entity ->
                        withContext(Dispatchers.IO) {
                            if (entity.id != null) {
                                dailiesUseCases.editDaily.updateDaily(entity)
                            } else {
                                dailiesUseCases.upsertDaily(entity)
                            }
                        }
                        _eventFlow.emit(AddEditDailyUiEvent.SavedDaily)
                    }
                }
            }

           is AddEditDailyEvent.DailyRecurringChanged -> {
                _daily.value = _daily.value.copy(isRecurring = event.isRecurring)
            }
            is AddEditDailyEvent.RecurringTypeSelected -> {
                _daily.value = _daily.value.copy(recurringType = event.type)
            }

            is AddEditDailyEvent.LocationSelected -> TODO()
            is AddEditDailyEvent.NotificationTimeSelected -> {
                _daily.value = _daily.value.copy(notificationTime = event.time)
            }
        }
    }
}
sealed interface AddEditDailyUiEvent {
    data class ShowMessage(val message: String) : AddEditDailyUiEvent
    data object SavedDaily : AddEditDailyUiEvent
}
private fun Int.toPriorityType(): PriorityType {
    return PriorityType.fromInt(this)
}