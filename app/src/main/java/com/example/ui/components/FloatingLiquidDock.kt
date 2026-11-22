package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPro
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyHeader
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkContainer
import com.example.ui.theme.DarkContainerElevated
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.AppTab

@Composable
fun FloatingLiquidDock(
    currentTab: AppTab,
    activeDownloadsCount: Int,
    completedDownloadsCount: Int,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Liquid Capsule Container
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = BurgundyDeep.copy(alpha = 0.5f),
                    spotColor = BurgundyDeep.copy(alpha = 0.6f)
                )
                .clip(RoundedCornerShape(38.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkContainerElevated.copy(alpha = 0.95f),
                            DarkContainer.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.10f),
                            GoldAccent.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockTabItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentTab == AppTab.HOME,
                    badgeCount = 0,
                    onClick = { onTabSelected(AppTab.HOME) },
                    testTag = "tab_home"
                )

                DockTabItem(
                    title = "Progress",
                    icon = Icons.Default.Sync,
                    isSelected = currentTab == AppTab.PROGRESS,
                    badgeCount = activeDownloadsCount,
                    onClick = { onTabSelected(AppTab.PROGRESS) },
                    testTag = "tab_progress"
                )

                DockTabItem(
                    title = "Downloads",
                    icon = Icons.Default.Folder,
                    isSelected = currentTab == AppTab.DOWNLOADS,
                    badgeCount = completedDownloadsCount,
                    onClick = { onTabSelected(AppTab.DOWNLOADS) },
                    testTag = "tab_downloads"
                )
            }
        }
    }
}

@Composable
private fun DockTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Spring animations for iOS-level physics
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.65f),
        label = "tab_color"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(
                            BurgundyAccent.copy(alpha = 0.85f),
                            BurgundyHeader.copy(alpha = 0.95f)
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(30.dp))
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 10.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AmberPro else BurgundyHeader)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isSelected) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
