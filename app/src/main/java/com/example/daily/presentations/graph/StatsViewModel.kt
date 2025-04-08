package com.example.daily.presentations.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily.domain.useCases.DailiesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dailiesUseCases: DailiesUseCases
) : ViewModel() {

    private val _statsData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statsData: StateFlow<Map<String, Int>> = _statsData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true

            dailiesUseCases.getDailies().collect { dailiesList ->
                val completedByDate = dailiesList
                    .filter { daily -> daily.done }
                    .groupBy { daily -> daily.date }
                    .mapValues { entry -> entry.value.size }

                _statsData.value = completedByDate
                _isLoading.value = false
            }
        }
    }
}