package com.joetr.bundle.ui.connection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.bundle.coroutineextensions.collectAsEffect
import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.design.button.PrimaryButton
import com.joetr.bundle.design.theme.ErrorState
import com.joetr.bundle.design.theme.LoadingState
import com.joetr.bundle.design.toolbar.DefaultToolbar
import com.joetr.bundle.design.toolbar.backOrNull
import com.joetr.bundle.ui.info.InfoScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectionScreen() : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ConnectionScreenModel>()
        val state = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffect(
            onStarted = {
                screenModel.init()
            },
        )

        val showDeleteConnectionConfirmation = remember {
            mutableStateOf(false)
        }

        val connectionAlreadyHasPartnerDialog = remember {
            mutableStateOf(false)
        }

        val connectionCodeDoesNotExistDialog = remember {
            mutableStateOf(false)
        }

        val connectionCode = remember {
            mutableStateOf("")
        }

        screenModel.action.collectAsEffect {
            when (it) {
                ConnectionScreenAction.DeleteConnection -> {
                    showDeleteConnectionConfirmation.value = true
                }

                ConnectionScreenAction.ConnectionAlreadyHasPartner -> {
                    connectionAlreadyHasPartnerDialog.value = true
                }

                ConnectionScreenAction.ConnectionCodeDoesNotExist -> {
                    connectionCodeDoesNotExistDialog.value = true
                }
            }
        }

        if (showDeleteConnectionConfirmation.value) {
            DefaultWarningDialog(
                title = "Confirmation",
                confirmText = "Confirm",
                dismissText = "Dismiss",
                text = "This will delete the connection between you and your partner",
                onDismiss = {
                    showDeleteConnectionConfirmation.value = false
                    screenModel.init()
                },
                onConfirm = {
                    showDeleteConnectionConfirmation.value = false
                    screenModel.deleteConnection()
                },
            )
        }

        // todo joer remove okay / dismiss and only allow for one action dialog

        if (connectionAlreadyHasPartnerDialog.value) {
            DefaultWarningDialog(
                title = "Alert",
                confirmText = "Okay",
                dismissText = "Dismiss",
                text = "This connection already has a partner. Please create a new connection with your partner",
                onDismiss = {
                    connectionAlreadyHasPartnerDialog.value = false
                    screenModel.init()
                },
                onConfirm = {
                    connectionAlreadyHasPartnerDialog.value = false
                    screenModel.init()
                },
            )
        }

        if (connectionCodeDoesNotExistDialog.value) {
            DefaultWarningDialog(
                title = "Alert",
                confirmText = "Okay",
                dismissText = "Dismiss",
                text = "This connection code does not exist",
                onDismiss = {
                    connectionCodeDoesNotExistDialog.value = false
                    screenModel.init()
                },
                onConfirm = {
                    connectionCodeDoesNotExistDialog.value = false
                    screenModel.init()
                },
            )
        }

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
                    is ConnectionScreenState.Error -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is ConnectionScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is ConnectionScreenState.Content -> {
                        val lastName = remember {
                            mutableStateOf(
                                targetState.lastName ?: "",
                            )
                        }
                        ContentState(
                            modifier = Modifier.padding(paddingValues),
                            connection = targetState.connection,
                            createConnection = {
                                screenModel.createConnection(
                                    lastName = lastName.value,
                                )
                            },
                            connectWithPartner = {
                                screenModel.connectWithPartner(it, lastName.value)
                            },
                            disconnectFromPartner = {
                                screenModel.disconnectFromPartner()
                            },
                            onTextChange = {
                                connectionCode.value = it
                            },
                            connectionCodeText = connectionCode.value,
                            saveLastName = {
                                screenModel.saveLastName(it)
                            },
                            lastNameText = lastName.value,
                            onLastNameTextChange = {
                                lastName.value = it
                            },
                            nameInformationClicked = {
                                navigator.push(InfoScreen())
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DefaultWarningDialog(
        onDismiss: () -> Unit,
        onConfirm: () -> Unit,
        confirmText: String,
        dismissText: String,
        title: String,
        text: String,
    ) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                )
            },
            title = {
                Text(title)
            },
            text = {
                Text(text)
            },
            confirmButton = {
                PrimaryButton(
                    onClick = {
                        onConfirm()
                    },
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                PrimaryButton(
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Text(dismissText)
                }
            },
        )
    }

    @Composable
    fun ContentState(
        modifier: Modifier,
        nameInformationClicked: () -> Unit,
        createConnection: () -> Unit,
        disconnectFromPartner: () -> Unit,
        connectWithPartner: (String) -> Unit,
        connection: Connection?,
        connectionCodeText: String,
        onTextChange: (String) -> Unit,
        lastNameText: String,
        onLastNameTextChange: (String) -> Unit,
        saveLastName: (String) -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        ) {
            val createConnectionText = if (connection == null) {
                "Create Connection To Share With Your Partner"
            } else {
                "Connected! Connection Code: ${connection.id}"
            }

            ConnectionScreenItem(
                text = createConnectionText,
                icon = Icons.Default.Public,
                showChevron = connection == null,
                onClick = {
                    if (connection == null) {
                        createConnection()
                    }
                },
            )

            val textFieldVisible = remember {
                mutableStateOf(false)
            }

            if (connection == null) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                val connectWithPartnerText = if (connection?.personTwo == null) {
                    "Enter Connection Code"
                } else {
                    "Connected with partner!"
                }

                ConnectionScreenItem(
                    text = connectWithPartnerText,
                    isExpanded = textFieldVisible.value,
                    onClick = {
                        textFieldVisible.value = textFieldVisible.value.not()
                    },
                )
            }

            AnimatedVisibility(
                visible = textFieldVisible.value,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = connectionCodeText,
                        onValueChange = onTextChange,
                        label = {
                            Text("Connection Code")
                        },
                    )

                    IconButton(
                        onClick = {
                            if (connectionCodeText.isNotEmpty()) {
                                connectWithPartner(connectionCodeText)
                            }
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        content = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }
            }

            if (connection != null) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                val disconnectText = if (connection.personTwo != null) {
                    "Disconnect from partner"
                } else {
                    "Delete connection"
                }
                ConnectionScreenItem(
                    text = disconnectText,
                    onClick = {
                        disconnectFromPartner()
                    },
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            val lastNameFieldVisible = remember {
                mutableStateOf(false)
            }

            ConnectionScreenItem(
                text = "Enter baby's last name to display alongside first names",
                isExpanded = lastNameFieldVisible.value,
                onClick = {
                    lastNameFieldVisible.value = lastNameFieldVisible.value.not()
                },
            )

            AnimatedVisibility(
                visible = lastNameFieldVisible.value,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = lastNameText,
                        onValueChange = onLastNameTextChange,
                        label = {
                            Text("Last Name")
                        },
                    )

                    AnimatedSaveIcon(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                        onClick = {
                            saveLastName(lastNameText)
                        },
                    )
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            ConnectionScreenItem(
                text = "Name information",
                onClick = {
                    nameInformationClicked()
                },
            )
        }
    }

    @Composable
    private fun AnimatedSaveIcon(
        modifier: Modifier,
        onClick: () -> Unit,
    ) {
        val coroutineScope = rememberCoroutineScope()

        val iconMorphState =
            remember {
                mutableStateOf(false)
            }

        Crossfade(
            modifier = modifier,
            targetState = iconMorphState.value,
            animationSpec = tween(1000),
            label = "save icon fade",
        ) { targetState ->
            val icon = if (targetState) Icons.Outlined.Check else Icons.Outlined.Save
            Icon(
                modifier =
                Modifier
                    .size(32.dp)
                    .clickable {
                        // trigger animation
                        iconMorphState.value = true
                        onClick()

                        // return to normal after delay
                        coroutineScope.launch {
                            delay(1000)
                            iconMorphState.value = false
                        }
                    },
                imageVector = icon,
                contentDescription = "save",
            )
        }
    }

    @Composable
    fun ConnectionScreenItem(
        text: String,
        onClick: () -> Unit,
        showChevron: Boolean = true,
        icon: ImageVector = Icons.Default.ChevronRight,
        isExpanded: Boolean = false,
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
                val rotation: Float by animateFloatAsState(if (isExpanded) 90f else 0f)
                Icon(
                    modifier = Modifier.padding(start = 4.dp).rotate(rotation),
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        }
    }
}
