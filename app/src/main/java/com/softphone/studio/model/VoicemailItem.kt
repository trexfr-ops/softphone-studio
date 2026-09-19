package com.softphone.studio.model

/**
 * Encapsulates a carrier voicemail entry with recipient line attribution.
 */
data class VoicemailItem(
    val id: String,
    val callerNumber: String,
    val callerTag: String,
    val title: String,
    val durationSeconds: Int,
    val transcript: String,
    val isUnread: Boolean,
    val timestamp: String,
    val recipientLine: String = ""
)
