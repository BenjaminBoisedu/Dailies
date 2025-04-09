package com.example.daily.presentations.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily.domain.model.Daily
import com.example.daily.domain.useCases.DailiesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dailiesUseCases: DailiesUseCases
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _statsData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statsData: StateFlow<Map<String, Int>> = _statsData

    private val _incompleteDailies = MutableStateFlow<List<Daily>>(emptyList())
    val incompleteDailies: StateFlow<List<Daily>> = _incompleteDailies


    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true

            dailiesUseCases.getDailies().collect { dailiesList ->
                val completedByDate = dailiesList
                    .filter { daily -> daily.done }
                    .groupBy { daily -> daily.DateDone ?: "Non défini" }
                    .mapValues { entry -> entry.value.size }

                _statsData.value = completedByDate
                _incompleteDailies.value = dailiesList.filter { !it.done }
                _isLoading.value = false
            }
        }
    }

    fun fixMissingDates() {
        viewModelScope.launch {
            _isLoading.value = true

            // Utiliser la méthode ajoutée dans UpsertDailyUseCase
            dailiesUseCases.upsertDaily.fixExistingCompletedDailies()

            // Recharger les stats après correction
            loadStats()
        }
    }
}