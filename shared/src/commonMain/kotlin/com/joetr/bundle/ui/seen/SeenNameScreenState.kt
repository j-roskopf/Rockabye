package com.joetr.bundle.ui.seen

import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.ui.seen.data.LocalSeenName

sealed interface SeenNameScreenState {
    val animationKey: Int

    data object Loading : SeenNameScreenState {
        override val animationKey: Int
            get() = 1
    }

    data object Error : SeenNameScreenState {
        override val animationKey: Int
            get() = 2
    }

    data class Content(
        val seenNames: List<List<LocalSeenName>>,
        val remoteSeenNames: List<List<LocalSeenName>>,
        val connection: Connection?,
        val gender: Gender,
        override val animationKey: Int = 3,
    ) : SeenNameScreenState
}
