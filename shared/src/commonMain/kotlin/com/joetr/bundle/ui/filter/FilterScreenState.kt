package com.joetr.bundle.ui.filter

import com.joetr.bundle.ui.data.TimePeriodFilters

sealed interface FilterScreenState {
    val animationKey: Int

    data object Loading : FilterScreenState {
        override val animationKey: Int
            get() = 1
    }

    data object Error : FilterScreenState {
        override val animationKey: Int
            get() = 2
    }

    data class Content(
        val selectedGender: com.joetr.bundle.data.model.Gender,
        val selectedTimePeriod: TimePeriodFilters,
        val selectedStartsWith: String,
        val selectedMaxLength: Int,
    ) : FilterScreenState {
        override val animationKey: Int
            get() = 3
    }

    data object Gender : FilterScreenState {
        override val animationKey: Int
            get() = 4
    }

    data object TimePeriod : FilterScreenState {
        override val animationKey: Int
            get() = 5
    }

    data object YearSelection : FilterScreenState {
        override val animationKey: Int
            get() = 6
    }

    data class StartsWith(val currentStartsWith: String) : FilterScreenState {
        override val animationKey: Int
            get() = 7
    }

    data class MaxLength(val maxLength: Int) : FilterScreenState {
        override val animationKey: Int
            get() = 8
    }
}
