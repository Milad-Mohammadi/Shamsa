package com.vimilad.shamsa

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class KeyRepeat {
    var job: Job? = null
    var key: Key? = null

    fun stop() {
        job?.cancel()
        job = null
        key = null
    }
}

/**
 * Makes the node focusable, requests focus when it enters the composition and maps hardware
 * keys to picker actions.
 *
 * Holding an increase/decrease key repeats the step on an internal timer and speeds up after a
 * while. System key-repeat events are ignored in that case, so devices that send them and
 * devices that do not behave the same.
 */
@Composable
internal fun Modifier.pickerKeys(
    keys: PersianDatePickerKeys,
    onStep: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
): Modifier {
    val scope = rememberCoroutineScope()
    val repeat = remember { KeyRepeat() }
    val focusRequester = remember { FocusRequester() }

    val currentKeys by rememberUpdatedState(keys)
    val currentOnStep by rememberUpdatedState(onStep)
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    val currentOnCancel by rememberUpdatedState(onCancel)

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    DisposableEffect(Unit) { onDispose { repeat.stop() } }

    return this
        .onPreviewKeyEvent { event ->
            val k = event.key
            val ks = currentKeys
            val isCancel = k in ks.cancel
            val isConfirm = !isCancel && k in ks.confirm
            val isUp = !isCancel && !isConfirm && k in ks.increase
            val isDown = !isCancel && !isConfirm && !isUp && k in ks.decrease
            if (!(isCancel || isConfirm || isUp || isDown)) return@onPreviewKeyEvent false

            // Key up ends a hold. It is consumed too, so default handlers
            // (e.g. the dialog's Back) do not run a second time.
            if (event.type == KeyEventType.KeyUp) {
                if (k == repeat.key) repeat.stop()
                return@onPreviewKeyEvent true
            }

            val isSystemRepeat = event.nativeKeyEvent.repeatCount > 0
            when {
                isUp || isDown -> {
                    val direction = if (isUp) 1 else -1
                    if (ks.repeatIntervalMillis > 0) {
                        // Our own timer drives holding; system repeat events are ignored.
                        if (!isSystemRepeat) {
                            repeat.stop()
                            currentOnStep(direction)
                            repeat.key = k
                            repeat.job = scope.launch {
                                delay(ks.repeatDelayMillis)
                                var count = 0
                                while (true) {
                                    val size = if (count >= ks.fastScrollAfterRepeats) {
                                        ks.fastScrollStep.coerceAtLeast(1)
                                    } else 1
                                    currentOnStep(direction * size)
                                    count++
                                    delay(ks.repeatIntervalMillis)
                                }
                            }
                        }
                    } else {
                        currentOnStep(direction)
                    }
                }
                isSystemRepeat -> Unit
                else -> {
                    repeat.stop()
                    if (isCancel) currentOnCancel() else currentOnConfirm()
                }
            }
            true
        }
        .onFocusChanged { if (!it.hasFocus) repeat.stop() }
        .focusRequester(focusRequester)
        .focusable()
}
