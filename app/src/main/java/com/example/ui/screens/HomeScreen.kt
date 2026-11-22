package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstagramParsedMedia
import com.example.model.PlatformType
import com.example.ui.components.CustomParsingProgressIndicator
import com.example.ui.components.VideoUrlParsingBanner
import com.example.ui.theme.AmberPro
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyHeader
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamCardBorder
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkContainer
import com.example.ui.theme.DarkContainerElevated
import com.example.ui.theme.EmeraldDone
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeRed

@Composable
fun HomeScreen(
    selectedPlatform: PlatformType?,
    selectedSubCategory: String,
    urlInput: String,
    isPro: Boolean,
    isYouTubeShort: Boolean,
    isParsingUrl: Boolean,
    isParsingStories: Boolean,
    parsedStories: List<InstagramParsedMedia>,
    parsedProfileInfo: String?,
    onPlatformSelected: (PlatformType?) -> Unit,
    onSubCategorySelected: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onClearUrl: () -> Unit,
    onOpenQualityModal: () -> Unit,
    onQuickDownload: () -> Unit,
    onDownloadShortDirect: () -> Unit,
    onParseStories: (String, Boolean) -> Unit,
    onToggleStorySelection: (String) -> Unit,
    onSelectAllStories: (Boolean) -> Unit,
    onDownloadSelectedStories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler(enabled = selectedPlatform != null) {
        onPlatformSelected(null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        if (selectedPlatform == null) {
            // Main Platform Selector View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .padding(bottom = 100.dp)
            ) {
                // Heading text: "Which website you wanna download from?"
                Text(
                    text = "Which website you wanna download from?",
                    color = BurgundyHeader,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap a platform to start, or paste any video link for instant magic.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Direct Quick Paste Bar
                QuickPasteBar(
                    urlInput = urlInput,
                    isYouTubeShort = isYouTubeShort,
                    isParsingUrl = isParsingUrl,
                    onUrlChanged = onUrlChanged,
                    onClearUrl = onClearUrl,
                    onOpenQuality = onOpenQualityModal,
                    onQuickDownload = onQuickDownload,
                    onDownloadShortDirect = onDownloadShortDirect
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SUPPORTED PLATFORMS",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Grid of 6 rounded boxy options
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val platforms = listOf(
                        PlatformType.FACEBOOK,
                        PlatformType.YOUTUBE,
                        PlatformType.INSTAGRAM,
                        PlatformType.SITE_A,
                        PlatformType.SITE_B,
                        PlatformType.TIKTOK
                    )

                    platforms.chunked(2).forEach { rowPlatforms ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            rowPlatforms.forEach { platform ->
                                PlatformCard(
                                    platform = platform,
                                    onClick = { onPlatformSelected(platform) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPlatforms.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // Specific Platform Downloader View
            PlatformDownloaderView(
                platform = selectedPlatform,
                selectedSubCategory = selectedSubCategory,
                urlInput = urlInput,
                isPro = isPro,
                isYouTubeShort = isYouTubeShort,
                isParsingUrl = isParsingUrl,
                isParsingStories = isParsingStories,
                parsedStories = parsedStories,
                parsedProfileInfo = parsedProfileInfo,
                onBack = { onPlatformSelected(null) },
                onSubCategorySelected = onSubCategorySelected,
                onUrlChanged = onUrlChanged,
                onClearUrl = onClearUrl,
                onOpenQualityModal = onOpenQualityModal,
                onQuickDownload = onQuickDownload,
                onDownloadShortDirect = onDownloadShortDirect,
                onParseStories = onParseStories,
                onToggleStorySelection = onToggleStorySelection,
                onSelectAllStories = onSelectAllStories,
                onDownloadSelectedStories = onDownloadSelectedStories
            )
        }
    }
}

@Composable
private fun PlatformCard(
    platform: PlatformType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = BurgundyDeep.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(DarkContainerElevated, DarkContainer)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.25f),
                        platform.brandColor.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
            .testTag("platform_card_${platform.name.lowercase()}"),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(platform.brandColor.copy(alpha = 0.2f))
                    .border(1.dp, platform.brandColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (platform) {
                        PlatformType.YOUTUBE -> Icons.Default.PlayCircle
                        PlatformType.INSTAGRAM -> Icons.Default.Share
                        PlatformType.FACEBOOK -> Icons.Default.OndemandVideo
                        PlatformType.TIKTOK -> Icons.Default.CloudDownload
                        PlatformType.SITE_A -> Icons.Default.Tune
                        PlatformType.SITE_B -> Icons.Default.Bolt
                    },
                    contentDescription = platform.title,
                    tint = platform.brandColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = platform.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = platform.subtitle,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun QuickPasteBar(
    urlInput: String,
    isYouTubeShort: Boolean,
    isParsingUrl: Boolean,
    onUrlChanged: (String) -> Unit,
    onClearUrl: () -> Unit,
    onOpenQuality: () -> Unit,
    onQuickDownload: () -> Unit,
    onDownloadShortDirect: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkContainer)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Magic Downloader",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                            if (clipText.isNotBlank()) {
                                onUrlChanged(clipText)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("home_quick_paste_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = GoldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "PASTE",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChanged,
                placeholder = {
                    Text("Paste any video link for magic...", color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp)
                },
                trailingIcon = {
                    if (urlInput.isNotBlank()) {
                        IconButton(onClick = onClearUrl) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_quick_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            if (urlInput.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                if (isParsingUrl) {
                    VideoUrlParsingBanner(
                        title = "Parsing Video URL...",
                        subtitle = "Detecting source & available HD streams",
                        accentColor = GoldAccent
                    )
                } else if (isYouTubeShort) {
                    // YouTube Short Detected: Direct Download in Highest Resolution (No quality modal needed)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(YouTubeRed.copy(alpha = 0.15f))
                            .border(1.dp, YouTubeRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "YouTube Short Detected",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Highest resolution ready instantly without selecting quality.",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onDownloadShortDirect,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("quick_download_short_direct"),
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Download Short (Highest Quality)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onQuickDownload,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("home_quick_download_action"),
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyAccent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                Text("Quick Download", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onOpenQuality,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("home_select_quality_action"),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkContainerElevated),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Select Quality", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformDownloaderView(
    platform: PlatformType,
    selectedSubCategory: String,
    urlInput: String,
    isPro: Boolean,
    isYouTubeShort: Boolean,
    isParsingUrl: Boolean,
    isParsingStories: Boolean,
    parsedStories: List<InstagramParsedMedia>,
    parsedProfileInfo: String?,
    onBack: () -> Unit,
    onSubCategorySelected: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onClearUrl: () -> Unit,
    onOpenQualityModal: () -> Unit,
    onQuickDownload: () -> Unit,
    onDownloadShortDirect: () -> Unit,
    onParseStories: (String, Boolean) -> Unit,
    onToggleStorySelection: (String) -> Unit,
    onSelectAllStories: (Boolean) -> Unit,
    onDownloadSelectedStories: () -> Unit
) {
    val context = LocalContext.current
    val isStoryOrHighlight = platform == PlatformType.INSTAGRAM &&
            (selectedSubCategory == "Story" || selectedSubCategory == "Highlights")
    val isHighlightMode = selectedSubCategory == "Highlights"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 100.dp)
    ) {
        // Platform Top Bar with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, CreamCardBorder, CircleShape)
                    .testTag("platform_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = BurgundyHeader,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "${platform.title} Downloader",
                    color = BurgundyHeader,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = platform.subtitle,
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Subcategory Pills (especially for Instagram: Reels, Photos, Story, Highlights, Profile Pictures)
        if (platform.subCategories.size > 1) {
            Text(
                text = "SELECT DOWNLOAD TYPE:",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(platform.subCategories) { category ->
                    val isSelected = category == selectedSubCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) platform.brandColor
                                else Color.White
                            )
                            .border(
                                1.dp,
                                if (isSelected) platform.brandColor else CreamCardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSubCategorySelected(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("subcat_${category.replace(" ", "_")}")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // ==========================================
        // INSTAGRAM STORIES & HIGHLIGHTS DISTINCT FLOW
        // ==========================================
        if (isStoryOrHighlight) {
            InstagramStoriesSection(
                isHighlightMode = isHighlightMode,
                urlInput = urlInput,
                isParsing = isParsingStories,
                parsedStories = parsedStories,
                profileInfo = parsedProfileInfo,
                onUrlChanged = onUrlChanged,
                onClearUrl = onClearUrl,
                onParse = { onParseStories(urlInput, isHighlightMode) },
                onToggleItem = onToggleStorySelection,
                onSelectAll = onSelectAllStories,
                onDownloadSelected = onDownloadSelectedStories
            )
        } else {
            // Standard Link Input Card (for YouTube, Reels, Photos, etc.)
            StandardLinkDownloaderCard(
                platform = platform,
                urlInput = urlInput,
                isPro = isPro,
                isYouTubeShort = isYouTubeShort,
                isParsingUrl = isParsingUrl,
                onUrlChanged = onUrlChanged,
                onClearUrl = onClearUrl,
                onOpenQualityModal = onOpenQualityModal,
                onQuickDownload = onQuickDownload,
                onDownloadShortDirect = onDownloadShortDirect
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pro Tip Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, CreamCardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Pro Tip for ${platform.title} (${selectedSubCategory}):",
                    color = BurgundyHeader,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        isStoryOrHighlight -> "Input either @username (e.g., @wanderer) or the full story/highlight link. Penguin Plus automatically parses all active story slides and highlights for bulk download."
                        platform == PlatformType.YOUTUBE && isYouTubeShort -> "YouTube Shorts are automatically detected! Tap 'Download Short (Highest Quality)' for direct instant 1080p saving without modal popups."
                        platform == PlatformType.YOUTUBE -> "Penguin Plus auto-detects regular videos vs. Shorts and extracts audio tracks automatically."
                        platform == PlatformType.TIKTOK -> "Downloads are 100% watermark-free with original high-bitrate audio."
                        else -> "High-speed multi-threaded chunks deliver instant downloads on 120Hz/60Hz devices."
                    },
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun StandardLinkDownloaderCard(
    platform: PlatformType,
    urlInput: String,
    isPro: Boolean,
    isYouTubeShort: Boolean,
    isParsingUrl: Boolean,
    onUrlChanged: (String) -> Unit,
    onClearUrl: () -> Unit,
    onOpenQualityModal: () -> Unit,
    onQuickDownload: () -> Unit,
    onDownloadShortDirect: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = BurgundyDeep.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(26.dp))
            .background(DarkContainer)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paste the link for magic:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                            if (clipText.isNotBlank()) {
                                onUrlChanged(clipText)
                            }
                        }
                        .testTag("liquid_paste_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from Clipboard",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChanged,
                placeholder = {
                    Text(platform.placeholderUrl, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                },
                trailingIcon = {
                    if (urlInput.isNotBlank()) {
                        IconButton(onClick = onClearUrl) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("platform_url_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Show Animatable parsing progress banner while analyzing pasted URL
            if (isParsingUrl) {
                VideoUrlParsingBanner(
                    title = "Parsing ${platform.title} Link...",
                    subtitle = "Extracting video metadata & stream resolutions",
                    accentColor = platform.brandColor
                )
            } else if (platform == PlatformType.YOUTUBE && isYouTubeShort) {
                // SPECIAL YOUTUBE SHORT DIRECT DOWNLOAD FLOW:
                // No quality selector modal needed! Direct download in highest resolution.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(YouTubeRed.copy(alpha = 0.15f))
                        .border(1.dp, YouTubeRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                            Text(
                                text = "YouTube Short Detected",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Direct download ready in Max HD resolution without quality modal popups.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onDownloadShortDirect,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("direct_youtube_short_download_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isPro) "Download Short in 4K Max" else "Download Short in 1080p HD",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // Standard Regular Video Flow: Quick Download & Select Quality
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onQuickDownload,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(50.dp)
                            .testTag("platform_quick_download_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(BurgundyAccent, AmberPro)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isPro) "Quick (4K Ultra)" else "Quick Download",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onOpenQualityModal,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(50.dp)
                            .testTag("platform_select_quality_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkContainerElevated),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Text("Quality", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstagramStoriesSection(
    isHighlightMode: Boolean,
    urlInput: String,
    isParsing: Boolean,
    parsedStories: List<InstagramParsedMedia>,
    profileInfo: String?,
    onUrlChanged: (String) -> Unit,
    onClearUrl: () -> Unit,
    onParse: () -> Unit,
    onToggleItem: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onDownloadSelected: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = InstagramPink.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(26.dp))
            .background(DarkContainer)
            .border(1.dp, InstagramPink.copy(alpha = 0.4f), RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isHighlightMode) "Instagram Highlights Downloader" else "Instagram Stories Downloader",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isHighlightMode) "Enter profile URL or highlight link" else "Enter profile username or story link",
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(InstagramPink.copy(alpha = 0.2f))
                        .border(1.dp, InstagramPink, CircleShape)
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                            if (clipText.isNotBlank()) {
                                onUrlChanged(clipText)
                            }
                        }
                        .testTag("story_paste_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Guided Input box
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChanged,
                placeholder = {
                    Text(
                        if (isHighlightMode) "e.g. @wanderlust or instagram.com/stories/highlights/..."
                        else "e.g. @wanderlust or instagram.com/stories/wanderlust/...",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                },
                trailingIcon = {
                    if (urlInput.isNotBlank()) {
                        IconButton(onClick = onClearUrl) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("instagram_story_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = InstagramPink,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedLabelColor = InstagramPink,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Example Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Try sample:", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable {
                            onUrlChanged(if (isHighlightMode) "https://instagram.com/stories/highlights/1789234891" else "@traveler_diaries")
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(if (isHighlightMode) "Highlight Link" else "@traveler_diaries", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isParsing) {
                VideoUrlParsingBanner(
                    title = if (isHighlightMode) "Parsing Instagram Highlights..." else "Parsing Instagram Stories...",
                    subtitle = "Inspecting profile feed & media metadata",
                    accentColor = InstagramPink
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Parse & Fetch Button
            Button(
                onClick = onParse,
                enabled = !isParsing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("fetch_stories_button"),
                colors = ButtonDefaults.buttonColors(containerColor = InstagramPink),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isParsing) {
                    CustomParsingProgressIndicator(
                        size = 20.dp,
                        strokeWidth = 2.4.dp,
                        primaryColor = Color.White,
                        secondaryColor = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetching Media Tokens...", color = Color.White, fontSize = 13.sp)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (isHighlightMode) "Fetch & Parse Highlights" else "Fetch & Parse Stories",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Parsed Stories Media Feed Preview & Selector
            if (parsedStories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profileInfo ?: "Parsed Media Items",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Toggle Select All
                    val allSelected = parsedStories.all { it.isSelected }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectAll(!allSelected) }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (allSelected) "Deselect All" else "Select All",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of parsed story items
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    parsedStories.forEach { media ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkContainerElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (media.isSelected) InstagramPink else DarkCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onToggleItem(media.id) }
                                .padding(12.dp)
                                .testTag("story_item_${media.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Media Badge Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(media.previewColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = media.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${media.mediaType} • ${media.timestampAgo} • ${media.approxSize}",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = media.isSelected,
                                    onCheckedChange = { onToggleItem(media.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = InstagramPink,
                                        uncheckedColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Download Selected Action Button
                val selectedCount = parsedStories.count { it.isSelected }
                Button(
                    onClick = onDownloadSelected,
                    enabled = selectedCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_selected_stories_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Download Selected ($selectedCount items)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
