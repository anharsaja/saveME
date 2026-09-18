package com.saveme.app.util

import android.content.ClipboardManager
import android.content.Context

/**
 * Membaca teks dari papan klip lewat layanan sistem. Cara ini dipakai agar
 * tidak bergantung pada API papan klip Compose yang masih berubah-ubah.
 */
fun readClipboardText(context: Context): String? {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return null
    val clip = manager.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0)?.coerceToText(context)?.toString()?.takeIf { it.isNotBlank() }
}
