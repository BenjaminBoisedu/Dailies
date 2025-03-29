package com.example.helloworld.presentations.list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.domain.model.Daily
import com.example.helloworld.domain.useCases.DailiesUseCases
import com.example.helloworld.presentations.PriorityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ListDailiesViewModel @Inject constructor(
    private val dailiesUseCases: DailiesUseCases
) : ViewModel() {
    private val _dailies: MutableState<List<DailyVM>> = mutableStateOf(emptyList())
    var dailies: State<List<DailyVM>> = _dailies
    private var job: Job? = null

    init {
       loadDailies()
    }

    private fun loadDailies() {
        job?.cancel()
        job = dailiesUseCases.getDailies().onEach { dailies ->
            _dailies.value = dailies.map { DailyVM.fromEntity(it) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: DailyEvent) {
        when(event) {
            is DailyEvent.Delete ->
            { deleteDaily(event.daily) }

            is DailyEvent.Edit ->
            {
                _dailies.value = _dailies.value.map {
                    if (it.id == event.daily.id) {
                        event.daily
                    } else {
                        it
                    }
                }
                saveDailyToDatabase(event.daily)
            }
            is DailyEvent.Detail ->
            { detailDaily(event.daily) }

        }
    }
    private fun saveDailyToDatabase(daily: DailyVM) {
        daily.toEntity().let { entity ->
            viewModelScope.launch {
                dailiesUseCases.upsertDaily(entity)
            }
        }
    }

    fun deleteDaily(daily: DailyVM) {
        daily.toEntity().let { entity ->
            viewModelScope.launch {
                try {
                    val isDeleted = dailiesUseCases.deleteDaily(entity)
                    if (isDeleted) {
                        _dailies.value = _dailies.value.filter { it.id != daily.id }
                    }
                } catch (e: Exception) {
                    // Handle the error if needed
                    println("Error deleting daily: ${e.message}")
                }
            }
        }
    }


    private fun detailDaily(daily: DailyVM) {
        _dailies.value = _dailies.value.filter { it != daily }
    }
}

data class DailyVM(
    val id: Int = Random.nextInt(),
    val title: String = "",
    val description: String? = "",
    val done: Boolean = false,
    val priority: PriorityType? = null,
    val date: String = "",
    val time: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val isRecurring: Boolean = false,
    val recurringType: String = "", // "daily", "weekly", "monthly"
    val recurringDays: String? = null, // Stored as comma-separated values
    val notificationTime: String = "30" // Default 30 minutes before

){
    companion object {
        fun fromEntity(entity: Daily): DailyVM {
            return DailyVM(
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
                recurringDays = entity.recurringDays?.split(",")?.filter { it.isNotEmpty() }.toString(),
                notificationTime = entity.notificationTime
            )
        }
    }
}

fun DailyVM.toEntity(): Daily {
    val id = if (this.id == -1) null else this.id
    return Daily(
        id = id,
        title = this.title,
        description = this.description ?: "", // Valeur par défaut si null
        done = this.done,
        priority = this.priority?.toInt() ?: 0, // Valeur par défaut si null
        date = this.date,
        time = this.time,
        latitude = this.latitude,
        longitude = this.longitude,
        locationName = this.locationName,
        recurringType = this.recurringType,
        isRecurring = this.isRecurring,
        recurringDays = this.recurringDays,
        notificationTime = this.notificationTime
    )
}