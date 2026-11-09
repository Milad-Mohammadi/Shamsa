package com.vimilad.shamsa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** One wheel of a picker. */
internal class WheelColumn(
    val items: List<String>,
    /** Reads the selected index from snapshot state, so callers always see the latest value. */
    val index: () -> Int,
    val onSelect: (Int) -> Unit,
    val weight: Float = 1f,
)

internal fun languageDirection(language: ShamsaLanguage): LayoutDirection =
    if (language == ShamsaLanguage.Persian) LayoutDirection.Rtl else LayoutDirection.Ltr

/** The wheels with the highlight bar behind the middle row. */
@Composable
private fun WheelsRow(
    stepKey: Any,
    columns: List<WheelColumn>,
    focused: Int,
    onFocus: (Int) -> Unit,
    colors: ShamsaColors,
    textStyles: ShamsaTextStyles,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WheelItemHeight * 3),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(WheelItemHeight)
                .background(colors.selectedRow, RoundedCornerShape(12.dp))
        )
        Row(Modifier.fillMaxSize()) {
            columns.forEachIndexed { i, column ->
                key(stepKey, i) {
                    WheelPicker(
                        items = column.items,
                        selectedIndex = column.index(),
                        onSelectedIndexChange = column.onSelect,
                        selectedColor = if (focused == i) colors.focusedText else colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        textStyle = textStyles.item,
                        selectedTextStyle = textStyles.selectedItem,
                        modifier = Modifier
                            .weight(column.weight)
                            .observeTouches { onFocus(i) },
                    )
                }
            }
        }
    }
}

/** Primary button (always on the visual right) and secondary button next to it. */
@Composable
private fun ActionButtons(
    primaryText: String,
    secondaryText: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    colors: ShamsaColors,
    textStyles: ShamsaTextStyles,
) {
    val primary: @Composable RowScope.() -> Unit = {
        Button(
            onClick = onPrimary,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.confirmContainer,
                contentColor = colors.confirmContent,
            ),
        ) { Text(primaryText, style = textStyles.button) }
    }
    val secondary: @Composable RowScope.() -> Unit = {
        TextButton(
            onClick = onSecondary,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = colors.cancelContainer,
                contentColor = colors.cancelContent,
            ),
        ) { Text(secondaryText, style = textStyles.button) }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The first child is on the right in RTL and on the left in LTR.
        if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
            primary(); secondary()
        } else {
            secondary(); primary()
        }
    }
}

/**
 * Everything inside a picker: wheels, buttons, touch-based focus and hardware keys.
 *
 * @param wheelsDirection Layout direction of the wheels only; defaults to the language direction.
 *   Time wheels use [LayoutDirection.Ltr] so the hour is always on the left.
 * @param stepKey Changes when the wheels are replaced by another set (date-time steps), so
 *   wheel scroll state is not reused between them.
 * @param onCancel Called by the cancel key. It cancels the whole picker, whereas [onSecondary]
 *   is whatever the secondary button does (cancel, or "back" in the second step of date-time).
 */
@Composable
internal fun PickerScaffold(
    language: ShamsaLanguage,
    columns: List<WheelColumn>,
    focused: Int,
    onFocusChange: (Int) -> Unit,
    primaryText: String,
    secondaryText: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    onCancel: () -> Unit,
    colors: ShamsaColors,
    textStyles: ShamsaTextStyles,
    keys: ShamsaKeys,
    focusKey: Key?,
    onTouch: () -> Unit,
    modifier: Modifier = Modifier,
    wheelsDirection: LayoutDirection? = null,
    stepKey: Any = Unit,
) {
    val direction = languageDirection(language)
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Column(
            modifier = modifier
                .observeTouches(onTouch)
                .pickerKeys(
                    keys = keys,
                    focusKey = focusKey,
                    onStep = { delta ->
                        val column = columns[focused.coerceIn(0, columns.lastIndex)]
                        column.onSelect((column.index() + delta).coerceIn(0, column.items.lastIndex))
                    },
                    onFocusNext = { onFocusChange((focused + 1) % columns.size) },
                    onConfirm = onPrimary,
                    onCancel = onCancel,
                )
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides (wheelsDirection ?: direction)) {
                WheelsRow(stepKey, columns, focused, onFocusChange, colors, textStyles)
            }
            Spacer(Modifier.height(16.dp))
            ActionButtons(primaryText, secondaryText, onPrimary, onSecondary, colors, textStyles)
        }
    }
}

/**
 * Modal bottom sheet shared by all pickers.
 *
 * The sheet state is created here and never exposed, so no experimental Material 3 type appears in
 * the library's public API and callers do not need `@OptIn(ExperimentalMaterial3Api::class)`.
 *
 * [content] receives `hideThen`, which closes the sheet with its animation and then runs an action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShamsaSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    colors: ShamsaColors,
    onTouch: () -> Unit,
    content: @Composable (hideThen: (() -> Unit) -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hideThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.sheetContainer,
        scrimColor = colors.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .observeTouches(onTouch)
        ) {
            content(hideThen)
        }
    }
}

/** Observes finger-down events without consuming them (Initial pass, so children work as usual). */
@Composable
internal fun Modifier.observeTouches(onTouch: () -> Unit): Modifier {
    val current by rememberUpdatedState(onTouch)
    return pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                event.changes.forEach { change ->
                    if (change.pressed && !change.previousPressed) {
                        current()
                    }
                }
            }
        }
    }
}