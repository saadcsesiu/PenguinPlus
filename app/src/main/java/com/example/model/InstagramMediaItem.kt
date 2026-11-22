package com.example.model

data class InstagramParsedMedia(
    val id: String,
    val title: String,
    val mediaType: String, // "Video Story", "Photo Story", "Highlight Video"
    val timestampAgo: String,
    val durationText: String,
    val approxSize: String,
    val previewColor: Long = 0xFFE1306C,
    val isSelected: Boolean = true
)
