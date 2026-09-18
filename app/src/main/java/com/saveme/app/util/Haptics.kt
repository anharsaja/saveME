package com.saveme.app.util

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Pembungkus getaran yang menghormati sakelar "Haptic Feedback" di Settings.
 * Semua sentuhan di aplikasi lewat sini, bukan memanggil HapticFeedback langsung.
 */
class Haptics(
    private val feedback: HapticFeedback? = null,
    private val enabled: Boolean = false,
) {
    fun tick() {
        if (enabled) feedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun longPress() {
        if (enabled) feedback?.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

val LocalHaptics = staticCompositionLocalOf { Haptics() }
