package com.example.daily.presentations.addedit


import android.util.Log
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

    private val TAG = "AddEditDailyViewModel"

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
                Log.d(TAG, "Titre modifié: ${event.title}")
                _daily.value = _daily.value.copy(title = event.title)
            }
            is AddEditDailyEvent.EnteredDescription -> {
                Log.d(TAG, "Description modifiée: ${event.description}")
                _daily.value = _daily.value.copy(description = event.description)
            }
            is AddEditDailyEvent.TimeSelected -> {
                Log.d(TAG, "Heure sélectionnée: ${event.time}")
                _daily.value = _daily.value.copy(time = event.time)
            }
            is AddEditDailyEvent.DateSelected -> {
                Log.d(TAG, "Date sélectionnée: ${event.date}")
                _daily.value = _daily.value.copy(date = event.date)
            }
            AddEditDailyEvent.DailyDone -> {
                val newDoneState = !_daily.value.done
                Log.d(TAG, "État de complétion modifié: $newDoneState")
                _daily.value = _daily.value.copy(done = newDoneState)
            }
            is AddEditDailyEvent.PrioritySelected -> {
                Log.d(TAG, "Priorité sélectionnée: ${event.priority}")
                _daily.value = _daily.value.copy(priority = event.priority)
            }
            AddEditDailyEvent.SaveDaily -> {
                Log.d(TAG, "Tentative de sauvegarde de la daily")
                Log.d(TAG, "Détails complets de la daily à sauvegarder: \n" +
                        "ID: ${daily.value.id}\n" +
                        "Titre: ${daily.value.title}\n" +
                        "Description: ${daily.value.description}\n" +
                        "Date: ${daily.value.date}\n" +
                        "Heure: ${daily.value.time}\n" +
                        "Priorité: ${daily.value.priority}\n" +
                        "Terminée: ${daily.value.done}\n" +
                        "Est récurrente: ${daily.value.isRecurring}\n" +
                        "Type de récurrence: ${daily.value.recurringType}\n" +
                        "Jours de récurrence: ${daily.value.recurringDays}\n" +
                        "Heure de notification: ${daily.value.notificationTime}")
                viewModelScope.launch {
                    if (daily.value.title.isEmpty() || daily.value.description?.isEmpty() != false ||
                        daily.value.date.isEmpty() || daily.value.time.isEmpty() ||
                        daily.value.priority == null
                    ) {
                        Log.e(TAG, "Échec de sauvegarde: champs obligatoires non remplis")
                        _eventFlow.emit(AddEditDailyUiEvent.ShowMessage("Unable to save daily"))
                        return@launch
                    }

                    daily.value.toEntity().let { entity ->
                        Log.d(TAG, "Sauvegarde de la daily: ${entity.title}, ID=${entity.id}, date=${entity.date}")
                        withContext(Dispatchers.IO) {
                            if (entity.id != null) {
                                Log.d(TAG, "Mise à jour de la daily existante (ID=${entity.id})")
                                dailiesUseCases.editDaily.updateDaily(entity)
                            } else {
                                Log.d(TAG, "Création d'une nouvelle daily")
                                dailiesUseCases.upsertDaily(entity)
                            }
                        }
                        Log.d(TAG, "Daily sauvegardée avec succès")
                        _eventFlow.emit(AddEditDailyUiEvent.SavedDaily)
                        Log.d(TAG, "Daily sauvegardée avec succès. Détails finaux: \n" +
                                "ID: ${entity.id}\n" +
                                "Titre: ${entity.title}\n" +
                                "Description: ${entity.description}\n" +
                                "Date: ${entity.date}\n" +
                                "Heure: ${entity.time}\n" +
                                "Priorité: ${entity.priority}\n" +
                                "Terminée: ${entity.done}\n" +
                                "Est récurrente: ${entity.isRecurring}\n" +
                                "Type de récurrence: ${entity.recurringType}\n" +
                                "Jours de récurrence: ${entity.recurringDays}\n" +
                                "Heure de notification: ${entity.notificationTime}")
                    }

                }
            }

            is AddEditDailyEvent.DailyRecurringChanged -> {
                Log.d(TAG, "Statut de récurrence modifié: ${event.isRecurring}")
                _daily.value = _daily.value.copy(isRecurring = event.isRecurring)
            }
            is AddEditDailyEvent.RecurringTypeSelected -> {
                Log.d(TAG, "Type de récurrence sélectionné: ${event.type}")
                _daily.value = _daily.value.copy(recurringType = event.type)
            }
            is AddEditDailyEvent.RecurringDaysSelected -> {
                Log.d(TAG, "Jours de récurrence sélectionnés: ${event.days}")
                _daily.value = _daily.value.copy(recurringDays = event.days)
            }

            is AddEditDailyEvent.LocationSelected -> {
                Log.d(TAG, "Sélection de localisation non implémentée")
                TODO()
            }
            is AddEditDailyEvent.NotificationTimeSelected -> {
                Log.d(TAG, "Heure de notification sélectionnée: ${event.time}")
                _daily.value = _daily.value.copy(notificationTime = event.time)
            }

            is AddEditDailyEvent.RecurringDaysChanged -> {
                val day = event.day
                Log.d(TAG, "Jour récurrent modifié: $day")

                // Get current list of selected days
                val currentDays = _daily.value.recurringDays?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
                Log.d(TAG, "Jours récurrents actuels: $currentDays")

                // Toggle the day (add if not present, remove if present)
                if (currentDays.contains(day)) {
                    currentDays.remove(day)
                    Log.d(TAG, "Jour retiré: $day")
                } else {
                    currentDays.add(day)
                    Log.d(TAG, "Jour ajouté: $day")
                }

                // Update the view model state with the new comma-separated list
                val newDaysList = currentDays.joinToString(",")
                Log.d(TAG, "Nouvelle liste de jours récurrents: $newDaysList")
                _daily.value = _daily.value.copy(
                    recurringDays = newDaysList
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