package com.example.deepworkai.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepworkai.models.UpdateWellnessRequest
import com.example.deepworkai.models.WellnessLog
import com.example.deepworkai.network.NetworkPreferences
import com.example.deepworkai.network.WellnessService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WellnessViewModel : ViewModel() {
    private val wellnessService = WellnessService()

    private val _wellnessLog = mutableStateOf<WellnessLog?>(null)
    val wellnessLog: State<WellnessLog?> = _wellnessLog

    init {
        fetchTodayWellness()
    }

    fun fetchTodayWellness() {
        val userId = NetworkPreferences.userId ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            val log = wellnessService.getWellness(userId, today)
            _wellnessLog.value = log
        }
    }

    fun updateWellness(
        sleepHours: Int? = null,
        hydrationLevel: Int? = null,
        meditated: Boolean? = null,
        exercise: Boolean? = null
    ) {
        val userId = NetworkPreferences.userId ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            val success = wellnessService.updateWellness(userId, today, UpdateWellnessRequest(sleepHours, hydrationLevel, meditated, exercise))
            if (success) {
                fetchTodayWellness()
            }
        }
    }
}
