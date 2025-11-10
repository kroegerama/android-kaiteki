package com.kroegerama.kaiteki.compose.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.fastForEach
import com.kroegerama.kaiteki.compose.ExperimentalKaitekiComposeApi
import com.kroegerama.kaiteki.compose.previews.SampleIcon

@ExperimentalKaitekiComposeApi
@Composable
fun ScaleBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    minScale: Float = 0f,
    maxScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val layoutWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            Constraints.Infinity
        }
        val layoutHeight = if (constraints.hasBoundedHeight) {
            constraints.maxHeight
        } else {
            Constraints.Infinity
        }

        val placeables = measurables.map {
            it.measure(Constraints())
        }

        layout(layoutWidth, layoutHeight) {
            placeables.fastForEach { placeable ->
                val sX = layoutWidth.toFloat() / placeable.width.toFloat()
                val sY = layoutHeight.toFloat() / placeable.height.toFloat()
                val scale = minOf(sX, sY).coerceIn(minScale, maxScale)
                val sW = placeable.width * scale
                val sH = placeable.height * scale

                val offset = contentAlignment.align(
                    Size(sW, sH).roundToIntSize(),
                    IntSize(layoutWidth, layoutHeight),
                    layoutDirection
                )

                placeable.placeRelativeWithLayer(offset.x, offset.y) {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
        }
    }
}

@OptIn(ExperimentalKaitekiComposeApi::class)
@Preview(showBackground = true)
@Composable
private fun ScaleBoxPreview() {
    ScaleBox(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = SampleIcon,
            contentDescription = null,
            modifier = Modifier.size(600.dp)
        )
    }
}
