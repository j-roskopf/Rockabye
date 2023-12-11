package com.joetr.bundle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionLikedName(
    val name: String,
    val genderAbbreviation: String,
    val personOneAlerted: Boolean,
    val personTwoAlerted: Boolean,
)
