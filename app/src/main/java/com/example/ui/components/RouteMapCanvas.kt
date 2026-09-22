package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated

@Composable
fun RouteMapCanvas(
    pickup: LocationPoint,
    dropoff: LocationPoint,
    distanceKm: Double,
    durationMinutes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "routeAnim")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val carProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "carMotion"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F1520))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle background urban grid
            val gridColor = Color(0xFF1E2838)
            val stepX = w / 8
            val stepY = h / 4

            for (i in 0..8) {
                drawLine(
                    color = gridColor,
                    start = Offset(i * stepX, 0f),
                    end = Offset(i * stepX, h),
                    strokeWidth = 1f
                )
            }
            for (j in 0..4) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, j * stepY),
                    end = Offset(w, j * stepY),
                    strokeWidth = 1f
                )
            }

            // 2. Pickup Point (Left side) & Dropoff Point (Right side)
            val pX = w * 0.16f
            val pY = h * 0.68f

            val dX = w * 0.84f
            val dY = h * 0.32f

            // Control points for smooth bezier road curve
            val ctrl1X = w * 0.38f
            val ctrl1Y = h * 0.88f
            val ctrl2X = w * 0.60f
            val ctrl2Y = h * 0.18f

            val routePath = Path().apply {
                moveTo(pX, pY)
                cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, dX, dY)
            }

            // Draw route outer glow
            drawPath(
                path = routePath,
                color = BoltGreen.copy(alpha = 0.25f),
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )

            // Draw route primary path
            drawPath(
                path = routePath,
                color = BoltGreen,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )

            // Draw dashed centerline for real road feel
            drawPath(
                path = routePath,
                color = Color.White.copy(alpha = 0.6f),
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            )

            // 3. Animated car moving along the curve (Cubic Bezier calculation)
            val t = carProgress
            val omt = 1f - t
            val carX = omt * omt * omt * pX +
                    3f * omt * omt * t * ctrl1X +
                    3f * omt * t * t * ctrl2X +
                    t * t * t * dX
            val carY = omt * omt * omt * pY +
                    3f * omt * omt * t * ctrl1Y +
                    3f * omt * t * t * ctrl2Y +
                    t * t * t * dY

            // Moving Bolt car beacon
            drawCircle(
                color = BoltGreen,
                radius = 7f,
                center = Offset(carX, carY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(carX, carY)
            )

            // 4. Pickup animated pulse
            drawCircle(
                color = BoltGreen.copy(alpha = (1.6f - pulseRatio).coerceIn(0f, 0.6f)),
                radius = 12f * pulseRatio,
                center = Offset(pX, pY)
            )
            drawCircle(
                color = BoltGreen,
                radius = 7.5f,
                center = Offset(pX, pY)
            )
            drawCircle(
                color = Color.Black,
                radius = 3f,
                center = Offset(pX, pY)
            )

            // 5. Dropoff destination marker
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.35f),
                radius = 11f,
                center = Offset(dX, dY)
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 7.5f,
                center = Offset(dX, dY)
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = Offset(dX, dY)
            )
        }

        // Overlay Route details tag
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkSurfaceElevated.copy(alpha = 0.88f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text(
                text = "$distanceKm km  •  ~$durationMinutes min",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Overlay Location names
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkSurfaceElevated.copy(alpha = 0.88f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = "${pickup.name.take(16)} → ${dropoff.name.take(16)}",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
