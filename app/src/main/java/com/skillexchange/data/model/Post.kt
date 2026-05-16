package com.skillexchange.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val skillRequired: String = "",
    val village: String = "",
    val hoursRequired: Int = 1,
    val timestamp: Long = 0L
)
