package com.saveme.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saveme.app.ui.theme.ButtonStyle
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.Motion
import com.saveme.app.ui.theme.OverlineStyle
import com.saveme.app.ui.theme.Paper
import com.saveme.app.ui.theme.Sunny
import com.saveme.app.ui.theme.readableOn
import com.saveme.app.ui.theme.softTint

/** Ketebalan garis tepi standar di seluruh aplikasi. */
val NeoBorder = 3.dp

/** Jarak bayangan solid di kanan-bawah setiap permukaan. */
val NeoShadow = 6.dp

/**
 * Sudut di gaya neo-brutalism nyaris siku: bentuk kotak yang jujur, tanpa
 * pembulatan yang melembutkan. Sisa lengkungan sekecil ini hanya supaya sudut
 * tidak tampak kasar di layar rapat.
 */
object NeoRadius {
    /** Keping kecil: tag, lencana, titik warna. */
    val Chip = 3.dp

    /** Kendali: tombol, kolom teks, baris daftar. */
    val Control = 4.dp

    /** Kartu, dialog, dan permukaan besar lainnya. */
    val Card = 6.dp
}

/**
 * Permukaan dasar: kotak berisi konten dengan garis hitam tebal dan bayangan
 * pejal di belakangnya. Saat bisa ditekan, isinya bergeser menimpa bayangan
 * sehingga terasa seperti tombol fisik yang ditekan masuk.
 */
@Composable
fun NeoSurface(
    modifier: Modifier = Modifier,
    background: Color = CardWhite,
    radius: Dp = NeoRadius.Card,
    borderWidth: Dp = NeoBorder,
    borderColor: Color = Ink,
    shadowOffset: Dp = NeoShadow,
    shadowColor: Color = Ink,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape: Shape = RoundedCornerShape(radius)
    val press = rememberNeoPress()
    val down = press.pressed && onClick != null && enabled
    val sink by animateDpAsState(
        targetValue = if (down) shadowOffset else 0.dp,
        // Turun cepat, naik memantul sedikit. Lihat catatan di Motion.
        animationSpec = if (down) Motion.PressInDp else Motion.PressOutDp,
        label = "neoPress",
    )

    // Bayangan digambar di belakang kotak yang sama, bukan sebagai kotak kedua.
    // Dengan begitu ukuran permukaan tetap mengikuti isinya.
    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .offset(x = sink, y = sink)
            .drawBehind {
                val gap = (shadowOffset - sink).toPx()
                if (gap > 0.5f) {
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(gap, gap),
                        size = size,
                        cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
                    )
                }
            }
            .clip(shape)
            .background(background, shape)
            .border(borderWidth, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.neoClickable(
                        press = press,
                        enabled = enabled,
                        scaleOnPress = false,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        content = content,
    )
}

/** Tombol persegi berisi satu ikon, dipakai di semua top bar. */
@Composable
fun NeoIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    background: Color = CardWhite,
    tint: Color = readableOn(background),
    radius: Dp = NeoRadius.Control,
    enabled: Boolean = true,
) {
    NeoSurface(
        modifier = modifier.size(size + NeoShadow),
        background = background,
        radius = radius,
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.align(Alignment.Center).size(iconSize),
        )
    }
}

/**
 * Tombol utama dengan label, opsional ikon di kiri atau kanan.
 *
 * Labelnya ditulis kapital semua: di gaya ini tombol adalah papan nama, bukan
 * kalimat. Label yang tidak muat dipangkas sebaris alih-alih melebarkan tombol.
 */
@Composable
fun NeoButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Sunny,
    contentColor: Color = readableOn(background),
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    radius: Dp = NeoRadius.Control,
    textStyle: TextStyle = ButtonStyle,
    uppercase: Boolean = true,
) {
    NeoSurface(
        modifier = modifier.height(height + NeoShadow),
        background = if (enabled) background else softTint(background, Paper, 0.45f),
        radius = radius,
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = contentColor, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(9.dp))
            }
            Text(
                text = if (uppercase) label.uppercase() else label,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(9.dp))
                Icon(trailingIcon, null, tint = contentColor, modifier = Modifier.size(19.dp))
            }
        }
    }
}

/**
 * Label kapital kecil pemisah seksi, didahului balok tinta pejal — penanda
 * khas gaya ini, menggantikan garis tipis yang biasa dipakai.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ink,
    icon: ImageVector? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        } else {
            Box(Modifier.size(10.dp).background(color))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text.uppercase(),
            style = OverlineStyle,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Garis pemisah tebal, sewarna tinta. */
@Composable
fun NeoDivider(modifier: Modifier = Modifier, thickness: Dp = NeoBorder, color: Color = Ink) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}

/** Keping kecil untuk tag dan penanda lain. */
@Composable
fun NeoChip(
    label: String,
    modifier: Modifier = Modifier,
    background: Color = Sunny,
    contentColor: Color = readableOn(background),
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    NeoSurface(
        modifier = modifier,
        background = background,
        radius = NeoRadius.Chip,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = contentColor, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(
                label,
                style = CaptionStyle,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(5.dp))
                Icon(
                    trailingIcon,
                    null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(13.dp)
                        .then(
                            if (onTrailingClick != null) {
                                Modifier.neoClickable(onClick = onTrailingClick)
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }
    }
}
