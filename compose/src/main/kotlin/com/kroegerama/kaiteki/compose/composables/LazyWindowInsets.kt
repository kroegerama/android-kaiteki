package com.kroegerama.kaiteki.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kroegerama.kaiteki.compose.utils.plus

internal val WindowInsets.Companion.insetsForLazyLayouts: WindowInsets
    @Composable get() = systemBars.union(WindowInsets.displayCutout)

@ExperimentalLayoutApi
@Composable
private inline fun LazyLayoutScaffold(
    contentWindowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    crossinline content: @Composable (Modifier, PaddingValues) -> Unit
) {
    val unconsumedInsets = remember(contentWindowInsets) { MutableWindowInsets(contentWindowInsets) }
    content(
        modifier
            .onConsumedWindowInsetsChanged { consumedWindowInsets ->
                unconsumedInsets.insets = contentWindowInsets.exclude(consumedWindowInsets)
            }
            .consumeWindowInsets(unconsumedInsets),
        unconsumedInsets.asPaddingValues()
    )
}

@ExperimentalLayoutApi
@Composable
fun LazyColumnWithInsets(
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.insetsForLazyLayouts,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    LazyLayoutScaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) { innerModifier, unconsumedPadding ->
        LazyColumn(
            modifier = innerModifier,
            state = state,
            contentPadding = contentPadding + unconsumedPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
    }
}

@ExperimentalLayoutApi
@Composable
fun LazyRowWithInsets(
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.insetsForLazyLayouts,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    LazyLayoutScaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) { innerModifier, unconsumedPadding ->
        LazyRow(
            modifier = innerModifier,
            state = state,
            contentPadding = contentPadding + unconsumedPadding,
            reverseLayout = reverseLayout,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
    }
}

@ExperimentalLayoutApi
@Composable
fun LazyHorizontalGridWithInsets(
    rows: GridCells,
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.insetsForLazyLayouts,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit
) {
    LazyLayoutScaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) { innerModifier, unconsumedPadding ->
        LazyHorizontalGrid(
            rows = rows,
            modifier = innerModifier,
            state = state,
            contentPadding = contentPadding + unconsumedPadding,
            reverseLayout = reverseLayout,
            horizontalArrangement = horizontalArrangement,
            verticalArrangement = verticalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
    }
}

@ExperimentalLayoutApi
@Composable
fun LazyVerticalGridWithInsets(
    columns: GridCells,
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.insetsForLazyLayouts,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit
) {
    LazyLayoutScaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) { innerModifier, unconsumedPadding ->
        LazyVerticalGrid(
            columns = columns,
            modifier = innerModifier,
            state = state,
            contentPadding = contentPadding + unconsumedPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
    }
}

@ExperimentalLayoutApi
@Composable
fun LazyHorizontalStaggeredGridWithInsets(
    rows: StaggeredGridCells,
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.insetsForLazyLayouts,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalItemSpacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit
) {
    LazyLayoutScaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) { innerModifier, unconsumedPadding ->
        LazyHorizontalStaggeredGrid(
            rows = rows,
            modifier = innerModifier,
            state = state,
            contentPadding = contentPadding + unconsumedPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalItemSpacing = horizontalItemSpacing,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
    }
}

@ExperimentalLayoutApi
@Composable
fun LazyVerticalStaggeredGridWithInsets(
    columns: StaggeredGridCells,
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.insetsForLazyLayouts,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit
) {
    LazyLayoutScaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier
    ) { innerModifier, unconsumedPadding ->
        LazyVerticalStaggeredGrid(
            columns = columns,
            modifier = innerModifier,
            state = state,
            contentPadding = contentPadding + unconsumedPadding,
            reverseLayout = reverseLayout,
            verticalItemSpacing = verticalItemSpacing,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showSystemUi = true, apiLevel = 35)
@Composable
private fun LazyColumnWithInsetsPreview() {
    LazyColumnWithInsets(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Magenta)
    ) {
        items(5) {
            Text(
                text = "Item $it",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (it % 2 == 0) Color.LightGray else Color.DarkGray
                    )
                    .padding(16.dp)
                    .background(Color.White)
                    .safeContentPadding()
            )
        }
    }
}
