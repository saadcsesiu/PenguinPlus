package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.DownloadHistoryItem
import com.example.model.DownloadItem
import com.example.model.VideoResolution
import com.example.ui.theme.AmberPro
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyHeader
import com.example.ui.theme.CoralError
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamCardBorder
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkContainer
import com.example.ui.theme.DarkContainerElevated
import com.example.ui.theme.EmeraldDone
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class DownloadsSubTab {
    OFFLINE_MEDIA,
    DOWNLOAD_HISTORY
}

@Composable
fun DownloadsScreen(
    completedDownloads: List<DownloadItem>,
    downloadHistory: List<DownloadHistoryItem>,
    onItemClick: (DownloadItem) -> Unit,
    onDeleteCompleted: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onRedownload: (DownloadHistoryItem) -> Unit,
    onGoToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(DownloadsSubTab.OFFLINE_MEDIA) }
    var filterTab by remember { mutableStateOf("All") }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val filteredCompletedList = when (filterTab) {
        "4K Ultra" -> completedDownloads.filter { it.resolution == VideoResolution.RES_4K }
        "1080p HD" -> completedDownloads.filter { it.resolution == VideoResolution.RES_1080P }
        "Reels & Shorts" -> completedDownloads.filter {
            it.subCategory.contains("Reel", ignoreCase = true) || it.subCategory.contains("Short", ignoreCase = true)
        }
        else -> completedDownloads
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            // Top Section Switcher: Offline Media vs Download History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkContainerElevated)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Offline Media Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (currentSubTab == DownloadsSubTab.OFFLINE_MEDIA) BurgundyHeader
                            else Color.Transparent
                        )
                        .clickable { currentSubTab = DownloadsSubTab.OFFLINE_MEDIA }
                        .padding(vertical = 10.dp)
                        .testTag("subtab_offline_media"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (currentSubTab == DownloadsSubTab.OFFLINE_MEDIA) GoldAccent else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Offline Files (${completedDownloads.size})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Download History Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (currentSubTab == DownloadsSubTab.DOWNLOAD_HISTORY) BurgundyHeader
                            else Color.Transparent
                        )
                        .clickable { currentSubTab = DownloadsSubTab.DOWNLOAD_HISTORY }
                        .padding(vertical = 10.dp)
                        .testTag("subtab_download_history"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (currentSubTab == DownloadsSubTab.DOWNLOAD_HISTORY) GoldAccent else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "History (${downloadHistory.size})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Render SubTab Content
            if (currentSubTab == DownloadsSubTab.OFFLINE_MEDIA) {
                // ==========================================
                // 1. OFFLINE MEDIA LIBRARY
                // ==========================================
                if (completedDownloads.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Folder,
                        title = "No Offline Files Yet",
                        description = "Videos and reels you download will be saved here for offline playback.",
                        actionText = "Download Something",
                        onAction = onGoToHome
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            // Filter Pills
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(end = 8.dp)
                            ) {
                                val tabs = listOf("All", "4K Ultra", "1080p HD", "Reels & Shorts")
                                items(tabs) { tab ->
                                    val isSelected = tab == filterTab
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) BurgundyHeader else Color.White)
                                            .border(1.dp, if (isSelected) BurgundyHeader else CreamCardBorder, RoundedCornerShape(14.dp))
                                            .clickable { filterTab = tab }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                            .testTag("filter_${tab.replace(" ", "_")}")
                                    ) {
                                        Text(
                                            text = tab,
                                            color = if (isSelected) Color.White else TextPrimaryDark,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(filteredCompletedList, key = { it.id }) { item ->
                            CompletedDownloadCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onShare = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, item.title)
                                        putExtra(Intent.EXTRA_TEXT, "Downloaded via Penguin Plus: ${item.originalUrl}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                                },
                                onDelete = { onDeleteCompleted(item.id) }
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // 2. DOWNLOAD HISTORY VIEW
                // ==========================================
                if (downloadHistory.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "No Download History",
                        description = "All previous downloads, their resolution, quality, and status will be recorded here.",
                        actionText = "Start Downloading",
                        onAction = onGoToHome
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // History Controls Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Download History Log",
                                        color = BurgundyHeader,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Source, quality, timestamp & status record",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }

                                // Clear All History Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, CreamCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { showClearHistoryDialog = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("clear_all_history_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = "Clear All",
                                            tint = CoralError,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Clear All",
                                            color = CoralError,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(downloadHistory, key = { it.id }) { historyItem ->
                            DownloadHistoryCard(
                                item = historyItem,
                                onRedownload = { onRedownload(historyItem) },
                                onDelete = { onDeleteHistoryItem(historyItem.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Clearing All History
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text("Clear Download History?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will clear all past download logs. Your offline media files will not be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear All", color = CoralError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DownloadHistoryCard(
    item: DownloadHistoryItem,
    onRedownload: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = BurgundyDeep.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(20.dp))
            .background(DarkContainer)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("history_card_${item.id}")
    ) {
        Column {
            // Row 1: Platform & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(item.platform.brandColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.platform.title,
                            color = item.platform.brandColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )

                    Text(
                        text = item.subCategory,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )

                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )

                    Text(
                        text = item.resolution.label,
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Badge (Completed / Failed)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (item.isSuccess) EmeraldDone.copy(alpha = 0.15f)
                            else CoralError.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (item.isSuccess) EmeraldDone else CoralError,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (item.isSuccess) "Completed" else "Failed",
                            color = if (item.isSuccess) EmeraldDone else CoralError,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (!item.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reason: ${item.errorMessage}",
                    color = CoralError.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Date, Size, Re-Download & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.formattedDate,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    if (item.fileSizeBytes > 0) {
                        Text(
                            text = item.formattedSize,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: Re-Download Button & Delete Item Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Re-download Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BurgundyAccent)
                            .clickable { onRedownload() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("redownload_button_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Re-download",
                                tint = GoldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Re-download",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Delete Individual History Item
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete History Entry",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedDownloadCard(
    item: DownloadItem,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = BurgundyDeep.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(22.dp))
            .background(DarkContainer)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("completed_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(BurgundyAccent, BurgundyDeep)
                        )
                    )
                    .border(1.dp, item.platform.brandColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = GoldAccent,
                    modifier = Modifier.size(32.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = item.resolution.label.split(" ").firstOrNull() ?: item.resolution.label,
                        color = GoldAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.platform.title,
                        color = item.platform.brandColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = item.totalFormatted,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to watch preview & playback",
                    color = GoldAccent.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CoralError.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(BurgundyHeader.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BurgundyHeader,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = BurgundyHeader,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            color = TextSecondaryDark,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyHeader),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(actionText, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
