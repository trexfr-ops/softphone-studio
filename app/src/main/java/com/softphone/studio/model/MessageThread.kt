package com.softphone.studio.model

/**
 * Encapsulates an SMS message thread with virtual line recipient attribution.
 */
data class MessageThread(
    val id: String,
    val title: String,
    val phoneNumber: String,
    val lastMessage: String,
    val timestamp: String,
    val isUnread: Boolean,
    val recipientLine: String = "",
    val numberId: Int? = null
)

/**
 * Individual chat message entry in a conversation.
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String
)
