package com.example.deepworkai.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FocusTimerManager {
    private val _seconds = MutableStateFlow(0)
    val seconds: StateFlow<Int> = _seconds.asStateFlow()

    private val _maxSeconds = MutableStateFlow(3600)
    val maxSeconds: StateFlow<Int> = _maxSeconds.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    fun updateTimerState(seconds: Int, maxSeconds: Int, isPaused: Boolean, isActive: Boolean) {
        _seconds.value = seconds
        _maxSeconds.value = maxSeconds
        _isPaused.value = isPaused
        _isActive.value = isActive
    }

    fun setMaxSeconds(max: Int) {
        _maxSeconds.value = max
    }
}
