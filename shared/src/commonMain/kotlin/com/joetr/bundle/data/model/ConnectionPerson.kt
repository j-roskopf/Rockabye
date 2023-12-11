package com.joetr.bundle.data.model

import kotlinx.serialization.Serializable

// person 1 will always be the one to move to matchedNames

// as person 1 and 2 go to screen, move from toBeAlerted -> alerted (if not exists) and remove from toBeAlerted

@Serializable
data class ConnectionPerson(
    val id: String,
)
