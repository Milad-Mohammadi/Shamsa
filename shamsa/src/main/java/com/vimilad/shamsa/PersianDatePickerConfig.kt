package com.vimilad.shamsa

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** All user-visible strings. Every field can be overridden. */
@Immutable
data class PersianDatePickerTexts(
    val confirm: String = "تایید",
    val cancel: String = "انصراف",
    val monthNames: List<String> = DefaultMonthNames,
    /** Render years and days with Persian digits instead of Latin digits. */
    val useFarsiDigits: Boolean = true,
) {
    init {
        require(monthNames.size == 12) { "monthNames must have 12 items" }
    }

    companion object {
        val DefaultMonthNames = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
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
    val cancelContent: Color,
)

object PersianDatePickerDefaults {

    @Composable
    fun colors(
        sheetContainer: Color = MaterialTheme.colorScheme.surface,
        selectedRow: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        selectedText: Color = MaterialTheme.colorScheme.onSurface,
        focusedText: Color = MaterialTheme.colorScheme.primary,
        unselectedText: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        confirmContainer: Color = MaterialTheme.colorScheme.primary,
        confirmContent: Color = MaterialTheme.colorScheme.onPrimary,
        cancelContent: Color = MaterialTheme.colorScheme.primary,
    ) = PersianDatePickerColors(
        sheetContainer = sheetContainer,
        selectedRow = selectedRow,
        selectedText = selectedText,
        focusedText = focusedText,
        unselectedText = unselectedText,
        confirmContainer = confirmContainer,
        confirmContent = confirmContent,
        cancelContent = cancelContent,
    )
}

internal fun Int.localized(farsi: Boolean): String {
    val s = toString()
    if (!farsi) return s
    return buildString(s.length) {
        s.forEach { append(if (it in '0'..'9') '۰' + (it - '0') else it) }
    }
}
