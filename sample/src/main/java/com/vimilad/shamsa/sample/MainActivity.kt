package com.vimilad.shamsa.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vimilad.shamsa.ShamsaCalendar
import com.vimilad.shamsa.ShamsaDate
import com.vimilad.shamsa.ShamsaDatePickerBottomSheet
import com.vimilad.shamsa.ShamsaDateTime
import com.vimilad.shamsa.ShamsaDateTimePickerBottomSheet
import com.vimilad.shamsa.ShamsaDefaults
import com.vimilad.shamsa.ShamsaLanguage
import com.vimilad.shamsa.ShamsaTimePickerBottomSheet

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

    // Settings. Language and calendar are two independent switches.
    var english by rememberSaveable { mutableStateOf(false) }
    var gregorian by rememberSaveable { mutableStateOf(false) }
    var showSeconds by rememberSaveable { mutableStateOf(false) }
    var minuteStepFive by rememberSaveable { mutableStateOf(false) }
    var customTheme by rememberSaveable { mutableStateOf(false) }

    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showDateTime by remember { mutableStateOf(false) }
    var showLeap by remember { mutableStateOf(false) }

    val language = if (english) ShamsaLanguage.English else ShamsaLanguage.Persian
    val calendar = if (gregorian) ShamsaCalendar.Gregorian else ShamsaCalendar.Jalali
    val minuteStep = if (minuteStepFive) 5 else 1

    // Texts, colors and fonts: library defaults, or the custom theme.
    val customConfirm = stringResource(if (english) R.string.custom_confirm_en else R.string.custom_confirm_fa)
    val customCancel = stringResource(if (english) R.string.custom_cancel_en else R.string.custom_cancel_fa)
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
    val focusKey = Key.Tab

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.result_date, dateResult ?: "—"))
        Text(stringResource(R.string.result_time, timeResult ?: "—"))
        Text(stringResource(R.string.result_datetime, dateTimeResult ?: "—"))
        Text(stringResource(R.string.touch_count, touchCount))

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)
                SettingRow(stringResource(R.string.setting_english), english) { english = it }
                SettingRow(stringResource(R.string.setting_gregorian), gregorian) { gregorian = it }
                SettingRow(stringResource(R.string.setting_seconds), showSeconds) { showSeconds = it }
                SettingRow(stringResource(R.string.setting_minute_step), minuteStepFive) { minuteStepFive = it }
                SettingRow(stringResource(R.string.setting_custom_theme), customTheme) { customTheme = it }
            }
        }

        Button(onClick = { showDate = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_date))
        }
        Button(onClick = { showTime = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_time))
        }
        Button(onClick = { showDateTime = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_datetime))
        }
        Button(onClick = { showLeap = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_leap))
        }

        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.keys_hint), style = MaterialTheme.typography.bodySmall)
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

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
