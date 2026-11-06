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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vimilad.shamsa.JalaliDate
import com.vimilad.shamsa.PersianDatePickerBottomSheet
import com.vimilad.shamsa.PersianDatePickerTexts

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
    var selected by remember { mutableStateOf<JalaliDate?>(null) }
    var showDefault by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("تاریخ انتخابی: ${selected ?: "—"}")

        Button(onClick = { showDefault = true }) { Text("پیش‌فرض (امروز)") }
        Button(onClick = { showCustom = true }) { Text("سفارشی (۳۰ اسفند ۱۴۰۳ کبیسه)") }
    }

    if (showDefault) {
        PersianDatePickerBottomSheet(
            onDismissRequest = { showDefault = false },
            onConfirm = { selected = it },
            initialDate = selected ?: JalaliDate.today(),
        )
    }

    if (showCustom) {
        PersianDatePickerBottomSheet(
            onDismissRequest = { showCustom = false },
            onConfirm = { selected = it },
            initialDate = JalaliDate(1403, 12, 30),
            yearRange = 1395..1410,
            texts = PersianDatePickerTexts(
                confirm = "ثبت",
                cancel = "بستن",
                useFarsiDigits = false,
            ),
        )
    }
}
