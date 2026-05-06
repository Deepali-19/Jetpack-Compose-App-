package com.example.jetpackcomposeapp.Topic

import androidx.compose.foundation.Canvas as DrawCanvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Canvas() {
    val touchPoints = remember { mutableStateListOf<Offset>() }

    DrawCanvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    touchPoints.add(change.position)
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1A1A1A), Color(0xFF424242))
            ),
            size = size
        )

        val step = 100f
        for (i in 0..(canvasWidth / step).toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(i * step, 0f),
                end = Offset(i * step, canvasHeight),
                strokeWidth = 1f
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Cyan, Color.Transparent),
                center = Offset(200f, 200f),
                radius = 150f
            ),
            radius = 150f,
            center = Offset(200f, 200f),
            blendMode = BlendMode.Screen
        )

        val curvePath = Path().apply {
            moveTo(50f, 500f)
            quadraticBezierTo(
                canvasWidth / 2,
                300f,
                canvasWidth - 50f,
                500f
            )
        }
        drawPath(
            path = curvePath,
            color = Color.Yellow,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        withTransform({
            rotate(45f, pivot = Offset(canvasWidth / 2, canvasHeight / 2))
        }) {
            drawRoundRect(
                color = Color(0xFFFF5722),
                topLeft = Offset(canvasWidth / 2 - 100f, canvasHeight / 2 - 100f),
                size = Size(200f, 200f),
                cornerRadius = CornerRadius(30f),
                style = Stroke(width = 8f)
            )
        }

        if (touchPoints.isNotEmpty()) {
            val userPath = Path().apply {
                val first = touchPoints.first()
                moveTo(first.x, first.y)
                touchPoints.forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = userPath,
                color = Color.Green,
                style = Stroke(
                    width = 10f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.cornerPathEffect(50f)
                )
            )
        }
    }
}
