package com.saveme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.saveme.app.data.db.CollectionSummary
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.db.previewFile
import com.saveme.app.ui.theme.AccentPalette
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.Archivo
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardTitleStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkFaint
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.OnAccent
import com.saveme.app.ui.theme.PaperDim
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.ui.theme.accentColor
import com.saveme.app.ui.theme.accentTab
import com.saveme.app.ui.theme.collectionIcon
import com.saveme.app.util.UrlUtil

/**
 * Kartu koleksi: sehelai "lidah" map berwarna di atas, lalu kartu putih berisi
 * keping ikon, jumlah isi, dan nama koleksi.
 */
@Composable
fun CollectionCard(
    summary: CollectionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val collection = summary.collection
    val accent = accentColor(collection.colorKey)
    val tabShape = RoundedCornerShape(topStart = NeoRadius.Chip, topEnd = NeoRadius.Chip)

    Column(modifier = modifier) {
        // Lebar lidah map mengikuti lebar kartu, jadi bentuknya tetap sama
        // entah petaknya dua kolom di ponsel atau lima kolom di tablet.
        Box(
            Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(0.44f)
                .height(16.dp)
                .background(accentTab(collection.colorKey), tabShape)
                .border(NeoBorder, Ink, tabShape),
        )
        NeoSurface(
            // Tinggi dikunci agar petak kartu tetap rapi dan proporsinya sama
            // dengan rancangan aslinya: nama koleksi duduk di dasar kartu.
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .offset(y = (-2).dp),
            radius = NeoRadius.Card,
            onClick = onClick,
            onLongClick = onLongClick,
            contentPadding = PaddingValues(12.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(accent, RoundedCornerShape(NeoRadius.Control))
                            .border(NeoBorder, Ink, RoundedCornerShape(NeoRadius.Control)),
                    ) {
                        Icon(
                            collectionIcon(collection.iconKey),
                            null,
                            tint = OnAccent,
                            modifier = Modifier.align(Alignment.Center).size(21.dp),
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = buildString {
                            append(summary.linkCount)
                            append(if (summary.linkCount == 1) " link" else " links")
                            if (summary.childCount > 0) append(" · ${summary.childCount} sub")
                        },
                        style = CaptionStyle,
                        color = InkSoft,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(top = 3.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    collection.name,
                    style = CardTitleStyle.copy(fontSize = 16.sp),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Kartu link: pratinjau gambar di atas, judul, lalu baris sumber.
 * Keping nama koleksi hanya muncul di konteks yang memerlukannya, seperti
 * "Recently saved" dan hasil pencarian.
 */
@Composable
fun LinkCard(
    item: LinkWithTags,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    collectionLabel: String? = null,
    collectionColor: Color = Mint,
    selectionMode: Boolean = false,
    selected: Boolean = false,
) {
    val link = item.link
    val markShape = RoundedCornerShape(NeoRadius.Chip)
    NeoSurface(
        modifier = modifier,
        radius = NeoRadius.Card,
        borderColor = if (selected) Sunny else Ink,
        borderWidth = if (selected) 4.5.dp else NeoBorder,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
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
                    Icon(
                        AppIcons.Link,
                        null,
                        tint = InkFaint,
                        modifier = Modifier.align(Alignment.Center).size(30.dp),
                    )
                }

                if (collectionLabel != null) {
                    NeoChip(
                        label = collectionLabel,
                        background = collectionColor,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 7.dp, bottom = 5.dp),
                    )
                }

                if (link.isPinned) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(26.dp)
                            .background(Sunny, markShape)
                            .border(2.5.dp, Ink, markShape),
                    ) {
                        Icon(
                            AppIcons.Pin,
                            null,
                            tint = OnAccent,
                            modifier = Modifier.align(Alignment.Center).size(14.dp),
                        )
                    }
                }

                if (selectionMode) {
                    Box(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(26.dp)
                            .background(if (selected) Sunny else CardWhite, markShape)
                            .border(2.5.dp, Ink, markShape),
                    ) {
                        if (selected) {
                            Icon(
                                AppIcons.Check,
                                null,
                                tint = OnAccent,
                                modifier = Modifier.align(Alignment.Center).size(15.dp),
                            )
                        }
                    }
                }
            }

            NeoDivider()

            Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(
                    link.title,
                    style = CardTitleStyle.copy(fontSize = 14.sp, lineHeight = 18.sp),
                    color = Ink,
                    // Dua baris selalu disediakan, terpakai atau tidak, supaya
                    // kartu berdampingan di satu baris petak sama tingginya.
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SourceBadge(link.url, size = 17)
                    Text(
                        link.siteName ?: UrlUtil.prettyHost(link.url),
                        style = CaptionStyle,
                        color = InkSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Lencana sumber berisi huruf awal situs. Sengaja tidak memakai logo layanan
 * mana pun, warnanya diturunkan dari nama host supaya tetap konsisten.
 */
@Composable
fun SourceBadge(url: String, modifier: Modifier = Modifier, size: Int = 18) {
    val host = UrlUtil.host(url)
    val color = AccentPalette[(host.hashCode().let { if (it < 0) -it else it }) % AccentPalette.size].second
    val letter = host.firstOrNull()?.uppercaseChar() ?: '?'
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(shape)
            .background(color)
            .border(2.dp, Ink, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            letter.toString(),
            fontFamily = Archivo,
            fontWeight = FontWeight.W900,
            fontSize = (size * 0.5f).sp,
            color = Ink,
        )
    }
}
