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
 * @param colors Colors of every part. See [PersianDatePickerDefaults.colors].
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
    onTouch: () -> Unit = {},
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

    val farsi = texts.useFarsiDigits
    val years = remember(yearRange, farsi) { yearRange.map { it.localized(farsi) } }
    val days = remember(monthLength, farsi) { (1..monthLength).map { it.localized(farsi) } }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .observeTouches(onTouch)
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
                        selectedColor = colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        modifier = Modifier.weight(1f),
                    )
                    WheelPicker(
                        items = texts.monthNames,
                        selectedIndex = month - 1,
                        onSelectedIndexChange = { month = it + 1 },
                        selectedColor = colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        modifier = Modifier.weight(1.4f),
                    )
                    WheelPicker(
                        items = years,
                        selectedIndex = year - yearRange.first,
                        onSelectedIndexChange = { year = yearRange.first + it },
                        selectedColor = colors.selectedText,
                        unselectedColor = colors.unselectedText,
                        modifier = Modifier.weight(1f),
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
                    onClick = { onConfirm(JalaliDate(year, month, safeDay)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.confirmContainer,
                        contentColor = colors.confirmContent,
                    ),
                ) { Text(texts.confirm) }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.cancelContent),
                ) { Text(texts.cancel) }
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
 * @param colors Colors of every part. See [PersianDatePickerDefaults.colors].
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
    onTouch: () -> Unit = {},
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
        modifier = modifier.observeTouches(onTouch),
    ) {
        PersianDatePickerContent(
            onConfirm = { date -> hideThen { onConfirm(date); onDismissRequest() } },
            onCancel = { hideThen(onDismissRequest) },
            initialDate = initialDate,
            yearRange = yearRange,
            texts = texts,
            colors = colors,
        )
    }
}

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
