package com.saveme.app.work

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.saveme.app.MainActivity
import com.saveme.app.R
import com.saveme.app.data.db.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Pengingat harian untuk link yang belum dibaca. Tidak ada yang dikirim ke luar
 * perangkat; pekerja ini hanya menghitung isi database lokal.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val unread = AppDatabase.get(applicationContext).linkDao().unreadCount()
        if (unread <= 0) return Result.success()
        if (!canNotify()) return Result.success()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val text = if (unread == 1) {
            "1 link is still waiting to be read."
        } else {
            "$unread links are still waiting to be read."
        }

        val notification: Notification = NotificationCompat.Builder(
            applicationContext,
            ReminderScheduler.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_stat_saveme)
            .setContentTitle("Time for a quick review")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(ReminderScheduler.NOTIFICATION_ID, notification)
        return Result.success()
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}

object ReminderScheduler {

    const val CHANNEL_ID = "saveme_reminders"
    const val NOTIFICATION_ID = 4101
    private const val WORK_NAME = "saveme_daily_reminder"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Link reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Gentle reminders to review unread links"
        }
        manager.createNotificationChannel(channel)
    }

    /** Menjadwalkan pengingat harian pada jam yang dipilih pengguna. */
    fun schedule(context: Context, hour: Int) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(hour), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun delayUntil(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
