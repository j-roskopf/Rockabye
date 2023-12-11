package com.joetr.bundle.network.data.behind

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BehindTheNameApiResponse(
    @SerialName("names")
    val names: List<String>,
)
