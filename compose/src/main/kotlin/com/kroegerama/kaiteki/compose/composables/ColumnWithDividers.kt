package com.kroegerama.kaiteki.compose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collection.MutableVector
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kroegerama.kaiteki.compose.ExperimentalKaitekiComposeApi

@ExperimentalKaitekiComposeApi
@Composable
fun ColumnWithDividers(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    divider: @Composable () -> Unit = { HorizontalDivider() },
    showTopDivider: Boolean = false,
    showBottomDivider: Boolean = false,
    content: ColumnWithDividersScope.() -> Unit
) {
    val latestContent by rememberUpdatedState(content)
    val scope by remember {
        derivedStateOf {
            ColumnWithDividersScopeImpl().apply(latestContent)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        if (!scope.itemList.isEmpty() && showTopDivider) {
            divider()
        }
        scope.itemList.forEachIndexed { index, itemContent ->
            if (index > 0) {
                divider()
            }
            itemContent()
        }
        if (!scope.itemList.isEmpty() && showBottomDivider) {
            divider()
        }
    }
}

interface ColumnWithDividersScope {
    fun item(content: @Composable ColumnScope.() -> Unit)
    fun items(count: Int, content: @Composable ColumnScope.(Int) -> Unit)
}

private interface ColumnItemProvider {
    val itemList: MutableVector<@Composable ColumnScope.() -> Unit>
}

private class ColumnWithDividersScopeImpl : ColumnWithDividersScope, ColumnItemProvider {

    override val itemList: MutableVector<@Composable (ColumnScope.() -> Unit)> = mutableVectorOf()

    override fun item(content: @Composable (ColumnScope.() -> Unit)) {
        itemList += content
    }

    override fun items(count: Int, content: @Composable ColumnScope.(Int) -> Unit) {
        repeat(count) { item { content(it) } }
    }

}

@OptIn(ExperimentalKaitekiComposeApi::class)
@Preview(showBackground = true)
@Composable
private fun ColumnWithDividersPreview() {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Test"
        )
        ColumnWithDividers(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
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
            text = "Top"
        )
        ColumnWithDividers(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            showTopDivider = true,
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
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
            text = "Bottom"
        )
        ColumnWithDividers(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            showBottomDivider = true,
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
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
        ColumnWithDividers(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            showTopDivider = true,
            showBottomDivider = true,
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
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
