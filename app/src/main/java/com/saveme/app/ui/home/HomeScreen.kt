package com.saveme.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.ui.components.BrandMark
import com.saveme.app.ui.components.CollectionCard
import com.saveme.app.ui.components.CollectionEditorDialog
import com.saveme.app.ui.components.CollectionPickerDialog
import com.saveme.app.ui.components.EmptyState
import com.saveme.app.ui.components.NeoAlertDialog
import com.saveme.app.ui.components.NeoBanner
import com.saveme.app.ui.components.NeoConfirmDialog
import com.saveme.app.ui.components.NeoIconButton
import com.saveme.app.ui.components.NeoMenuDialog
import com.saveme.app.ui.components.NeoMenuItem
import com.saveme.app.ui.components.NeoTextField
import com.saveme.app.ui.components.cardGridCells
import com.saveme.app.ui.components.cardGridSpacing
import com.saveme.app.ui.components.gridContentPadding
import com.saveme.app.ui.components.neoClickable
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.DisplayStyle
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.SectionTitleStyle
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.util.readClipboardText

/**
 * Layar utama: kolom tempel cepat di atas, lalu petak koleksi.
 * Semua pembatas versi berbayar dihilangkan, jadi jumlah koleksi bebas.
 */
@Composable
fun HomeScreen(
    onOpenCollection: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showQuickPicker by remember { mutableStateOf(false) }
    var showManage by remember { mutableStateOf(false) }
    var editorTarget by remember { mutableStateOf<EditorTarget?>(null) }
    var pendingDelete by remember { mutableStateOf<CollectionEntity?>(null) }


    Box(Modifier.fillMaxSize()) {
        val spacing = cardGridSpacing()
        LazyVerticalGrid(
            columns = cardGridCells(),
            contentPadding = gridContentPadding(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeoIconButton(
                            icon = AppIcons.Settings,
                            contentDescription = "Settings",
                            onClick = onOpenSettings,
                        )
                        Spacer(Modifier.weight(1f))
                        NeoIconButton(
                            icon = AppIcons.FolderCog,
                            contentDescription = "Manage collections",
                            onClick = { showManage = true },
                        )
                        Spacer(Modifier.width(6.dp))
                        NeoIconButton(
                            icon = AppIcons.Search,
                            contentDescription = "Search",
                            onClick = onOpenSearch,
                        )
                    }

                    Spacer(Modifier.height(22.dp))
                    Text("SAVE ME\nBOOKMARK", style = DisplayStyle, color = Ink)
                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeoTextField(
                            value = viewModel.quickUrl,
                            onValueChange = viewModel::onQuickUrlChange,
                            placeholder = "Paste link to quick save",
                            leadingIcon = AppIcons.Hash,
                            modifier = Modifier.weight(1f),
                            onImeAction = {
                                if (viewModel.quickUrlIsUsable()) showQuickPicker = true
                            },
                            trailing = {
                                // Ikon polos tanpa bingkai: tombol di dalam kolom teks
                                // akan tampak seperti kotak bertumpuk kalau diberi tepi.
                                Icon(
                                    imageVector = AppIcons.Clipboard,
                                    contentDescription = "Paste from clipboard",
                                    tint = Ink,
                                    modifier = Modifier
                                        .size(21.dp)
                                        .neoClickable {
                                            readClipboardText(context)
                                                ?.let(viewModel::onQuickUrlChange)
                                        },
                                )
                            },
                        )
                        Spacer(Modifier.width(10.dp))
                        NeoIconButton(
                            icon = AppIcons.Plus,
                            contentDescription = "Save link",
                            onClick = { if (viewModel.quickUrlIsUsable()) showQuickPicker = true },
                            background = Sunny,
                            size = 56.dp,
                            iconSize = 26.dp,
                        )
                    }

                    Spacer(Modifier.height(26.dp))
                    Text("MY COLLECTIONS", style = SectionTitleStyle, color = Ink)
                    Spacer(Modifier.height(4.dp))
                }
            }

            if (collections.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "No collections yet",
                        message = "Collections keep your links sorted. Make the first one.",
                        actionLabel = "New collection",
                        onAction = { editorTarget = EditorTarget.New(null) },
                        art = { BrandMark(size = 108.dp) },
                    )
                }
            } else {
                items(collections, key = { it.collection.id }) { summary ->
                    CollectionCard(
                        summary = summary,
                        onClick = { onOpenCollection(summary.collection.id) },
                        onLongClick = { editorTarget = EditorTarget.Edit(summary.collection) },
                    )
                }
            }
        }

        NeoBanner(
            message = viewModel.banner,
            onDismiss = { viewModel.banner = null },
        )
    }

    // ------------------------------------------------------------- dialog

    viewModel.alert?.let { alert ->
        NeoAlertDialog(
            title = alert.title,
            message = alert.message,
            confirmLabel = "OK",
            onDismiss = { viewModel.alert = null },
        )
    }

    if (showQuickPicker) {
        CollectionPickerDialog(
            title = "Save to",
            collections = allCollections,
            onDismiss = { showQuickPicker = false },
            onPick = { id ->
                val name = allCollections.firstOrNull { it.id == id }?.name.orEmpty()
                viewModel.saveQuickUrl(id, name)
            },
            onCreateNew = { editorTarget = EditorTarget.New(null) },
        )
    }


    if (showManage) {
        NeoMenuDialog(
            title = "Collections",
            items = buildList {
                add(
                    NeoMenuItem(AppIcons.Plus, "New collection") {
                        editorTarget = EditorTarget.New(null)
                    },
                )
                allCollections.forEach { collection ->
                    add(
                        NeoMenuItem(
                            com.saveme.app.ui.theme.collectionIcon(collection.iconKey),
                            "Edit \"${collection.name}\"",
                        ) { editorTarget = EditorTarget.Edit(collection) },
                    )
                }
            },
            onDismiss = { showManage = false },
        )
    }

    editorTarget?.let { target ->
        val existing = (target as? EditorTarget.Edit)?.collection
        CollectionEditorDialog(
            initial = existing,
            onDismiss = { editorTarget = null },
            onDelete = existing?.let {
                {
                    editorTarget = null
                    pendingDelete = it
                }
            },
            onSave = { name, colorKey, iconKey ->
                if (existing != null) {
                    viewModel.updateCollection(existing, name, colorKey, iconKey)
                } else {
                    viewModel.createCollection(
                        name,
                        colorKey,
                        iconKey,
                        (target as EditorTarget.New).parentId,
                    )
                }
                editorTarget = null
            },
        )
    }

    pendingDelete?.let { collection ->
        NeoConfirmDialog(
            title = "Delete collection?",
            message = "\"${collection.name}\" and every link inside it will be removed.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteCollection(collection.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

private sealed interface EditorTarget {
    data class New(val parentId: Long?) : EditorTarget
    data class Edit(val collection: CollectionEntity) : EditorTarget
}
