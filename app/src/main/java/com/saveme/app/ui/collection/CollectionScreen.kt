package com.saveme.app.ui.collection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saveme.app.ui.components.CollectionCard
import com.saveme.app.ui.components.CollectionEditorDialog
import com.saveme.app.ui.components.CollectionPickerDialog
import com.saveme.app.ui.components.EmptyState
import com.saveme.app.ui.components.LinkCard
import com.saveme.app.ui.components.NeoAlertDialog
import com.saveme.app.ui.components.NeoBanner
import com.saveme.app.ui.components.NeoButton
import com.saveme.app.ui.components.NeoConfirmDialog
import com.saveme.app.ui.components.NeoDialog
import com.saveme.app.ui.components.NeoIconButton
import com.saveme.app.ui.components.NeoMenuDialog
import com.saveme.app.ui.components.NeoMenuItem
import com.saveme.app.ui.components.NeoTextField
import com.saveme.app.ui.components.SectionLabel
import com.saveme.app.ui.components.cardGridCells
import com.saveme.app.ui.components.cardGridSpacing
import com.saveme.app.ui.components.gridContentPadding
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Danger
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.ScreenTitleStyle
import com.saveme.app.ui.theme.Sunny

/**
 * Isi satu koleksi: subkoleksi di atas, lalu petak kartu link.
 * Tombol kuning di kanan atas menyalakan mode pilih banyak.
 */
@Composable
fun CollectionScreen(
    collectionId: Long,
    onBack: () -> Unit,
    onOpenLink: (Long) -> Unit,
    onOpenCollection: (Long) -> Unit,
    viewModel: CollectionViewModel = viewModel(
        key = "collection-$collectionId",
        factory = CollectionViewModel.factory(collectionId),
    ),
) {
    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val links by viewModel.links.collectAsStateWithLifecycle()
    val children by viewModel.children.collectAsStateWithLifecycle()
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }
    var showSubEditor by remember { mutableStateOf(false) }
    var showMovePicker by remember { mutableStateOf(false) }
    var showAddLink by remember { mutableStateOf(false) }
    var showDeleteCollection by remember { mutableStateOf(false) }
    var showDeleteLinks by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var invalidUrlAlert by remember { mutableStateOf(false) }

    BackHandler(enabled = viewModel.selectionMode) { viewModel.exitSelection() }

    Box(Modifier.fillMaxSize()) {
        val spacing = cardGridSpacing()
        LazyVerticalGrid(
            columns = cardGridCells(),
            contentPadding = gridContentPadding(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    if (viewModel.selectionMode) {
                        SelectionTopBar(
                            count = viewModel.selected.size,
                            onCancel = viewModel::exitSelection,
                            onSelectAll = viewModel::selectAll,
                            onMove = { if (viewModel.selected.isNotEmpty()) showMovePicker = true },
                            onDelete = { if (viewModel.selected.isNotEmpty()) showDeleteLinks = true },
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NeoIconButton(
                                icon = AppIcons.ArrowLeft,
                                contentDescription = "Back",
                                onClick = onBack,
                            )
                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    collection?.name.orEmpty(),
                                    style = ScreenTitleStyle,
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    buildString {
                                        append(links.size)
                                        append(if (links.size == 1) " link" else " links")
                                        append(" · ")
                                        append(children.size)
                                        append(if (children.size == 1) " subcollection" else " subcollections")
                                    },
                                    style = CaptionStyle,
                                    color = InkSoft,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            NeoIconButton(
                                icon = AppIcons.Layers,
                                contentDescription = "Select links",
                                onClick = { viewModel.enterSelection() },
                                background = Sunny,
                            )
                            Spacer(Modifier.width(6.dp))
                            NeoIconButton(
                                icon = AppIcons.MoreVertical,
                                contentDescription = "More",
                                onClick = { showMenu = true },
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }

            if (children.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        SectionLabel("Subcollections (${children.size})", icon = AppIcons.Folder)
                        Spacer(Modifier.height(10.dp))
                    }
                }
                items(children, key = { "child-${it.collection.id}" }) { child ->
                    CollectionCard(
                        summary = child,
                        onClick = { onOpenCollection(child.collection.id) },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(10.dp)) }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SectionLabel("All links (${links.size})")
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (links.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "No links yet",
                        message = "Tap + or share to save one.",
                        actionLabel = "See how",
                        onAction = { showHelp = true },
                    )
                }
            } else {
                items(links, key = { it.link.id }) { item ->
                    LinkCard(
                        item = item,
                        selectionMode = viewModel.selectionMode,
                        selected = item.link.id in viewModel.selected,
                        onClick = {
                            if (viewModel.selectionMode) {
                                viewModel.toggleSelect(item.link.id)
                            } else {
                                onOpenLink(item.link.id)
                            }
                        },
                        onLongClick = { viewModel.enterSelection(item.link.id) },
                    )
                }
            }
        }

        NeoBanner(message = viewModel.banner, onDismiss = { viewModel.banner = null })
    }

    // ------------------------------------------------------------- dialog

    if (showMenu) {
        NeoMenuDialog(
            title = collection?.name.orEmpty(),
            items = listOf(
                NeoMenuItem(AppIcons.Plus, "Add link here") { showAddLink = true },
                NeoMenuItem(AppIcons.Folder, "New subcollection") { showSubEditor = true },
                NeoMenuItem(AppIcons.Pencil, "Edit collection") { showEditor = true },
                NeoMenuItem(AppIcons.Filter, "Sort: ${sortOrder.label}") { showSort = true },
                NeoMenuItem(AppIcons.Layers, "Select links") { viewModel.enterSelection() },
                NeoMenuItem(AppIcons.Trash, "Delete collection", destructive = true) {
                    showDeleteCollection = true
                },
            ),
            onDismiss = { showMenu = false },
        )
    }

    if (showSort) {
        NeoMenuDialog(
            title = "Sort links",
            items = CollectionViewModel.SortOrder.entries.map { order ->
                NeoMenuItem(
                    icon = if (order == sortOrder) AppIcons.CheckCircle else AppIcons.Clock,
                    label = order.label,
                ) { viewModel.setSortOrder(order) }
            },
            onDismiss = { showSort = false },
        )
    }

    if (showEditor) {
        CollectionEditorDialog(
            initial = collection,
            onDismiss = { showEditor = false },
            onSave = { name, colorKey, iconKey ->
                viewModel.updateCollection(name, colorKey, iconKey)
                showEditor = false
            },
        )
    }

    if (showSubEditor) {
        CollectionEditorDialog(
            initial = null,
            parentName = collection?.name,
            onDismiss = { showSubEditor = false },
            onSave = { name, colorKey, iconKey ->
                viewModel.createSubcollection(name, colorKey, iconKey)
                showSubEditor = false
            },
        )
    }

    if (showMovePicker) {
        CollectionPickerDialog(
            title = "Move ${viewModel.selected.size} to",
            collections = allCollections,
            excludeIds = setOf(collectionId),
            onDismiss = { showMovePicker = false },
            onPick = { id ->
                val name = allCollections.firstOrNull { it.id == id }?.name.orEmpty()
                viewModel.moveSelected(id, name)
            },
        )
    }

    if (showAddLink) {
        AddLinkDialog(
            onDismiss = { showAddLink = false },
            onSave = { url ->
                viewModel.addLink(url) { invalidUrlAlert = true }
                showAddLink = false
            },
        )
    }

    if (showDeleteCollection) {
        NeoConfirmDialog(
            title = "Delete collection?",
            message = "\"${collection?.name}\" and every link inside it will be removed.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteCollection = false
                viewModel.deleteCollection(onBack)
            },
            onDismiss = { showDeleteCollection = false },
        )
    }

    if (showDeleteLinks) {
        NeoConfirmDialog(
            title = "Delete ${viewModel.selected.size} links?",
            message = "This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteLinks = false
                viewModel.deleteSelected()
            },
            onDismiss = { showDeleteLinks = false },
        )
    }

    if (showHelp) {
        NeoAlertDialog(
            title = "Two ways to save",
            message = "1. Paste a link on the home screen and tap +.\n\n" +
                "2. In any app, tap Share and pick saveME. The link lands straight in " +
                "the collection you choose.",
            confirmLabel = "Got it",
            icon = AppIcons.Share,
            iconTint = Ink,
            onDismiss = { showHelp = false },
        )
    }

    if (invalidUrlAlert) {
        NeoAlertDialog(
            title = "That's not a link",
            message = "Check the address and try again",
            confirmLabel = "OK",
            onDismiss = { invalidUrlAlert = false },
        )
    }
}

@Composable
private fun SelectionTopBar(
    count: Int,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NeoIconButton(
            icon = AppIcons.Close,
            contentDescription = "Cancel selection",
            onClick = onCancel,
        )
        Text(
            "$count selected",
            style = ScreenTitleStyle,
            color = Ink,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
        )
        NeoIconButton(
            icon = AppIcons.Check,
            contentDescription = "Select all",
            onClick = onSelectAll,
        )
        Spacer(Modifier.width(6.dp))
        NeoIconButton(
            icon = AppIcons.Move,
            contentDescription = "Move",
            onClick = onMove,
            background = Sunny,
        )
        Spacer(Modifier.width(6.dp))
        NeoIconButton(
            icon = AppIcons.Trash,
            contentDescription = "Delete",
            onClick = onDelete,
            background = Danger,
        )
    }
}

@Composable
private fun AddLinkDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    NeoDialog(onDismissRequest = onDismiss) {
        Column {
            Text("Add a link", style = ScreenTitleStyle, color = Ink)
            Spacer(Modifier.height(14.dp))
            NeoTextField(
                value = url,
                onValueChange = { url = it },
                placeholder = "https://...",
                leadingIcon = AppIcons.Link,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                onImeAction = { if (url.isNotBlank()) onSave(url) },
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeoButton(
                    label = "Cancel",
                    onClick = onDismiss,
                    background = CardWhite,
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
                NeoButton(
                    label = "Save",
                    onClick = { onSave(url) },
                    enabled = url.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
            }
        }
    }
}
