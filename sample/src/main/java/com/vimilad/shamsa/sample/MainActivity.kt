package com.vimilad.shamsa.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    var result by remember { mutableStateOf<String?>(null) }
    var language by remember { mutableStateOf(ShamsaLanguage.Persian) }
    var calendar by remember { mutableStateOf(ShamsaCalendar.Jalali) }
    var touchCount by remember { mutableIntStateOf(0) }

    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showDateTime by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }

    // Optional hardware key that moves key focus between the wheels (touch always works too).
    val focusKey = Key.Tab

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.result, result ?: "—"))
        Text(stringResource(R.string.touch_count, touchCount))

        // Language and calendar are two independent settings.
        OutlinedButton(onClick = {
            language = if (language == ShamsaLanguage.Persian) ShamsaLanguage.English else ShamsaLanguage.Persian
        }) {
            val name = if (language == ShamsaLanguage.Persian) R.string.language_persian else R.string.language_english
            Text(stringResource(R.string.language_toggle, stringResource(name)))
        }
        OutlinedButton(onClick = {
            calendar = if (calendar == ShamsaCalendar.Jalali) ShamsaCalendar.Gregorian else ShamsaCalendar.Jalali
        }) {
            val name = if (calendar == ShamsaCalendar.Jalali) R.string.calendar_jalali else R.string.calendar_gregorian
            Text(stringResource(R.string.calendar_toggle, stringResource(name)))
        }

        Button(onClick = { showDate = true }) { Text(stringResource(R.string.open_date)) }
        Button(onClick = { showTime = true }) { Text(stringResource(R.string.open_time)) }
        Button(onClick = { showDateTime = true }) { Text(stringResource(R.string.open_datetime)) }
        Button(onClick = { showCustom = true }) { Text(stringResource(R.string.open_custom)) }
    }

    if (showDate) {
        ShamsaDatePickerBottomSheet(
            onDismissRequest = { showDate = false },
            onConfirm = { result = it.toString() },
            language = language,
            calendar = calendar,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }

    if (showTime) {
        ShamsaTimePickerBottomSheet(
            onDismissRequest = { showTime = false },
            onConfirm = { result = it.toString() },
            language = language,
            showSeconds = true,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }

    if (showDateTime) {
        ShamsaDateTimePickerBottomSheet(
            onDismissRequest = { showDateTime = false },
            onConfirm = { result = it.toString() },
            language = language,
            calendar = calendar,
            minuteStep = 5,
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }

    if (showCustom) {
        ShamsaDatePickerBottomSheet(
            onDismissRequest = { showCustom = false },
            onConfirm = { result = it.toString() },
            language = language,
            calendar = calendar,
            // 30 Esfand 1403 only exists because 1403 is a leap year.
            initialDate = ShamsaDate.jalali(1403, 12, 30),
            yearRange = if (calendar == ShamsaCalendar.Jalali) 1395..1410 else 2015..2035,
            texts = ShamsaDefaults.texts(
                language = language,
                calendar = calendar,
                confirm = stringResource(R.string.custom_confirm),
                cancel = stringResource(R.string.custom_cancel),
                usePersianDigits = false,
            ),
            // One line to change the font of the whole picker. Swap in your own FontFamily,
            // e.g. FontFamily(Font(R.font.vazirmatn)).
            textStyles = ShamsaDefaults.textStyles(
                fontFamily = FontFamily.Monospace,
                selectedItem = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            ),
            // Override only the colors you care about; the rest follow the theme.
            colors = ShamsaDefaults.colors(
                sheetContainer = Color(0xFF1B2B34),
                selectedRow = Color(0xFF2C4452),
                selectedText = Color.White,
                focusedText = Color(0xFFFFC857),
                unselectedText = Color.White.copy(alpha = 0.35f),
                dragHandle = Color.White.copy(alpha = 0.5f),
                confirmContainer = Color(0xFFFFC857),
                confirmContent = Color.Black,
                cancelContent = Color.White,
            ),
            onTouch = { touchCount++ },
            focusKey = focusKey,
        )
    }
}
