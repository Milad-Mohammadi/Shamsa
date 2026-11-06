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
    val increase: Set<Key> = setOf(Key.DirectionUp, Key.DirectionRight),
    /** -1 on the focused column. */
    val decrease: Set<Key> = setOf(Key.DirectionDown, Key.DirectionLeft),
    /** Confirm the selected date. */
    val confirm: Set<Key> = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter),
    /** Close the sheet and cancel (the "X" key on POS terminals). */
    val cancel: Set<Key> = setOf(Key.Escape, Key.Back, Key.Clear),
    /**
     * Delay before a held increase/decrease key starts repeating.
     * The picker runs its own repeat timer, so holding works even when the device
     * does not send key-repeat events.
     */
    val repeatDelayMillis: Long = 400,
    /** Interval between repeats while the key is held. Set to 0 to turn hold-to-repeat off. */
    val repeatIntervalMillis: Long = 70,
    /** Number of repeats after which fast scroll kicks in. */
    val fastScrollAfterRepeats: Int = 10,
    /** Step size once fast scroll kicks in. Set to 1 to disable fast scroll. */
    val fastScrollStep: Int = 3,
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
