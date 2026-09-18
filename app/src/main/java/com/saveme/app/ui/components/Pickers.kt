package com.saveme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.ui.theme.AccentPalette
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardTitleStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.CollectionIconChoices
import com.saveme.app.ui.theme.Danger
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.OnAccent
import com.saveme.app.ui.theme.ScreenTitleStyle
import com.saveme.app.ui.theme.accentColor
import com.saveme.app.ui.theme.collectionIcon

/**
 * Membuat koleksi baru atau menyunting yang sudah ada: nama, warna, ikon.
 *
 * Isinya panjang — sepuluh warna dan enam belas ikon — jadi penggulirannya
 * diserahkan ke [NeoDialog], yang tahu persis berapa tinggi layar yang tersisa
 * setelah papan ketik muncul.
 */
@Composable
fun CollectionEditorDialog(
    initial: CollectionEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorKey: String, iconKey: String) -> Unit,
    parentName: String? = null,
    onDelete: (() -> Unit)? = null,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var colorKey by remember { mutableStateOf(initial?.colorKey ?: AccentPalette.first().first) }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: CollectionIconChoices.first().first) }

    NeoDialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = when {
                    initial != null -> "Edit collection"
                    parentName != null -> "New subcollection"
                    else -> "New collection"
                },
                style = ScreenTitleStyle,
                color = Ink,
            )
            if (parentName != null && initial == null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    "Inside $parentName",
                    style = CaptionStyle,
                    color = InkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(16.dp))

            NeoTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Collection name",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = collectionIcon(iconKey),
                minHeight = 52.dp,
            )

            Spacer(Modifier.height(18.dp))
            SectionLabel("Color")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AccentPalette.forEach { (key, color) ->
                    ColorDot(
                        color = color,
                        selected = key == colorKey,
                        onClick = { colorKey = key },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionLabel("Icon")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CollectionIconChoices.forEach { (key, vector) ->
                    IconChoice(
                        icon = vector,
                        selected = key == iconKey,
                        onClick = { iconKey = key },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NeoButton(
                    label = "Cancel",
                    onClick = onDismiss,
                    background = CardWhite,
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
                NeoButton(
                    label = if (initial != null) "Save" else "Create",
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), colorKey, iconKey)
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
            }
            if (onDelete != null) {
                Spacer(Modifier.height(10.dp))
                NeoButton(
                    label = "Delete collection",
                    onClick = onDelete,
                    leadingIcon = AppIcons.Trash,
                    background = Danger,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp,
                )
            }
        }
    }
}

/**
 * Daftar koleksi untuk memilih tujuan simpan atau pindah. Subkoleksi
 * ditampilkan menjorok mengikuti kedalamannya.
 */
@Composable
fun CollectionPickerDialog(
    title: String,
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
    excludeIds: Set<Long> = emptySet(),
    onCreateNew: (() -> Unit)? = null,
) {
    // Daftarnya punya penggulir sendiri supaya judul dan tombol tetap di tempat.
    NeoDialog(onDismissRequest = onDismiss, scrollable = false) {
        CollectionPickerContent(title, collections, onDismiss, onPick, excludeIds, onCreateNew)
    }
}

/**
 * Pemilih koleksi tanpa jendela dialog, untuk layar yang sudah menjadi lapisan
 * mengambang sendiri — misalnya tujuan lembar berbagi.
 */
@Composable
fun CollectionPickerSheet(
    title: String,
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    excludeIds: Set<Long> = emptySet(),
) {
    NeoSurface(
        modifier = modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth(),
        radius = NeoRadius.Card,
        borderWidth = 4.dp,
        shadowOffset = 8.dp,
        contentPadding = PaddingValues(20.dp),
    ) {
        CollectionPickerContent(title, collections, onDismiss, onPick, excludeIds, null)
    }
}

@Composable
private fun CollectionPickerContent(
    title: String,
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
    excludeIds: Set<Long>,
    onCreateNew: (() -> Unit)?,
) {
    val depths = remember(collections) { computeDepths(collections) }

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = ScreenTitleStyle,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            NeoIconButton(
                icon = AppIcons.Close,
                contentDescription = "Tutup",
                onClick = onDismiss,
                size = 38.dp,
                iconSize = 17.dp,
            )
        }
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .heightIn(max = dialogListMaxHeight())
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            collections.filter { it.id !in excludeIds }.forEach { collection ->
                // Indentasinya dibatasi supaya subkoleksi yang dalam tidak
                // menghimpit namanya sampai tak terbaca di layar sempit.
                val depth = (depths[collection.id] ?: 0).coerceAtMost(4)
                NeoSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (depth * 14).dp),
                    radius = NeoRadius.Control,
                    shadowOffset = 4.dp,
                    onClick = {
                        onPick(collection.id)
                        onDismiss()
                    },
                    contentPadding = PaddingValues(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .background(
                                    accentColor(collection.colorKey),
                                    RoundedCornerShape(NeoRadius.Chip),
                                )
                                .border(2.dp, Ink, RoundedCornerShape(NeoRadius.Chip)),
                        ) {
                            Icon(
                                collectionIcon(collection.iconKey),
                                null,
                                tint = OnAccent,
                                modifier = Modifier.align(Alignment.Center).size(17.dp),
                            )
                        }
                        Spacer(Modifier.width(11.dp))
                        Text(
                            collection.name,
                            style = CardTitleStyle,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        if (onCreateNew != null) {
            Spacer(Modifier.height(14.dp))
            NeoButton(
                label = "New collection",
                onClick = {
                    onDismiss()
                    onCreateNew()
                },
                leadingIcon = AppIcons.Plus,
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp,
            )
        }
    }
}

/** Kedalaman tiap koleksi dalam pohon, dipakai untuk indentasi daftar. */
private fun computeDepths(collections: List<CollectionEntity>): Map<Long, Int> {
    val byId = collections.associateBy { it.id }
    val cache = mutableMapOf<Long, Int>()
    fun depthOf(id: Long, guard: Int = 0): Int {
        cache[id]?.let { return it }
        if (guard > 12) return 0
        val parent = byId[id]?.parentId
        val depth = if (parent == null) 0 else depthOf(parent, guard + 1) + 1
        cache[id] = depth
        return depth
    }
    collections.forEach { depthOf(it.id) }
    return cache
}
