package com.example.daily.presentations.list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily.domain.model.Daily
import com.example.daily.domain.useCases.DailiesUseCases
import com.example.daily.presentations.PriorityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.Locale

@HiltViewModel
class ListDailiesViewModel @Inject constructor(
    private val dailiesUseCases: DailiesUseCases
) : ViewModel() {
    // Listes séparées pour les tâches complétées et non complétées
    private val _completedDailies: MutableState<List<DailyVM>> = mutableStateOf(emptyList())
    val completedDailies: State<List<DailyVM>> = _completedDailies

    private val _pendingDailies: MutableState<List<DailyVM>> = mutableStateOf(emptyList())
    val pendingDailies: State<List<DailyVM>> = _pendingDailies

    // Conserver cette propriété pour la compatibilité
    private val _dailies: MutableState<List<DailyVM>> = mutableStateOf(emptyList())
    var dailies: State<List<DailyVM>> = _dailies

    private var job: Job? = null

    init {
        loadDailies()
    }

    private fun loadDailies() {
        job?.cancel()
        job = dailiesUseCases.getDailies().onEach { dailies ->
            val allDailies = dailies.map { DailyVM.fromEntity(it) }

            // Définir un comparateur qui combine priorité et date
            val sortByPriorityAndDate = compareByDescending<DailyVM> { daily ->
                daily.priority?.toInt() ?: Int.MIN_VALUE
            }.thenByDescending { daily ->
                try {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.parse(daily.date)?.time ?: 0
                } catch (e: Exception) {
                    0
                }
            }

            // Appliquer le tri aux deux listes
            _pendingDailies.value = allDailies
                .filter { !it.done }
                .sortedWith(sortByPriorityAndDate)

            _completedDailies.value = allDailies
                .filter { it.done }
                .sortedWith(sortByPriorityAndDate)

            // Maintenir la liste complète pour la compatibilité
            _dailies.value = _pendingDailies.value + _completedDailies.value
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
    val recurringType: String? = "", // "daily", "weekly", "monthly"
    val recurringDays: List<String>,// Utilisation d'une liste de chaînes
    val notificationTime: String? = "30" // Default 30 minutes before

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
                recurringDays = entity.recurringDays?.split(",") ?: emptyList(),
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
        recurringType = this.recurringType.toString(),
        isRecurring = this.isRecurring,
        recurringDays = this.recurringDays.toString(),
        notificationTime = this.notificationTime.toString()
    )
}