package com.joetr.bundle.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.bundle.design.theme.ErrorState
import com.joetr.bundle.design.theme.LoadingState
import com.joetr.bundle.design.toolbar.DefaultToolbar
import com.joetr.bundle.design.toolbar.backOrNull
import com.joetr.bundle.network.data.behind.BehindTheNameApiResponse
import com.joetr.bundle.network.data.nationalize.NationalizeApiResponse
import com.joetr.bundle.ui.data.NameYearData

class NameDetailScreen(val data: Pair<String, List<NameYearData>>) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<NameDetailScreenModel>()
        val state = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffect(
            onStarted = {
                screenModel.fetchData(data)
            },
        )

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = {
                        navigator.backOrNull()?.invoke()
                    },
                )
            },
        ) { paddingValues ->
            AnimatedContent(
                targetState = state,
            ) { targetState ->
                when (targetState) {
                    is NameDetailScreenState.Error -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is NameDetailScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is NameDetailScreenState.Content -> ContentState(
                        data = targetState.data,
                        nationality = targetState.nationalizeApiResponse,
                        relatedNames = targetState.relatedNames,
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }

    @Composable
    fun ContentState(
        modifier: Modifier,
        data: Pair<String, List<NameYearData>>,
        nationality: NationalizeApiResponse,
        relatedNames: BehindTheNameApiResponse,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
        ) {
            val name = data.first

            Text(
                text = name,
                style = MaterialTheme.typography.headlineLarge,
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                item {
                    Text(
                        text = "Popularity",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
                data.second.forEach {
                    item {
                        Text(
                            text = "${it.popularity} occurrences in the year ${it.year}",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }

                if (nationality.country.isNotEmpty()) {
                    item {
                        Text(
                            text = "Nationality",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                    nationality.country.forEach {
                        item {
                            Text(
                                text = "${getCountryName(it.countryId)} - ${decimalToPercentage(it.probability)}% probability",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                            )
                        }
                    }
                }

                if (relatedNames.names.isNotEmpty()) {
                    item {
                        Text(
                            text = "Nicknames",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                    relatedNames.names.forEach {
                        item {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    fun decimalToPercentage(decimal: Double): Int {
        return (decimal * 100).toInt()
    }
}
