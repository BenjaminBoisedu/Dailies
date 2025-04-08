package com.example.daily.presentations.addedit

import android.location.Location
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
        notificationTime = "30" // Valeur par défaut à 30 minutes
    ))
    val daily: State<DailyVM> = _daily

    private val _eventFlow = MutableSharedFlow<AddEditDailyUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Variable pour suivre si l'utilisateur a choisi d'utiliser la localisation
    private val _useLocation = MutableStateFlow(false)
    val useLocation: StateFlow<Boolean> = _useLocation.asStateFlow()

    // Variable pour stocker la dernière position connue
    private val _currentLocation = mutableStateOf<Location?>(null)
    val currentLocation: State<Location?> = _currentLocation

    private fun updateLocationData(location: Location) {
        Log.d(TAG, "Mise à jour des données de localisation: ${location.latitude}, ${location.longitude}")
        _currentLocation.value = location
        _daily.value = _daily.value.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = "Position actuelle"
        )
    }

    init {
        val dailyId = savedStateHandle.get<Int>("dailyId") ?: -1
        Log.d(TAG, "Initialisation avec dailyId: $dailyId")

        if (dailyId != -1) {
            viewModelScope.launch {
                Log.d(TAG, "Chargement des données pour dailyId: $dailyId")
                dailiesUseCases.editDaily.getDaily(dailyId).collect { daily ->
                    if (true) {
                        // Convertir recurringDays de String à List<String> en gérant le cas null
                        val recurringDaysList = daily.recurringDays?.split(",")?.filter { day -> day.isNotEmpty() } ?: emptyList()

                        Log.d(TAG, "Données chargées avec succès: ${daily.title}")
                        Log.d(TAG, "Latitude: ${daily.latitude}, Longitude: ${daily.longitude}, Nom: ${daily.locationName}")
                        Log.d(TAG, "Jours récurrents: ${daily.recurringDays} -> $recurringDaysList")

                        // Si nous avons des coordonnées de localisation, activer l'option useLocation
                        _useLocation.value = daily.latitude != null && daily.longitude != null

                        // Si des coordonnées existent, créer une Location
                        if (daily.latitude != null && daily.longitude != null) {
                            val location = Location("DATABASE")
                            location.latitude = daily.latitude
                            location.longitude = daily.longitude
                            _currentLocation.value = location
                        }

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
        } else {
            Log.d(TAG, "Création d'une nouvelle daily")
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

            // Sauvegarde de la localisation
            is AddEditDailyEvent.LocationSelected -> {
                Log.d(TAG, "Localisation sélectionnée manuellement: latitude=${event.latitude}, longitude=${event.longitude}, nom=${event.locationName}")
                // Mettre à jour l'état useLocation en fonction des données reçues
                _useLocation.value = event.latitude != null && event.longitude != null
                Log.d(TAG, "Utilisation de la localisation: $useLocation")

                _daily.value = _daily.value.copy(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    locationName = event.locationName
                )
                Log.d(TAG, "Daily mise à jour avec localisation: lat=${_daily.value.latitude}, long=${_daily.value.longitude}")

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

                // Si l'utilisateur a activé l'utilisation de la localisation et que nous avons une localisation actuelle
                // Mettre à jour les coordonnées depuis le capteur
                val finalDaily = if (_useLocation.value && _currentLocation.value != null) {
                    val location = _currentLocation.value!!
                    Log.d(TAG, "Utilisation de la localisation du capteur pour la sauvegarde: " +
                            "Latitude=${String.format("%.6f", location.latitude)}, " +
                            "Longitude=${String.format("%.6f", location.longitude)}")

                    updatedDaily.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        locationName = "Position actuelle"
                    )
                } else if (!_useLocation.value) {
                    // Si l'utilisateur a désactivé la localisation, supprimer les coordonnées
                    Log.d(TAG, "Localisation désactivée, suppression des coordonnées")
                    updatedDaily.copy(
                        latitude = null,
                        longitude = null,
                        locationName = null
                    )
                } else {
                    updatedDaily
                }

                // Mettre à jour l'état si changé
                if (finalDaily != _daily.value) {
                    _daily.value = finalDaily
                }

                Log.d(TAG, "Détails complets de la daily à sauvegarder: \n" +
                        "ID: ${finalDaily.id}\n" +
                        "Titre: ${finalDaily.title}\n" +
                        "Description: ${finalDaily.description}\n" +
                        "Date: ${finalDaily.date}\n" +
                        "Heure: ${finalDaily.time}\n" +
                        "Priorité: ${finalDaily.priority}\n" +
                        "Terminée: ${finalDaily.done}\n" +
                        "Est récurrente: ${finalDaily.isRecurring}\n" +
                        "Type de récurrence: ${finalDaily.recurringType}\n" +
                        "Jours de récurrence: ${finalDaily.recurringDays}\n" +
                        "Heure de notification: ${finalDaily.notificationTime}\n" +
                        "Latitude: ${finalDaily.latitude}\n" +
                        "Longitude: ${finalDaily.longitude}\n" +
                        "Nom du lieu: ${finalDaily.locationName}")

                viewModelScope.launch {
                    if (finalDaily.title.isEmpty() || finalDaily.description?.isEmpty() != false ||
                        finalDaily.date.isEmpty() || finalDaily.time.isEmpty() ||
                        finalDaily.priority == null
                    ) {
                        Log.e(TAG, "Échec de sauvegarde: champs obligatoires non remplis")
                        _eventFlow.emit(ShowMessage("Unable to save daily"))
                        return@launch
                    }

                    finalDaily.toEntity().let { entity ->
                        Log.d(TAG, "Sauvegarde de la daily: ${entity.title}, ID=${entity.id}, date=${entity.date}")
                        Log.d(TAG, "Données de localisation: lat=${entity.latitude}, long=${entity.longitude}, nom=${entity.locationName}")

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
                    }
                }
            }

            is AddEditDailyEvent.NotificationTimeSelected -> {
                Log.d(TAG, "Heure de notification sélectionnée: ${event.time}")
                _daily.value = _daily.value.copy(notificationTime = event.time)
            }
            is AddEditDailyEvent.RecurringDaysChanged -> {
                val day = event.day
                Log.d(TAG, "Jour récurrent modifié: $day")

                // Créer une nouvelle liste à partir de la liste actuelle
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
                _daily.value = _daily.value.copy(recurringDays = event.days)
            }
        }
    }

    // Méthode pour mettre à jour l'état d'utilisation de la localisation
    fun setUseLocationState(useLocation: Boolean) {
        Log.d(TAG, "Changement du statut d'utilisation de la localisation: $useLocation")
        _useLocation.value = useLocation

        // Si on active la localisation et qu'on a déjà une position, la mettre à jour immédiatement
        if (useLocation && _currentLocation.value != null) {
            Log.d(TAG, "Mise à jour immédiate avec la localisation actuelle du capteur")
            updateLocationData(_currentLocation.value!!)
        }
        // Si on désactive la localisation, effacer les données de localisation
        else if (!useLocation) {
            Log.d(TAG, "Effacement des données de localisation")
            _daily.value = _daily.value.copy(
                latitude = null,
                longitude = null,
                locationName = null
            )
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