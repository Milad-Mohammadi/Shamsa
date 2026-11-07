package com.vimilad.shamsa.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vimilad.shamsa.JalaliDate
import com.vimilad.shamsa.PersianDatePickerBottomSheet
import com.vimilad.shamsa.PersianDatePickerDefaults

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sample() {
    var selected by remember { mutableStateOf<JalaliDate?>(null) }
    var showDefault by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    var touchCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.selected_date, selected?.toString() ?: "—"))
        Text(stringResource(R.string.touch_count, touchCount))

        Button(onClick = { showDefault = true }) { Text(stringResource(R.string.open_default)) }
        Button(onClick = { showCustom = true }) { Text(stringResource(R.string.open_custom)) }
    }

    if (showDefault) {
        PersianDatePickerBottomSheet(
            onDismissRequest = { showDefault = false },
            onConfirm = { selected = it },
            initialDate = selected ?: JalaliDate.today(),
            onTouch = { touchCount++ },
        )
    }

    if (showCustom) {
        PersianDatePickerBottomSheet(
            onDismissRequest = { showCustom = false },
            onConfirm = { selected = it },
            initialDate = JalaliDate(1403, 12, 30),
            yearRange = 1395..1410,
            texts = PersianDatePickerDefaults.texts(
                confirm = stringResource(R.string.custom_confirm),
                cancel = stringResource(R.string.custom_cancel),
                useFarsiDigits = false,
            ),
            // One line to change the font of the whole picker. Swap in your own FontFamily,
            // e.g. FontFamily(Font(R.font.vazirmatn)).
            textStyles = PersianDatePickerDefaults.textStyles(
                fontFamily = FontFamily.Monospace,
                selectedItem = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            ),
            // Override only the colors you care about; the rest follow the theme.
            colors = PersianDatePickerDefaults.colors(
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
        )
    }
}
