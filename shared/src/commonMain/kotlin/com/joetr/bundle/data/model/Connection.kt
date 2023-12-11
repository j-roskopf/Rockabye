package com.joetr.bundle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val id: String,
    val personOne: ConnectionPerson,
    val personOneLikedNames: List<ConnectionLikedName>,
    val personTwo: ConnectionPerson?,
    val personTwoLikedNames: List<ConnectionLikedName>,
    val matchedNames: List<ConnectionLikedName>,
)
