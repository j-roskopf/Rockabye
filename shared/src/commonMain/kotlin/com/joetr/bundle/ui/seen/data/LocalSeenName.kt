package com.joetr.bundle.ui.seen.data

import com.joetr.bundle.data.model.Gender

data class LocalSeenName(
    val name: String,
    val gender: Gender,
    val liked: Boolean,
)
