package com.saveme.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saveme.app.data.prefs.ThemeMode
import com.saveme.app.ui.components.Mascot
import com.saveme.app.ui.components.NeoBanner
import com.saveme.app.ui.components.NeoButton
import com.saveme.app.ui.components.NeoChip
import com.saveme.app.ui.components.NeoConfirmDialog
import com.saveme.app.ui.components.NeoIconButton
import com.saveme.app.ui.components.NeoMenuDialog
import com.saveme.app.ui.components.NeoMenuItem
import com.saveme.app.ui.components.NeoSurface
import com.saveme.app.ui.components.NeoSwitch
import com.saveme.app.ui.components.SectionLabel
import com.saveme.app.ui.components.SettingRow
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardTitleStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Danger
import com.saveme.app.ui.theme.DisplayStyle
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.Nunito
import com.saveme.app.ui.theme.OnAccent
import com.saveme.app.ui.theme.ScreenTitleStyle
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.ui.theme.softTint
import com.saveme.app.util.formatDay

/**
 * Pengaturan. Kartu kuning di atas menggantikan kartu paket berbayar milik
 * aplikasi aslinya: tidak ada kuota harian, semua fitur tersedia.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenFocus: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showThemePicker by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.setReminders(context, granted) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeoIconButton(AppIcons.ArrowLeft, "Back", onBack)
                Text(
                    "Settings",
                    style = ScreenTitleStyle,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(end = 52.dp),
                )
            }

            Spacer(Modifier.height(18.dp))
            LibraryCard(
                links = stats.links,
                collections = stats.collections,
                lastBackupAt = settings.lastBackupAt,
                onOpenFocus = onOpenFocus,
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel("Appearance")
            Spacer(Modifier.height(12.dp))
            SettingRow(
                icon = if (settings.themeMode == ThemeMode.DARK) AppIcons.Sparkles else AppIcons.Zap,
                title = "Theme",
                subtitle = settings.themeMode.label,
                onClick = { showThemePicker = true },
                trailing = {
                    Icon(AppIcons.ChevronRight, null, tint = InkSoft, modifier = Modifier.size(20.dp))
                },
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel("Preferences")
            Spacer(Modifier.height(12.dp))
            SettingRow(
                icon = AppIcons.Zap,
                title = "Haptic Feedback",
                subtitle = "Vibrate for actions and selections",
                trailing = {
                    NeoSwitch(
                        checked = settings.hapticsEnabled,
                        onCheckedChange = viewModel::setHaptics,
                    )
                },
            )
            Spacer(Modifier.height(10.dp))
            SettingRow(
                icon = AppIcons.Bell,
                title = "New Link Reminders",
                subtitle = "Gentle reminders to review unread links",
                trailing = {
                    NeoSwitch(
                        checked = settings.remindersEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setReminders(context, enabled)
                            }
                        },
                    )
                },
            )
            if (settings.remindersEnabled) {
                Spacer(Modifier.height(10.dp))
                ReminderHourRow(
                    hour = settings.reminderHour,
                    onChange = { viewModel.setReminderHour(context, it) },
                )
            }
            Spacer(Modifier.height(10.dp))
            SettingRow(
                icon = AppIcons.BookOpen,
                title = "Focus Mode: unread only",
                subtitle = "Review just the links you haven't opened yet",
                trailing = {
                    NeoSwitch(
                        checked = settings.focusUnreadOnly,
                        onCheckedChange = viewModel::setFocusUnreadOnly,
                    )
                },
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel("Your saveME data")
            Spacer(Modifier.height(12.dp))
            SettingRow(
                icon = AppIcons.Download,
                title = "Export Backup",
                subtitle = "Save a backup file you can keep anywhere",
                onClick = { exportLauncher.launch(viewModel.suggestedBackupName()) },
                trailing = {
                    Icon(AppIcons.ChevronRight, null, tint = InkSoft, modifier = Modifier.size(20.dp))
                },
            )
            Spacer(Modifier.height(10.dp))
            SettingRow(
                icon = AppIcons.Upload,
                title = "Restore Backup",
                subtitle = "Replace everything with a backup file",
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                trailing = {
                    Icon(AppIcons.ChevronRight, null, tint = InkSoft, modifier = Modifier.size(20.dp))
                },
            )
            Spacer(Modifier.height(10.dp))
            SettingRow(
                icon = AppIcons.Trash,
                title = "Clear everything",
                subtitle = "Delete all collections, links, and tags",
                iconBackground = softTint(Danger, strength = 0.14f),
                iconTint = Danger,
                onClick = { showClearConfirm = true },
                trailing = {
                    Icon(AppIcons.ChevronRight, null, tint = InkSoft, modifier = Modifier.size(20.dp))
                },
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel("About")
            Spacer(Modifier.height(12.dp))
            NeoSurface(
                modifier = Modifier.fillMaxWidth(),
                radius = 14.dp,
                contentPadding = PaddingValues(16.dp),
            ) {
                Column {
                    Text("saveME", style = CardTitleStyle, color = Ink)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Everything lives on this device. No account, no sync, no ads, " +
                            "nothing locked behind a payment.",
                        style = CaptionStyle,
                        color = InkSoft,
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
        }

        NeoBanner(message = viewModel.banner, onDismiss = { viewModel.banner = null })
    }

    if (showThemePicker) {
        NeoMenuDialog(
            title = "Theme",
            items = ThemeMode.entries.map { mode ->
                NeoMenuItem(
                    icon = if (mode == settings.themeMode) AppIcons.CheckCircle else AppIcons.Clock,
                    label = mode.label,
                ) { viewModel.setThemeMode(mode) }
            },
            onDismiss = { showThemePicker = false },
        )
    }

    if (showClearConfirm) {
        NeoConfirmDialog(
            title = "Clear everything?",
            message = "Every collection, link, tag, and preview will be deleted from this device.",
            confirmLabel = "Clear all",
            onConfirm = {
                showClearConfirm = false
                viewModel.clearEverything()
            },
            onDismiss = { showClearConfirm = false },
        )
    }

    if (showRestoreConfirm) {
        NeoConfirmDialog(
            title = "Restore backup?",
            message = "Your current library will be replaced by the contents of this file.",
            confirmLabel = "Restore",
            onConfirm = {
                showRestoreConfirm = false
                pendingRestoreUri?.let(viewModel::import)
                pendingRestoreUri = null
            },
            onDismiss = {
                showRestoreConfirm = false
                pendingRestoreUri = null
            },
        )
    }
}

/** Pengganti kartu paket: identitas aplikasi plus hitungan isi pustaka. */
@Composable
private fun LibraryCard(
    links: Int,
    collections: Int,
    lastBackupAt: Long,
    onOpenFocus: () -> Unit,
) {
    NeoSurface(
        modifier = Modifier.fillMaxWidth(),
        background = Sunny,
        radius = 20.dp,
        borderWidth = 3.dp,
        shadowOffset = 5.dp,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    SectionLabel("Your library", color = OnAccent.copy(alpha = 0.65f))
                    Spacer(Modifier.height(6.dp))
                    Text("saveME", style = DisplayStyle.copy(fontSize = 30.sp), color = OnAccent)
                    Spacer(Modifier.height(2.dp))
                    Text("Save smarter, every day.", style = BodyStyle, color = OnAccent.copy(alpha = 0.72f))
                }
                Mascot(size = 82.dp)
            }

            Spacer(Modifier.height(16.dp))
            NeoSurface(
                modifier = Modifier.fillMaxWidth(),
                radius = 14.dp,
                shadowOffset = 0.dp,
                contentPadding = PaddingValues(15.dp),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("Everything unlocked", modifier = Modifier.weight(1f))
                        NeoChip(
                            label = "No limits",
                            background = Mint,
                            leadingIcon = AppIcons.CheckCircle,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    CountRow(AppIcons.Link, "Links saved", links)
                    Spacer(Modifier.height(11.dp))
                    CountRow(AppIcons.Folder, "Collections", collections)
                    Spacer(Modifier.height(11.dp))
                    Text(
                        if (lastBackupAt > 0) {
                            "Last backup · ${formatDay(lastBackupAt)}"
                        } else {
                            "No backup yet"
                        },
                        style = CaptionStyle,
                        color = InkSoft,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            NeoButton(
                label = "Open focus mode",
                onClick = onOpenFocus,
                trailingIcon = AppIcons.ChevronRight,
                background = com.saveme.app.ui.theme.SunnySoft,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp,
            )
        }
    }
}

@Composable
private fun CountRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(34.dp)
                .background(com.saveme.app.ui.theme.PaperDim, RoundedCornerShape(9.dp))
                .border(2.dp, Ink.copy(alpha = 0.14f), RoundedCornerShape(9.dp)),
        ) {
            Icon(icon, null, tint = Ink, modifier = Modifier.align(Alignment.Center).size(17.dp))
        }
        Spacer(Modifier.width(11.dp))
        Text(label, style = CardTitleStyle, color = Ink, modifier = Modifier.weight(1f))
        Text(
            value.toString(),
            fontFamily = Nunito,
            fontWeight = androidx.compose.ui.text.font.FontWeight.W800,
            fontSize = 18.sp,
            color = Ink,
        )
    }
}

/** Pemilih jam pengingat, bergerak per satu jam. */
@Composable
private fun ReminderHourRow(hour: Int, onChange: (Int) -> Unit) {
    SettingRow(
        icon = AppIcons.Clock,
        title = "Reminder time",
        subtitle = "Every day at ${"%02d".format(hour)}:00",
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NeoIconButton(
                    icon = AppIcons.ChevronDown,
                    contentDescription = "Earlier",
                    onClick = { onChange((hour + 23) % 24) },
                    size = 34.dp,
                    iconSize = 15.dp,
                    radius = 10.dp,
                    background = CardWhite,
                )
                NeoIconButton(
                    icon = AppIcons.ChevronRight,
                    contentDescription = "Later",
                    onClick = { onChange((hour + 1) % 24) },
                    size = 34.dp,
                    iconSize = 15.dp,
                    radius = 10.dp,
                    background = CardWhite,
                )
            }
        },
    )
}
