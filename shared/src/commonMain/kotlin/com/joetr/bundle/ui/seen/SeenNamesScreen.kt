package com.joetr.bundle.ui.seen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.design.segment.SegmentText
import com.joetr.bundle.design.segment.SegmentedControl
import com.joetr.bundle.design.theme.ErrorState
import com.joetr.bundle.design.theme.LoadingState
import com.joetr.bundle.design.toolbar.DefaultToolbar
import com.joetr.bundle.design.toolbar.backOrNull
import com.joetr.bundle.ui.seen.data.LocalSeenName
import com.joetr.bundle.ui.seen.data.NameOrigin
import com.kevinnzou.swipebox.SwipeBox
import com.kevinnzou.swipebox.SwipeDirection
import com.kevinnzou.swipebox.widget.SwipeIcon
import kotlinx.coroutines.launch

class SeenNamesScreen(
    private val connectionCode: String?,
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SeenNameScreenModel>()
        val state = screenModel.state.collectAsState().value

        LifecycleEffect(
            onStarted = {
                screenModel.getData(Gender.MALE, connectionCode)
            },
        )
        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = LocalNavigator.currentOrThrow.backOrNull(),
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
                    is SeenNameScreenState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        seenNames = targetState.seenNames,
                        gender = targetState.gender,
                        refreshData = {
                            screenModel.getData(it, connectionCode)
                        },
                        reverseLike = {
                            screenModel.reverseLike(it, connectionCode)
                        },
                        connection = targetState.connection,
                        remoteLikedNames = targetState.remoteSeenNames,
                        markAsReadIfNeeded = {
                            screenModel.markRemoteAsReadIfNeeded(connectionCode)
                        },
                    )

                    is SeenNameScreenState.Error -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is SeenNameScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ContentState(
        seenNames: List<List<LocalSeenName>>,
        remoteLikedNames: List<List<LocalSeenName>>,
        modifier: Modifier,
        refreshData: (Gender) -> Unit,
        reverseLike: (LocalSeenName) -> Unit,
        markAsReadIfNeeded: () -> Unit,
        gender: Gender,
        connection: Connection?,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
        ) {
            val nameOriginSegments = remember { listOf(NameOrigin.LOCAL, NameOrigin.REMOTE) }
            val selectedOrigin = remember {
                mutableStateOf(nameOriginSegments.first())
            }
            val isLocalDisplaying = selectedOrigin.value == NameOrigin.LOCAL

            if (connection != null) {
                SegmentedControl(
                    modifier = Modifier.padding(bottom = 16.dp),
                    segments = nameOriginSegments,
                    selectedSegment = selectedOrigin.value,
                    onSegmentSelected = {
                        selectedOrigin.value = it
                        if (it == NameOrigin.REMOTE) {
                            markAsReadIfNeeded()
                        }
                    },
                ) {
                    SegmentText(it.display)
                }
            }

            val genderSegments = remember { listOf(Gender.MALE, Gender.FEMALE) }

            val pagerState = rememberPagerState(
                initialPage = genderSegments.indexOfFirst { it == gender },
            ) {
                genderSegments.size
            }

            val coroutineScope = rememberCoroutineScope()

            SegmentedControl(
                genderSegments,
                gender,
                onSegmentSelected = {
                    refreshData(it)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            genderSegments.indexOfFirst { genderSegment ->
                                genderSegment == it
                            },
                        )
                    }
                },
            ) {
                SegmentText(it.display)
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                ) {
                    val data = if (isLocalDisplaying) {
                        seenNames[it]
                    } else {
                        remoteLikedNames[it]
                    }
                    if (data.isNotEmpty()) {
                        itemsIndexed(
                            items = data,
                            key = { _, seenName -> "${seenName.name} ${seenName.gender} ${seenName.liked}" },
                        ) { index, seenName ->
                            val icon = if (seenName.liked) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Filled.ThumbDown
                            }
                            val tint = if (seenName.liked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                            if (isLocalDisplaying) {
                                // only local gets swipe behavior
                                SwipeContainer(
                                    modifier = Modifier.fillMaxWidth().animateItemPlacement(),
                                    seenName = seenName,
                                    reverseLike = reverseLike,
                                    content = {
                                        NameRowItem(
                                            name = seenName.name,
                                            icon = icon,
                                            tint = tint,
                                        )
                                    },
                                )
                            } else {
                                NameRowItem(
                                    name = seenName.name,
                                    icon = icon,
                                    tint = tint,
                                )
                            }

                            if (index < seenNames[it].size - 1) {
                                Divider(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp,
                                    ),
                                )
                            }
                        }
                    } else {
                        item {
                            Text("No names have been liked yet")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NameRowItem(
        icon: ImageVector,
        tint: Color,
        name: String,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                imageVector = icon,
                tint = tint,
                contentDescription = null,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun SwipeContainer(
        modifier: Modifier,
        seenName: LocalSeenName,
        reverseLike: (LocalSeenName) -> Unit,
        content: @Composable () -> Unit,
    ) {
        val coroutineScope = rememberCoroutineScope()
        SwipeBox(
            modifier = modifier.fillMaxWidth(),
            swipeDirection = SwipeDirection.EndToStart,
            endContentWidth = 60.dp,
            endContent = { swipeableState, _ ->
                val icon = if (seenName.liked) {
                    Icons.Filled.ThumbDown
                } else {
                    Icons.Filled.ThumbUp
                }
                SwipeIcon(
                    imageVector = icon,
                    contentDescription = null,
                    background = MaterialTheme.colorScheme.background,
                    tint = MaterialTheme.colorScheme.onBackground,
                    weight = 1f,
                    iconSize = 24.dp,
                ) {
                    coroutineScope.launch {
                        swipeableState.snapTo(0)
                    }
                    reverseLike(seenName)
                }
            },
        ) { _, _, _ ->
            content()
        }
    }
}
