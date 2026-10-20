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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PicassoBlue
import com.example.ui.theme.PicassoInk
import com.example.ui.theme.PicassoOchre
import com.example.ui.theme.PicassoOlive
import com.example.ui.theme.PicassoTerracotta
import com.example.ui.theme.PicassoVermilion

@Composable
fun BatteryGauge(
    level: Int,
    isCharging: Boolean,
    threshold: Int = 20,
    modifier: Modifier = Modifier
) {
    val isCritical = level <= threshold

    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "BatteryLevelAnim"
    )

    val activeColor by animateColorAsState(
        targetValue = when {
            isCritical -> PicassoVermilion
            level <= 50 -> PicassoOchre
            else -> PicassoBlue
        },
        animationSpec = tween(400),
        label = "ActiveColorAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PicassoBreathing")
    val alertPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isCritical) 1.0f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlertPulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 240.dp, height = 240.dp)
            .testTag("picasso_battery_gauge")
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 3.5.dp.toPx()
            val inkColor = if (isCritical) PicassoVermilion else PicassoInk

            // Cubist angular background framing
            val framePath = Path().apply {
                moveTo(20f, 30f)
                lineTo(size.width - 25f, 15f)
                lineTo(size.width - 15f, size.height - 25f)
                lineTo(25f, size.height - 15f)
                close()
            }

            // Draw abstract cubist shadow plane
            val shadowPath = Path().apply {
                moveTo(32f, 42f)
                lineTo(size.width - 15f, 28f)
                lineTo(size.width - 5f, size.height - 15f)
                lineTo(38f, size.height - 5f)
                close()
            }
            drawPath(
                path = shadowPath,
                color = (if (isCritical) PicassoVermilion else PicassoTerracotta).copy(alpha = 0.08f)
            )

            // Draw subtle Mediterranean facet
            val facetPath = Path().apply {
                moveTo(20f, 30f)
                lineTo(size.width / 2, size.height / 2)
                lineTo(25f, size.height - 15f)
                close()
            }
            drawPath(
                path = facetPath,
                color = (if (isCritical) PicassoVermilion else PicassoBlue).copy(alpha = 0.05f)
            )

            // Draw main contour frame with bold Picasso charcoal ink line
            drawPath(
                path = framePath,
                color = inkColor.copy(alpha = if (isCritical) alertPulse else 0.8f),
                style = Stroke(width = strokeWidth)
            )

            // Inner Battery Monument: Modernist Geometric Gauge
            val gaugeLeft = 50f
            val gaugeRight = size.width - 50f
            val gaugeWidth = gaugeRight - gaugeLeft
            val gaugeBottom = size.height - 45f
            val gaugeHeight = 16.dp.toPx()
            val gaugeTop = gaugeBottom - gaugeHeight

            // Track background
            drawRoundRect(
                color = inkColor.copy(alpha = 0.12f),
                topLeft = Offset(gaugeLeft, gaugeTop),
                size = Size(gaugeWidth, gaugeHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Fill level
            val fillWidth = (animatedLevel / 100f) * gaugeWidth
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(gaugeLeft, gaugeTop),
                    size = Size(fillWidth, gaugeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            // Threshold marker notch at 20%
            val thresholdX = gaugeLeft + (threshold / 100f) * gaugeWidth
            drawLine(
                color = PicassoInk,
                start = Offset(thresholdX, gaugeTop - 6.dp.toPx()),
                end = Offset(thresholdX, gaugeBottom + 6.dp.toPx()),
                strokeWidth = 2.5.dp.toPx()
            )

            // Cubist accent line (Picasso signature geometric slash)
            drawLine(
                color = if (isCritical) PicassoVermilion else PicassoOchre,
                start = Offset(30f, 65f),
                end = Offset(65f, 25f),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Center Minimalist Typographic Core
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            if (isCharging) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PicassoOlive.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "En charge",
                        tint = PicassoOlive,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "EN CHARGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PicassoOlive,
                        letterSpacing = 1.sp
                    )
                }
            } else if (isCritical) {
                Text(
                    text = "CRITIQUE (<$threshold%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = PicassoVermilion,
                    letterSpacing = 1.5.sp
                )
            } else {
                Text(
                    text = "PICASSO • MINIMAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Percentage displayed in bold sculptural typography
            Text(
                text = "${animatedLevel.toInt()}%",
                fontSize = 58.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                color = if (isCritical) PicassoVermilion else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("battery_percentage_text")
            )

            Text(
                text = "Seuil d'alerte à $threshold%",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
