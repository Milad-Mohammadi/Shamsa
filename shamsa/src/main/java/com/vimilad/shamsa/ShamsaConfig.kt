package com.vimilad.shamsa

import android.content.res.Resources
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import java.util.Locale

/**
 * Language of the library's own texts (buttons, month names) and of the layout direction.
 * It is independent of the device language and of the calendar type.
 */
enum class ShamsaLanguage(internal val tag: String) {
    Persian("fa"),
    English("en"),
}

/**
 * All user-visible strings.
 * Build one with [ShamsaDefaults.texts]; the defaults come from the library's string resources
 * for the chosen language and calendar.
 */
@Immutable
data class ShamsaTexts(
    val confirm: String,
    val cancel: String,
    /** Primary button of the first step of the date-time picker. */
    val next: String,
    /** Secondary button of the second step of the date-time picker. */
    val back: String,
    val monthNames: List<String>,
    /** Render numbers with Persian digits instead of Latin digits. */
    val usePersianDigits: Boolean,
) {
    init {
        require(monthNames.size == 12) { "monthNames must have 12 items" }
    }
}

/**
 * Colors of every part of the picker.
 * Build one with [ShamsaDefaults.colors] and override only what you need.
 */
@Immutable
data class ShamsaColors(
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
     * Text of the selected row in the wheel that currently has key focus.
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

/** Text styles of the picker. Build one with [ShamsaDefaults.textStyles]. */
@Immutable
data class ShamsaTextStyles(
    /** Wheel rows that are not selected. */
    val item: TextStyle,
    /** The selected wheel row. */
    val selectedItem: TextStyle,
    /** Confirm and cancel buttons. */
    val button: TextStyle,
)

object ShamsaDefaults {

    /** Years offered by default: 1300..1500 for Jalali, 1920..2120 for Gregorian. */
    fun yearRange(calendar: ShamsaCalendar): IntRange = when (calendar) {
        ShamsaCalendar.Jalali -> 1300..1500
        ShamsaCalendar.Gregorian -> 1920..2120
    }

    /**
     * Texts for [language] and [calendar], read from string resources. The two are independent:
     * for example English + Jalali gives "Farvardin", "Ordibehesht", ...; Persian + Gregorian gives
     * Persian names of the Gregorian months.
     *
     * Every value can be overridden here, or app-wide by defining the same resource names
     * (`shamsa_confirm`, `shamsa_cancel`, `shamsa_next`, `shamsa_back`, `shamsa_months_jalali`,
     * `shamsa_months_gregorian`) in your own `strings.xml`.
     */
    @Composable
    fun texts(
        language: ShamsaLanguage = ShamsaLanguage.Persian,
        calendar: ShamsaCalendar = ShamsaCalendar.Jalali,
        confirm: String? = null,
        cancel: String? = null,
        next: String? = null,
        back: String? = null,
        monthNames: List<String>? = null,
        usePersianDigits: Boolean? = null,
    ): ShamsaTexts {
        val res = rememberLocalizedResources(language)
        val monthsRes = when (calendar) {
            ShamsaCalendar.Jalali -> R.array.shamsa_months_jalali
            ShamsaCalendar.Gregorian -> R.array.shamsa_months_gregorian
        }
        return ShamsaTexts(
            confirm = confirm ?: res.getString(R.string.shamsa_confirm),
            cancel = cancel ?: res.getString(R.string.shamsa_cancel),
            next = next ?: res.getString(R.string.shamsa_next),
            back = back ?: res.getString(R.string.shamsa_back),
            monthNames = monthNames ?: res.getStringArray(monthsRes).toList(),
            usePersianDigits = usePersianDigits ?: (language == ShamsaLanguage.Persian),
        )
    }

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
    ) = ShamsaColors(
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
    ): ShamsaTextStyles {
        fun TextStyle.withFont() = if (fontFamily != null) copy(fontFamily = fontFamily) else this
        return ShamsaTextStyles(
            item = item.withFont(),
            selectedItem = selectedItem.withFont(),
            button = button.withFont(),
        )
    }
}

/** Resources forced to [language], regardless of the device language. */
@Composable
private fun rememberLocalizedResources(language: ShamsaLanguage): Resources {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration, language) {
        val forced = android.content.res.Configuration(configuration)
        forced.setLocale(Locale(language.tag))
        context.createConfigurationContext(forced).resources
    }
}

/** Converts to a string with at least [minDigits] digits, in Persian or Latin digits. */
internal fun Int.localized(persian: Boolean, minDigits: Int = 1): String {
    val s = toString().padStart(minDigits, '0')
    if (!persian) return s
    return buildString(s.length) {
        s.forEach { append(if (it in '0'..'9') '۰' + (it - '0') else it) }
    }
}
