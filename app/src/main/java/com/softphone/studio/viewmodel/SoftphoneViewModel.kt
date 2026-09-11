package com.softphone.studio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softphone.studio.model.*
import com.softphone.studio.network.NrApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SoftphoneViewModel : ViewModel() {

    // 2NR Cloud Account State
    val authToken = MutableStateFlow<String?>(null)
    val userEmail = MutableStateFlow<String?>(null)
    val isAuthLoading = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)
    val authSuccessMessage = MutableStateFlow<String?>(null)

    // Virtual Numbers State
    private val _numbers = MutableStateFlow<List<PhoneNumberItem>>(
        listOf(
            PhoneNumberItem("1", "+48 732 458 912", "PL", "2NR Cloud Poland", 28),
            PhoneNumberItem("2", "+48 690 124 551", "PL", "2NR Line 2", 14)
        )
    )
    val numbers: StateFlow<List<PhoneNumberItem>> = _numbers.asStateFlow()

    // Random Number Pending Reservation
    val pendingRandomNumber = MutableStateFlow<Pair<String, Int>?>(null)
    val isNumberLoading = MutableStateFlow(false)

    // Messages State
    private val _messages = MutableStateFlow<List<MessageThread>>(
        listOf(
            MessageThread(
                id = "1",
                title = "2NR Verification",
                phoneNumber = "+48 732 000 111",
                lastMessage = "Your verification code is: 849201. Valid for 10 minutes.",
                timestamp = "Just now",
                isUnread = true
            ),
            MessageThread(
                id = "2",
                title = "Softphone Studio",
                phoneNumber = "System",
                lastMessage = "Welcome to Softphone Studio OLED Black Edition.",
                timestamp = "Yesterday",
                isUnread = false
            )
        )
    )
    val messages: StateFlow<List<MessageThread>> = _messages.asStateFlow()

    // Voicemails State
    private val _voicemails = MutableStateFlow<List<VoicemailItem>>(
        listOf(
            VoicemailItem(
                id = "1",
                callerNumber = "+48 22 100 4567",
                callerTag = "PL",
                title = "Voicemail: Dispatch Center",
                durationSeconds = 32,
                transcript = "Hello, your virtual 2NR softphone registration is active and verified for TLS calling.",
                isUnread = true,
                timestamp = "10:14 AM"
            )
        )
    )
    val voicemails: StateFlow<List<VoicemailItem>> = _voicemails.asStateFlow()

    // Voicemail playback state
    val playingVoicemailId = MutableStateFlow<String?>(null)
    val voicemailCurrentSeconds = MutableStateFlow(0f)
    val voicemailPlaybackSpeed = MutableStateFlow(1.0f)
    private var voicemailJob: Job? = null

    // Dialer State
    val dialerInput = MutableStateFlow("+48 ")

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
                    isAuthLoading.value = false
                    authSuccessMessage.value = "Connected to 2NR Cloud!"
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
    }

    fun fetchUserNumbers() {
        val token = authToken.value ?: return
        viewModelScope.launch {
            NrApiClient.getUserNumbers(token).onSuccess { list ->
                if (list.isNotEmpty()) {
                    _numbers.value = list
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

    fun reservePendingNumber(name: String, onComplete: () -> Unit) {
        val token = authToken.value ?: return
        val pair = pendingRandomNumber.value ?: return
        viewModelScope.launch {
            isNumberLoading.value = true
            NrApiClient.reserveNumber(token, pair.second, name).fold(
                onSuccess = {
                    isNumberLoading.value = false
                    pendingRandomNumber.value = null
                    fetchUserNumbers()
                    onComplete()
                },
                onFailure = {
                    isNumberLoading.value = false
                }
            )
        }
    }

    fun fetchSms() {
        val token = authToken.value ?: return
        viewModelScope.launch {
            NrApiClient.getSms(token).onSuccess { list ->
                if (list.isNotEmpty()) {
                    _messages.value = list
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
            if (current.isNotEmpty()) current.dropLast(1) else "+"
        }
    }

    fun clearDialer() {
        dialerInput.value = "+48 "
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

    fun renewNumber(id: String) {
        _numbers.update { list ->
            list.map { if (it.id == id) it.copy(daysRemaining = 30) else it }
        }
    }

    fun addVirtualNumber(number: String, tag: String, carrier: String) {
        val newItem = PhoneNumberItem(
            id = System.currentTimeMillis().toString(),
            number = number,
            countryTag = tag,
            carrierName = carrier,
            daysRemaining = 30
        )
        _numbers.update { listOf(newItem) + it }
    }
}
