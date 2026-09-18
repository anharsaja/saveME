package com.saveme.app.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.saveme.app.ui.theme.Motion
import com.saveme.app.util.LocalHaptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Status tertekan yang ditahan sebentar setelah jari terangkat.
 *
 * Satu ketukan cuma berlangsung sekitar 80 milidetik, dan kalau ketukan itu
 * memindahkan halaman, geseran halaman dimulai tepat di saat yang sama. Dengan
 * status mentah dari [InteractionSource], tombolnya memang bergerak — hanya saja
 * gerakan itu selesai sebelum mata sempat menangkapnya. Menahan status selama
 * [Motion.PressHoldMillis] membuat bentuk tertekan tetap terlihat di awal
 * geseran, lalu terlepas dengan pantulan kecil.
 */
@Composable
fun rememberHeldPress(
    source: InteractionSource,
    holdMillis: Long = Motion.PressHoldMillis,
): State<Boolean> {
    val held = remember { mutableStateOf(false) }

    LaunchedEffect(source, holdMillis) {
        val active = mutableListOf<PressInteraction.Press>()
        var pressedAt = 0L
        var release: Job? = null

        source.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    // Ketukan susulan membatalkan pelepasan yang masih menunggu,
                    // jadi mengetuk berkali-kali tidak menimbulkan kedipan.
                    release?.cancel()
                    if (active.isEmpty()) pressedAt = SystemClock.uptimeMillis()
                    active += interaction
                    held.value = true
                }

                is PressInteraction.Release -> active.remove(interaction.press)
                is PressInteraction.Cancel -> active.remove(interaction.press)
                else -> return@collect
            }

            if (active.isEmpty() && held.value) {
                val elapsed = SystemClock.uptimeMillis() - pressedAt
                release = launch {
                    if (elapsed < holdMillis) delay(holdMillis - elapsed)
                    held.value = false
                }
            }
        }
    }

    return held
}

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
    val pressed by rememberHeldPress(source)
    val down = pressed && enabled && scaleOnPress
    val scale by animateFloatAsState(
        targetValue = if (down) Motion.PressScale else 1f,
        animationSpec = if (down) Motion.PressInFloat else Motion.PressOutFloat,
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
