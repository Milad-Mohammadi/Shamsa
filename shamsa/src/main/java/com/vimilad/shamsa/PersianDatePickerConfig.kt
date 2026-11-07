package com.vimilad.shamsa

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

/**
 * All user-visible strings.
 * Build one with [PersianDatePickerDefaults.texts]; the defaults come from the library's
 * `strings.xml` (`shamsa_confirm`, `shamsa_cancel`, `shamsa_month_names`).
 */
@Immutable
data class PersianDatePickerTexts(
    val confirm: String,
    val cancel: String,
    val monthNames: List<String>,
    /** Render years and days with Persian digits instead of Latin digits. */
    val useFarsiDigits: Boolean = true,
) {
    init {
        require(monthNames.size == 12) { "monthNames must have 12 items" }
    }
}

/**
 * Colors of every part of the picker.
 * Build one with [PersianDatePickerDefaults.colors] and override only what you need.
 */
@Immutable
data class PersianDatePickerColors(
    /** Background of the bottom sheet. */
    val sheetContainer: Color,
    /** Dimmed overlay behind the sheet. */
    val scrim: Color,
    /** The small drag handle at the top of the sheet. */
    val dragHandle: Color,
    /** Highlight bar behind the selected (middle) row. */
    val selectedRow: Color,
    /** Text of the selected row in wheels that do not have key focus. */
    val selectedText: Color,
    /**
     * Text of the selected row in the wheel that currently has hardware-key focus.
     * Set it equal to [selectedText] to hide the focus indication.
     */
    val focusedText: Color,
    /** Text of the previous and next rows. */
    val unselectedText: Color,
    val confirmContainer: Color,
    val confirmContent: Color,
    val cancelContainer: Color,
    val cancelContent: Color,
)

/** Text styles of the picker. Build one with [PersianDatePickerDefaults.textStyles]. */
@Immutable
data class PersianDatePickerTextStyles(
    /** Wheel rows that are not selected. */
    val item: TextStyle,
    /** The selected wheel row. */
    val selectedItem: TextStyle,
    /** Confirm and cancel buttons. */
    val button: TextStyle,
)

object PersianDatePickerDefaults {

    /**
     * Texts. Defaults come from string resources, so you can also change them for the whole app
     * by defining the same resource names in your own `strings.xml`:
     * `shamsa_confirm`, `shamsa_cancel` and the 12-item array `shamsa_month_names`.
     */
    @Composable
    fun texts(
        confirm: String = stringResource(R.string.shamsa_confirm),
        cancel: String = stringResource(R.string.shamsa_cancel),
        monthNames: List<String> = stringArrayResource(R.array.shamsa_month_names).toList(),
        useFarsiDigits: Boolean = true,
    ) = PersianDatePickerTexts(
        confirm = confirm,
        cancel = cancel,
        monthNames = monthNames,
        useFarsiDigits = useFarsiDigits,
    )

    @Composable
    fun colors(
        sheetContainer: Color = MaterialTheme.colorScheme.surface,
        scrim: Color = Color.Black.copy(alpha = 0.32f),
        dragHandle: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        selectedRow: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        selectedText: Color = MaterialTheme.colorScheme.onSurface,
        focusedText: Color = MaterialTheme.colorScheme.primary,
        unselectedText: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        confirmContainer: Color = MaterialTheme.colorScheme.primary,
        confirmContent: Color = MaterialTheme.colorScheme.onPrimary,
        cancelContainer: Color = Color.Transparent,
        cancelContent: Color = MaterialTheme.colorScheme.primary,
    ) = PersianDatePickerColors(
        sheetContainer = sheetContainer,
        scrim = scrim,
        dragHandle = dragHandle,
        selectedRow = selectedRow,
        selectedText = selectedText,
        focusedText = focusedText,
        unselectedText = unselectedText,
        confirmContainer = confirmContainer,
        confirmContent = confirmContent,
        cancelContainer = cancelContainer,
        cancelContent = cancelContent,
    )

    /**
     * Text styles. Pass [fontFamily] to change the font of the whole picker in one line.
     * When [fontFamily] is null, styles come from `MaterialTheme.typography`, so an app-wide
     * font set in the theme is picked up automatically.
     */
    @Composable
    fun textStyles(
        fontFamily: FontFamily? = null,
        item: TextStyle = MaterialTheme.typography.titleMedium,
        selectedItem: TextStyle = item,
        button: TextStyle = MaterialTheme.typography.labelLarge,
    ): PersianDatePickerTextStyles {
        fun TextStyle.withFont() = if (fontFamily != null) copy(fontFamily = fontFamily) else this
        return PersianDatePickerTextStyles(
            item = item.withFont(),
            selectedItem = selectedItem.withFont(),
            button = button.withFont(),
        )
    }
}

internal fun Int.localized(farsi: Boolean): String {
    val s = toString()
    if (!farsi) return s
    return buildString(s.length) {
        s.forEach { append(if (it in '0'..'9') '۰' + (it - '0') else it) }
    }
}
