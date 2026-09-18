package com.saveme.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.saveme.app.data.db.previewFile
import com.saveme.app.ui.components.BrandMark
import com.saveme.app.ui.components.EmptyState
import com.saveme.app.ui.components.NeoButton
import com.saveme.app.ui.components.NeoDivider
import com.saveme.app.ui.components.NeoIconButton
import com.saveme.app.ui.components.NeoRadius
import com.saveme.app.ui.components.NeoSurface
import com.saveme.app.ui.components.SectionLabel
import com.saveme.app.ui.components.readableColumn
import com.saveme.app.ui.components.screenGutter
import com.saveme.app.ui.components.windowWidth
import com.saveme.app.ui.theme.AppIcons
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkFaint
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.PaperDim
import com.saveme.app.ui.theme.ScreenTitleStyle
import com.saveme.app.util.UrlUtil
import com.saveme.app.util.openUrl

@Composable
fun FocusScreen(
    onBack: () -> Unit,
    onOpenLink: (Long) -> Unit,
    viewModel: FocusViewModel = viewModel(factory = FocusViewModel.Factory),
) {
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pending = queue.filter { it.link.id !in viewModel.skipped }
    val current = pending.firstOrNull()

    // Tiga tombol sejajar baru muat mulai ponsel berukuran sedang. Di bawah itu
    // "Done" mengambil barisnya sendiri — kebetulan juga tindakan utamanya.
    val stackedActions = windowWidth() < 380.dp

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .readableColumn()
                .fillMaxSize()
                .padding(horizontal = screenGutter() + 4.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeoIconButton(AppIcons.ArrowLeft, "Back", onBack)
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("FOCUS MODE", style = ScreenTitleStyle, color = Ink)
                    Text(
                        "${pending.size} to go · ${viewModel.doneCount} done",
                        style = CaptionStyle,
                        color = InkSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                NeoIconButton(AppIcons.Refresh, "Start over", viewModel::reset)
            }

            Spacer(Modifier.height(20.dp))

            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    EmptyState(
                        title = "All caught up",
                        message = "Nothing left in the queue. Save something new and come back later.",
                        actionLabel = "Back home",
                        onAction = onBack,
                        art = { BrandMark(size = 116.dp) },
                    )
                }
                return@Column
            }

            val link = current.link
            NeoSurface(
                modifier = Modifier.fillMaxWidth(),
                radius = NeoRadius.Card,
                borderWidth = 4.dp,
                shadowOffset = 8.dp,
            ) {
                Column(Modifier.fillMaxWidth()) {
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
                            Icon(
                                AppIcons.Link,
                                null,
                                tint = InkFaint,
                                modifier = Modifier.align(Alignment.Center).size(34.dp),
                            )
                        }
                    }
                    NeoDivider()
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel(link.siteName ?: UrlUtil.prettyHost(link.url), color = InkSoft)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            link.title,
                            style = ScreenTitleStyle,
                            color = Ink,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!link.description.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                link.description,
                                style = BodyStyle,
                                color = InkSoft,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        NeoButton(
                            label = "Open link",
                            onClick = { openUrl(context, link.url) },
                            leadingIcon = AppIcons.ExternalLink,
                            modifier = Modifier.fillMaxWidth(),
                            height = 50.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NeoButton(
                    label = "Skip",
                    onClick = { viewModel.skip(link.id) },
                    background = CardWhite,
                    modifier = Modifier.weight(1f),
                    height = 52.dp,
                )
                NeoButton(
                    label = "Details",
                    onClick = { onOpenLink(link.id) },
                    background = CardWhite,
                    modifier = Modifier.weight(1f),
                    height = 52.dp,
                )
                if (!stackedActions) {
                    DoneButton(
                        onClick = { viewModel.markRead(link.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (stackedActions) {
                Spacer(Modifier.height(10.dp))
                DoneButton(
                    onClick = { viewModel.markRead(link.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DoneButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    NeoButton(
        label = "Done",
        onClick = onClick,
        leadingIcon = AppIcons.Check,
        background = Mint,
        modifier = modifier,
        height = 52.dp,
    )
}
