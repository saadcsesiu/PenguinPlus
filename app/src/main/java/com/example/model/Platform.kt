package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.SiteAPurple
import com.example.ui.theme.SiteBTeal
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.YouTubeRed

enum class PlatformType(
    val title: String,
    val subtitle: String,
    val brandColor: Color,
    val subCategories: List<String>,
    val placeholderUrl: String,
    val hintText: String
) {
    YOUTUBE(
        title = "YouTube",
        subtitle = "Regular Videos & Shorts",
        brandColor = YouTubeRed,
        subCategories = listOf("All Videos", "Shorts", "Audio MP3"),
        placeholderUrl = "https://youtube.com/watch?v=... or shorts/...",
        hintText = "Paste YouTube or Shorts link for magic"
    ),
    INSTAGRAM(
        title = "Instagram",
        subtitle = "Reels, Photos, Story & Highlights",
        brandColor = InstagramPink,
        subCategories = listOf("Reels", "Photos", "Story", "Highlights", "Profile Pictures"),
        placeholderUrl = "https://instagram.com/reel/... or /p/...",
        hintText = "Paste Instagram Reel, Story, or Post link"
    ),
    FACEBOOK(
        title = "Facebook",
        subtitle = "Reels & Public Videos",
        brandColor = FacebookBlue,
        subCategories = listOf("Reels", "Watch Videos", "Stories"),
        placeholderUrl = "https://facebook.com/reel/... or /watch/...",
        hintText = "Paste Facebook video or reel link"
    ),
    TIKTOK(
        title = "TikTok",
        subtitle = "HD Videos (Watermark Free)",
        brandColor = TikTokPink,
        subCategories = listOf("Videos No-Watermark", "Audio Sound"),
        placeholderUrl = "https://vm.tiktok.com/... or tiktok.com/@...",
        hintText = "Paste TikTok video link for magic"
    ),
    SITE_A(
        title = "Site A",
        subtitle = "Custom Script Downloader A",
        brandColor = SiteAPurple,
        subCategories = listOf("Direct MP4", "Stream M3U8", "High Speed"),
        placeholderUrl = "https://site-a.example.com/video/...",
        hintText = "Paste video link from supported Site A"
    ),
    SITE_B(
        title = "Site B",
        subtitle = "Custom Script Downloader B",
        brandColor = SiteBTeal,
        subCategories = listOf("Direct Stream", "Audio Extractor", "Multi-Thread"),
        placeholderUrl = "https://site-b.example.com/stream/...",
        hintText = "Paste media link from supported Site B"
    );

    companion object {
        fun detectPlatform(url: String): PlatformType {
            val lower = url.lowercase()
            return when {
                lower.contains("youtu.be") || lower.contains("youtube.com") -> YOUTUBE
                lower.contains("instagram.com") || lower.contains("instagr.am") -> INSTAGRAM
                lower.contains("facebook.com") || lower.contains("fb.watch") -> FACEBOOK
                lower.contains("tiktok.com") -> TIKTOK
                lower.contains("site-a") -> SITE_A
                lower.contains("site-b") -> SITE_B
                else -> YOUTUBE
            }
        }
    }
}
