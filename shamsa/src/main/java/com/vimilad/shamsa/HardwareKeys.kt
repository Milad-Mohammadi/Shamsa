package com.vimilad.shamsa

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

private const val TAG = "ShamsaKeys"

/**
 * Makes the node focusable, requests focus when it enters the composition and maps hardware
 * keys to picker actions.
 *
 * Every key down of an increase/decrease key is one step. Confirm and cancel act once per press.
 * Key up events of handled keys are consumed too, so default handlers (e.g. the dialog's Back)
 * do not run a second time.
 */
@Composable
internal fun Modifier.pickerKeys(
    keys: PersianDatePickerKeys,
    onStep: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
): Modifier {
    val focusRequester = remember { FocusRequester() }

    val currentKeys by rememberUpdatedState(keys)
    val currentOnStep by rememberUpdatedState(onStep)
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    val currentOnCancel by rememberUpdatedState(onCancel)

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    return this
        .onPreviewKeyEvent { event ->
            val k = event.key
            val ks = currentKeys
            val repeatCount = event.nativeKeyEvent.repeatCount
            if (ks.debugLog) {
                Log.d(TAG, "keyCode=${event.nativeKeyEvent.keyCode} key=$k type=${event.type} repeatCount=$repeatCount")
            }

            val isCancel = k in ks.cancel
            val isConfirm = !isCancel && k in ks.confirm
            val isIncrease = !isCancel && !isConfirm && k in ks.increase
            val isDecrease = !isCancel && !isConfirm && !isIncrease && k in ks.decrease
            if (!(isCancel || isConfirm || isIncrease || isDecrease)) return@onPreviewKeyEvent false

            if (event.type == KeyEventType.KeyDown) {
                when {
                    isIncrease -> currentOnStep(+1)
                    isDecrease -> currentOnStep(-1)
                    repeatCount == 0 -> if (isCancel) currentOnCancel() else currentOnConfirm()
                }
            }
            true
        }
        .focusRequester(focusRequester)
        .focusable()
}
