package com.joetr.bundle.ui.filter

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.design.button.PrimaryButton
import com.joetr.bundle.design.theme.ErrorState
import com.joetr.bundle.design.theme.LoadingState
import com.joetr.bundle.design.toolbar.DefaultToolbar
import com.joetr.bundle.design.toolbar.backOrNull
import com.joetr.bundle.ui.data.TimePeriodFilters

class FilterScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<FilterScreenModel>()
        val state = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow
        LifecycleEffect(
            onStarted = {
                screenModel.init()
            },
        )

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = {
                        if (screenModel.shouldGoBack()) {
                            navigator.backOrNull()?.invoke()
                        } else {
                            screenModel.init()
                        }
                    },
                )
            },
        ) { paddingValues ->
            AnimatedContent(
                targetState = state,
                contentKey = {
                    state.animationKey
                },
            ) { targetState ->
                when (targetState) {
                    is FilterScreenState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        selectedGender = targetState.selectedGender,
                        genderClicked = {
                            screenModel.genderScreen()
                        },
                        timePeriodClicked = {
                            screenModel.timePeriodScreen()
                        },
                        startsWithClicked = {
                            screenModel.startsWithScreen()
                        },
                        selectedTimePeriod = targetState.selectedTimePeriod,
                        selectedStartsWith = targetState.selectedStartsWith,
                        selectedMaxLength = targetState.selectedMaxLength,
                        maxLengthClicked = {
                            screenModel.maxLengthScreen()
                        },
                        reset = {
                            screenModel.reset()
                        },
                    )

                    is FilterScreenState.Error -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is FilterScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is FilterScreenState.Gender -> GenderSelection(
                        modifier = Modifier.padding(paddingValues),
                        onGenderSelected = {
                            screenModel.onGenderSelected(it)
                        },
                    )

                    FilterScreenState.TimePeriod -> TimePeriodSelection(
                        modifier = Modifier.padding(paddingValues),
                        onTimePeriodSelected = {
                            screenModel.timePeriodSelected(it)
                        },
                        yearSelection = {
                            screenModel.yearSelection()
                        },
                    )

                    FilterScreenState.YearSelection -> YearSelection(
                        modifier = Modifier.padding(paddingValues),
                        range = textFileRange.toList().reversed(),
                        onYearSelection = { year: Int ->
                            screenModel.onYearSelected(year)
                        },
                    )

                    is FilterScreenState.StartsWith -> StartsWithSelection(
                        modifier = Modifier.padding(paddingValues),
                        currentStartsWith = targetState.currentStartsWith,
                        onStartsWithSelected = { startsWith: String ->
                            screenModel.startsWithSelected(startsWith)
                        },
                    )

                    is FilterScreenState.MaxLength -> MaxLengthSection(
                        modifier = Modifier.padding(paddingValues),
                        currentMaxLength = targetState.maxLength,
                        maxLengthSelection = { maxLength: Int ->
                            screenModel.maxLengthSelected(maxLength)
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun StartsWithSelection(
        modifier: Modifier,
        currentStartsWith: String,
        onStartsWithSelected: (String) -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            val text = remember {
                mutableStateOf(currentStartsWith)
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text.value,
                label = {
                    Text("Starts with:")
                },
                onValueChange = {
                    text.value = it
                },
            )

            PrimaryButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    onStartsWithSelected(text.value)
                },
            ) {
                Text("Submit")
            }
        }
    }

    @Composable
    private fun MaxLengthSection(
        modifier: Modifier,
        currentMaxLength: Int,
        maxLengthSelection: (Int) -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            val text = remember {
                mutableStateOf(if (currentMaxLength == Int.MAX_VALUE) "" else currentMaxLength.toString())
            }
            val pattern = remember { Regex("^\\d+\$") }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text.value,
                label = {
                    Text("Max length:")
                },
                onValueChange = {
                    if (it.isEmpty() || it.matches(pattern)) {
                        text.value = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            PrimaryButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    if (text.value.isEmpty()) {
                        maxLengthSelection(Int.MAX_VALUE)
                    } else {
                        try {
                            maxLengthSelection(text.value.toInt())
                        } catch (throwable: Throwable) {
                            // I don't care about input validation :(
                        }
                    }
                },
            ) {
                Text("Submit")
            }
        }
    }

    @Composable
    fun ContentState(
        modifier: Modifier,
        genderClicked: () -> Unit,
        timePeriodClicked: () -> Unit,
        startsWithClicked: () -> Unit,
        maxLengthClicked: () -> Unit,
        reset: () -> Unit,
        selectedGender: Gender,
        selectedStartsWith: String,
        selectedMaxLength: Int,
        selectedTimePeriod: TimePeriodFilters,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f),
            ) {
                FilterRowItem(
                    text = "Gender: ${selectedGender.display}",
                    onClick = genderClicked,
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                FilterRowItem(
                    text = "Time Period: ${selectedTimePeriod.display}",
                    onClick = timePeriodClicked,
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                FilterRowItem(
                    text = "Starts With: $selectedStartsWith",
                    onClick = startsWithClicked,
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                val display = if (selectedMaxLength == Int.MAX_VALUE) {
                    ""
                } else {
                    "$selectedMaxLength characters"
                }
                FilterRowItem(
                    text = "Max Length: $display",
                    onClick = maxLengthClicked,
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    reset()
                },
            ) {
                Text("Reset")
            }
        }
    }

    @Composable
    fun FilterRowItem(
        text: String,
        onClick: () -> Unit,
        showChevron: Boolean = true,
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = 64.dp).clickable {
                onClick()
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )

            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
        }
    }

    @Composable
    fun GenderSelection(
        modifier: Modifier = Modifier,
        onGenderSelected: (Gender) -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.clickable {
                    onGenderSelected(Gender.FEMALE)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Filled.Female,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Female",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Column(
                modifier = Modifier.clickable {
                    onGenderSelected(Gender.MALE)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Filled.Male,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Male",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Column(
                modifier = Modifier.clickable {
                    onGenderSelected(Gender.BOTH)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Filled.Transgender,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Both / Either",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }

    @Composable
    fun TimePeriodSelection(
        modifier: Modifier,
        onTimePeriodSelected: (TimePeriodFilters) -> Unit,
        yearSelection: () -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        ) {
            FilterRowItem(
                text = "Current Year",
                onClick = {
                    onTimePeriodSelected(TimePeriodFilters.Default())
                },
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            FilterRowItem(
                text = "Specific Year",
                onClick = {
                    yearSelection()
                },
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            FilterRowItem(
                text = "Current Millennium (2000-Current)",
                onClick = {
                    onTimePeriodSelected(TimePeriodFilters.CurrentMillennium())
                },
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            FilterRowItem(
                text = "Previous Millennium (1900-1999)",
                onClick = {
                    onTimePeriodSelected(TimePeriodFilters.PreviousMillennium())
                },
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            FilterRowItem(
                text = "1880-1899",
                onClick = {
                    onTimePeriodSelected(TimePeriodFilters.EighteenHundreds())
                },
            )
        }
    }

    @Composable
    fun YearSelection(
        modifier: Modifier,
        range: List<Int>,
        onYearSelection: (Int) -> Unit,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(16.dp),
        ) {
            itemsIndexed(
                range,
            ) { index, item ->
                FilterRowItem(
                    text = item.toString(),
                    onClick = {
                        onYearSelection(item)
                    },
                    showChevron = false,
                )
                if (index < range.size - 1) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}
