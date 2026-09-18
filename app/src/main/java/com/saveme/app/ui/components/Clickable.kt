package com.saveme.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.saveme.app.ui.theme.Motion
import com.saveme.app.util.LocalHaptics

/**
 * Klik tanpa ripple — riak Material akan merusak tampilan blok warna datar di
 * desain ini. Sebagai gantinya elemen mengecil sedikit selama ditekan, sehingga
 * apa pun yang bisa diketuk memberi jawaban yang sama.
 *
 * [scaleOnPress] dimatikan oleh permukaan yang sudah punya gerakan tekannya
 * sendiri, misalnya [NeoSurface] yang isinya bergeser menimpa bayangan.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.neoClickable(
    interaction: MutableInteractionSource? = null,
    enabled: Boolean = true,
    scaleOnPress: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {
    val source = interaction ?: remember { MutableInteractionSource() }
    val haptics = LocalHaptics.current
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && scaleOnPress) Motion.PressScale else 1f,
        animationSpec = Motion.PressFloat,
        label = "pressScale",
    )

    return this
        .then(
            if (scaleOnPress) {
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            } else {
                Modifier
            },
        )
        .combinedClickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick?.let { action -> { haptics.longPress(); action() } },
            onClick = { haptics.tick(); onClick() },
        )
}
