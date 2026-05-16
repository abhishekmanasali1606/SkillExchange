package com.skillexchange.data.model

data class Message(
    val id: String = "",
    val swapId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
