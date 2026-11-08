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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

private const val TAG = "ShamsaKeys"

/**
 * Makes the node focusable, requests focus when it enters the composition and maps hardware
 * keys to picker actions.
 *
 * Every key down of an increase/decrease key is one step. Confirm, cancel and the focus key act
 * once per press. Key up events of handled keys are consumed too, so default handlers
 * (e.g. the dialog's Back) do not run a second time.
 */
@Composable
internal fun Modifier.pickerKeys(
    keys: ShamsaKeys,
    focusKey: Key?,
    onStep: (Int) -> Unit,
    onFocusNext: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
): Modifier {
    val focusRequester = remember { FocusRequester() }

    val currentKeys by rememberUpdatedState(keys)
    val currentFocusKey by rememberUpdatedState(focusKey)
    val currentOnStep by rememberUpdatedState(onStep)
    val currentOnFocusNext by rememberUpdatedState(onFocusNext)
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

            val isFocus = currentFocusKey != null && k == currentFocusKey
            val isCancel = !isFocus && k in ks.cancel
            val isConfirm = !isFocus && !isCancel && k in ks.confirm
            val isIncrease = !isFocus && !isCancel && !isConfirm && k in ks.increase
            val isDecrease = !isFocus && !isCancel && !isConfirm && !isIncrease && k in ks.decrease
            if (!(isFocus || isCancel || isConfirm || isIncrease || isDecrease)) {
                return@onPreviewKeyEvent false
            }

            if (event.type == KeyEventType.KeyDown) {
                when {
                    isIncrease -> currentOnStep(+1)
                    isDecrease -> currentOnStep(-1)
                    repeatCount == 0 -> when {
                        isFocus -> currentOnFocusNext()
                        isCancel -> currentOnCancel()
                        else -> currentOnConfirm()
                    }
                }
            }
            true
        }
        .focusRequester(focusRequester)
        .focusable()
}
