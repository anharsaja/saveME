package com.saveme.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.Motion
import kotlinx.coroutines.delay

/**
 * Pesan singkat yang muncul dari bawah layar setelah sebuah aksi berhasil,
 * lalu menghilang sendiri.
 */
@Composable
fun BoxScope.NeoBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.CheckCircle,
    durationMillis: Long = 2600,
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(durationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(Motion.QuickOffset) { it } + fadeIn(Motion.QuickFloat),
        exit = slideOutVertically(Motion.QuickOffset) { it } + fadeOut(Motion.QuickFloat),
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        NeoSurface(
            background = Mint,
            radius = NeoRadius.Card,
            onClick = onDismiss,
            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 13.dp),
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Ink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(11.dp))
                Box(Modifier.weight(1f)) {
                    Text(message.orEmpty(), style = BodyStyle, color = Ink)
                }
            }
        }
    }
}
