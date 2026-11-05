package com.vimilad.shamsa

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter

internal val WheelItemHeight = 44.dp

/**
 * A 3-row wheel (previous, current, next). The middle row is the selected one.
 * Spacer items at the top and bottom let the first and last items reach the middle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    selectedColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier,
) {
    val itemHeightPx = with(LocalDensity.current) { WheelItemHeight.toPx() }
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = state)

    val lastIndex by rememberUpdatedState(items.lastIndex)
    val currentOnChange by rememberUpdatedState(onSelectedIndexChange)

    val centered by remember {
        derivedStateOf {
            val extra = if (state.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
            (state.firstVisibleItemIndex + extra).coerceIn(0, lastIndex)
        }
    }

    // When scrolling stops, report the selected value to the caller.
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .filter { !it }
            .collect { currentOnChange(centered) }
    }

    // Apply changes that come from outside (e.g. day clamping).
    LaunchedEffect(selectedIndex, items.size) {
        if (!state.isScrollInProgress && centered != selectedIndex) {
            state.animateScrollToItem(selectedIndex)
        }
    }

    LazyColumn(
        state = state,
        flingBehavior = fling,
        modifier = modifier.height(WheelItemHeight * 3),
    ) {
        item { Spacer(Modifier.height(WheelItemHeight)) }
        itemsIndexed(items) { index, text ->
            val isSelected = index == centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) selectedColor else unselectedColor,
                )
            }
        }
        item { Spacer(Modifier.height(WheelItemHeight)) }
    }
}
