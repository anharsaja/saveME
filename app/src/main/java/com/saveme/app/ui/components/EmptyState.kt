package com.saveme.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.ScreenTitleStyle

/** Tampilan saat sebuah daftar masih kosong: ilustrasi, ajakan, lalu satu tombol. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    art: @Composable () -> Unit = { EmptyFolderArt() },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        art()
        Spacer(Modifier.height(14.dp))
        Text(title, style = ScreenTitleStyle, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = BodyStyle, color = InkSoft, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            NeoButton(label = actionLabel, onClick = onAction, height = 48.dp)
        }
    }
}
