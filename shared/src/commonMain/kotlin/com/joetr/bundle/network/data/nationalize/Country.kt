package com.joetr.bundle.network.data.nationalize

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Country(
    @SerialName("country_id")
    val countryId: String,
    @SerialName("probability")
    val probability: Double,
)
