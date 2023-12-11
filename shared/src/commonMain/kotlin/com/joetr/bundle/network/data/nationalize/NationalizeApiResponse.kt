package com.joetr.bundle.network.data.nationalize

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NationalizeApiResponse(
    @SerialName("count")
    val count: Int,
    @SerialName("country")
    val country: List<Country>,
    @SerialName("name")
    val name: String,
)
