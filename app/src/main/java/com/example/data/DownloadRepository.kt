package com.example.data

import android.content.Context
import com.example.model.DownloadHistoryItem
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.model.PlatformType
import com.example.model.VideoResolution
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

sealed class ToastEvent(val message: String, val durationMs: Long = 3500) {
    class Downloading(msg: String = "Downloading bro") : ToastEvent(msg, 3500)
    class Done(msg: String = "Done") : ToastEvent(msg, 2000)
    class Sorry(reason: String) : ToastEvent("Sorry bro: $reason", 4000)
}

class DownloadRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val notificationHelper = NotificationHelper(context)

    private val _activeDownloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val activeDownloads: StateFlow<List<DownloadItem>> = _activeDownloads.asStateFlow()

    private val _completedDownloads = MutableStateFlow<List<DownloadItem>>(createSampleDownloads())
    val completedDownloads: StateFlow<List<DownloadItem>> = _completedDownloads.asStateFlow()

    private val _downloadHistory = MutableStateFlow<List<DownloadHistoryItem>>(createSampleHistory())
    val downloadHistory: StateFlow<List<DownloadHistoryItem>> = _downloadHistory.asStateFlow()

    private val _toastEvents = MutableSharedFlow<ToastEvent>()
    val toastEvents: SharedFlow<ToastEvent> = _toastEvents.asSharedFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    fun startDownload(
        platform: PlatformType,
        url: String,
        subCategory: String,
        resolution: VideoResolution,
        title: String? = null
    ) {
        val totalBytes = when (resolution) {
            VideoResolution.RES_360P -> 1024L * 1024L * 14L
            VideoResolution.RES_480P -> 1024L * 1024L * 28L
            VideoResolution.RES_720P -> 1024L * 1024L * 68L
            VideoResolution.RES_1080P -> 1024L * 1024L * 152L
            VideoResolution.RES_4K -> 1024L * 1024L * 390L
        }

        val cleanTitle = title ?: generateSmartTitle(platform, subCategory, url)
        val newItem = DownloadItem(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            platform = platform,
            subCategory = subCategory,
            originalUrl = url,
            resolution = resolution,
            status = DownloadStatus.DOWNLOADING,
            progress = 0f,
            downloadedBytes = 0L,
            totalBytes = totalBytes,
            speedKbps = 2400L,
            etaSeconds = 25
        )

        _activeDownloads.value = listOf(newItem) + _activeDownloads.value

        // Emit "Downloading bro" toast (3-4 seconds motion UI)
        scope.launch {
            _toastEvents.emit(ToastEvent.Downloading())
        }

        // Start asynchronous download simulation with realistic physics
        startDownloadJob(newItem)
    }

    private fun startDownloadJob(initialItem: DownloadItem) {
        val itemId = initialItem.id
        val job = scope.launch(Dispatchers.Default) {
            var currentItem = initialItem
            notificationHelper.showDownloadProgress(currentItem)

            var downloaded = currentItem.downloadedBytes
            val total = currentItem.totalBytes

            while (isActive && downloaded < total) {
                delay(300) // Smooth 300ms intervals for 60/120Hz UI updates

                if (currentItem.status == DownloadStatus.PAUSED) {
                    delay(500)
                    continue
                }

                // Dynamic speed fluctuation (between 3.5 MB/s and 12.8 MB/s)
                val baseSpeedKbps = Random.nextLong(3200, 11500)
                val chunk = (baseSpeedKbps * 1024 / (1000 / 300)).coerceAtLeast(1024 * 100)
                downloaded = (downloaded + chunk).coerceAtMost(total)

                val progress = downloaded.toFloat() / total.toFloat()
                val remainingBytes = total - downloaded
                val speedBytesPerSec = (baseSpeedKbps * 1024).coerceAtLeast(1)
                val etaSec = (remainingBytes / speedBytesPerSec).toInt().coerceAtLeast(1)

                currentItem = currentItem.copy(
                    downloadedBytes = downloaded,
                    progress = progress,
                    speedKbps = baseSpeedKbps,
                    etaSeconds = etaSec
                )

                updateActiveItem(currentItem)
                notificationHelper.showDownloadProgress(currentItem)
            }

            if (downloaded >= total) {
                // Completed successfully!
                val completedItem = currentItem.copy(
                    status = DownloadStatus.COMPLETED,
                    progress = 1f,
                    downloadedBytes = total,
                    speedKbps = 0L,
                    etaSeconds = 0
                )

                _activeDownloads.value = _activeDownloads.value.filter { it.id != itemId }
                _completedDownloads.value = listOf(completedItem) + _completedDownloads.value

                // Record in download history
                recordHistory(
                    title = completedItem.title,
                    platform = completedItem.platform,
                    subCategory = completedItem.subCategory,
                    url = completedItem.originalUrl,
                    resolution = completedItem.resolution,
                    isSuccess = true,
                    fileSizeBytes = completedItem.totalBytes,
                    error = null
                )

                notificationHelper.showDownloadCompleted(completedItem)

                // Show "Done" popup for 2 seconds
                _toastEvents.emit(ToastEvent.Done())
            }
        }

        downloadJobs[itemId] = job
    }

    fun pauseDownload(id: String) {
        _activeDownloads.value = _activeDownloads.value.map {
            if (it.id == id) it.copy(status = DownloadStatus.PAUSED, speedKbps = 0L) else it
        }
    }

    fun resumeDownload(id: String) {
        val item = _activeDownloads.value.find { it.id == id } ?: return
        val resumed = item.copy(status = DownloadStatus.DOWNLOADING)
        updateActiveItem(resumed)
        downloadJobs[id]?.cancel()
        startDownloadJob(resumed)
    }

    fun cancelDownload(id: String, reason: String = "Cancelled by user") {
        val item = _activeDownloads.value.find { it.id == id }
        downloadJobs[id]?.cancel()
        downloadJobs.remove(id)

        if (item != null) {
            notificationHelper.cancelNotification(item)
            _activeDownloads.value = _activeDownloads.value.filter { it.id != id }

            // Record in download history as failed / cancelled
            recordHistory(
                title = item.title,
                platform = item.platform,
                subCategory = item.subCategory,
                url = item.originalUrl,
                resolution = item.resolution,
                isSuccess = false,
                fileSizeBytes = item.downloadedBytes,
                error = reason
            )

            scope.launch {
                _toastEvents.emit(ToastEvent.Sorry(reason))
            }
        }
    }

    fun deleteCompleted(id: String) {
        _completedDownloads.value = _completedDownloads.value.filter { it.id != id }
    }

    fun deleteHistoryItem(id: String) {
        _downloadHistory.value = _downloadHistory.value.filter { it.id != id }
    }

    fun clearAllHistory() {
        _downloadHistory.value = emptyList()
    }

    fun redownloadHistoryItem(item: DownloadHistoryItem) {
        startDownload(
            platform = item.platform,
            url = item.originalUrl,
            subCategory = item.subCategory,
            resolution = item.resolution,
            title = item.title
        )
    }

    private fun recordHistory(
        title: String,
        platform: PlatformType,
        subCategory: String,
        url: String,
        resolution: VideoResolution,
        isSuccess: Boolean,
        fileSizeBytes: Long,
        error: String?
    ) {
        val entry = DownloadHistoryItem(
            id = UUID.randomUUID().toString(),
            title = title,
            platform = platform,
            subCategory = subCategory,
            originalUrl = url,
            resolution = resolution,
            isSuccess = isSuccess,
            fileSizeBytes = fileSizeBytes,
            timestampMillis = System.currentTimeMillis(),
            errorMessage = error
        )
        _downloadHistory.value = listOf(entry) + _downloadHistory.value
    }

    private fun updateActiveItem(updated: DownloadItem) {
        _activeDownloads.value = _activeDownloads.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    private fun generateSmartTitle(platform: PlatformType, subCategory: String, url: String): String {
        val prefix = when (platform) {
            PlatformType.YOUTUBE -> if (url.contains("shorts")) "Shorts" else "YouTube Video"
            PlatformType.INSTAGRAM -> "Instagram $subCategory"
            PlatformType.FACEBOOK -> "Facebook Reel"
            PlatformType.TIKTOK -> "TikTok Video (HD)"
            PlatformType.SITE_A -> "Site A Stream"
            PlatformType.SITE_B -> "Site B Media"
        }
        val randId = Random.nextInt(1000, 9999)
        return "$prefix #$randId"
    }

    private fun createSampleDownloads(): List<DownloadItem> {
        return listOf(
            DownloadItem(
                id = "sample_1",
                title = "Amazing Nature Reel (4K HDR)",
                platform = PlatformType.INSTAGRAM,
                subCategory = "Reels",
                originalUrl = "https://instagram.com/reel/sample1",
                resolution = VideoResolution.RES_4K,
                status = DownloadStatus.COMPLETED,
                progress = 1f,
                downloadedBytes = 1024L * 1024L * 142L,
                totalBytes = 1024L * 1024L * 142L,
                localFileName = "instagram_nature_4k.mp4",
                durationSeconds = 45,
                createdAtMillis = System.currentTimeMillis() - 3600000 * 2
            ),
            DownloadItem(
                id = "sample_2",
                title = "Tech Review 2026 Ultimate Guide",
                platform = PlatformType.YOUTUBE,
                subCategory = "All Videos",
                originalUrl = "https://youtube.com/watch?v=sample2",
                resolution = VideoResolution.RES_1080P,
                status = DownloadStatus.COMPLETED,
                progress = 1f,
                downloadedBytes = 1024L * 1024L * 89L,
                totalBytes = 1024L * 1024L * 89L,
                localFileName = "youtube_review_1080p.mp4",
                durationSeconds = 620,
                createdAtMillis = System.currentTimeMillis() - 3600000 * 24
            ),
            DownloadItem(
                id = "sample_3",
                title = "Viral Dance Trend (Watermark Free)",
                platform = PlatformType.TIKTOK,
                subCategory = "Videos No-Watermark",
                originalUrl = "https://tiktok.com/@sample3",
                resolution = VideoResolution.RES_720P,
                status = DownloadStatus.COMPLETED,
                progress = 1f,
                downloadedBytes = 1024L * 1024L * 22L,
                totalBytes = 1024L * 1024L * 22L,
                localFileName = "tiktok_viral_720p.mp4",
                durationSeconds = 30,
                createdAtMillis = System.currentTimeMillis() - 3600000 * 48
            )
        )
    }

    private fun createSampleHistory(): List<DownloadHistoryItem> {
        val now = System.currentTimeMillis()
        return listOf(
            DownloadHistoryItem(
                id = "hist_1",
                title = "Amazing Nature Reel (4K HDR)",
                platform = PlatformType.INSTAGRAM,
                subCategory = "Reels",
                originalUrl = "https://instagram.com/reel/sample1",
                resolution = VideoResolution.RES_4K,
                isSuccess = true,
                fileSizeBytes = 1024L * 1024L * 142L,
                timestampMillis = now - 3600000 * 2
            ),
            DownloadHistoryItem(
                id = "hist_2",
                title = "Travel Vlog Kyoto Ep 4",
                platform = PlatformType.YOUTUBE,
                subCategory = "Shorts",
                originalUrl = "https://youtube.com/shorts/kyoto_sample",
                resolution = VideoResolution.RES_1080P,
                isSuccess = true,
                fileSizeBytes = 1024L * 1024L * 38L,
                timestampMillis = now - 3600000 * 12
            ),
            DownloadHistoryItem(
                id = "hist_3",
                title = "Private Account Story Stream",
                platform = PlatformType.INSTAGRAM,
                subCategory = "Story",
                originalUrl = "https://instagram.com/stories/private_user",
                resolution = VideoResolution.RES_720P,
                isSuccess = false,
                fileSizeBytes = 0L,
                timestampMillis = now - 3600000 * 18,
                errorMessage = "Account is set to private. Unable to fetch story token."
            ),
            DownloadHistoryItem(
                id = "hist_4",
                title = "Tech Review 2026 Ultimate Guide",
                platform = PlatformType.YOUTUBE,
                subCategory = "All Videos",
                originalUrl = "https://youtube.com/watch?v=sample2",
                resolution = VideoResolution.RES_1080P,
                isSuccess = true,
                fileSizeBytes = 1024L * 1024L * 89L,
                timestampMillis = now - 3600000 * 24
            )
        )
    }
}
