package com.saveme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.CardTitleStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Danger
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.PaperDim
import com.saveme.app.ui.theme.ScreenTitleStyle
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.ui.theme.readableOn
import com.saveme.app.ui.theme.softTint

/** Kerangka bersama semua dialog: latar gelap, kartu putih bergaris tebal. */
@Composable
fun NeoDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnClickOutside: Boolean = true,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            contentAlignment = Alignment.Center,
        ) {
            NeoSurface(
                modifier = modifier.fillMaxWidth(),
                radius = 20.dp,
                borderWidth = 3.dp,
                shadowOffset = 6.dp,
                contentPadding = PaddingValues(20.dp),
            ) {
                content()
            }
        }
    }
}

/**
 * Peringatan singkat dengan satu tombol, seperti pesan "No URL" saat kolom
 * tempel tautan masih kosong.
 */
@Composable
fun NeoAlertDialog(
    title: String,
    message: String?,
    confirmLabel: String,
    onDismiss: () -> Unit,
    icon: ImageVector = AppIcons.AlertCircle,
    iconTint: Color = Sunny,
    onConfirm: (() -> Unit)? = null,
    cancelLabel: String? = null,
    confirmColor: Color = Sunny,
) {
    NeoDialog(onDismissRequest = onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = ScreenTitleStyle, color = Ink, textAlign = TextAlign.Center)
            if (message != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    message,
                    style = BodyStyle,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (cancelLabel != null) {
                    NeoButton(
                        label = cancelLabel,
                        onClick = onDismiss,
                        background = CardWhite,
                        modifier = Modifier.weight(1f),
                        height = 50.dp,
                    )
                }
                NeoButton(
                    label = confirmLabel,
                    onClick = { (onConfirm ?: onDismiss).invoke() },
                    background = confirmColor,
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
            }
        }
    }
}

/** Konfirmasi tindakan merusak: tombol utama merah. */
@Composable
fun NeoConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    NeoAlertDialog(
        title = title,
        message = message,
        confirmLabel = confirmLabel,
        icon = AppIcons.Trash,
        iconTint = Danger,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        cancelLabel = "Cancel",
        confirmColor = Danger,
    )
}

data class NeoMenuItem(
    val icon: ImageVector,
    val label: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

/** Daftar tindakan yang muncul dari tombol titik tiga. */
@Composable
fun NeoMenuDialog(
    title: String,
    items: List<NeoMenuItem>,
    onDismiss: () -> Unit,
) {
    NeoDialog(onDismissRequest = onDismiss) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = ScreenTitleStyle, color = Ink, modifier = Modifier.weight(1f))
                NeoIconButton(
                    icon = AppIcons.Close,
                    contentDescription = "Tutup",
                    onClick = onDismiss,
                    size = 38.dp,
                    iconSize = 17.dp,
                    radius = 11.dp,
                )
            }
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items.forEach { item ->
                    NeoSurface(
                        modifier = Modifier.fillMaxWidth(),
                        radius = 12.dp,
                        background = if (item.destructive) softTint(Danger) else CardWhite,
                        shadowOffset = 3.dp,
                        onClick = {
                            item.onClick()
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                item.icon,
                                null,
                                tint = if (item.destructive) Danger else Ink,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                item.label,
                                style = CardTitleStyle,
                                color = if (item.destructive) Danger else Ink,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Titik warna yang bisa dipilih di editor koleksi. */
@Composable
fun ColorDot(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .background(color, CircleShape)
            .border(if (selected) 3.5.dp else 2.dp, Ink, CircleShape)
            .neoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(AppIcons.Check, null, tint = readableOn(color), modifier = Modifier.size(18.dp))
        }
    }
}

/** Keping ikon yang bisa dipilih di editor koleksi. */
@Composable
fun IconChoice(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(if (selected) Sunny else PaperDim, RoundedCornerShape(12.dp))
            .border(if (selected) 3.dp else 2.dp, Ink, RoundedCornerShape(12.dp))
            .neoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = readableOn(if (selected) Sunny else PaperDim), modifier = Modifier.size(21.dp))
    }
}
