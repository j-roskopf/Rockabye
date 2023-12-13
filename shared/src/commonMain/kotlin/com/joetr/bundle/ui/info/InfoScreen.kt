package com.joetr.bundle.ui.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.bundle.design.toolbar.DefaultToolbar
import com.joetr.bundle.design.toolbar.backOrNull

class InfoScreen() : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = {
                        navigator.backOrNull()?.invoke()
                    },
                )
            },
        ) { paddingValues ->
            ContentState(
                modifier = Modifier.padding(paddingValues),
            )
        }
    }

    @Composable
    fun ContentState(
        modifier: Modifier,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        ) {
            val ssaWebsiteTag = remember {
                "SSA Website"
            }
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(
                        "The names that appear in this app have been pulled directly from the SSA website.\n\n" +
                            "Every name with more than 5 appearances in a given year is included.\n\n",
                    )
                }

                pushStringAnnotation(
                    tag = ssaWebsiteTag,
                    annotation = "https://www.ssa.gov/oact/babynames/",
                )
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("SSA Website")
                }
            }
            val uriHandler = LocalUriHandler.current

            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = ssaWebsiteTag,
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let {
                        uriHandler.openUri(it.item)
                    }
                },
            )
        }
    }
}
