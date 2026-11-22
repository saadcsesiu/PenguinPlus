package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DownloadHistoryItem(
    val id: String,
    val title: String,
    val platform: PlatformType,
    val subCategory: String,
    val originalUrl: String,
    val resolution: VideoResolution,
    val isSuccess: Boolean,
    val fileSizeBytes: Long,
    val timestampMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    val formattedSize: String
        get() = String.format("%.1f MB", fileSizeBytes / (1024f * 1024f))
}
