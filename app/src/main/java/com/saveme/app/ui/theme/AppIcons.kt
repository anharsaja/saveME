package com.saveme.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * Ikon digambar tangan sebagai ImageVector, bukan diambil dari material-icons.
 * Semuanya di kanvas 24x24 dengan stroke 2 dan ujung bulat supaya satu gaya
 * dengan garis tebal yang dipakai di seluruh aplikasi.
 */
object AppIcons {

    /**
     * Pembantu gambar sengaja ditaruh di dalam object ini, bukan di tingkat berkas.
     * Kalau berada di luar, menyentuhnya akan memicu inisialisasi berkas yang juga
     * membangun [CollectionIconChoices], dan daftar itu balik membaca AppIcons yang
     * belum selesai dibuat sehingga isinya null.
     */
    private const val SW = 2f

    private fun icon(
        name: String,
        fill: (PathBuilder.() -> Unit)? = null,
        stroke: (PathBuilder.() -> Unit)? = null,
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        if (fill != null) {
            addPath(pathData = PathData(fill), fill = SolidColor(Color.Black))
        }
        if (stroke != null) {
            addPath(
                pathData = PathData(stroke),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = SW,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcToRelative(r, r, 0f, false, true, 2 * r, 0f)
        arcToRelative(r, r, 0f, false, true, -2 * r, 0f)
        close()
    }

    private fun PathBuilder.roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float) {
        moveTo(l + rad, t)
        lineTo(r - rad, t)
        arcToRelative(rad, rad, 0f, false, true, rad, rad)
        lineTo(r, b - rad)
        arcToRelative(rad, rad, 0f, false, true, -rad, rad)
        lineTo(l + rad, b)
        arcToRelative(rad, rad, 0f, false, true, -rad, -rad)
        lineTo(l, t + rad)
        arcToRelative(rad, rad, 0f, false, true, rad, -rad)
        close()
    }

    private fun PathBuilder.folderBody() {
        moveTo(4.5f, 5f)
        lineTo(9f, 5f)
        lineTo(11f, 7.5f)
        lineTo(19.5f, 7.5f)
        arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, 1.5f)
        lineTo(21f, 17.5f)
        arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
        lineTo(4.5f, 19f)
        arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
        lineTo(3f, 6.5f)
        arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
        close()
    }

    val Settings: ImageVector = icon("settings", stroke = {
        circle(12f, 12f, 2.8f)
        circle(12f, 12f, 6.6f)
        moveTo(18.6f, 12f); lineTo(20.6f, 12f)
        moveTo(16.67f, 16.67f); lineTo(18.08f, 18.08f)
        moveTo(12f, 18.6f); lineTo(12f, 20.6f)
        moveTo(7.33f, 16.67f); lineTo(5.92f, 18.08f)
        moveTo(5.4f, 12f); lineTo(3.4f, 12f)
        moveTo(7.33f, 7.33f); lineTo(5.92f, 5.92f)
        moveTo(12f, 5.4f); lineTo(12f, 3.4f)
        moveTo(16.67f, 7.33f); lineTo(18.08f, 5.92f)
    })

    val Folder: ImageVector = icon("folder", stroke = { folderBody() })

    val FolderCog: ImageVector = icon("folderCog", stroke = {
        folderBody()
        circle(16.5f, 14.5f, 1.8f)
        moveTo(16.5f, 11.4f); lineTo(16.5f, 12.7f)
        moveTo(16.5f, 16.3f); lineTo(16.5f, 17.6f)
        moveTo(19.6f, 14.5f); lineTo(18.3f, 14.5f)
        moveTo(14.7f, 14.5f); lineTo(13.4f, 14.5f)
    })

    val Search: ImageVector = icon("search", stroke = {
        circle(11f, 11f, 7f)
        moveTo(16.2f, 16.2f); lineTo(20.8f, 20.8f)
    })

    val Plus: ImageVector = icon("plus", stroke = {
        moveTo(12f, 5f); lineTo(12f, 19f)
        moveTo(5f, 12f); lineTo(19f, 12f)
    })

    val Clipboard: ImageVector = icon("clipboard", stroke = {
        moveTo(9f, 4f)
        lineTo(6.5f, 4f)
        arcToRelative(1.5f, 1.5f, 0f, false, false, -1.5f, 1.5f)
        lineTo(5f, 19.5f)
        arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, 1.5f)
        lineTo(17.5f, 21f)
        arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, -1.5f)
        lineTo(19f, 5.5f)
        arcToRelative(1.5f, 1.5f, 0f, false, false, -1.5f, -1.5f)
        lineTo(15f, 4f)
        roundRect(8.6f, 2.2f, 15.4f, 6f, 1.2f)
    })

    val Hash: ImageVector = icon("hash", stroke = {
        moveTo(10f, 3.5f); lineTo(8f, 20.5f)
        moveTo(16f, 3.5f); lineTo(14f, 20.5f)
        moveTo(4f, 9f); lineTo(20f, 9f)
        moveTo(3.2f, 15f); lineTo(19.2f, 15f)
    })

    val ArrowLeft: ImageVector = icon("arrowLeft", stroke = {
        moveTo(20f, 12f); lineTo(4f, 12f)
        moveTo(10.5f, 5.5f); lineTo(4f, 12f); lineTo(10.5f, 18.5f)
    })

    val MoreVertical: ImageVector = icon("moreVertical", fill = {
        circle(12f, 5f, 1.7f)
        circle(12f, 12f, 1.7f)
        circle(12f, 19f, 1.7f)
    })

    val Layers: ImageVector = icon("layers", stroke = {
        roundRect(8.5f, 8.5f, 21f, 21f, 2.6f)
        moveTo(5.6f, 15.5f)
        lineTo(4.6f, 15.5f)
        arcToRelative(1.6f, 1.6f, 0f, false, true, -1.6f, -1.6f)
        lineTo(3f, 4.6f)
        arcToRelative(1.6f, 1.6f, 0f, false, true, 1.6f, -1.6f)
        lineTo(13.9f, 3f)
        arcToRelative(1.6f, 1.6f, 0f, false, true, 1.6f, 1.6f)
        lineTo(15.5f, 5.6f)
    })

    val Link: ImageVector = icon("link", stroke = {
        moveTo(10f, 13f)
        arcToRelative(5f, 5f, 0f, false, false, 7.54f, 0.54f)
        lineToRelative(3f, -3f)
        arcToRelative(5f, 5f, 0f, false, false, -7.07f, -7.07f)
        lineToRelative(-1.72f, 1.71f)
        moveTo(14f, 11f)
        arcToRelative(5f, 5f, 0f, false, false, -7.54f, -0.54f)
        lineToRelative(-3f, 3f)
        arcToRelative(5f, 5f, 0f, false, false, 7.07f, 7.07f)
        lineToRelative(1.71f, -1.71f)
    })

    val Pin: ImageVector = icon("pin", stroke = {
        moveTo(9f, 3f); lineTo(15f, 3f)
        moveTo(10f, 3f); lineTo(10f, 9.2f); lineTo(6.8f, 12.6f); lineTo(17.2f, 12.6f); lineTo(14f, 9.2f); lineTo(14f, 3f)
        moveTo(12f, 12.6f); lineTo(12f, 21f)
    })

    val Tag: ImageVector = icon("tag", stroke = {
        moveTo(4.5f, 3f)
        lineTo(12.2f, 3f)
        lineTo(21f, 11.8f)
        arcToRelative(1.6f, 1.6f, 0f, false, true, 0f, 2.3f)
        lineTo(14.1f, 21f)
        arcToRelative(1.6f, 1.6f, 0f, false, true, -2.3f, 0f)
        lineTo(3f, 12.2f)
        lineTo(3f, 4.5f)
        arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
        close()
        circle(7.6f, 7.6f, 1.2f)
    })

    val Clock: ImageVector = icon("clock", stroke = {
        circle(12f, 12f, 9f)
        moveTo(12f, 6.8f); lineTo(12f, 12f); lineTo(15.6f, 14.1f)
    })

    val Bell: ImageVector = icon("bell", stroke = {
        moveTo(6f, 9.5f)
        arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
        lineTo(18f, 14f)
        lineTo(19.6f, 16.6f)
        arcToRelative(1f, 1f, 0f, false, true, -0.85f, 1.52f)
        lineTo(5.25f, 18.12f)
        arcToRelative(1f, 1f, 0f, false, true, -0.85f, -1.52f)
        lineTo(6f, 14f)
        close()
        moveTo(9.9f, 20.4f)
        arcToRelative(2.4f, 2.4f, 0f, false, false, 4.2f, 0f)
    })

    val Zap: ImageVector = icon("zap", stroke = {
        moveTo(13f, 2.5f)
        lineTo(4f, 13.5f)
        lineTo(11.4f, 13.5f)
        lineTo(10.6f, 21.5f)
        lineTo(20f, 10.5f)
        lineTo(12.6f, 10.5f)
        close()
    })

    val Share: ImageVector = icon("share", stroke = {
        circle(18f, 5f, 2.4f)
        circle(6f, 12f, 2.4f)
        circle(18f, 19f, 2.4f)
        moveTo(8.1f, 10.8f); lineTo(15.9f, 6.2f)
        moveTo(8.1f, 13.2f); lineTo(15.9f, 17.8f)
    })

    val Close: ImageVector = icon("close", stroke = {
        moveTo(6f, 6f); lineTo(18f, 18f)
        moveTo(18f, 6f); lineTo(6f, 18f)
    })

    val Pencil: ImageVector = icon("pencil", stroke = {
        moveTo(16.4f, 3.6f)
        arcToRelative(2.12f, 2.12f, 0f, false, true, 3f, 3f)
        lineTo(8f, 18f)
        lineTo(3.4f, 19.6f)
        lineTo(5f, 15f)
        close()
    })

    val Trash: ImageVector = icon("trash", stroke = {
        moveTo(4f, 6.5f); lineTo(20f, 6.5f)
        moveTo(9.5f, 6.5f)
        lineTo(9.5f, 4.5f)
        arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
        lineTo(13.5f, 3.5f)
        arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
        lineTo(14.5f, 6.5f)
        moveTo(6.6f, 6.5f)
        lineTo(7.4f, 19.6f)
        arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, 1.4f)
        lineTo(15.1f, 21f)
        arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, -1.4f)
        lineTo(17.4f, 6.5f)
        moveTo(10f, 10.3f); lineTo(10.3f, 17.2f)
        moveTo(14f, 10.3f); lineTo(13.7f, 17.2f)
    })

    val Refresh: ImageVector = icon("refresh", stroke = {
        moveTo(3f, 12f)
        arcToRelative(9f, 9f, 0f, false, true, 9f, -9f)
        arcToRelative(9.75f, 9.75f, 0f, false, true, 6.74f, 2.74f)
        lineTo(21f, 8f)
        moveTo(21f, 3f); lineTo(21f, 8f); lineTo(16f, 8f)
        moveTo(21f, 12f)
        arcToRelative(9f, 9f, 0f, false, true, -9f, 9f)
        arcToRelative(9.75f, 9.75f, 0f, false, true, -6.74f, -2.74f)
        lineTo(3f, 16f)
        moveTo(8f, 16f); lineTo(3f, 16f); lineTo(3f, 21f)
    })

    val Move: ImageVector = icon("move", stroke = {
        moveTo(12f, 2.5f); lineTo(12f, 21.5f)
        moveTo(2.5f, 12f); lineTo(21.5f, 12f)
        moveTo(9f, 5.5f); lineTo(12f, 2.5f); lineTo(15f, 5.5f)
        moveTo(9f, 18.5f); lineTo(12f, 21.5f); lineTo(15f, 18.5f)
        moveTo(5.5f, 9f); lineTo(2.5f, 12f); lineTo(5.5f, 15f)
        moveTo(18.5f, 9f); lineTo(21.5f, 12f); lineTo(18.5f, 15f)
    })

    val ExternalLink: ImageVector = icon("externalLink", stroke = {
        moveTo(14f, 4f); lineTo(20f, 4f); lineTo(20f, 10f)
        moveTo(20f, 4f); lineTo(11f, 13f)
        moveTo(18f, 14.5f)
        lineTo(18f, 19f)
        arcToRelative(1.8f, 1.8f, 0f, false, true, -1.8f, 1.8f)
        lineTo(5.8f, 20.8f)
        arcToRelative(1.8f, 1.8f, 0f, false, true, -1.8f, -1.8f)
        lineTo(4f, 7.8f)
        arcToRelative(1.8f, 1.8f, 0f, false, true, 1.8f, -1.8f)
        lineTo(10f, 6f)
    })

    val ChevronRight: ImageVector = icon("chevronRight", stroke = {
        moveTo(9.5f, 5f); lineTo(16.5f, 12f); lineTo(9.5f, 19f)
    })

    val ChevronDown: ImageVector = icon("chevronDown", stroke = {
        moveTo(5f, 9.5f); lineTo(12f, 16.5f); lineTo(19f, 9.5f)
    })

    val Code: ImageVector = icon("code", stroke = {
        moveTo(9f, 17f); lineTo(4f, 12f); lineTo(9f, 7f)
        moveTo(15f, 7f); lineTo(20f, 12f); lineTo(15f, 17f)
        moveTo(13.4f, 4.5f); lineTo(10.6f, 19.5f)
    })

    val Leaf: ImageVector = icon("leaf", stroke = {
        moveTo(11f, 20f)
        arcToRelative(7f, 7f, 0f, false, true, -1.2f, -13.9f)
        curveToRelative(5.7f, -1.1f, 7.2f, -1.62f, 9.2f, -4.1f)
        curveToRelative(1f, 2f, 2f, 4.18f, 2f, 8f)
        curveToRelative(0f, 5.5f, -4.78f, 10f, -10f, 10f)
        close()
        moveTo(2.5f, 21f)
        curveToRelative(0f, -3f, 1.85f, -5.36f, 5.08f, -6f)
        curveToRelative(2.42f, -0.48f, 4.92f, -2f, 5.92f, -3f)
    })

    val MapPin: ImageVector = icon("mapPin", stroke = {
        moveTo(12f, 21.5f)
        curveToRelative(-4.5f, -5.5f, -7f, -8.8f, -7f, -11.5f)
        arcToRelative(7f, 7f, 0f, false, true, 14f, 0f)
        curveToRelative(0f, 2.7f, -2.5f, 6f, -7f, 11.5f)
        close()
        circle(12f, 10f, 2.6f)
    })

    val Calendar: ImageVector = icon("calendar", stroke = {
        roundRect(3f, 5f, 21f, 21f, 2.5f)
        moveTo(3f, 10f); lineTo(21f, 10f)
        moveTo(8f, 3f); lineTo(8f, 7f)
        moveTo(16f, 3f); lineTo(16f, 7f)
    })

    val Sparkles: ImageVector = icon("sparkles", stroke = {
        moveTo(10.5f, 3f)
        lineTo(12.3f, 8.2f)
        lineTo(17.5f, 10f)
        lineTo(12.3f, 11.8f)
        lineTo(10.5f, 17f)
        lineTo(8.7f, 11.8f)
        lineTo(3.5f, 10f)
        lineTo(8.7f, 8.2f)
        close()
        moveTo(18.5f, 14.5f)
        lineTo(19.3f, 17f)
        lineTo(21.8f, 17.8f)
        lineTo(19.3f, 18.6f)
        lineTo(18.5f, 21.1f)
        lineTo(17.7f, 18.6f)
        lineTo(15.2f, 17.8f)
        lineTo(17.7f, 17f)
        close()
    })

    val CheckCircle: ImageVector = icon("checkCircle", stroke = {
        circle(12f, 12f, 9f)
        moveTo(8f, 12.3f); lineTo(11f, 15.3f); lineTo(16.2f, 9.4f)
    })

    val Check: ImageVector = icon("check", stroke = {
        moveTo(4.5f, 12.5f); lineTo(9.5f, 17.5f); lineTo(19.5f, 6.5f)
    })

    val Play: ImageVector = icon("play", fill = {
        moveTo(8.5f, 5.2f); lineTo(19f, 12f); lineTo(8.5f, 18.8f); close()
    })

    val VolumeOff: ImageVector = icon("volumeOff", stroke = {
        moveTo(11f, 4.8f); lineTo(6.5f, 9f); lineTo(3f, 9f); lineTo(3f, 15f); lineTo(6.5f, 15f); lineTo(11f, 19.2f); close()
        moveTo(15.8f, 9.6f); lineTo(21f, 14.4f)
        moveTo(21f, 9.6f); lineTo(15.8f, 14.4f)
    })

    val AlertCircle: ImageVector = icon(
        "alertCircle",
        fill = { circle(12f, 16.4f, 1.2f) },
        stroke = {
            circle(12f, 12f, 9f)
            moveTo(12f, 7.2f); lineTo(12f, 13f)
        },
    )

    val Download: ImageVector = icon("download", stroke = {
        moveTo(12f, 3.5f); lineTo(12f, 15.5f)
        moveTo(7f, 10.5f); lineTo(12f, 15.5f); lineTo(17f, 10.5f)
        moveTo(4f, 20.5f); lineTo(20f, 20.5f)
    })

    val Upload: ImageVector = icon("upload", stroke = {
        moveTo(12f, 16.5f); lineTo(12f, 4.5f)
        moveTo(7f, 9.5f); lineTo(12f, 4.5f); lineTo(17f, 9.5f)
        moveTo(4f, 20.5f); lineTo(20f, 20.5f)
    })

    val BookOpen: ImageVector = icon("bookOpen", stroke = {
        moveTo(3f, 4.5f)
        lineTo(8.5f, 4.5f)
        arcToRelative(3.5f, 3.5f, 0f, false, true, 3.5f, 3.5f)
        lineTo(12f, 20f)
        arcToRelative(3.5f, 3.5f, 0f, false, false, -3.5f, -3.5f)
        lineTo(3f, 16.5f)
        close()
        moveTo(21f, 4.5f)
        lineTo(15.5f, 4.5f)
        arcToRelative(3.5f, 3.5f, 0f, false, false, -3.5f, 3.5f)
        lineTo(12f, 20f)
        arcToRelative(3.5f, 3.5f, 0f, false, true, 3.5f, -3.5f)
        lineTo(21f, 16.5f)
        close()
    })

    val Image: ImageVector = icon("image", stroke = {
        roundRect(3f, 4f, 21f, 20f, 2.5f)
        circle(8.4f, 9.4f, 1.6f)
        moveTo(3.6f, 17.4f); lineTo(9f, 12.4f); lineTo(14.2f, 17.6f); lineTo(16.6f, 15.2f); lineTo(20.6f, 19.2f)
    })

    val Globe: ImageVector = icon("globe", stroke = {
        circle(12f, 12f, 9f)
        moveTo(3.2f, 12f); lineTo(20.8f, 12f)
        moveTo(12f, 3f)
        curveToRelative(2.6f, 2.4f, 4f, 5.6f, 4f, 9f)
        curveToRelative(0f, 3.4f, -1.4f, 6.6f, -4f, 9f)
        curveToRelative(-2.6f, -2.4f, -4f, -5.6f, -4f, -9f)
        curveToRelative(0f, -3.4f, 1.4f, -6.6f, 4f, -9f)
        close()
    })

    val Heart: ImageVector = icon("heart", stroke = {
        moveTo(12f, 20.5f)
        lineTo(4.2f, 12.8f)
        arcToRelative(4.6f, 4.6f, 0f, false, true, 6.5f, -6.5f)
        lineTo(12f, 7.6f)
        lineTo(13.3f, 6.3f)
        arcToRelative(4.6f, 4.6f, 0f, false, true, 6.5f, 6.5f)
        close()
    })

    val Star: ImageVector = icon("star", stroke = {
        moveTo(12f, 3f)
        lineTo(14.8f, 9.1f)
        lineTo(21.4f, 9.9f)
        lineTo(16.5f, 14.4f)
        lineTo(17.8f, 21f)
        lineTo(12f, 17.7f)
        lineTo(6.2f, 21f)
        lineTo(7.5f, 14.4f)
        lineTo(2.6f, 9.9f)
        lineTo(9.2f, 9.1f)
        close()
    })

    val Bookmark: ImageVector = icon("bookmark", stroke = {
        moveTo(6f, 3.5f)
        lineTo(18f, 3.5f)
        lineTo(18f, 20.5f)
        lineTo(12f, 15.5f)
        lineTo(6f, 20.5f)
        close()
    })

    val Filter: ImageVector = icon("filter", stroke = {
        moveTo(3f, 5f)
        lineTo(21f, 5f)
        lineTo(14f, 13.2f)
        lineTo(14f, 20.5f)
        lineTo(10f, 17.8f)
        lineTo(10f, 13.2f)
        close()
    })

    val Inbox: ImageVector = icon("inbox", stroke = {
        roundRect(3f, 4f, 21f, 20f, 2.5f)
        moveTo(3f, 14f); lineTo(8f, 14f); lineTo(9.6f, 16.6f); lineTo(14.4f, 16.6f); lineTo(16f, 14f); lineTo(21f, 14f)
    })
}

/** Pilihan ikon untuk sampul koleksi, dipetakan dari key yang disimpan di database. */
val CollectionIconChoices: List<Pair<String, ImageVector>> = listOf(
    "code" to AppIcons.Code,
    "leaf" to AppIcons.Leaf,
    "pin" to AppIcons.MapPin,
    "folder" to AppIcons.Folder,
    "star" to AppIcons.Star,
    "heart" to AppIcons.Heart,
    "tag" to AppIcons.Tag,
    "link" to AppIcons.Link,
    "spark" to AppIcons.Sparkles,
    "zap" to AppIcons.Zap,
    "book" to AppIcons.BookOpen,
    "image" to AppIcons.Image,
    "globe" to AppIcons.Globe,
    "clock" to AppIcons.Clock,
    "calendar" to AppIcons.Calendar,
    "bookmark" to AppIcons.Bookmark,
)

private val iconByKey: Map<String, ImageVector> by lazy { CollectionIconChoices.toMap() }

fun collectionIcon(key: String?): ImageVector = iconByKey[key] ?: AppIcons.Folder
