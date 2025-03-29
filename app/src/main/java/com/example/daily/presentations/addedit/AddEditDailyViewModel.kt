package com.example.daily.presentations.addedit


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily.domain.useCases.DailiesUseCases
import com.example.daily.presentations.PriorityType
import com.example.daily.presentations.list.DailyVM
import com.example.daily.presentations.list.toEntity
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
            is AddEditDailyEvent.RecurringDaysSelected -> {
                _daily.value = _daily.value.copy(recurringDays = event.days)
            }

            is AddEditDailyEvent.LocationSelected -> TODO()
            is AddEditDailyEvent.NotificationTimeSelected -> {
                _daily.value = _daily.value.copy(notificationTime = event.time)
            }

            is AddEditDailyEvent.RecurringDaysChanged -> {
                val day = event.day  // Day name (e.g., "Lun", "Mar")

                // Get current list of selected days
                val currentDays = _daily.value.recurringDays?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()

                // Toggle the day (add if not present, remove if present)
                if (currentDays.contains(day)) {
                    currentDays.remove(day)
                } else {
                    currentDays.add(day)
                }

                // Update the view model state with the new comma-separated list
                _daily.value = _daily.value.copy(
                    recurringDays = currentDays.joinToString(",")
                )
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