package com.joetr.bundle.design.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * To control the Twyper.
 * Used to swipe card programmatically
 */
@Composable
fun rememberTwyperController(): TwyperController {
    return remember { TwyperControllerImpl() }
}

interface TwyperController {
    /**
     * Points to the top card's [CardController]
     */
    var currentCardController: CardController?
    fun swipeRight()
    fun swipeLeft()
}

class TwyperControllerImpl : TwyperController {
    override var currentCardController: CardController? = null

    override fun swipeRight() {
        currentCardController?.swipeRight()
    }

    override fun swipeLeft() {
        currentCardController?.swipeLeft()
    }
}
