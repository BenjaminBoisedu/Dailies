package com.example.daily.presentations.addedit


import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily.domain.useCases.DailiesUseCases
import com.example.daily.presentations.PriorityType
import com.example.daily.presentations.addedit.AddEditDailyUiEvent.*
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

    private val _daily = mutableStateOf(DailyVM(
        id = -1,
        title = "",
        description = "",
        done = false,
        priority = null,
        date = "",
        time = "",
        latitude = null,
        longitude = null,
        locationName = null,
        isRecurring = false,
        recurringType = null,
        recurringDays = emptyList(),
        notificationTime = null
    ))
    val daily: State<DailyVM> = _daily

    private val _eventFlow = MutableSharedFlow<AddEditDailyUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val dailyId = savedStateHandle.get<Int>("dailyId") ?: -1
        if (dailyId != -1) {
            viewModelScope.launch {
                dailiesUseCases.editDaily.getDaily(dailyId).collect { daily ->
                    if (daily != null) {
                        // Convertir recurringDays de String à List<String> en gérant le cas null
                        val recurringDaysList = daily.recurringDays?.split(",")?.filter { day -> day.isNotEmpty() } ?: emptyList()

                        _daily.value = DailyVM(
                            id = daily.id ?: -1,
                            title = daily.title,
                            description = daily.description,
                            date = daily.date,
                            time = daily.time,
                            priority = daily.priority.toPriorityType(),
                            done = daily.done,
                            latitude = daily.latitude,
                            longitude = daily.longitude,
                            locationName = daily.locationName,
                            isRecurring = daily.isRecurring,
                            recurringType = daily.recurringType,
                            recurringDays = recurringDaysList,
                            notificationTime = daily.notificationTime
                        )
                    } else {
                        // Gérer le cas où daily est null (routine supprimée)
                        Log.d(TAG, "Daily avec ID $dailyId non trouvée (peut-être supprimée)")
                    }
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

                // Vérifier et mettre à jour isRecurring si nécessaire
                val currentDaily = _daily.value
                val updatedDaily = if (currentDaily.recurringDays.isNotEmpty() && currentDaily.recurringType != null) {
                    // Si on a des jours récurrents et un type de récurrence, isRecurring doit être true
                    currentDaily.copy(isRecurring = true)
                } else if (currentDaily.isRecurring && (currentDaily.recurringDays.isEmpty() || currentDaily.recurringType == null)) {
                    // Si isRecurring est true mais sans jours ou type, le réinitialiser
                    currentDaily.copy(isRecurring = false, recurringType = null, recurringDays = emptyList())
                } else {
                    currentDaily
                }

                // Mettre à jour l'état si changé
                if (updatedDaily != currentDaily) {
                    _daily.value = updatedDaily
                }

                Log.d(TAG, "Détails complets de la daily à sauvegarder: \n" +
                        "ID: ${updatedDaily.id}\n" +
                        "Titre: ${updatedDaily.title}\n" +
                        "Description: ${updatedDaily.description}\n" +
                        "Date: ${updatedDaily.date}\n" +
                        "Heure: ${updatedDaily.time}\n" +
                        "Priorité: ${updatedDaily.priority}\n" +
                        "Terminée: ${updatedDaily.done}\n" +
                        "Est récurrente: ${updatedDaily.isRecurring}\n" +
                        "Type de récurrence: ${updatedDaily.recurringType}\n" +
                        "Jours de récurrence: ${updatedDaily.recurringDays}\n" +
                        "Heure de notification: ${updatedDaily.notificationTime}")
                viewModelScope.launch {
                    if (daily.value.title.isEmpty() || daily.value.description?.isEmpty() != false ||
                        daily.value.date.isEmpty() || daily.value.time.isEmpty() ||
                        daily.value.priority == null
                    ) {
                        Log.e(TAG, "Échec de sauvegarde: champs obligatoires non remplis")
                        _eventFlow.emit(ShowMessage("Unable to save daily"))
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
                        _eventFlow.emit(SavedDaily)
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
            is AddEditDailyEvent.RecurringTypeSelected -> {
                // Si il y a plusieurs jours de récurrence alors on est en weekly
                // sinon on est en daily
                val type = if (_daily.value.recurringDays.size > 1) "weekly" else "daily"
                Log.d(TAG, "Type de récurrence déterminé automatiquement: $type")

                // Mettre à jour le type de récurrence et définir isRecurring comme true
                _daily.value = _daily.value.copy(
                    recurringType = type,
                    isRecurring = true  // Mettre à true quand un type est sélectionné
                )
            }
            is AddEditDailyEvent.RecurringDaysSelected -> {
                Log.d(TAG, "Jours de récurrence sélectionnés: ${event.days}")
                // Supposons que event.days est également mis à jour pour être une List<String>
                _daily.value = _daily.value.copy(recurringDays = event.days)
            }

            is AddEditDailyEvent.LocationSelected -> {
                Log.d(TAG, "Localisation sélectionnée: latitude=${event.latitude}, longitude=${event.longitude}, nom=${event.locationName}")
                _daily.value = _daily.value.copy(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    locationName = event.locationName
                )
            }
            is AddEditDailyEvent.NotificationTimeSelected -> {
                Log.d(TAG, "Heure de notification sélectionnée: ${event.time}")
                _daily.value = _daily.value.copy(notificationTime = event.time)
            }

            is AddEditDailyEvent.RecurringDaysChanged -> {
                val day = event.day
                Log.d(TAG, "Jour récurrent modifié: $day")

                // Créer une nouvelle liste à partir de la liste actuelle
                // Assurez-vous qu'elle soit plate (pas de liste imbriquée)
                val currentDays = _daily.value.recurringDays.toMutableList()
                Log.d(TAG, "Jours récurrents actuels (avant modification): $currentDays")

                // Ajouter ou retirer le jour (toggle)
                if (currentDays.contains(day)) {
                    currentDays.remove(day)
                    Log.d(TAG, "Jour retiré: $day")
                } else {
                    // Vérifier qu'il n'existe pas déjà avant d'ajouter
                    if (!currentDays.contains(day)) {
                        currentDays.add(day)
                        Log.d(TAG, "Jour ajouté: $day")
                    }
                }

                // Trier les jours selon l'ordre de la semaine
                val daysOrder = mapOf("Lu" to 0, "Ma" to 1, "Me" to 2, "Je" to 3, "Ve" to 4, "Sa" to 5, "Di" to 6)

                // Créer une nouvelle liste plate et triée
                val sortedDays = currentDays
                    .filter { it.length <= 2 } // Filtrer les éléments valides (pour éviter les listes imbriquées)
                    .distinct() // Éliminer les doublons
                    .sortedBy { daysOrder[it] ?: Int.MAX_VALUE }

                Log.d(TAG, "Liste triée et nettoyée: $sortedDays")

                // Déterminer le type de récurrence en fonction du nombre de jours
                val recurringType = if (sortedDays.size > 1) "weekly" else "daily"

                // Mettre à jour l'état avec la nouvelle liste
                _daily.value = _daily.value.copy(
                    recurringDays = sortedDays,
                    recurringType = recurringType,
                    isRecurring = sortedDays.isNotEmpty()
                )

                Log.d(TAG, "État final des jours récurrents: ${_daily.value.recurringDays}")
            }

            is AddEditDailyEvent.DailyRecurringChanged -> {
                Log.d(TAG, "État de récurrence modifié: ${event.isRecurring}")
                _daily.value = _daily.value.copy(isRecurring = event.isRecurring)
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