package com.softphone.studio.model

data class MessageThread(
    val id: String,
    val title: String,
    val phoneNumber: String,
    val lastMessage: String,
    val timestamp: String,
    val isUnread: Boolean
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String
)
