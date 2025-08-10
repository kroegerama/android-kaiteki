package com.kroegerama.kaiteki.compose.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun Modifier.pressAndHold(
    enabled: Boolean = true,
    maxDelayMillis: Long = 600,
    minDelayMillis: Long = 20,
    delayDecayFactor: Float = .25f,
    onClick: () -> Unit
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val updatedOnClick by rememberUpdatedState(onClick)

    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            updatedOnClick()
            val job = scope.launch {
                var currentDelayMillis = maxDelayMillis
                while (isActive && down.pressed) {
                    delay(currentDelayMillis)
                    updatedOnClick()
                    val nextMillis = currentDelayMillis - (currentDelayMillis * delayDecayFactor)
                    currentDelayMillis = nextMillis.toLong().coerceAtLeast(minDelayMillis)
                }
            }
            try {
                waitForUpOrCancellation()
            } finally {
                job.cancel()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PressAndHoldPreview() {
    var value by remember { mutableIntStateOf(0) }
    Column {
        Text(
            text = "Value: $value"
        )
        Button(
            onClick = { },
            modifier = Modifier.pressAndHold {
                value++
            }
        ) {
            Text(
                text = "Press and hold"
            )
        }
    }
}
