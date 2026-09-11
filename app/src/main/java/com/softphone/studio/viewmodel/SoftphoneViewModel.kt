package com.softphone.studio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softphone.studio.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SoftphoneViewModel : ViewModel() {

    // Virtual Numbers State
    private val _numbers = MutableStateFlow<List<PhoneNumberItem>>(
        listOf(
            PhoneNumberItem("1", "+48 732 458 912", "PL", "Play SIP Trunk", 28),
            PhoneNumberItem("2", "+48 690 124 551", "PL", "Orange Poland", 14),
            PhoneNumberItem("3", "+44 770 982 110", "UK", "Vodafone Direct", 30)
        )
    )
    val numbers: StateFlow<List<PhoneNumberItem>> = _numbers.asStateFlow()

    // Voicemails State
    private val _voicemails = MutableStateFlow<List<VoicemailItem>>(
        listOf(
            VoicemailItem(
                id = "1",
                callerNumber = "+48 22 100 4567",
                callerTag = "PL",
                title = "Voicemail: Dispatch Center",
                durationSeconds = 32,
                transcript = "Hello, we confirmed your virtual SIP softphone registration. Your line is active and verified for TLS calling.",
                isUnread = true,
                timestamp = "10:14 AM"
            ),
            VoicemailItem(
                id = "2",
                callerNumber = "+44 20 7946 0919",
                callerTag = "UK",
                title = "Voicemail: Tech Support",
                durationSeconds = 18,
                transcript = "System check completed. Jitter buffer calibrated to 40ms.",
                isUnread = false,
                timestamp = "Yesterday"
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
