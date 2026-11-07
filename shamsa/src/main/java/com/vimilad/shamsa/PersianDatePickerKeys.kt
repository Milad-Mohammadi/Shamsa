package com.vimilad.shamsa

import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.key.Key

/**
 * Hardware key mapping. Every set can be overridden.
 * If a key appears in several sets the priority is: cancel > confirm > increase > decrease.
 * Use [None] to turn hardware key handling off completely.
 */
@Immutable
data class PersianDatePickerKeys(
    /** +1 on the focused column. */
    val increase: Set<Key> = setOf(Key.DirectionDown, Key.DirectionRight),
    /** -1 on the focused column. */
    val decrease: Set<Key> = setOf(Key.DirectionUp, Key.DirectionLeft),
    /** Confirm the selected date. */
    val confirm: Set<Key> = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter),
    /** Close the sheet and cancel (the "X" key on POS terminals). */
    val cancel: Set<Key> = setOf(Key.Escape, Key.Back, Key.Clear),
    /**
     * Logs every key event the picker receives to Logcat (tag "ShamsaKeys"),
     * including keys it does not handle. Useful to find out which key codes a device sends.
     */
    val debugLog: Boolean = false,
) {
    companion object {
        val None = PersianDatePickerKeys(
            increase = emptySet(),
            decrease = emptySet(),
            confirm = emptySet(),
            cancel = emptySet(),
        )
    }
}
