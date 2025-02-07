package com.kroegerama.kaiteki.compose.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@ReadOnlyComposable
@Composable
fun Modifier.withHorizontalDivider(
    thickness: Dp = DividerDefaults.Thickness,
    alignment: Alignment.Vertical = Alignment.Bottom,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) = drawWithCache {
    val height = thickness.toPx()
    val y = alignment.align(
        size = height.toInt(),
        space = size.height.toInt()
    ) + height / 2f
    val start = Offset(0f, y)
    val end = Offset(size.width, y)
    onDrawBehind {
        drawLine(
            color = color,
            strokeWidth = height,
            start = start,
            end = end
        )
    }
}

@ReadOnlyComposable
@Composable
fun Modifier.withVerticalDivider(
    thickness: Dp = DividerDefaults.Thickness,
    alignment: Alignment.Horizontal = Alignment.End,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current
) = drawWithCache {
    val width = thickness.toPx()
    val x = alignment.align(
        width.toInt(),
        size.width.toInt(),
        layoutDirection
    ) + width / 2f
    val start = Offset(x, 0f)
    val end = Offset(x, size.height)
    onDrawWithContent {
        drawContent()
        drawLine(
            color = color,
            strokeWidth = width,
            start = start,
            end = end
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DividerModifierPreview() {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RectangleShape)
            .background(Color.LightGray)
    ) {
        Text(
            text = "Alignment.Top",
            modifier = Modifier.withHorizontalDivider(
                alignment = Alignment.Top,
                color = Color.Red
            )
        )
        Text(
            text = "Alignment.CenterVertically",
            modifier = Modifier.withHorizontalDivider(
                alignment = Alignment.CenterVertically,
                color = Color.Red
            )
        )
        Text(
            text = "Alignment.Bottom",
            modifier = Modifier.withHorizontalDivider(
                alignment = Alignment.Bottom,
                color = Color.Red
            )
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Alignment.Start",
            modifier = Modifier.withVerticalDivider(
                alignment = Alignment.Start,
                color = Color.Red
            )
        )
        Text(
            text = "Alignment.CenterHorizontally",
            modifier = Modifier.withVerticalDivider(
                alignment = Alignment.CenterHorizontally,
                color = Color.Red
            )
        )
        Text(
            text = "Alignment.End",
            modifier = Modifier.withVerticalDivider(
                alignment = Alignment.End,
                color = Color.Red
            )
        )
    }
}
