package com.joetr.bundle.ui.name

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Badge
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.bundle.design.button.PrimaryButton
import com.joetr.bundle.design.button.debouncedClick
import com.joetr.bundle.design.swipe.SwipedOutDirection
import com.joetr.bundle.design.swipe.flip.TwyperFlip
import com.joetr.bundle.design.swipe.flip.rememberTwyperFlipController
import com.joetr.bundle.design.theme.ErrorState
import com.joetr.bundle.design.theme.LoadingState
import com.joetr.bundle.design.toolbar.DefaultToolbar
import com.joetr.bundle.ui.connection.ConnectionScreen
import com.joetr.bundle.ui.data.NameYearData
import com.joetr.bundle.ui.detail.NameDetailScreen
import com.joetr.bundle.ui.filter.FilterScreen
import com.joetr.bundle.ui.name.data.NameSort
import com.joetr.bundle.ui.seen.SeenNamesScreen

class NameScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<NameScreenModel>()
        val navigator = LocalNavigator.currentOrThrow
        val state = screenModel.state.collectAsState().value

        LifecycleEffect(
            onStarted = {
                screenModel.readData()
                screenModel.startCacheCollection()
            },
        )

        Scaffold(
            topBar = {
                DefaultToolbar(
                    actions = {
                        PopupMenu(
                            sortClicked = {
                                screenModel.setSorting(it)
                            },
                        )

                        IconButton(
                            onClick = {
                                if (state is NameScreenState.Content) {
                                }
                            },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
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
                    is NameScreenState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        data = targetState.data,
                        nameRemoved = { name, gender, liked ->
                            screenModel.nameRemoved(name, gender, liked)
                        },
                        seenNamesClicked = {
                            navigator.push(SeenNamesScreen(it))
                        },
                        filterClicked = {
                            navigator.push(FilterScreen())
                        },
                        endReached = {
                            screenModel.readData()
                        },
                        learnMore = {
                            navigator.push(NameDetailScreen(it))
                        },
                        connectionClicked = {
                            navigator.push(ConnectionScreen())
                        },
                        personStatus = targetState.personStatus,
                        connectionId = targetState.connectionCode,
                        lastName = targetState.lastName,
                    )

                    is NameScreenState.Error -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is NameScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is NameScreenState.Empty -> EmptyState(
                        modifier = Modifier.padding(paddingValues),
                        personStatus = targetState.personStatus,
                        seenNamesClicked = {
                            navigator.push(SeenNamesScreen(it))
                        },
                        filterClicked = {
                            navigator.push(FilterScreen())
                        },
                        connectionClicked = {
                            navigator.push(ConnectionScreen())
                        },
                        connectionId = targetState.connectionCode,
                    )
                }
            }
        }
    }

    @Composable
    private fun PopupMenu(
        sortClicked: (NameSort) -> Unit,
    ) {
        val expanded = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd),
        ) {
            IconButton(onClick = { expanded.value = expanded.value.not() }) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Sort by:",
                )

                Divider(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                )

                NameSort.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(it.display) },
                        onClick = {
                            sortClicked(it)
                            expanded.value = false
                        },
                    )

                    if (it != NameSort.entries.last()) {
                        Divider(
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

    @Composable
    fun ContentState(
        data: List<Pair<String, List<NameYearData>>>,
        nameRemoved: (String, String, Boolean) -> Unit,
        seenNamesClicked: (String?) -> Unit,
        connectionClicked: () -> Unit,
        filterClicked: () -> Unit,
        endReached: () -> Unit,
        modifier: Modifier = Modifier,
        learnMore: (Pair<String, List<NameYearData>>) -> Unit,
        personStatus: Pair<Boolean, Boolean>,
        connectionId: String?,
        lastName: String?,
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            TwyperPreview(
                data = data,
                nameRemoved = nameRemoved,
                seenNamesClicked = seenNamesClicked,
                filterClicked = filterClicked,
                endReached = endReached,
                connectionClicked = connectionClicked,
                learnMore = learnMore,
                personStatus = personStatus,
                connectionId = connectionId,
                lastName = lastName,
            )
        }
    }

    @Composable
    fun TwyperPreview(
        data: List<Pair<String, List<NameYearData>>>,
        nameRemoved: (String, String, Boolean) -> Unit,
        seenNamesClicked: (String?) -> Unit,
        filterClicked: () -> Unit,
        connectionClicked: () -> Unit,
        endReached: () -> Unit,
        learnMore: (Pair<String, List<NameYearData>>) -> Unit,
        personStatus: Pair<Boolean, Boolean>,
        connectionId: String?,
        lastName: String?,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HeaderColumn(
                filterClicked = filterClicked,
                seenNamesClicked = seenNamesClicked,
                connectionClicked = connectionClicked,
                personStatus = personStatus,
                connectionId = connectionId,
            )

            Column(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val twyperController = rememberTwyperFlipController()
                val items = remember { mutableStateListOf(*data.toTypedArray()) }

                val generateBoxModifier: () -> Modifier = {
                    Modifier
                }

                TwyperFlip(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    items = items,
                    twyperFlipController = twyperController,
                    onItemRemoved = { item, direction ->
                        nameRemoved(
                            item.first,
                            item.second.first().genderAbbreviation,
                            when (direction) {
                                SwipedOutDirection.LEFT -> false
                                SwipedOutDirection.RIGHT -> true
                            },
                        )
                        items.remove(item)
                    },
                    cardModifier = generateBoxModifier,
                    onEmpty = {
                        endReached()
                    },
                    front = { item ->
                        Box(
                            modifier = Modifier
                                .border(
                                    4.dp,
                                    Brush.sweepGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                        ),
                                    ),
                                    RoundedCornerShape(4.dp),
                                )
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                        ),
                                    ),
                                )
                                .aspectRatio(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = item.first,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )

                                if (!lastName.isNullOrEmpty()) {
                                    Text(
                                        text = lastName.capitalize(Locale.current),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    },
                    back = { item ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary,
                                        ),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            PrimaryButton(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                ),
                                onClick = debouncedClick {
                                    learnMore(item)
                                },
                            ) {
                                Text(
                                    text = "Learn More",
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(50.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    Button(
                        onClick = {
                            twyperController.swipeLeft()
                        },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbDown,
                            contentDescription = null,
                        )
                    }

                    Button(
                        onClick = {
                            twyperController.flip()
                        },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flip,
                            contentDescription = null,
                        )
                    }

                    Button(
                        onClick = {
                            twyperController.swipeRight()
                        },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun EmptyState(
        modifier: Modifier = Modifier,
        filterClicked: () -> Unit,
        seenNamesClicked: (String?) -> Unit,
        connectionId: String?,
        connectionClicked: () -> Unit,
        personStatus: Pair<Boolean, Boolean>,
    ) {
        Box(
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            HeaderColumn(
                modifier = Modifier.align(Alignment.TopCenter),
                filterClicked = filterClicked,
                seenNamesClicked = seenNamesClicked,
                connectionClicked = connectionClicked,
                personStatus = personStatus,
                connectionId = connectionId,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "No names found! Try adjusting the filters",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HeaderColumn(
        modifier: Modifier = Modifier,
        connectionClicked: () -> Unit,
        filterClicked: () -> Unit,
        connectionId: String?,
        seenNamesClicked: (String?) -> Unit,
        personStatus: Pair<Boolean, Boolean>,
    ) {
        Row(
            modifier = modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (personStatus.first || personStatus.second) {
                BadgedBox(
                    modifier = Modifier.padding(16.dp),
                    badge = {
                        Badge(
                            backgroundColor = MaterialTheme.colorScheme.secondary,
                        ) {
                            Text(
                                text = "New",
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                    },
                ) {
                    FavoriteIcon(
                        iconSize = 48.dp,
                        seenNamesClicked = seenNamesClicked,
                        connectionId = connectionId,
                    )
                }
            } else {
                FavoriteIcon(
                    modifier = Modifier.padding(16.dp),
                    iconSize = 32.dp,
                    seenNamesClicked = seenNamesClicked,
                    connectionId = connectionId,
                )
            }

            IconButton(
                onClick = debouncedClick {
                    connectionClicked()
                },
            ) {
                Icon(
                    modifier = Modifier.padding(16.dp).size(32.dp),
                    imageVector = Icons.Filled.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }

            IconButton(
                onClick = debouncedClick {
                    filterClicked()
                },
            ) {
                Icon(
                    modifier = Modifier.padding(16.dp).size(32.dp),
                    imageVector = Icons.Filled.FilterAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }

    @Composable
    private fun FavoriteIcon(
        modifier: Modifier = Modifier,
        seenNamesClicked: (String?) -> Unit,
        connectionId: String?,
        iconSize: Dp,
    ) {
        Icon(
            modifier = modifier.size(iconSize).clickable(
                onClick = debouncedClick {
                    seenNamesClicked(connectionId)
                },
            ),
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * todo joer custom font
 */
