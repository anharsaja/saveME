package com.saveme.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saveme.app.ui.components.CollectionCard
import com.saveme.app.ui.components.LinkCard
import com.saveme.app.ui.components.NeoButton
import com.saveme.app.ui.components.NeoChip
import com.saveme.app.ui.components.NeoDivider
import com.saveme.app.ui.components.NeoSurface
import com.saveme.app.ui.components.NeoTextField
import com.saveme.app.ui.components.SectionLabel
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkFaint
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.Nunito
import com.saveme.app.ui.theme.OnAccent
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.ui.theme.accentColor

/**
 * Pencarian menyeluruh: kartu ringkasan pustaka, deretan simpanan terbaru,
 * filter tag, lalu hasil untuk kata kunci yang diketik.
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenCollection: (Long) -> Unit,
    onOpenLink: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val activeTagId by viewModel.activeTagId.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val linkResults by viewModel.linkResults.collectAsStateWithLifecycle()
    val collectionResults by viewModel.collectionResults.collectAsStateWithLifecycle()
    val collections by viewModel.allCollections.collectAsStateWithLifecycle()

    val browsing = query.isBlank() && activeTagId == null

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = "Find collections or links...",
                leadingIcon = AppIcons.Search,
                modifier = Modifier.weight(1f),
                minHeight = 52.dp,
            )
            Spacer(Modifier.width(10.dp))
            NeoButton(
                label = "Cancel",
                onClick = {
                    if (query.isBlank() && activeTagId == null) onBack() else viewModel.clear()
                },
                background = CardWhite,
                height = 52.dp,
            )
        }
        NeoDivider(thickness = 2.5.dp)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (browsing) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LibraryCard(
                        collections = stats.collections,
                        links = stats.links,
                        pinned = stats.pinned,
                        tags = stats.tags,
                    )
                }

                if (tags.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Spacer(Modifier.height(6.dp))
                            SectionLabel("Tags", icon = AppIcons.Tag)
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                tags.forEach { tag ->
                                    NeoChip(
                                        label = tag.name,
                                        background = if (tag.id == activeTagId) Sunny else CardWhite,
                                        leadingIcon = AppIcons.Tag,
                                        onClick = { viewModel.toggleTag(tag.id) },
                                    )
                                }
                            }
                        }
                    }
                }

                if (recent.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Spacer(Modifier.height(6.dp))
                            SectionLabel("Recently saved", icon = AppIcons.Clock)
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(recent, key = { it.link.id }) { item ->
                                    val owner = collections
                                        .firstOrNull { it.id == item.link.collectionId }
                                    LinkCard(
                                        item = item,
                                        onClick = { onOpenLink(item.link.id) },
                                        collectionLabel = owner?.name,
                                        collectionColor = accentColor(owner?.colorKey),
                                        modifier = Modifier.width(172.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 42.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            AppIcons.Search,
                            null,
                            tint = Ink,
                            modifier = Modifier.width(52.dp).height(52.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Type something to search...",
                            style = BodyStyle,
                            color = InkFaint,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                if (collectionResults.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            SectionLabel("Collections (${collectionResults.size})")
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    items(collectionResults, key = { "c-${it.collection.id}" }) { summary ->
                        CollectionCard(
                            summary = summary,
                            onClick = { onOpenCollection(summary.collection.id) },
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        SectionLabel("Links (${linkResults.size})")
                        Spacer(Modifier.height(10.dp))
                    }
                }

                if (linkResults.isEmpty() && collectionResults.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                AppIcons.Inbox,
                                null,
                                tint = InkFaint,
                                modifier = Modifier.width(46.dp).height(46.dp),
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("Nothing matched", style = BodyStyle, color = InkSoft)
                        }
                    }
                } else {
                    items(linkResults, key = { it.link.id }) { item ->
                        val owner = collections.firstOrNull { it.id == item.link.collectionId }
                        LinkCard(
                            item = item,
                            onClick = { onOpenLink(item.link.id) },
                            collectionLabel = owner?.name,
                            collectionColor = accentColor(owner?.colorKey),
                        )
                    }
                }
            }
        }
    }
}

/** Kartu kuning berisi empat angka ringkasan isi pustaka. */
@Composable
private fun LibraryCard(collections: Int, links: Int, pinned: Int, tags: Int) {
    NeoSurface(
        modifier = Modifier.fillMaxWidth(),
        background = Sunny,
        radius = 18.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            SectionLabel("Your library", color = OnAccent)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatColumn(AppIcons.Folder, collections, "Collections", Modifier.weight(1f))
                StatDivider()
                StatColumn(AppIcons.Link, links, "Links", Modifier.weight(1f))
                StatDivider()
                StatColumn(AppIcons.Pin, pinned, "Pinned", Modifier.weight(1f))
                StatDivider()
                StatColumn(AppIcons.Tag, tags, "Tags", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatColumn(icon: ImageVector, value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = OnAccent, modifier = Modifier.width(24.dp).height(24.dp))
        Spacer(Modifier.height(7.dp))
        Text(
            value.toString(),
            fontFamily = Nunito,
            fontWeight = androidx.compose.ui.text.font.FontWeight.W800,
            fontSize = 27.sp,
            color = OnAccent,
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = CaptionStyle, color = OnAccent.copy(alpha = 0.72f))
    }
}

@Composable
private fun StatDivider() {
    Box(
        Modifier
            .width(1.6.dp)
            .height(56.dp)
            .background(OnAccent.copy(alpha = 0.22f)),
    )
}
