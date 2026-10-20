package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BatteryAmber
import com.example.ui.theme.BatteryAmberLight
import com.example.ui.theme.BatteryGreen
import com.example.ui.theme.BatteryGreenLight
import com.example.ui.theme.BatteryRed
import com.example.ui.theme.BatteryRedLight

@Composable
fun BatteryGauge(
    level: Int,
    isCharging: Boolean,
    threshold: Int = 20,
    size: Dp = 110.dp,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "BatteryLevelAnimation"
    )

    val isCritical = level <= threshold

    val primaryColor by animateColorAsState(
        targetValue = when {
            level <= threshold -> BatteryRed
            level <= 50 -> BatteryAmber
            else -> BatteryGreen
        },
        animationSpec = tween(500),
        label = "PrimaryColorAnimation"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when {
            level <= threshold -> BatteryRedLight
            level <= 50 -> BatteryAmberLight
            else -> BatteryGreenLight
        },
        animationSpec = tween(500),
        label = "SecondaryColorAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "CriticalPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isCritical) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(if (isCritical) pulseScale else 1f)
            .testTag("battery_gauge_container")
    ) {
        if (isCritical) {
            Box(
                modifier = Modifier
                    .size(size - 16.dp)
                    .clip(CircleShape)
                    .background(BatteryRed.copy(alpha = 0.15f))
            )
        }

        Canvas(modifier = Modifier.size(size - 10.dp)) {
            val strokeWidth = (size.toPx() * 0.09f).coerceIn(8.dp.toPx(), 14.dp.toPx())
            val diameter = this.size.minDimension - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(
                (this.size.width - diameter) / 2,
                (this.size.height - diameter) / 2
            )
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            // Background track (260 degree arc)
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Threshold marker indicator line
            val thresholdAngle = 140f + (threshold.toFloat() / 100f) * 260f
            drawArc(
                color = Color.White.copy(alpha = 0.7f),
                startAngle = thresholdAngle - 1f,
                sweepAngle = 2f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Square)
            )

            // Progress Arc
            val sweep = (animatedLevel / 100f) * 260f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor, secondaryColor, primaryColor)
                    ),
                    startAngle = 140f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("battery_gauge_content")
        ) {
            if (isCharging) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "En charge",
                    tint = BatteryGreen,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isCritical) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Critique",
                    tint = BatteryRed,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = "${animatedLevel.toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.23f).sp
                ),
                color = if (isCritical) BatteryRed else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("battery_percentage_text")
            )

            Text(
                text = "<$threshold%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
