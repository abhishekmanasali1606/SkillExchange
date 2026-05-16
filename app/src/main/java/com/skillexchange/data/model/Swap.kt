package com.skillexchange.data.model

data class Swap(
    val id: String = "",
    val postId: String = "",
    val offeredBy: String = "",
    val postOwner: String = "",
    val status: String = "pending",
    val message: String = "",
    val hoursOffered: Int = 1,
    val confirmedByOwner: Boolean = false,
    val confirmedByOfferer: Boolean = false
)
