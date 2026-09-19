package com.softphone.studio.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.softphone.studio.model.*
import com.softphone.studio.network.NrApiClient
import com.softphone.studio.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Flagship ViewModel orchestrating VoIP state, live 2NR virtual carrier numbers,
 * real-time SMS streams, and native system notifications.
 */
class SoftphoneViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("phantomline_prefs", Context.MODE_PRIVATE)

    // PhantomLine Cloud Account State
    val authToken = MutableStateFlow<String?>(prefs.getString("auth_token", null))
    val userEmail = MutableStateFlow<String?>(prefs.getString("user_email", null))
    val isAuthLoading = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)
    val authSuccessMessage = MutableStateFlow<String?>(null)

    // Notification deduplication set to avoid notification storms on initial load
    private val notifiedMessageIds = mutableSetOf<String>()
    private var isInitialSmsLoad = true

    init {
        NotificationHelper.initChannel(application)
        val savedToken = prefs.getString("auth_token", null)
        if (!savedToken.isNullOrBlank()) {
            fetchUserNumbers()
            fetchSms()
        }
    }

    // Virtual Numbers State (100% Genuine Polish Carrier Numbers)
    private val _numbers = MutableStateFlow<List<PhoneNumberItem>>(emptyList())
    val numbers: StateFlow<List<PhoneNumberItem>> = _numbers.asStateFlow()

    // Random Number Pending Reservation
    val pendingRandomNumber = MutableStateFlow<Pair<String, Int>?>(null)
    val isNumberLoading = MutableStateFlow(false)

    // Messages State
    private val _messages = MutableStateFlow<List<MessageThread>>(emptyList())
    val messages: StateFlow<List<MessageThread>> = _messages.asStateFlow()

    // Voicemails State
    private val _voicemails = MutableStateFlow<List<VoicemailItem>>(emptyList())
    val voicemails: StateFlow<List<VoicemailItem>> = _voicemails.asStateFlow()

    // Voicemail playback state
    val playingVoicemailId = MutableStateFlow<String?>(null)
    val voicemailCurrentSeconds = MutableStateFlow(0f)
    val voicemailPlaybackSpeed = MutableStateFlow(1.0f)
    private var voicemailJob: Job? = null

    // Dialer State
    val dialerInput = MutableStateFlow("")

    // Active Call State
    val isCallActive = MutableStateFlow(false)
    val activeCallTarget = MutableStateFlow("")
    val activeCallElapsedSeconds = MutableStateFlow(0)
    val isCallMuted = MutableStateFlow(false)
    val isSpeakerOn = MutableStateFlow(false)
    val isCallOnHold = MutableStateFlow(false)
    private var callTimerJob: Job? = null

    // Audio & SIP Settings State
    val settingsState = MutableStateFlow(AudioSettingsState())

    // 2NR Cloud Authentication
    fun login2nr(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authError.value = null
            authSuccessMessage.value = null

            NrApiClient.login(email, pass).fold(
                onSuccess = { token ->
                    authToken.value = token
                    userEmail.value = email
                    prefs.edit()
                        .putString("auth_token", token)
                        .putString("user_email", email)
                        .apply()
                    isAuthLoading.value = false
                    authSuccessMessage.value = "Connected to PhantomLine Cloud!"
                    fetchUserNumbers()
                    fetchSms()
                    onSuccess()
                },
                onFailure = { err ->
                    isAuthLoading.value = false
                    authError.value = err.message ?: "Authentication failed"
                }
            )
        }
    }

    fun register2nr(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authError.value = null
            authSuccessMessage.value = null

            NrApiClient.register(email, pass).fold(
                onSuccess = {
                    isAuthLoading.value = false
                    authSuccessMessage.value = "Registration successful! Please check your email to activate."
                    onSuccess()
                },
                onFailure = { err ->
                    isAuthLoading.value = false
                    authError.value = err.message ?: "Registration failed"
                }
            )
        }
    }

    fun logout2nr() {
        authToken.value = null
        userEmail.value = null
        authError.value = null
        authSuccessMessage.value = null
        _numbers.value = emptyList()
        _messages.value = emptyList()
        prefs.edit()
            .remove("auth_token")
            .remove("user_email")
            .apply()
    }

    fun fetchUserNumbers() {
        val token = authToken.value ?: return
        viewModelScope.launch {
            NrApiClient.getUserNumbers(token).onSuccess { list ->
                _numbers.value = list
                // Check if any number expires within 1 day and warn user
                list.firstOrNull { it.daysRemaining <= 1 && it.isActive }?.let { expiringItem ->
                    NotificationHelper.showLeaseExpiryAlert(
                        context = getApplication(),
                        number = expiringItem.number,
                        daysLeft = expiringItem.daysRemaining
                    )
                }
            }
        }
    }

    fun fetchRandomNumber() {
        val token = authToken.value ?: return
        viewModelScope.launch {
            isNumberLoading.value = true
            NrApiClient.getRandomNumber(token).fold(
                onSuccess = { pair ->
                    isNumberLoading.value = false
                    pendingRandomNumber.value = pair
                },
                onFailure = {
                    isNumberLoading.value = false
                }
            )
        }
    }

    fun reservePendingNumber(name: String, onResult: (Boolean, String) -> Unit) {
        val token = authToken.value ?: run {
            onResult(false, "Authentication required to reserve numbers.")
            return
        }
        val pair = pendingRandomNumber.value ?: run {
            onResult(false, "No pending number selected.")
            return
        }
        viewModelScope.launch {
            isNumberLoading.value = true
            NrApiClient.reserveNumber(token, pair.second, name).fold(
                onSuccess = {
                    isNumberLoading.value = false
                    pendingRandomNumber.value = null
                    fetchUserNumbers()
                    onResult(true, "Number successfully reserved on Warsaw gateway!")
                },
                onFailure = { err ->
                    isNumberLoading.value = false
                    onResult(false, err.message ?: "Reservation failed.")
                }
            )
        }
    }

    fun renewNumber(id: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val token = authToken.value ?: run {
            onResult(false, "Authentication required to renew line.")
            return
        }
        val numberId = id.toIntOrNull() ?: run {
            onResult(false, "Invalid number identifier.")
            return
        }
        viewModelScope.launch {
            isNumberLoading.value = true
            NrApiClient.extendNumberValidity(token, numberId).fold(
                onSuccess = {
                    isNumberLoading.value = false
                    fetchUserNumbers()
                    onResult(true, "Validity extended successfully on carrier switch!")
                },
                onFailure = { err ->
                    isNumberLoading.value = false
                    onResult(false, err.message ?: "Carrier validity extension failed.")
                }
            )
        }
    }

    fun fetchSms() {
        val token = authToken.value ?: return
        viewModelScope.launch {
            NrApiClient.getSms(token).onSuccess { list ->
                _messages.value = list

                if (isInitialSmsLoad) {
                    list.forEach { notifiedMessageIds.add(it.id) }
                    isInitialSmsLoad = false
                } else {
                    for (msg in list) {
                        if (msg.isUnread && !notifiedMessageIds.contains(msg.id)) {
                            notifiedMessageIds.add(msg.id)
                            NotificationHelper.showIncomingSms(
                                context = getApplication(),
                                sender = msg.title,
                                message = msg.lastMessage,
                                lineName = msg.recipientLine,
                                notificationId = msg.id.hashCode()
                            )
                        }
                    }
                }
            }
        }
    }

    // Actions
    fun pressDialerKey(key: String) {
        dialerInput.update { it + key }
    }

    fun backspaceDialer() {
        dialerInput.update { current ->
            if (current.isNotEmpty()) current.dropLast(1) else ""
        }
    }

    fun clearDialer() {
        dialerInput.value = ""
    }

    fun startCall(number: String? = null) {
        val target = number ?: dialerInput.value
        activeCallTarget.value = target
        isCallActive.value = true
        activeCallElapsedSeconds.value = 0
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isCallActive.value) {
                delay(1000)
                activeCallElapsedSeconds.update { it + 1 }
            }
        }
    }

    fun endCall() {
        isCallActive.value = false
        callTimerJob?.cancel()
    }

    fun toggleVoicemailPlayback(item: VoicemailItem) {
        if (playingVoicemailId.value == item.id) {
            pauseVoicemail()
        } else {
            playVoicemail(item)
        }
    }

    private fun playVoicemail(item: VoicemailItem) {
        playingVoicemailId.value = item.id
        voicemailJob?.cancel()
        voicemailJob = viewModelScope.launch {
            while (playingVoicemailId.value == item.id) {
                delay(500)
                val next = voicemailCurrentSeconds.value + (0.5f * voicemailPlaybackSpeed.value)
                if (next >= item.durationSeconds) {
                    voicemailCurrentSeconds.value = 0f
                    playingVoicemailId.value = null
                    break
                } else {
                    voicemailCurrentSeconds.value = next
                }
            }
        }
    }

    fun pauseVoicemail() {
        playingVoicemailId.value = null
        voicemailJob?.cancel()
    }

    fun deleteVoicemail(id: String) {
        if (playingVoicemailId.value == id) pauseVoicemail()
        _voicemails.update { list -> list.filterNot { it.id == id } }
    }
}
