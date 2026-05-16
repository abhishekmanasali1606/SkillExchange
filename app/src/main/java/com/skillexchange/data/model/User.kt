package com.skillexchange.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val skillOffered: String = "",
    val skillWanted: String = "",
    val phoneNumber: String = "",
    val village: String = "",
    val trustScore: Int = 0,
    val points: Int = 0
)
