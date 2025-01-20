package com.kroegerama.kaiteki.compose.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun BoxCanvas(
    modifier: Modifier,
    contentDescription: String?,
    onDraw: DrawScope.() -> Unit
) = Box(
    modifier
        .drawBehind(onDraw)
        .semantics {
            contentDescription?.let {
                this.contentDescription = it
            }
        }
)
