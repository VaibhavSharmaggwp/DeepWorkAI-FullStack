package com.example.deepworkai.viewmodel

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepworkai.BuildConfig
import com.example.deepworkai.models.SaveSessionRequest
import com.example.deepworkai.models.SessionSummaryResponse
import com.example.deepworkai.network.KtorClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.ktor.client.request.get
import io.ktor.client.call.body
import com.example.deepworkai.models.FocusSession
import com.example.deepworkai.models.EndSessionResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class SessionViewModel : ViewModel() {

    private val _history = MutableStateFlow<List<FocusSession>>(emptyList())
    val history: StateFlow<List<FocusSession>> = _history.asStateFlow()

    private val _currentSession = MutableStateFlow<EndSessionResponse?>(null)
    val currentSession: StateFlow<EndSessionResponse?> = _currentSession.asStateFlow()

    private val _cognitiveLoad = MutableStateFlow("Medium")
    val cognitiveLoad: StateFlow<String> = _cognitiveLoad.asStateFlow()

    fun updateCognitiveLoad() {
        // Simulating real-world dynamic behavior
        val loads = listOf("Low", "Medium", "High")
        _cognitiveLoad.value = loads.random()
    }

    fun setLatestSession(session: EndSessionResponse) {
        _currentSession.value = session
    }

    fun fetchHistory(userId: String) {
        viewModelScope.launch {
            try {
                println("SessionViewModel: Fetching history for user $userId from ${com.example.deepworkai.network.NetworkPreferences.backendUrl}")
                val response = KtorClient.httpClient.get(
                    "${com.example.deepworkai.network.NetworkPreferences.backendUrl}/sessions/history/$userId"
                )
                println("SessionViewModel: Received history response status: ${response.status}")
                if (response.status == HttpStatusCode.OK) {
                    val historyList: List<FocusSession> = response.body()
                    println("SessionViewModel: Successfully fetched ${historyList.size} history items")
                    _history.value = historyList
                } else {
                    println("SessionViewModel: Failed to fetch history. Code: ${response.status}")
                }
            } catch (e: Exception) {
                println("SessionViewModel: CRITICAL ERROR fetching history: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Removed redundant saveSession() since backend already saves it on end Session

    fun downloadReport(userId: String) {
        // TODO: Implement Ktor call to /sessions/export/{userId} to download the PDF report
    }
}