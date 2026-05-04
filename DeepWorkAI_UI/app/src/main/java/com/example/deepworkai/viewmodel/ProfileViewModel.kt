package com.example.deepworkai.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepworkai.models.User
import com.example.deepworkai.network.ProfileService
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel : ViewModel() {
    private val profileService = ProfileService()

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val profile = profileService.getProfile()
            if (profile != null) {
                _user.value = profile
            } else {
                _error.value = "Failed to fetch profile"
            }
            _isLoading.value = false
        }
    }

    fun updatePreferences(
        darkMode: Boolean? = null,
        behavioralTracking: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        notificationType: String? = null,
        notificationTime: String? = null
    ) {
        viewModelScope.launch {
            val success = profileService.updateProfile(
                darkMode, behavioralTracking, notificationsEnabled, notificationType, notificationTime
            )
            if (success) {
                fetchProfile() // Refresh
            }
        }
    }

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _isLoading.value = true
            val imageUrl = profileService.uploadProfileImage(file)
            if (imageUrl != null) {
                fetchProfile() // Refresh to get updated user with image URL
            } else {
                _error.value = "Failed to upload image"
            }
            _isLoading.value = false
        }
    }
}
