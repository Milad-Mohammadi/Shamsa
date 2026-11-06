package com.vimilad.shamsa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * The picker content without the sheet. Use it inside your own dialog or screen if needed.
 *
 * @param onTouch Called on every finger-down anywhere inside the content. The event is not
 *   consumed, so wheels and buttons behave as usual.
 * @param keys Hardware key mapping. Defaults: up/right = +1, down/left = -1, OK = confirm,
 *   X/Escape/Back = cancel. Key focus starts on the day column; touching the month or year
 *   wheel moves key focus to that column.
 * @param colors Colors of every part. See [PersianDatePickerDefaults.colors].
 * @param textStyles Fonts and text styles. See [PersianDatePickerDefaults.textStyles].
 */
@Composable
fun PersianDatePickerContent(
    onConfirm: (JalaliDate) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    initialDate: JalaliDate = JalaliDate.today(),
    yearRange: IntRange = 1300..1500,
    texts: PersianDatePickerTexts = PersianDatePickerTexts(),
    colors: PersianDatePickerColors = PersianDatePickerDefaults.colors(),
    textStyles: PersianDatePickerTextStyles = PersianDatePickerDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: PersianDatePickerKeys = PersianDatePickerKeys(),
) {
    require(!yearRange.isEmpty()) { "yearRange must not be empty" }

    var year by rememberSaveable { mutableIntStateOf(initialDate.year.coerceIn(yearRange)) }
    var month by rememberSaveable { mutableIntStateOf(initialDate.month) }
    var day by rememberSaveable { mutableIntStateOf(initialDate.day) }

    val monthLength = JalaliCalendar.monthLength(year, month)
    val safeDay = day.coerceAtMost(monthLength)

    // Clamp the day when the month or year changes,
    // e.g. 31 Mehr -> Aban becomes 30, and 30 Esfand of a leap year -> a normal year becomes 29.
    LaunchedEffect(monthLength) { day = day.coerceAtMost(monthLength) }

    var focused by remember { mutableStateOf(PickerField.Day) }

    // Always reads the latest state, so rapid key events and the repeat timer never use stale values.
    fun step(delta: Int) {
        val length = JalaliCalendar.monthLength(year, month)
        when (focused) {
            PickerField.Day -> day = (day.coerceAtMost(length) + delta).coerceIn(1, length)
            PickerField.Month -> month = (month + delta).coerceIn(1, 12)
            PickerField.Year -> year = (year + delta).coerceIn(yearRange)
        }
    }

    fun currentDate() = JalaliDate(
        year, month, day.coerceAtMost(JalaliCalendar.monthLength(year, month))
    )

    val farsi = texts.useFarsiDigits
    val years = remember(yearRange, farsi) { yearRange.map { it.localized(farsi) } }
    val days = remember(monthLength, farsi) { (1..monthLength).map { it.localized(farsi) } }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .observeTouches(onTouch)
                .pickerKeys(
                    keys = keys,
                    onStep = { step(it) },
                    onConfirm = { onConfirm(currentDate()) },
                    onCancel = onCancel,
                )
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight * 3),
                contentAlignment = Alignment.Center,
            ) {
                // Highlight bar behind the middle row.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight)
                        .background(colors.selectedRow, RoundedCornerShape(12.dp))
                )
                // RTL layout: the first child is on the right, so the order is day | month | year.
                Row(Modifier.fillMaxSize()) {
                    WheelPicker(
                        items = days,
                        selectedIndex = safeDay - 1,
                        onSelectedIndexChange = { day = it + 1 },
                        selectedColor = if (focused == PickerField.Day) colors.focusedText else colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        textStyle = textStyles.item,
                        selectedTextStyle = textStyles.selectedItem,
                        modifier = Modifier
                            .weight(1f)
                            .observeTouches { focused = PickerField.Day },
                    )
                    WheelPicker(
                        items = texts.monthNames,
                        selectedIndex = month - 1,
                        onSelectedIndexChange = { month = it + 1 },
                        selectedColor = if (focused == PickerField.Month) colors.focusedText else colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        textStyle = textStyles.item,
                        selectedTextStyle = textStyles.selectedItem,
                        modifier = Modifier
                            .weight(1.4f)
                            .observeTouches { focused = PickerField.Month },
                    )
                    WheelPicker(
                        items = years,
                        selectedIndex = year - yearRange.first,
                        onSelectedIndexChange = { year = yearRange.first + it },
                        selectedColor = if (focused == PickerField.Year) colors.focusedText else colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        textStyle = textStyles.item,
                        selectedTextStyle = textStyles.selectedItem,
                        modifier = Modifier
                            .weight(1f)
                            .observeTouches { focused = PickerField.Year },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onConfirm(currentDate()) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.confirmContainer,
                        contentColor = colors.confirmContent,
                    ),
                ) { Text(texts.confirm, style = textStyles.button) }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colors.cancelContainer,
                        contentColor = colors.cancelContent,
                    ),
                ) { Text(texts.cancel, style = textStyles.button) }
            }
        }
    }
}

/**
 * Jalali date picker as a modal bottom sheet.
 *
 * @param onDismissRequest Called after the sheet has closed (cancel, confirm, swipe down,
 *   tap outside, Back). Set your "show" state to false here.
 * @param onConfirm Called with the selected date when the user confirms.
 * @param onTouch Called on every finger-down anywhere on the sheet, including the drag handle.
 *   Handy for resetting an inactivity timer.
 * @param keys Hardware key mapping. Use [PersianDatePickerKeys.None] to turn it off.
 * @param colors Colors of every part. See [PersianDatePickerDefaults.colors].
 * @param textStyles Fonts and text styles. See [PersianDatePickerDefaults.textStyles].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianDatePickerBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirm: (JalaliDate) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: JalaliDate = JalaliDate.today(),
    yearRange: IntRange = 1300..1500,
    texts: PersianDatePickerTexts = PersianDatePickerTexts(),
    colors: PersianDatePickerColors = PersianDatePickerDefaults.colors(),
    textStyles: PersianDatePickerTextStyles = PersianDatePickerDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: PersianDatePickerKeys = PersianDatePickerKeys(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()

    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.sheetContainer,
        scrimColor = colors.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        modifier = modifier.observeTouches(onTouch),
    ) {
        PersianDatePickerContent(
            onConfirm = { date -> hideThen { onConfirm(date); onDismissRequest() } },
            onCancel = { hideThen(onDismissRequest) },
            initialDate = initialDate,
            yearRange = yearRange,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            keys = keys,
        )
    }
}

private enum class PickerField { Day, Month, Year }

/** Observes finger-down events without consuming them (Initial pass, so children work as usual). */
@Composable
private fun Modifier.observeTouches(onTouch: () -> Unit): Modifier {
    val current by rememberUpdatedState(onTouch)
    return pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.any { it.pressed && !it.previousPressed }) current()
            }
        }
    }
}
