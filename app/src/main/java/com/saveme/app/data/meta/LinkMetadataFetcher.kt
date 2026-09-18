package com.saveme.app.data.meta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class LinkMetadata(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val siteName: String? = null,
    /** Baris ringkas "penulis · tanggal · 219 likes · 5 comments" bila bisa diurai. */
    val authorLine: String? = null,
    val isVideo: Boolean = false,
)

/**
 * Mengambil pratinjau sebuah tautan dan mengunduh gambarnya ke penyimpanan
 * internal, supaya kartu tetap tampil saat perangkat sedang offline.
 *
 * Urutan usahanya: tag OpenGraph halaman aslinya, lalu — kalau halaman itu
 * menyembunyikan gambarnya di balik dinding login seperti yang biasa dilakukan
 * Instagram dan TikTok — lewat jalur sematan publik masing-masing layanan.
 */
class LinkMetadataFetcher(private val context: Context) {

    suspend fun fetch(url: String): LinkMetadata = withContext(Dispatchers.IO) {
        val openGraph = fetchOpenGraph(url)
        if (openGraph?.imageUrl != null && openGraph.title != null) {
            return@withContext openGraph
        }
        // Tebakan dari alamatnya ditaruh paling akhir supaya hanya mengisi
        // bagian yang benar-benar kosong, bukan menutupi judul asli.
        (openGraph ?: LinkMetadata())
            .fillFrom(fetchOEmbed(url) ?: fetchInstagramMedia(url))
            .fillFrom(guessFromUrl(url))
    }

    /** Mengunduh gambar pratinjau dan mengembalikan path lokalnya. */
    suspend fun downloadThumbnail(imageUrl: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val target = newThumbFile("web")
            val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Referer", "https://www.google.com/")
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = true
            }
            connection.inputStream.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            connection.disconnect()
            target.takeIf { it.length() > 0 }?.absolutePath.also {
                if (it == null) target.delete()
            }
        }.getOrElse {
            Log.w(TAG, "Gagal mengunduh pratinjau: ${it.message}")
            null
        }
    }

    /**
     * Menyalin gambar pilihan pengguna dari galeri ke folder aplikasi. Isinya
     * disalin, bukan hanya alamatnya, karena izin baca ke URI galeri hanya
     * berlaku sesaat.
     */
    suspend fun importImage(source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeUpright(source) ?: return@runCatching null
            val target = newThumbFile("custom")
            target.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            bitmap.recycle()
            target.takeIf { it.length() > 0 }?.absolutePath.also { if (it == null) target.delete() }
        }.getOrElse {
            Log.w(TAG, "Gagal menyalin gambar: ${it.message}")
            null
        }
    }

    /**
     * Membaca gambar galeri ke ukuran wajar dan meluruskan arahnya.
     *
     * Foto kamera kerap tersimpan miring dengan penanda EXIF, dan ukurannya bisa
     * puluhan megapiksel. Keduanya dibereskan di sini supaya berkas pratinjau
     * tetap kecil dan tidak tampil terbalik.
     */
    private fun decodeUpright(source: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / sample > MAX_PREVIEW_PX * 2 ||
            bounds.outHeight / sample > MAX_PREVIEW_PX * 2
        ) {
            sample *= 2
        }

        val decoded = context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val rotation = context.contentResolver.openInputStream(source)?.use {
            when (
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        val longest = maxOf(decoded.width, decoded.height)
        val scale = if (longest > MAX_PREVIEW_PX) MAX_PREVIEW_PX.toFloat() / longest else 1f
        if (scale == 1f && rotation == 0f) return decoded

        val matrix = Matrix().apply {
            if (scale != 1f) postScale(scale, scale)
            if (rotation != 0f) postRotate(rotation)
        }
        return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            .also { if (it !== decoded) decoded.recycle() }
    }

    /** Menulis gambar yang sudah berupa byte, dipakai saat memulihkan cadangan. */
    suspend fun storeImageBytes(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (bytes.isEmpty()) return@runCatching null
            val target = newThumbFile("custom")
            target.writeBytes(bytes)
            target.absolutePath
        }.getOrNull()
    }

    fun deleteThumbnail(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    // ------------------------------------------------------------ pengambil

    /**
     * Null bila halamannya sama sekali tidak bisa dibaca.
     *
     * Dicoba dua kali: sebagai peramban biasa, lalu — kalau tidak ada gambar —
     * sebagai perayap tautan. Instagram dan beberapa situs lain menyembunyikan
     * isinya dari peramban tak masuk akun, tetapi tetap melayani perayap karena
     * dari situlah kartu pratinjau mereka di aplikasi chat berasal.
     */
    private fun fetchOpenGraph(url: String): LinkMetadata? {
        val asBrowser = loadDocument(url, USER_AGENT)?.let { readTags(url, it) }
        if (asBrowser?.imageUrl != null) return asBrowser

        val asCrawler = loadDocument(url, CRAWLER_AGENT)?.let { readTags(url, it) }
        if (asCrawler?.imageUrl != null) return asCrawler.fillFrom(asBrowser)

        return asBrowser ?: asCrawler
    }

    private fun readTags(url: String, doc: Document): LinkMetadata {
        val rawDescription = doc.meta("og:description")
            ?: doc.meta("twitter:description")
            ?: doc.meta("description")
        val parsed = parseSocialDescription(rawDescription)
        val type = doc.meta("og:type").orEmpty()

        return LinkMetadata(
            title = (doc.meta("og:title") ?: doc.meta("twitter:title") ?: doc.title())
                .trim().ifBlank { null },
            description = parsed?.body ?: rawDescription?.trim()?.ifBlank { null },
            imageUrl = (doc.meta("og:image") ?: doc.meta("twitter:image"))?.trim()?.ifBlank { null },
            siteName = doc.meta("og:site_name") ?: hostOf(url),
            authorLine = parsed?.authorLine,
            isVideo = doc.meta("og:video") != null ||
                doc.meta("og:video:url") != null ||
                type.startsWith("video") ||
                looksLikeVideoUrl(url),
        )
    }

    /**
     * YouTube, TikTok, dan Vimeo menyediakan oEmbed terbuka yang mengembalikan
     * judul, penulis, dan alamat gambar tanpa perlu kunci API.
     */
    private fun fetchOEmbed(url: String): LinkMetadata? {
        val endpoint = oEmbedEndpoint(url) ?: return null
        val body = readText(endpoint) ?: return null
        val json = runCatching { JSONObject(body) }.getOrNull() ?: return null
        val image = json.optString("thumbnail_url").ifBlank { null } ?: return null
        return LinkMetadata(
            title = json.optString("title").ifBlank { null },
            imageUrl = image,
            siteName = json.optString("provider_name").ifBlank { null } ?: hostOf(url),
            authorLine = json.optString("author_name").ifBlank { null },
            isVideo = true,
        )
    }

    /**
     * Jalur cadangan terakhir untuk Instagram: alamat media langsung sebuah
     * unggahan, yang mengembalikan berkas gambar tanpa perlu masuk akun.
     */
    private fun fetchInstagramMedia(url: String): LinkMetadata? {
        val match = INSTAGRAM_PATTERN.find(url) ?: return null
        val kind = match.groupValues[1].let { if (it == "reels") "reel" else it }
        val code = match.groupValues[2]
        val direct = "https://www.instagram.com/$kind/$code/media/?size=l"
        if (!resolvesToImage(direct)) return null
        return LinkMetadata(imageUrl = direct, siteName = "Instagram")
    }

    /** Memastikan alamat benar-benar berujung pada gambar, bukan halaman masuk. */
    private fun resolvesToImage(url: String): Boolean = runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
        }
        val ok = connection.responseCode in 200..299 &&
            connection.contentType.orEmpty().startsWith("image/")
        connection.disconnect()
        ok
    }.getOrDefault(false)

    // ------------------------------------------------------------- bantuan

    private fun loadDocument(url: String, agent: String): Document? = runCatching {
        Jsoup.connect(url)
            .userAgent(agent)
            .referrer("https://www.google.com/")
            .header("Accept-Language", "en-US,en;q=0.9")
            .timeout(TIMEOUT_MS)
            .followRedirects(true)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .get()
    }.getOrElse {
        Log.w(TAG, "Gagal memuat $url: ${it.message}")
        null
    }

    private fun readText(url: String): String? = runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
        }
        val text = connection.inputStream.use { it.readBytes().decodeToString() }
        connection.disconnect()
        text.ifBlank { null }
    }.getOrElse {
        Log.w(TAG, "Gagal membaca $url: ${it.message}")
        null
    }

    private fun oEmbedEndpoint(url: String): String? {
        val host = hostOf(url)?.lowercase(Locale.ROOT) ?: return null
        val encoded = URLEncoder.encode(url, "UTF-8")
        return when {
            host.contains("youtube") || host.contains("youtu.be") ->
                "https://www.youtube.com/oembed?url=$encoded&format=json"

            host.contains("tiktok") -> "https://www.tiktok.com/oembed?url=$encoded"
            host.contains("vimeo") -> "https://vimeo.com/api/oembed.json?url=$encoded"
            else -> null
        }
    }

    private fun newThumbFile(prefix: String): File {
        val dir = File(context.filesDir, THUMB_DIR).apply { mkdirs() }
        return File(dir, "$prefix-${UUID.randomUUID()}.img")
    }

    private fun LinkMetadata.fillFrom(other: LinkMetadata?): LinkMetadata {
        if (other == null) return this
        return copy(
            title = title ?: other.title,
            description = description ?: other.description,
            imageUrl = imageUrl ?: other.imageUrl,
            siteName = siteName ?: other.siteName,
            authorLine = authorLine ?: other.authorLine,
            isVideo = isVideo || other.isVideo,
        )
    }

    private fun Document.meta(property: String): String? {
        val byProperty = selectFirst("meta[property=$property]")?.attr("content")
        if (!byProperty.isNullOrBlank()) return byProperty
        return selectFirst("meta[name=$property]")?.attr("content")?.ifBlank { null }
    }

    private data class SocialDescription(val authorLine: String, val body: String?)

    /**
     * Instagram, TikTok, dan sejenisnya menaruh statistik di depan og:description,
     * contohnya: `219 likes, 5 comments - jaysonbuildz on September 15, 2026: "teks..."`.
     * Bagian itu dipisah agar bisa ditampilkan sebagai baris meta tersendiri.
     */
    private fun parseSocialDescription(raw: String?): SocialDescription? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return null
        val match = SOCIAL_PATTERN.find(text) ?: return null
        val (likes, comments, author, date, body) = match.destructured
        val parts = buildList {
            add(author.trim())
            if (date.isNotBlank()) add(date.trim())
            add("$likes likes")
            add("$comments comments")
        }
        val cleaned = body.trim().removeSurrounding("\"").trim().ifBlank { null }
        return SocialDescription(parts.joinToString(" · "), cleaned)
    }

    private fun looksLikeVideoUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT)
        return VIDEO_HINTS.any { lower.contains(it) }
    }

    /** Tebakan terakhir saat tidak ada satu pun sumber yang memberi data. */
    private fun guessFromUrl(url: String) = LinkMetadata(
        title = fallbackTitle(url),
        siteName = hostOf(url),
        isVideo = looksLikeVideoUrl(url),
    )

    private fun fallbackTitle(url: String): String {
        val host = hostOf(url) ?: return url
        val path = runCatching { URL(url).path }.getOrNull().orEmpty()
            .split('/')
            .lastOrNull { it.isNotBlank() }
            ?.replace('-', ' ')
            ?.replace('_', ' ')
        return if (path.isNullOrBlank()) host else "$host · $path"
    }

    private fun hostOf(url: String): String? = runCatching {
        URL(url).host?.removePrefix("www.")?.replaceFirstChar { it.uppercase() }
    }.getOrNull()

    private companion object {
        const val TAG = "LinkMetadata"
        const val TIMEOUT_MS = 15_000
        const val THUMB_DIR = "thumbs"
        const val MAX_PREVIEW_PX = 1080
        const val JPEG_QUALITY = 85
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"

        /** Perayap tautan: banyak situs sosial hanya melayani tag OpenGraph ke sini. */
        const val CRAWLER_AGENT =
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"

        val SOCIAL_PATTERN = Regex(
            """^([\d.,KkMm]+)\s+likes?,\s*([\d.,KkMm]+)\s+comments?\s*[-–—]\s*(.+?)\s+on\s+(.+?):\s*([\s\S]*)$""",
        )

        val INSTAGRAM_PATTERN =
            Regex("""instagram\.com/(p|reel|reels|tv)/([A-Za-z0-9_-]+)""", RegexOption.IGNORE_CASE)

        val VIDEO_HINTS = listOf(
            "/reel/", "/reels/", "/video/", "youtube.com/watch", "youtu.be/",
            "tiktok.com", "/shorts/", "vimeo.com",
        )
    }
}
