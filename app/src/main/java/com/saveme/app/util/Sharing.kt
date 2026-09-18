package com.saveme.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Membuka tautan di peramban atau aplikasi yang menanganinya. */
fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app can open this link", Toast.LENGTH_SHORT).show()
    }
}

/** Meneruskan tautan ke aplikasi lain lewat lembar berbagi bawaan Android. */
fun shareUrl(context: Context, url: String, title: String?) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        if (!title.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, "Share link").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private val dateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
private val dayFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

fun formatTimestamp(millis: Long): String = dateFormat.format(Date(millis))

fun formatDay(millis: Long): String = dayFormat.format(Date(millis))
