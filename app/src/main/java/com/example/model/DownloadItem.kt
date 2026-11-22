package com.example.model

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

enum class VideoResolution(
    val label: String,
    val isProRequired: Boolean,
    val approxSizeMb: String,
    val description: String
) {
    RES_360P("360p", false, "12 MB", "Standard Definition - Data Saver"),
    RES_480P("480p", false, "24 MB", "Enhanced Mobile Resolution"),
    RES_720P("720p HD", false, "65 MB", "Crisp High Definition (Free Top Quality)"),
    RES_1080P("1080p Full HD", true, "145 MB", "Full HD Ultra Crisp • Pro Only"),
    RES_4K("4K Ultra HD", true, "380 MB", "Master Cinema 4K 60fps • Pro Only")
}

data class DownloadItem(
    val id: String,
    val title: String,
    val platform: PlatformType,
    val subCategory: String,
    val originalUrl: String,
    val resolution: VideoResolution,
    val status: DownloadStatus = DownloadStatus.DOWNLOADING,
    val progress: Float = 0f, // 0.0f to 1.0f
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 1024L * 1024L * 65L,
    val speedKbps: Long = 0L,
    val etaSeconds: Int = 0,
    val thumbnailPlaceholderColor: Long = 0xFF4A1525,
    val localFileName: String = "video_${System.currentTimeMillis()}.mp4",
    val fileSizeBytes: Long = 1024L * 1024L * 65L,
    val durationSeconds: Int = 180,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val progressPercentage: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)

    val speedFormatted: String
        get() = when {
            speedKbps >= 1024 -> String.format("%.1f MB/s", speedKbps / 1024f)
            else -> "$speedKbps KB/s"
        }

    val downloadedFormatted: String
        get() = String.format("%.1f MB", downloadedBytes / (1024f * 1024f))

    val totalFormatted: String
        get() = String.format("%.1f MB", totalBytes / (1024f * 1024f))

    val etaFormatted: String
        get() = when {
            etaSeconds <= 0 -> "--"
            etaSeconds < 60 -> "${etaSeconds}s left"
            else -> "${etaSeconds / 60}m ${etaSeconds % 60}s left"
        }
}
