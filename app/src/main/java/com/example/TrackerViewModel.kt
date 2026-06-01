package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TrackerRepository

    val allTrackers: StateFlow<List<Tracker>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TrackerRepository(database.trackerDao())
        
        allTrackers = repository.allTrackers
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Prepopulate database with default items if empty to help showcase UX on first launch
        viewModelScope.launch {
            try {
                val list = repository.allTrackers.first()
                // Clean up the third default item (Financial Goal) if it exists from previous installations
                list.find { it.label == "Financial Goal" && it.value == 85 }?.let { oldDefault ->
                    repository.delete(oldDefault)
                }
                if (list.isEmpty()) {
                    repository.insert(
                        Tracker(
                            label = "Daily Water Intake",
                            value = 75,
                            style = WidgetStyle.WHEEL.name,
                            color = WidgetColor.EMERALD.label,
                            bgPath = null
                        )
                    )
                    repository.insert(
                        Tracker(
                            label = "Fitness Reps",
                            value = 40,
                            style = WidgetStyle.CORNER_CIRCLE.name,
                            color = WidgetColor.CORAL.label,
                            bgPath = null
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTracker(label: String, value: Int, style: WidgetStyle, color: WidgetColor, bgPath: String?) = viewModelScope.launch {
        repository.insert(
            Tracker(
                label = label,
                value = value,
                style = style.name,
                color = color.label,
                bgPath = bgPath,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun updateTracker(tracker: Tracker) = viewModelScope.launch {
        repository.update(tracker)
    }

    fun deleteTracker(tracker: Tracker) = viewModelScope.launch {
        repository.delete(tracker)
    }

    fun updateTrackerValue(tracker: Tracker, newValue: Int) = viewModelScope.launch {
        val updated = tracker.copy(
            value = newValue.coerceIn(0, 100)
        )
        repository.update(updated)
    }
}

class TrackerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
