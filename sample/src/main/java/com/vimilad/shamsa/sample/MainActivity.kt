package com.vimilad.shamsa.sample

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vimilad.shamsa.ShamsaCalendar
import com.vimilad.shamsa.ShamsaDate
import com.vimilad.shamsa.ShamsaDatePickerBottomSheet
import com.vimilad.shamsa.ShamsaDateTime
import com.vimilad.shamsa.ShamsaDateTimePickerBottomSheet
import com.vimilad.shamsa.ShamsaDefaults
import com.vimilad.shamsa.ShamsaLanguage
import com.vimilad.shamsa.ShamsaTimePickerBottomSheet
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { Sample() }
            }
        }
    }
}

@Composable
private fun Sample() {
    // Last picked values, shown as text.
    var dateResult by rememberSaveable { mutableStateOf<String?>(null) }
    var timeResult by rememberSaveable { mutableStateOf<String?>(null) }
    var dateTimeResult by rememberSaveable { mutableStateOf<String?>(null) }
    var touchCount by remember { mutableIntStateOf(0) }

    // Settings. Language and calendar are two independent choices.
    var language by rememberSaveable { mutableStateOf(ShamsaLanguage.Persian) }
    var calendar by rememberSaveable { mutableStateOf(ShamsaCalendar.Jalali) }
    var showSeconds by rememberSaveable { mutableStateOf(false) }
    var minuteStepFive by rememberSaveable { mutableStateOf(false) }
    var customTheme by rememberSaveable { mutableStateOf(false) }

    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showDateTime by remember { mutableStateOf(false) }
    var showLeap by remember { mutableStateOf(false) }

    val english = language == ShamsaLanguage.English
    val minuteStep = if (minuteStepFive) 5 else 1

    // Every text on this page follows the language setting, not the device language.
    val resources = rememberLocalizedResources(english)
    fun str(@StringRes id: Int, vararg args: Any): String = resources.getString(id, *args)

    // Texts, colors and fonts: library defaults, or the custom theme.
    val customConfirm = str(if (english) R.string.custom_confirm_en else R.string.custom_confirm_fa)
    val customCancel = str(if (english) R.string.custom_cancel_en else R.string.custom_cancel_fa)
    val texts = ShamsaDefaults.texts(
        language = language,
        calendar = calendar,
        confirm = if (customTheme) customConfirm else null,
        cancel = if (customTheme) customCancel else null,
        usePersianDigits = if (customTheme) false else null,
    )
    val colors = if (customTheme) {
        ShamsaDefaults.colors(
            sheetContainer = Color(0xFF1B2B34),
            selectedRow = Color(0xFF2C4452),
            selectedText = Color.White,
            focusedText = Color(0xFFFFC857),
            unselectedText = Color.White.copy(alpha = 0.35f),
            dragHandle = Color.White.copy(alpha = 0.5f),
            confirmContainer = Color(0xFFFFC857),
            confirmContent = Color.Black,
            cancelContent = Color.White,
        )
    } else {
        ShamsaDefaults.colors()
    }
    val textStyles = if (customTheme) {
        // One line changes the font of the whole picker. Use your own FontFamily here,
        // e.g. FontFamily(Font(R.font.vazirmatn)).
        ShamsaDefaults.textStyles(
            fontFamily = FontFamily.Monospace,
            selectedItem = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
    } else {
        ShamsaDefaults.textStyles()
    }

    // Optional hardware key that moves key focus between the wheels (touch always works too).
    val focusKey = Key.Menu

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()) // only a fallback on very small screens
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        // Results: one small centered block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            CenteredText(str(R.string.result_date, dateResult ?: "—"))
            CenteredText(str(R.string.result_time, timeResult ?: "—"))
            CenteredText(str(R.string.result_datetime, dateTimeResult ?: "—"))
            CenteredText(
                str(R.string.touch_count, touchCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Settings: language and calendar share one row, the three toggles share another
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Segmented(
                description = str(R.string.setting_language),
                options = listOf(
                    ShamsaLanguage.Persian to str(R.string.language_persian),
                    ShamsaLanguage.English to str(R.string.language_english),
                ),
                selected = language,
                onSelect = { language = it },
                modifier = Modifier.weight(1f),
            )
            Segmented(
                description = str(R.string.setting_calendar),
                options = listOf(
                    ShamsaCalendar.Jalali to str(R.string.calendar_jalali),
                    ShamsaCalendar.Gregorian to str(R.string.calendar_gregorian),
                ),
                selected = calendar,
                onSelect = { calendar = it },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ToggleChip(
                text = str(R.string.chip_seconds),
                checked = showSeconds,
                onCheckedChange = { showSeconds = it },
                modifier = Modifier.weight(1f),
            )
            ToggleChip(
                text = str(R.string.chip_minute_step),
                checked = minuteStepFive,
                onCheckedChange = { minuteStepFive = it },
                modifier = Modifier.weight(1f),
            )
            ToggleChip(
                text = str(R.string.chip_custom_theme),
                checked = customTheme,
                onCheckedChange = { customTheme = it },
                modifier = Modifier.weight(1f),
            )
        }

        // Picker actions, two per row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionButton(str(R.string.open_date), { showDate = true }, Modifier.weight(1f))
            ActionButton(str(R.string.open_time), { showTime = true }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionButton(str(R.string.open_datetime), { showDateTime = true }, Modifier.weight(1f))
            ActionButton(str(R.string.open_leap), { showLeap = true }, Modifier.weight(1f))
        }

        CenteredText(
            str(R.string.keys_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showDate) {
        ShamsaDatePickerBottomSheet(
            onDismissRequest = { showDate = false },
            onConfirm = { dateResult = it.toString() },
            language = language,
            calendar = calendar,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }

    // Time picker on its own. The seconds wheel is optional.
    if (showTime) {
        ShamsaTimePickerBottomSheet(
            onDismissRequest = { showTime = false },
            onConfirm = { timeResult = it.toString() },
            language = language,
            showSeconds = showSeconds,
            minuteStep = minuteStep,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }

    // Date first, then time, in one sheet. The seconds wheel is optional here too.
    if (showDateTime) {
        ShamsaDateTimePickerBottomSheet(
            onDismissRequest = { showDateTime = false },
            onConfirm = { value: ShamsaDateTime -> dateTimeResult = value.toString() },
            language = language,
            calendar = calendar,
            showSeconds = showSeconds,
            minuteStep = minuteStep,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }

    if (showLeap) {
        ShamsaDatePickerBottomSheet(
            onDismissRequest = { showLeap = false },
            onConfirm = { dateResult = it.toString() },
            language = language,
            calendar = calendar,
            // 30 Esfand 1403 only exists because 1403 is a leap year.
            // The date is converted to the picker's calendar automatically.
            initialDate = ShamsaDate.jalali(1403, 12, 30),
            yearRange = if (calendar == ShamsaCalendar.Jalali) 1395..1410 else 2015..2035,
            texts = texts,
            colors = colors,
            textStyles = textStyles,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }
}

/** Resources forced to English or Persian, regardless of the device language. */
@Composable
private fun rememberLocalizedResources(english: Boolean): Resources {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration, english) {
        val forced = Configuration(configuration)
        forced.setLocale(Locale(if (english) "en" else "fa"))
        context.createConfigurationContext(forced).resources
    }
}

// ---------------------------------------------------------------------------------------------
// Small custom components. They only use foundation and Text, so they take little space and need
// no experimental Material 3 API.
// ---------------------------------------------------------------------------------------------

/** A single line of text, centered. */
@Composable
private fun CenteredText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = Color.Unspecified,
) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Compact segmented control: all options side by side, one selected. */
@Composable
private fun <T> Segmented(
    description: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val outline = MaterialTheme.colorScheme.outline
    Row(
        modifier = modifier
            .height(30.dp)
            .clip(shape)
            .border(1.dp, outline, shape)
            .semantics { contentDescription = description },
    ) {
        options.forEachIndexed { index, (value, label) ->
            if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(outline))
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(value) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Compact on/off chip. Filled when on, outlined when off. */
@Composable
private fun ToggleChip(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(if (checked) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (checked) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/** Compact filled button. */
@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}