package com.kroegerama.kaiteki.compose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collection.MutableVector
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kroegerama.kaiteki.compose.ExperimentalKaitekiComposeApi
import com.kroegerama.kaiteki.compose.utils.PreviewLayoutDirection

@ExperimentalKaitekiComposeApi
@Composable
fun RowWithDividers(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    divider: @Composable () -> Unit = { VerticalDivider() },
    showStartDivider: Boolean = false,
    showEndDivider: Boolean = false,
    content: RowWithDividersScope.() -> Unit
) {
    val latestContent by rememberUpdatedState(content)
    val scope by remember {
        derivedStateOf {
            RowWithDividersScopeImpl().apply(latestContent)
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        if (!scope.itemList.isEmpty() && showStartDivider) {
            divider()
        }
        scope.itemList.forEachIndexed { index, itemContent ->
            if (index > 0) {
                divider()
            }
            itemContent()
        }
        if (!scope.itemList.isEmpty() && showEndDivider) {
            divider()
        }
    }
}

interface RowWithDividersScope {
    fun item(content: @Composable RowScope.() -> Unit)
    fun items(count: Int, content: @Composable RowScope.(Int) -> Unit)
}

private interface RowItemProvider {
    val itemList: MutableVector<@Composable RowScope.() -> Unit>
}

private class RowWithDividersScopeImpl : RowWithDividersScope, RowItemProvider {

    override val itemList: MutableVector<@Composable (RowScope.() -> Unit)> = mutableVectorOf()

    override fun item(content: @Composable (RowScope.() -> Unit)) {
        itemList += content
    }

    override fun items(count: Int, content: @Composable RowScope.(Int) -> Unit) {
        repeat(count) { item { content(it) } }
    }

}

@OptIn(ExperimentalKaitekiComposeApi::class)
@PreviewLayoutDirection
@Composable
private fun RowWithDividersPreview() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Test"
        )
        RowWithDividers(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            items(4) {
                Text("$it")
                Icon(
                    imageVector = Icons.Rounded.Face,
                    contentDescription = null
                )
            }
        }
        Text(
            text = "Start"
        )
        RowWithDividers(
            horizontalArrangement = Arrangement.SpaceEvenly,
            showStartDivider = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            items(4) {
                Text("$it")
                Icon(
                    imageVector = Icons.Rounded.Face,
                    contentDescription = null
                )
            }
        }
        Text(
            text = "End"
        )
        RowWithDividers(
            horizontalArrangement = Arrangement.SpaceEvenly,
            showEndDivider = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            items(4) {
                Text("$it")
                Icon(
                    imageVector = Icons.Rounded.Face,
                    contentDescription = null
                )
            }
        }
        Text(
            text = "Both"
        )
        RowWithDividers(
            horizontalArrangement = Arrangement.SpaceEvenly,
            showStartDivider = true,
            showEndDivider = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            items(4) {
                Text("$it")
                Icon(
                    imageVector = Icons.Rounded.Face,
                    contentDescription = null
                )
            }
        }
    }
}
