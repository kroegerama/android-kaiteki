package com.kroegerama.kaiteki.compose.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive

@Stable
fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape,
    intervals: Array<Dp> = arrayOf(
        width * 4,
        width * 4
    ),
    phase: () -> Float = { 0f },
    cap: StrokeCap = StrokeCap.Square
) = drawWithCache {
    val strokeWidth = width.toPx()
    val pathEffect = PathEffect.dashPathEffect(
        intervals = intervals.map {
            it.toPx()
        }.toFloatArray(),
        phase()
    )
    val stroke = Stroke(
        width = strokeWidth,
        cap = cap,
        pathEffect = pathEffect
    )

    val outline = shape.createOutline(
        size = Size(
            size.width - strokeWidth,
            size.height - strokeWidth
        ),
        layoutDirection = LayoutDirection.Ltr,
        density = this
    )

    onDrawBehind {
        translate(
            left = strokeWidth / 2f,
            top = strokeWidth / 2f
        ) {
            drawOutline(
                outline = outline,
                color = color,
                style = stroke
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashedBorderPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Hello World",
            modifier = Modifier
                .dashedBorder(
                    width = 1.dp,
                    color = Color.Red
                )
                .padding(8.dp)
        )
        Text(
            text = "Hello World",
            modifier = Modifier
                .dashedBorder(
                    width = 1.dp,
                    color = Color.Red,
                    shape = CircleShape
                )
                .padding(8.dp)
        )
        Text(
            text = "Hello World",
            modifier = Modifier
                .dashedBorder(
                    width = 1.dp,
                    color = Color.Red,
                    intervals = remember {
                        arrayOf(
                            1.dp,
                            2.dp,
                            3.dp,
                            4.dp
                        )
                    },
                    cap = StrokeCap.Round
                )
                .padding(8.dp)
        )
        var phase by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(Unit) {
            var lastUpdateTime = awaitFrame()
            while (isActive) {
                withFrameNanos { time ->
                    if (time - lastUpdateTime >= 33_333_333L) {
                        lastUpdateTime = time
                        phase += 2f
                    }
                }
            }
        }

        var c by remember { mutableIntStateOf(0) }
        Text(
            text = "Hello World $c",
            modifier = Modifier
                .dashedBorder(
                    width = 1.dp,
                    color = Color.Red,
                    shape = CircleShape,
                    intervals = arrayOf(
                        2.dp,
                        4.dp
                    ),
                    cap = StrokeCap.Round,
                    phase = { -phase }
                )
                .padding(8.dp)
                .clickable { c++ }
        )
    }
}
