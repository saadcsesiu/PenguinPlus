package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.model.DownloadItem
import com.example.model.DownloadStatus

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "penguin_plus_downloads"
        const val CHANNEL_NAME = "Penguin Plus Downloads"
        const val CHANNEL_DESC = "Notifications for active downloads and completions"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showDownloadProgress(item: DownloadItem) {
        if (!hasNotificationPermission()) return

        val notificationId = item.id.hashCode()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Penguin Plus: ${item.title}")
            .setContentText("${item.progressPercentage}% • ${item.speedFormatted} • ${item.etaFormatted}")
            .setProgress(100, item.progressPercentage, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun showDownloadCompleted(item: DownloadItem) {
        if (!hasNotificationPermission()) return

        val notificationId = item.id.hashCode()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Done: ${item.title}")
            .setContentText("Downloaded in ${item.resolution.label} (${item.totalFormatted})")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun showDownloadFailed(item: DownloadItem, reason: String) {
        if (!hasNotificationPermission()) return

        val notificationId = item.id.hashCode()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Sorry bro: ${item.title}")
            .setContentText("Failed: $reason")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun cancelNotification(item: DownloadItem) {
        val notificationId = item.id.hashCode()
        try {
            notificationManager.cancel(notificationId)
        } catch (_: Exception) {
        }
    }
}
