package com.joetr.bundle.ui.data

import com.joetr.bundle.ui.filter.FilterScreen
import com.joetr.bundle.ui.filter.FilterScreenModel

/**
 * When adding a new filter -
 *
 * 1. Add to UI in [FilterScreen]
 * 2. Must add to the de-serialization in [FilterScreenModel.getTimePeriodOrDefault]
 */
sealed interface TimePeriodFilters {
    val range: IntRange
    val display: String

    data class Default(
        override val range: IntRange = 2022..2022,
        override val display: String = "Current Year",
    ) : TimePeriodFilters

    data class SpecificYear(
        override val range: IntRange,
        override val display: String,
    ) : TimePeriodFilters

    data class CurrentMillennium(
        override val range: IntRange = 2000..2022,
        override val display: String = "2000-2022",
    ) : TimePeriodFilters

    data class PreviousMillennium(
        override val range: IntRange = 1900..1999,
        override val display: String = "1900-1999",
    ) : TimePeriodFilters

    data class EighteenHundreds(
        override val range: IntRange = 1880..1899,
        override val display: String = "1880-1899",
    ) : TimePeriodFilters
}
