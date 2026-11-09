package com.vimilad.shamsa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.LayoutDirection

// =====================================================================================
// Date picker
// =====================================================================================

/**
 * The date picker content without the sheet. Use it inside your own dialog or screen if needed.
 *
 * @param language Language of the buttons, month names and layout direction.
 * @param calendar Calendar type of the wheels and of the returned date. Independent of [language].
 * @param initialDate Date selected at the start. It is converted to [calendar] if needed.
 * @param yearRange Years offered by the year wheel. Defaults depend on [calendar].
 * @param onTouch Called on every finger-down anywhere inside the content. The event is not
 *   consumed, so wheels and buttons behave as usual.
 * @param keys Hardware key mapping. Defaults: down/right = +1, up/left = -1, OK = confirm,
 *   X/Escape/Back = cancel. Key focus starts on the first column (day) and moves when you touch
 *   another wheel or press [focusKey].
 * @param focusKey Optional hardware key that moves key focus to the next column
 *   (day, month, year, day, ...). Touching a wheel always works as well, with or without it.
 */
@Composable
fun ShamsaDatePickerContent(
    onConfirm: (ShamsaDate) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    language: ShamsaLanguage = ShamsaLanguage.Persian,
    calendar: ShamsaCalendar = ShamsaCalendar.Jalali,
    initialDate: ShamsaDate = ShamsaDate.today(calendar),
    yearRange: IntRange = ShamsaDefaults.yearRange(calendar),
    texts: ShamsaTexts = ShamsaDefaults.texts(language, calendar),
    colors: ShamsaColors = ShamsaDefaults.colors(),
    textStyles: ShamsaTextStyles = ShamsaDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: ShamsaKeys = ShamsaKeys(),
    focusKey: Key? = null,
) {
    val fields = rememberDateFields(initialDate, calendar, yearRange)
    val columns = rememberDateColumns(fields, yearRange, texts)
    var focused by remember { mutableIntStateOf(0) }

    PickerScaffold(
        language = language,
        columns = columns,
        focused = focused,
        onFocusChange = { focused = it },
        primaryText = texts.confirm,
        secondaryText = texts.cancel,
        onPrimary = { onConfirm(fields.toDate()) },
        onSecondary = onCancel,
        onCancel = onCancel,
        colors = colors,
        textStyles = textStyles,
        keys = keys,
        focusKey = focusKey,
        onTouch = onTouch,
        modifier = modifier,
    )
}

/**
 * Date picker as a modal bottom sheet.
 *
 * @param onDismissRequest Called after the sheet has closed (cancel, confirm, swipe down,
 *   tap outside, Back). Set your "show" state to false here.
 * @param onConfirm Called with the selected date, expressed in [calendar].
 * @param onTouch Called on every finger-down anywhere on the sheet, including the drag handle.
 *   Handy for resetting an inactivity timer.
 * @param colors Colors of every part. See [ShamsaDefaults.colors].
 * @param textStyles Fonts and text styles. See [ShamsaDefaults.textStyles].
 * See [ShamsaDatePickerContent] for the other parameters.
 */
@Composable
fun ShamsaDatePickerBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirm: (ShamsaDate) -> Unit,
    modifier: Modifier = Modifier,
    language: ShamsaLanguage = ShamsaLanguage.Persian,
    calendar: ShamsaCalendar = ShamsaCalendar.Jalali,
    initialDate: ShamsaDate = ShamsaDate.today(calendar),
    yearRange: IntRange = ShamsaDefaults.yearRange(calendar),
    texts: ShamsaTexts = ShamsaDefaults.texts(language, calendar),
    colors: ShamsaColors = ShamsaDefaults.colors(),
    textStyles: ShamsaTextStyles = ShamsaDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: ShamsaKeys = ShamsaKeys(),
    focusKey: Key? = null,
) {
    ShamsaSheet(onDismissRequest, modifier, colors, onTouch) { hideThen ->
        ShamsaDatePickerContent(
            onConfirm = { date -> hideThen { onConfirm(date); onDismissRequest() } },
            onCancel = { hideThen(onDismissRequest) },
            language = language,
            calendar = calendar,
            initialDate = initialDate,
            yearRange = yearRange,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            keys = keys,
            focusKey = focusKey,
        )
    }
}

// =====================================================================================
// Time picker
// =====================================================================================

/**
 * The time picker content without the sheet.
 *
 * Wheels are hour | minute (| second), always left to right, 24-hour clock.
 *
 * @param showSeconds Adds a seconds wheel.
 * @param minuteStep Minutes offered every N minutes (1..30). An initial minute that is not a
 *   multiple of the step is rounded down.
 * @param focusKey Optional hardware key that moves key focus to the next wheel.
 * See [ShamsaDatePickerContent] for the other parameters.
 */
@Composable
fun ShamsaTimePickerContent(
    onConfirm: (ShamsaTime) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    language: ShamsaLanguage = ShamsaLanguage.Persian,
    initialTime: ShamsaTime = ShamsaTime.now(),
    showSeconds: Boolean = false,
    minuteStep: Int = 1,
    texts: ShamsaTexts = ShamsaDefaults.texts(language),
    colors: ShamsaColors = ShamsaDefaults.colors(),
    textStyles: ShamsaTextStyles = ShamsaDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: ShamsaKeys = ShamsaKeys(),
    focusKey: Key? = null,
) {
    val fields = rememberTimeFields(initialTime)
    val columns = rememberTimeColumns(fields, showSeconds, minuteStep, texts.usePersianDigits)
    var focused by remember { mutableIntStateOf(0) }

    PickerScaffold(
        language = language,
        columns = columns,
        focused = focused,
        onFocusChange = { focused = it },
        primaryText = texts.confirm,
        secondaryText = texts.cancel,
        onPrimary = { onConfirm(fields.toTime(showSeconds, minuteStep)) },
        onSecondary = onCancel,
        onCancel = onCancel,
        colors = colors,
        textStyles = textStyles,
        keys = keys,
        focusKey = focusKey,
        onTouch = onTouch,
        modifier = modifier,
        wheelsDirection = LayoutDirection.Ltr,
    )
}

/**
 * Time picker as a modal bottom sheet. See [ShamsaTimePickerContent] and
 * [ShamsaDatePickerBottomSheet] for the parameters.
 */
@Composable
fun ShamsaTimePickerBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirm: (ShamsaTime) -> Unit,
    modifier: Modifier = Modifier,
    language: ShamsaLanguage = ShamsaLanguage.Persian,
    initialTime: ShamsaTime = ShamsaTime.now(),
    showSeconds: Boolean = false,
    minuteStep: Int = 1,
    texts: ShamsaTexts = ShamsaDefaults.texts(language),
    colors: ShamsaColors = ShamsaDefaults.colors(),
    textStyles: ShamsaTextStyles = ShamsaDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: ShamsaKeys = ShamsaKeys(),
    focusKey: Key? = null,
) {
    ShamsaSheet(onDismissRequest, modifier, colors, onTouch) { hideThen ->
        ShamsaTimePickerContent(
            onConfirm = { time -> hideThen { onConfirm(time); onDismissRequest() } },
            onCancel = { hideThen(onDismissRequest) },
            language = language,
            initialTime = initialTime,
            showSeconds = showSeconds,
            minuteStep = minuteStep,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            keys = keys,
            focusKey = focusKey,
        )
    }
}

// =====================================================================================
// Date-time picker
// =====================================================================================

/**
 * The date-time picker content without the sheet. Two steps in order: first the date, then the
 * time. The first step's primary button is "Next"; the second step's secondary button is "Back".
 * The cancel key cancels the whole picker from either step.
 *
 * @param initialDateTime Selected at the start. Its date is converted to [calendar] if needed.
 * See [ShamsaDatePickerContent] and [ShamsaTimePickerContent] for the other parameters.
 */
@Composable
fun ShamsaDateTimePickerContent(
    onConfirm: (ShamsaDateTime) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    language: ShamsaLanguage = ShamsaLanguage.Persian,
    calendar: ShamsaCalendar = ShamsaCalendar.Jalali,
    initialDateTime: ShamsaDateTime = ShamsaDateTime.now(calendar),
    yearRange: IntRange = ShamsaDefaults.yearRange(calendar),
    showSeconds: Boolean = false,
    minuteStep: Int = 1,
    texts: ShamsaTexts = ShamsaDefaults.texts(language, calendar),
    colors: ShamsaColors = ShamsaDefaults.colors(),
    textStyles: ShamsaTextStyles = ShamsaDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: ShamsaKeys = ShamsaKeys(),
    focusKey: Key? = null,
) {
    val dateFields = rememberDateFields(initialDateTime.date, calendar, yearRange)
    val timeFields = rememberTimeFields(initialDateTime.time)
    val dateColumns = rememberDateColumns(dateFields, yearRange, texts)
    val timeColumns = rememberTimeColumns(timeFields, showSeconds, minuteStep, texts.usePersianDigits)

    var step by rememberSaveable { mutableIntStateOf(0) }       // 0 = date, 1 = time
    var focused by remember { mutableIntStateOf(0) }

    val onDateStep = step == 0
    PickerScaffold(
        language = language,
        columns = if (onDateStep) dateColumns else timeColumns,
        focused = focused,
        onFocusChange = { focused = it },
        primaryText = if (onDateStep) texts.next else texts.confirm,
        secondaryText = if (onDateStep) texts.cancel else texts.back,
        onPrimary = {
            if (onDateStep) {
                step = 1
                focused = 0
            } else {
                onConfirm(ShamsaDateTime(dateFields.toDate(), timeFields.toTime(showSeconds, minuteStep)))
            }
        },
        onSecondary = {
            if (onDateStep) {
                onCancel()
            } else {
                step = 0
                focused = 0
            }
        },
        onCancel = onCancel,
        colors = colors,
        textStyles = textStyles,
        keys = keys,
        focusKey = focusKey,
        onTouch = onTouch,
        modifier = modifier,
        wheelsDirection = if (onDateStep) null else LayoutDirection.Ltr,
        stepKey = step,
    )
}

/**
 * Date-time picker as a modal bottom sheet: date first, then time.
 * [onConfirm] receives both, with the date expressed in [calendar].
 * See [ShamsaDateTimePickerContent] and [ShamsaDatePickerBottomSheet] for the parameters.
 */
@Composable
fun ShamsaDateTimePickerBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirm: (ShamsaDateTime) -> Unit,
    modifier: Modifier = Modifier,
    language: ShamsaLanguage = ShamsaLanguage.Persian,
    calendar: ShamsaCalendar = ShamsaCalendar.Jalali,
    initialDateTime: ShamsaDateTime = ShamsaDateTime.now(calendar),
    yearRange: IntRange = ShamsaDefaults.yearRange(calendar),
    showSeconds: Boolean = false,
    minuteStep: Int = 1,
    texts: ShamsaTexts = ShamsaDefaults.texts(language, calendar),
    colors: ShamsaColors = ShamsaDefaults.colors(),
    textStyles: ShamsaTextStyles = ShamsaDefaults.textStyles(),
    onTouch: () -> Unit = {},
    keys: ShamsaKeys = ShamsaKeys(),
    focusKey: Key? = null,
) {
    ShamsaSheet(onDismissRequest, modifier, colors, onTouch) { hideThen ->
        ShamsaDateTimePickerContent(
            onConfirm = { value -> hideThen { onConfirm(value); onDismissRequest() } },
            onCancel = { hideThen(onDismissRequest) },
            language = language,
            calendar = calendar,
            initialDateTime = initialDateTime,
            yearRange = yearRange,
            showSeconds = showSeconds,
            minuteStep = minuteStep,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            keys = keys,
            focusKey = focusKey,
        )
    }
}