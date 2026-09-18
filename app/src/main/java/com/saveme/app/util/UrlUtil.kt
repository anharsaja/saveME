package com.saveme.app.util

import android.util.Patterns
import java.net.URL

object UrlUtil {

    /**
     * Teks yang dibagikan dari aplikasi lain sering berisi kalimat plus tautan.
     * Ambil tautan pertama yang valid dari teks tersebut.
     */
    fun extractFirstUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = Patterns.WEB_URL.matcher(text)
        while (matcher.find()) {
            val candidate = text.substring(matcher.start(), matcher.end())
            val normalized = normalize(candidate)
            if (normalized != null) return normalized
        }
        return null
    }

    /** Menambahkan skema bila belum ada dan menolak masukan yang bukan tautan. */
    fun normalize(input: String?): String? {
        val raw = input?.trim()?.trim('"', '\'', '<', '>') ?: return null
        if (raw.isEmpty()) return null
        val withScheme = when {
            raw.startsWith("http://", ignoreCase = true) -> raw
            raw.startsWith("https://", ignoreCase = true) -> raw
            else -> "https://$raw"
        }
        if (!Patterns.WEB_URL.matcher(withScheme).matches()) return null
        return runCatching {
            val url = URL(withScheme)
            if (url.host.isNullOrBlank() || !url.host.contains('.')) null else withScheme
        }.getOrNull()
    }

    fun host(url: String): String = runCatching {
        URL(url).host.removePrefix("www.")
    }.getOrDefault(url)

    /** Nama situs yang enak dibaca, dipakai bila halaman tidak menyediakan og:site_name. */
    fun prettyHost(url: String): String =
        host(url).substringBefore('.').replaceFirstChar { it.uppercase() }
}
