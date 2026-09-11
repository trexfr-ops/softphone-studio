package com.softphone.studio.model

enum class SipTransport {
    TLS, TCP, UDP
}

data class AudioSettingsState(
    val speakerVolumePercent: Int = 85,
    val microphoneGainDb: Int = 6,
    val jitterBufferMs: Int = 40,
    val ringtoneVolumePercent: Int = 70,
    val sipTransport: SipTransport = SipTransport.TLS,
    val isOpusHdEnabled: Boolean = true,
    val isEchoCancellationEnabled: Boolean = true,
    val isAiNoiseFilterEnabled: Boolean = true,
    val isDtmfAudioEnabled: Boolean = true,
    val isVibrationHapticsEnabled: Boolean = true
)
