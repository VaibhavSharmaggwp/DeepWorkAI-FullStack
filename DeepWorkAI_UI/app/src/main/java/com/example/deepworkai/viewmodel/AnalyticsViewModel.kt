package com.example.deepworkai.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepworkai.BuildConfig
import com.example.deepworkai.models.AnalyticsDashboard
import com.example.deepworkai.models.SessionSummaryResponse
import com.example.deepworkai.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch

class AnalyticsViewModel : ViewModel() {
    private val _historyState = mutableStateOf<List<SessionSummaryResponse>>(emptyList())
    val historyState: State<List<SessionSummaryResponse>> = _historyState


    fun fetchHistory(userId: String){
        viewModelScope.launch {
            try{
                val response: List<SessionSummaryResponse> = KtorClient.httpClient
                    .get ("${BuildConfig.BACKEND_URL}/sessions/history/$userId")
                    .body()
                _historyState.value = response
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    private val _uiState = mutableStateOf<AnalyticsDashboard?>(null)
    val uiState: State<AnalyticsDashboard?> = _uiState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _selectedPeriod = mutableStateOf("Weekly")
    val selectedPeriod: State<String> = _selectedPeriod

    init {
        val uId = com.example.deepworkai.network.NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"
        fetchAnalytics(uId, "weekly")
    }

    fun togglePeriod() {
        val next = if (_selectedPeriod.value == "Weekly") "Monthly" else "Weekly"
        _selectedPeriod.value = next
        val uId = com.example.deepworkai.network.NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"
        fetchAnalytics(uId, next.lowercase())
    }

    fun fetchAnalytics(userId: String, period: String = "weekly") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val baseUrl = com.example.deepworkai.network.NetworkPreferences.backendUrl
                val response: AnalyticsDashboard = KtorClient.httpClient
                    .get("${baseUrl}/analytics/dashboard/$userId?period=$period")
                    .body()
                _uiState.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}