package com.example

import android.app.Application
import android.content.Context
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

    private fun getCupraBackgroundPath(context: Context): String? {
        return try {
            val file = java.io.File(context.filesDir, "bg_cupra.jpg")
            if (!file.exists()) {
                context.assets.open("bg_cupra.jpg").use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TrackerRepository(database.trackerDao())
        
        allTrackers = repository.allTrackers
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Prepopulate database with default items only once on first run
        viewModelScope.launch {
            try {
                val prefs = application.getSharedPreferences("percentify_prefs", Context.MODE_PRIVATE)
                val cupraBgPath = getCupraBackgroundPath(application)
                
                // Migrate or set up default items
                val hasCupraDefault = prefs.getBoolean("has_cupra_default_v2", false)
                if (!hasCupraDefault) {
                    val list = repository.allTrackers.first()
                    val oldFitness = list.find { it.label == "Fitness Reps" }
                    if (oldFitness != null) {
                        repository.update(
                            oldFitness.copy(
                                label = "Cupra",
                                value = 85,
                                style = WidgetStyle.SOLID_FILL.name,
                                color = WidgetColor.BRONZE.label,
                                bgPath = cupraBgPath
                            )
                        )
                    } else if (list.none { it.label == "Cupra" } && !prefs.getBoolean("has_prepopulated_defaults", false)) {
                        repository.insert(
                            Tracker(
                                label = "Cupra",
                                value = 85,
                                style = WidgetStyle.SOLID_FILL.name,
                                color = WidgetColor.BRONZE.label,
                                bgPath = cupraBgPath
                            )
                        )
                    }
                    prefs.edit().putBoolean("has_cupra_default_v2", true).apply()
                }

                val hasPrepopulated = prefs.getBoolean("has_prepopulated_defaults", false)
                if (!hasPrepopulated) {
                    val list = repository.allTrackers.first()
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
                                label = "Cupra",
                                value = 85,
                                style = WidgetStyle.SOLID_FILL.name,
                                color = WidgetColor.BRONZE.label,
                                bgPath = cupraBgPath
                            )
                        )
                    }
                    prefs.edit().putBoolean("has_prepopulated_defaults", true).apply()
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
