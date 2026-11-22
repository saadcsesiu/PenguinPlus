package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPro
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkContainerElevated
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom circular progress animation built with Compose [Animatable] and [Canvas]
 * to provide smooth 120Hz/60Hz visual feedback during video URL and media parsing.
 */
@Composable
fun CustomParsingProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    strokeWidth: Dp = 3.dp,
    primaryColor: Color = GoldAccent,
    secondaryColor: Color = AmberPro,
    trackColor: Color = Color.White.copy(alpha = 0.16f)
) {
    val rotationAnim = remember { Animatable(0f) }
    val sweepAnim = remember { Animatable(48f) }
    val glowPulseAnim = remember { Animatable(0.55f) }

    LaunchedEffect(Unit) {
        launch {
            while (true) {
                rotationAnim.animateTo(
                    targetValue = rotationAnim.value + 360f,
                    animationSpec = tween(durationMillis = 1050, easing = LinearEasing)
                )
            }
        }
        launch {
            while (true) {
                sweepAnim.animateTo(
                    targetValue = 275f,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
                sweepAnim.animateTo(
                    targetValue = 52f,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
            }
        }
        launch {
            while (true) {
                glowPulseAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                )
                glowPulseAnim.animateTo(
                    targetValue = 0.5f,
                    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            .size(size)
            .testTag("custom_animatable_progress")
    ) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        val topLeft = Offset(inset, inset)

        // Subtle outer track ring
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Rotating gradient arc + orbiting glow dot
        rotate(degrees = rotationAnim.value) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.15f),
                        secondaryColor,
                        primaryColor
                    )
                ),
                startAngle = 0f,
                sweepAngle = sweepAnim.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val radius = (this.size.minDimension - strokePx) / 2f
            val tipRad = (sweepAnim.value * PI / 180.0)
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val tipOffset = Offset(
                x = center.x + (radius * cos(tipRad)).toFloat(),
                y = center.y + (radius * sin(tipRad)).toFloat()
            )

            drawCircle(
                color = primaryColor.copy(alpha = 0.35f * glowPulseAnim.value),
                radius = strokePx * 1.45f,
                center = tipOffset
            )
            drawCircle(
                color = Color.White,
                radius = strokePx * 0.55f,
                center = tipOffset
            )
        }
    }
}

/**
 * Rich status banner displayed during automatic video URL parsing and stream extraction.
 */
@Composable
fun VideoUrlParsingBanner(
    title: String,
    subtitle: String,
    accentColor: Color = GoldAccent,
    modifier: Modifier = Modifier
) {
    val progressRatio = remember { Animatable(0.12f) }

    LaunchedEffect(title) {
        progressRatio.snapTo(0.12f)
        progressRatio.animateTo(
            targetValue = 0.94f,
            animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkContainerElevated)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("url_parsing_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            CustomParsingProgressIndicator(
                size = 32.dp,
                strokeWidth = 3.5.dp,
                primaryColor = accentColor,
                secondaryColor = AmberPro
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Text(
                text = "${(progressRatio.value * 100).toInt()}%",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
