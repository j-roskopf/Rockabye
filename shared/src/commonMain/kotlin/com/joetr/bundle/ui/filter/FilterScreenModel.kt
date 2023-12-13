package com.joetr.bundle.ui.filter

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.bundle.data.NameRepository
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.ui.data.TimePeriodFilters
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal val textFileRange = 1880..2022

class FilterScreenModel(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val nameRepository: NameRepository,
) : ScreenModel {

    private lateinit var selectedGender: Gender
    private lateinit var selectedTimePeriod: TimePeriodFilters
    private lateinit var selectedStartsWith: String
    private var selectedMaxLength: Int = Int.MAX_VALUE

    private val _state = MutableStateFlow<FilterScreenState>(FilterScreenState.Loading)
    val state: StateFlow<FilterScreenState> = _state

    fun init(
        clearCache: Boolean = false,
    ) {
        screenModelScope.launch(coroutineDispatcher) {
            selectedGender = nameRepository.getGenderOrDefault()
            selectedTimePeriod = nameRepository.getTimePeriodOrDefault()
            selectedStartsWith = nameRepository.getStartsWithOrDefault()
            selectedMaxLength = nameRepository.getMaxLengthOrDefault()
            _state.value = FilterScreenState.Content(
                selectedGender = selectedGender,
                selectedTimePeriod = selectedTimePeriod,
                selectedStartsWith = selectedStartsWith,
                selectedMaxLength = selectedMaxLength,
            )

            if (clearCache) {
                nameRepository.emitClearCacheSignal()
            }
        }
    }

    fun shouldGoBack(): Boolean {
        return _state.value is FilterScreenState.Content
    }

    fun genderScreen() {
        _state.value = FilterScreenState.Gender
    }

    fun onGenderSelected(gender: Gender) {
        nameRepository.setGender(gender)
        init(
            clearCache = true,
        )
    }

    fun timePeriodScreen() {
        _state.value = FilterScreenState.TimePeriod
    }

    fun yearSelection() {
        _state.value = FilterScreenState.YearSelection
    }

    fun onYearSelected(year: Int) {
        nameRepository.setYear(year)
        init(
            clearCache = true,
        )
    }

    fun timePeriodSelected(timePeriodFilters: TimePeriodFilters) {
        nameRepository.setTimePeriod(timePeriodFilters)
        init(
            clearCache = true,
        )
    }

    fun startsWithScreen() {
        screenModelScope.launch(coroutineDispatcher) {
            val startsWith = nameRepository.getStartsWithOrDefault()
            _state.value = FilterScreenState.StartsWith(startsWith)
        }
    }

    fun startsWithSelected(startsWith: String) {
        nameRepository.setStartsWith(startsWith)
        init(
            clearCache = true,
        )
    }

    fun maxLengthScreen() {
        screenModelScope.launch(coroutineDispatcher) {
            _state.value = FilterScreenState.MaxLength(nameRepository.getMaxLengthOrDefault())
        }
    }

    fun maxLengthSelected(maxLength: Int) {
        nameRepository.setMaxLength(maxLength)
        init(
            clearCache = true,
        )
    }

    fun reset() {
        selectedGender = Gender.BOTH
        selectedTimePeriod = TimePeriodFilters.Default()
        selectedStartsWith = ""
        selectedMaxLength = Int.MAX_VALUE

        nameRepository.setMaxLength(selectedMaxLength)
        nameRepository.setStartsWith(selectedStartsWith)
        nameRepository.setTimePeriod(selectedTimePeriod)
        nameRepository.setGender(selectedGender)

        init(
            clearCache = true,
        )
    }
}
