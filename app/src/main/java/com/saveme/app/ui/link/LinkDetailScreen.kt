package com.saveme.app.ui.link

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.db.hasCustomPreview
import com.saveme.app.data.db.previewFile
import com.saveme.app.ui.components.CollectionPickerDialog
import com.saveme.app.ui.components.NeoBanner
import com.saveme.app.ui.components.NeoButton
import com.saveme.app.ui.components.NeoChip
import com.saveme.app.ui.components.NeoConfirmDialog
import com.saveme.app.ui.components.NeoDialog
import com.saveme.app.ui.components.NeoDivider
import com.saveme.app.ui.components.NeoIconButton
import com.saveme.app.ui.components.NeoMenuDialog
import com.saveme.app.ui.components.NeoMenuItem
import com.saveme.app.ui.components.NeoSurface
import com.saveme.app.ui.components.NeoTextField
import com.saveme.app.ui.components.SectionLabel
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Danger
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkFaint
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.OnAccent
import com.saveme.app.ui.theme.OverlineStyle
import com.saveme.app.ui.theme.PaperDim
import com.saveme.app.ui.theme.ScreenTitleStyle
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.ui.theme.SunnySoft
import com.saveme.app.util.UrlUtil
import com.saveme.app.util.formatTimestamp
import com.saveme.app.util.openUrl
import com.saveme.app.util.shareUrl
import kotlinx.coroutines.delay

/**
 * Halaman detail satu link: pratinjau, judul, deskripsi dari halaman asal,
 * tautan aslinya, tag, dan catatan pribadi.
 */
@Composable
fun LinkDetailScreen(
    linkId: Long,
    onBack: () -> Unit,
    viewModel: LinkDetailViewModel = viewModel(
        key = "link-$linkId",
        factory = LinkDetailViewModel.factory(linkId),
    ),
) {
    val loaded by viewModel.item.collectAsStateWithLifecycle()
    val collectionName by viewModel.collectionName.collectAsStateWithLifecycle()

    // Saat halaman ini ditutup, ViewModel-nya dibersihkan dan aliran datanya
    // mengirim null. Tanpa menahan data terakhir, isi kartu akan berkedip
    // kosong tepat di tengah animasi geser keluar.
    var lastKnown by remember { mutableStateOf<LinkWithTags?>(null) }
    SideEffect { if (loaded != null) lastKnown = loaded }
    val item = loaded ?: lastKnown
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showMovePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showAddTag by remember { mutableStateOf(false) }
    var showPreviewMenu by remember { mutableStateOf(false) }

    // Pemilih foto bawaan Android: tidak perlu izin akses galeri sama sekali.
    val pickPreview = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::setCustomPreview) }

    LaunchedEffect(linkId) { viewModel.markRead() }

    val current = item
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Link not found", style = BodyStyle, color = InkSoft)
        }
        return
    }
    val link = current.link

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            NeoSurface(
                modifier = Modifier.fillMaxWidth(),
                radius = 18.dp,
                borderWidth = 3.dp,
                shadowOffset = 5.dp,
            ) {
                Column(Modifier.fillMaxWidth()) {
                    HeaderRow(
                        siteName = link.siteName ?: UrlUtil.prettyHost(link.url),
                        onBack = onBack,
                        onRefresh = viewModel::refresh,
                        onShare = { shareUrl(context, link.url, link.title) },
                        onMove = { showMovePicker = true },
                        onDelete = { showDelete = true },
                    )
                    NeoDivider(thickness = 3.dp)
                    MediaBlock(
                        item = current,
                        onOpen = { openUrl(context, link.url) },
                        onEditPreview = { showPreviewMenu = true },
                    )
                    NeoDivider(thickness = 3.dp)

                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    link.title,
                                    style = ScreenTitleStyle.copy(fontSize = 22.sp),
                                    color = Ink,
                                )
                                Spacer(Modifier.height(7.dp))
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(Ink),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            NeoIconButton(
                                icon = AppIcons.Pencil,
                                contentDescription = "Edit title",
                                onClick = { showEdit = true },
                                size = 42.dp,
                                iconSize = 19.dp,
                                radius = 12.dp,
                            )
                        }

                        Spacer(Modifier.height(11.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                com.saveme.app.ui.theme.collectionIcon(
                                    allCollections
                                        .firstOrNull { it.id == link.collectionId }
                                        ?.iconKey,
                                ),
                                null,
                                tint = Mint,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "$collectionName  ·  ${formatTimestamp(link.createdAt)}",
                                style = CaptionStyle,
                                color = InkSoft,
                            )
                        }

                        if (!link.description.isNullOrBlank() || !link.authorLine.isNullOrBlank()) {
                            Spacer(Modifier.height(16.dp))
                            SectionLabel("Description", color = Ink)
                            if (!link.authorLine.isNullOrBlank()) {
                                Spacer(Modifier.height(7.dp))
                                Text(link.authorLine, style = CaptionStyle, color = InkSoft)
                            }
                            if (!link.description.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(link.description, style = BodyStyle, color = Ink)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        NeoSurface(
                            modifier = Modifier.fillMaxWidth(),
                            background = Sunny,
                            radius = 12.dp,
                            onClick = { openUrl(context, link.url) },
                            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 13.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    AppIcons.ExternalLink,
                                    null,
                                    tint = OnAccent,
                                    modifier = Modifier.size(19.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    link.url,
                                    style = BodyStyle,
                                    color = OnAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        SectionLabel("Tags", color = Ink)
                        Spacer(Modifier.height(9.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            current.tags.forEach { tag ->
                                NeoChip(
                                    label = tag.name,
                                    background = Mint,
                                    leadingIcon = AppIcons.Tag,
                                    trailingIcon = AppIcons.Close,
                                    onTrailingClick = { viewModel.removeTag(tag.id) },
                                )
                            }
                            NeoChip(
                                label = "Add",
                                background = CardWhite,
                                leadingIcon = AppIcons.Plus,
                                onClick = { showAddTag = true },
                            )
                        }

                        Spacer(Modifier.height(18.dp))
                        NeoDivider(thickness = 2.dp, color = PaperDim)
                        Spacer(Modifier.height(16.dp))

                        SectionLabel("Notes", color = Ink)
                        Spacer(Modifier.height(9.dp))
                        NotesField(
                            linkId = link.id,
                            initial = link.notes.orEmpty(),
                            onSave = viewModel::saveNotes,
                        )

                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeoButton(
                                label = if (link.isPinned) "Unpin" else "Pin",
                                onClick = viewModel::togglePin,
                                leadingIcon = AppIcons.Pin,
                                background = if (link.isPinned) Sunny else CardWhite,
                                modifier = Modifier.weight(1f),
                                height = 48.dp,
                            )
                            NeoButton(
                                label = "Open",
                                onClick = { openUrl(context, link.url) },
                                leadingIcon = AppIcons.ExternalLink,
                                modifier = Modifier.weight(1f),
                                height = 48.dp,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        NeoBanner(message = viewModel.banner, onDismiss = { viewModel.banner = null })
    }

    if (showMovePicker) {
        CollectionPickerDialog(
            title = "Move to",
            collections = allCollections,
            excludeIds = setOf(link.collectionId),
            onDismiss = { showMovePicker = false },
            onPick = { id ->
                viewModel.move(id, allCollections.firstOrNull { it.id == id }?.name.orEmpty())
            },
        )
    }

    if (showDelete) {
        NeoConfirmDialog(
            title = "Delete link?",
            message = "\"${link.title}\" will be removed from your library.",
            confirmLabel = "Delete",
            onConfirm = {
                showDelete = false
                viewModel.delete(onBack)
            },
            onDismiss = { showDelete = false },
        )
    }

    if (showEdit) {
        EditDetailsDialog(
            title = link.title,
            description = link.description.orEmpty(),
            onDismiss = { showEdit = false },
            onSave = { newTitle, newDescription ->
                viewModel.updateDetails(newTitle, newDescription)
                showEdit = false
            },
        )
    }

    if (showPreviewMenu) {
        NeoMenuDialog(
            title = "Preview image",
            items = buildList {
                add(
                    NeoMenuItem(AppIcons.Image, "Choose from gallery") {
                        pickPreview.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
                add(
                    NeoMenuItem(AppIcons.Refresh, "Fetch again from the link") {
                        viewModel.refresh()
                    },
                )
                if (link.hasCustomPreview()) {
                    add(
                        NeoMenuItem(AppIcons.Close, "Use the original preview") {
                            viewModel.clearCustomPreview()
                        },
                    )
                }
            },
            onDismiss = { showPreviewMenu = false },
        )
    }

    if (showAddTag) {
        AddTagDialog(
            onDismiss = { showAddTag = false },
            onAdd = {
                viewModel.addTag(it)
                showAddTag = false
            },
        )
    }
}

@Composable
private fun HeaderRow(
    siteName: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 9.dp, end = 9.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeoIconButton(AppIcons.ArrowLeft, "Back", onBack, size = 36.dp, iconSize = 17.dp, radius = 11.dp)
        Spacer(Modifier.width(9.dp))
        Text(
            siteName.uppercase(),
            style = OverlineStyle,
            color = Ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        NeoIconButton(AppIcons.Refresh, "Refresh preview", onRefresh, size = 36.dp, iconSize = 17.dp, radius = 11.dp)
        Spacer(Modifier.width(4.dp))
        NeoIconButton(AppIcons.Share, "Share", onShare, size = 36.dp, iconSize = 17.dp, radius = 11.dp)
        Spacer(Modifier.width(4.dp))
        NeoIconButton(AppIcons.Move, "Move", onMove, size = 36.dp, iconSize = 17.dp, radius = 11.dp)
        Spacer(Modifier.width(4.dp))
        NeoIconButton(
            AppIcons.Trash,
            "Delete",
            onDelete,
            size = 36.dp,
            iconSize = 17.dp,
            radius = 11.dp,
            background = Danger,
        )
    }
}

@Composable
private fun MediaBlock(
    item: LinkWithTags,
    onOpen: () -> Unit,
    onEditPreview: () -> Unit,
) {
    val link = item.link
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .background(PaperDim),
    ) {
        val preview = link.previewFile()
        if (preview != null) {
            AsyncImage(
                model = preview,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(AppIcons.Image, null, tint = InkFaint, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(6.dp))
                Text("No preview", style = CaptionStyle, color = InkFaint)
            }
        }

        NeoChip(
            label = if (link.hasCustomPreview()) "Preview · yours" else "Preview",
            background = if (link.hasCustomPreview()) Sunny else CardWhite,
            leadingIcon = AppIcons.Image,
            onClick = onEditPreview,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
        )

        NeoChip(
            label = "Open",
            background = CardWhite,
            leadingIcon = AppIcons.ExternalLink,
            onClick = onOpen,
            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
        )
    }
}

/** Kotak catatan yang menyimpan sendiri beberapa saat setelah pengetikan berhenti. */
@Composable
private fun NotesField(linkId: Long, initial: String, onSave: (String) -> Unit) {
    // Kunci pada id link, bukan pada nilainya: kalau dikunci ke nilai, simpanan
    // ke database akan memantul balik dan memindahkan kursor ke akhir teks.
    var text by remember(linkId) { mutableStateOf(initial) }

    LaunchedEffect(text) {
        if (text != initial) {
            delay(700)
            onSave(text)
        }
    }

    NeoTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = "Tap to add notes...",
        modifier = Modifier.fillMaxWidth(),
        background = SunnySoft,
        singleLine = false,
        minHeight = 104.dp,
        radius = 12.dp,
        shadow = false,
    )
}

@Composable
private fun EditDetailsDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var newTitle by remember { mutableStateOf(title) }
    var newDescription by remember { mutableStateOf(description) }

    NeoDialog(onDismissRequest = onDismiss) {
        Column {
            Text("Edit details", style = ScreenTitleStyle, color = Ink)
            Spacer(Modifier.height(14.dp))
            SectionLabel("Title")
            Spacer(Modifier.height(7.dp))
            NeoTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                placeholder = "Title",
                modifier = Modifier.fillMaxWidth(),
                minHeight = 52.dp,
            )
            Spacer(Modifier.height(14.dp))
            SectionLabel("Description")
            Spacer(Modifier.height(7.dp))
            NeoTextField(
                value = newDescription,
                onValueChange = { newDescription = it },
                placeholder = "Description",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minHeight = 96.dp,
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
                    onClick = { onSave(newTitle, newDescription) },
                    enabled = newTitle.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
            }
        }
    }
}

@Composable
private fun AddTagDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    NeoDialog(onDismissRequest = onDismiss) {
        Column {
            Text("Add tag", style = ScreenTitleStyle, color = Ink)
            Spacer(Modifier.height(14.dp))
            NeoTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. coding",
                leadingIcon = AppIcons.Tag,
                modifier = Modifier.fillMaxWidth(),
                minHeight = 52.dp,
                onImeAction = { if (name.isNotBlank()) onAdd(name) },
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
                    label = "Add",
                    onClick = { onAdd(name) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
            }
        }
    }
}
