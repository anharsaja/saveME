package com.saveme.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saveme.app.ui.theme.BodyStyle
import com.saveme.app.ui.theme.CaptionStyle
import com.saveme.app.ui.theme.CardTitleStyle
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.InkFaint
import com.saveme.app.ui.theme.InkSoft
import com.saveme.app.ui.theme.Mint
import com.saveme.app.ui.theme.Motion
import com.saveme.app.ui.theme.PaperDim

/** Kolom teks satu gaya dengan permukaan lain: kotak bergaris tebal. */
@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minHeight: Dp = 56.dp,
    radius: Dp = NeoRadius.Control,
    background: Color = CardWhite,
    textStyle: TextStyle = BodyStyle,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    shadow: Boolean = true,
) {
    NeoSurface(
        modifier = modifier,
        background = background,
        radius = radius,
        shadowOffset = if (shadow) NeoShadow else 0.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight - 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = Ink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle, color = InkFaint)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = textStyle.copy(color = Ink),
                    singleLine = singleLine,
                    cursorBrush = SolidColor(Ink),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onImeAction?.invoke() },
                        onSearch = { onImeAction?.invoke() },
                        onGo = { onImeAction?.invoke() },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

/**
 * Sakelar dua posisi bergaya sama: rel persegi bergaris hitam, kenop kotak
 * yang menggeser dari tepi ke tepi. Tidak ada bentuk kapsul di mana pun.
 */
@Composable
fun NeoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onColor: Color = Mint,
    offColor: Color = PaperDim,
) {
    val shape = RoundedCornerShape(NeoRadius.Chip)
    val trackColor by animateColorAsState(
        targetValue = if (checked) onColor else offColor,
        animationSpec = Motion.QuickColor,
        label = "switchTrack",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 25.dp else 3.dp,
        animationSpec = Motion.QuickDp,
        label = "switchThumb",
    )

    Box(
        modifier = modifier
            .size(width = 54.dp, height = 32.dp)
            .background(trackColor, shape)
            .border(3.dp, Ink, shape)
            .neoClickable { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbOffset)
                .size(23.dp)
                .background(CardWhite, shape)
                .border(3.dp, Ink, shape),
        )
    }
}

/**
 * Baris pengaturan: keping ikon, judul, keterangan, lalu kendali di sisi kanan.
 */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    iconBackground: Color = PaperDim,
    iconTint: Color = Ink,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    NeoSurface(
        modifier = modifier.fillMaxWidth(),
        radius = NeoRadius.Card,
        onClick = onClick,
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(iconBackground, RoundedCornerShape(NeoRadius.Chip))
                    .border(2.dp, Ink, RoundedCornerShape(NeoRadius.Chip)),
            ) {
                Icon(
                    icon,
                    null,
                    tint = iconTint,
                    modifier = Modifier.align(Alignment.Center).size(21.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = CardTitleStyle, color = Ink)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = CaptionStyle, color = InkSoft)
                }
            }
            trailing?.invoke()
        }
    }
}
