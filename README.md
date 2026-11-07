# Shamsa

[![Maven Central](https://img.shields.io/maven-central/v/com.vimilad/shamsa)](https://central.sonatype.com/artifact/com.vimilad/shamsa)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
![minSdk](https://img.shields.io/badge/minSdk-21-brightgreen)

A Jetpack Compose **Jalali (Persian) date picker** shown as a modal bottom sheet.
Three scrolling wheels (day, month, year), a confirm and a cancel button, and nothing else.

- **Zero third-party dependencies.** Only Compose. The calendar math is built in.
- **Correct calendar.** Leap years and 29/30/31-day months are handled, and the day is
  clamped automatically when you scroll to a shorter month or a non-leap year.
- **Compact.** Each wheel shows three rows: previous, current and next.
- **Hardware keys.** D-pad, OK and cancel keys work out of the box, with hold-to-repeat and
  fast scroll. Built for POS terminals and other devices with physical keypads.
- **Touch callback.** Get notified on every touch inside the picker
  (for example, to reset an inactivity timer).
- **Easy to theme.** Texts, colors and fonts are all configurable from the outside.
- **RTL.** The picker is always laid out right-to-left: day | month | year.

## Installation

Shamsa is published to Maven Central.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.vimilad:shamsa:1.0.0")
}
```

The library is built with Compose BOM `2024.12.01` and Material 3. Your app needs Compose and
Material 3 on its own classpath. `minSdk` is 21.

## Quick start

```kotlin
@Composable
fun Screen() {
    var show by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf<JalaliDate?>(null) }

    Button(onClick = { show = true }) { Text("Pick a date") }

    if (show) {
        PersianDatePickerBottomSheet(
            onDismissRequest = { show = false },   // called after the sheet has closed
            onConfirm = { date = it },             // JalaliDate(year, month, day)
            initialDate = date ?: JalaliDate.today(),
        )
    }
}
```

`onDismissRequest` is called after the sheet closes for any reason (cancel, confirm,
swipe down, tap outside, Back), so always set your "show" state to `false` there.

## Parameters

| Parameter | Default | Description |
|---|---|---|
| `onDismissRequest` | required | Called after the sheet has closed. |
| `onConfirm` | required | Called with the selected `JalaliDate`. |
| `initialDate` | `JalaliDate.today()` | Date selected when the sheet opens. |
| `yearRange` | `1300..1500` | Years available in the year wheel. |
| `texts` | `PersianDatePickerTexts()` | Button labels, month names, digit style. |
| `colors` | `PersianDatePickerDefaults.colors()` | Colors of every part. |
| `textStyles` | `PersianDatePickerDefaults.textStyles()` | Fonts and text styles. |
| `onTouch` | `{}` | Called on every finger-down inside the sheet. |
| `keys` | `PersianDatePickerKeys()` | Hardware key mapping. |
| `sheetState` | skip partially expanded | Material 3 `SheetState`. |

`PersianDatePickerContent` takes the same parameters (minus `onDismissRequest` and
`sheetState`, plus `onCancel`) if you want to host the picker in your own dialog or screen.

## Customization

### Texts

```kotlin
PersianDatePickerBottomSheet(
    /* ... */
    texts = PersianDatePickerTexts(
        confirm = "Select",
        cancel = "Close",
        monthNames = myTwelveMonthNames,
        useFarsiDigits = false,          // 1405 instead of Persian digits
    ),
)
```

### Fonts

```kotlin
PersianDatePickerBottomSheet(
    /* ... */
    textStyles = PersianDatePickerDefaults.textStyles(fontFamily = vazirmatn),
)
```

Without `textStyles` the picker uses `MaterialTheme.typography`, so a font set in your theme is
picked up automatically. `item`, `selectedItem` and `button` can also be set individually.

### Colors

Override only what you need; everything else follows your Material theme.

```kotlin
PersianDatePickerBottomSheet(
    /* ... */
    colors = PersianDatePickerDefaults.colors(
        sheetContainer = Color(0xFF1B2B34),
        selectedRow = Color(0xFF2C4452),
        confirmContainer = Color(0xFFFFC857),
        confirmContent = Color.Black,
    ),
)
```

| Color | Used for |
|---|---|
| `sheetContainer` | Sheet background |
| `scrim` | Overlay behind the sheet |
| `dragHandle` | Drag handle at the top |
| `selectedRow` | Highlight bar behind the selected row |
| `selectedText` | Selected row |
| `focusedText` | Selected row of the column that has hardware-key focus |
| `unselectedText` | Previous and next rows |
| `confirmContainer`, `confirmContent` | Confirm button |
| `cancelContainer`, `cancelContent` | Cancel button |

## Hardware keys

| Action | Default keys |
|---|---|
| +1 on the focused column | DPAD up, DPAD right |
| -1 on the focused column | DPAD down, DPAD left |
| Confirm | DPAD center, Enter, Numpad Enter |
| Cancel (closes the sheet) | Escape, Back, Clear |

- Key focus starts on the **day** column. Touching the month or year wheel moves key focus to
  that column, and touching the day wheel moves it back.
- The focused column is drawn with `focusedText`. Set `focusedText` equal to `selectedText` to
  hide the indication.
- **Holding** an up/down key keeps scrolling. The picker runs its own repeat timer, so this works
  even on keypads that do not send key-repeat events. After a number of repeats the step grows
  (fast scroll).
- Values do not wrap around: a column stops at its first and last item, and the day never
  exceeds the length of the selected month.

Everything is configurable:

```kotlin
keys = PersianDatePickerKeys(
    increase = setOf(Key.DirectionUp),
    decrease = setOf(Key.DirectionDown),
    confirm = setOf(Key.Enter),
    cancel = setOf(Key.Escape, Key.Back),
    repeatDelayMillis = 400,      // delay before a held key starts repeating
    repeatIntervalMillis = 70,    // 0 turns hold-to-repeat off
    fastScrollAfterRepeats = 10,  // repeats before the step grows
    fastScrollStep = 3,           // 1 turns fast scroll off
)

keys = PersianDatePickerKeys.None   // turn hardware key handling off
```

If a key is listed in more than one set, the priority is cancel, confirm, increase, decrease.

## Touch callback

```kotlin
PersianDatePickerBottomSheet(
    /* ... */
    onTouch = { inactivityTimer.reset() },
)
```

`onTouch` fires on every finger-down anywhere on the sheet, including the drag handle. The event
is not consumed, so the picker behaves exactly as it would without the callback. Hardware key
presses do not trigger it.

## Calendar utilities

The calendar math is public and can be used without the UI.

```kotlin
JalaliCalendar.isLeapYear(1403)            // true
JalaliCalendar.monthLength(1403, 12)       // 30
JalaliCalendar.monthLength(1404, 12)       // 29

val d = JalaliDate(1405, 8, 12)            // throws IllegalArgumentException for impossible dates
d.toDate()                                 // java.util.Date at midnight, device time zone
JalaliDate.today()
JalaliDate.fromGregorian(2026, 11, 3)      // 1405/08/12
JalaliDate.from(date)                      // from java.util.Date
JalaliDate.fromMillis(millis)
```

The algorithm is [jalaali-js](https://github.com/jalaali/jalaali-js), which matches the official
Iranian calendar for years 1178 to 1633. It was checked against every day from 1300 to 1500.

## Sample app

The `sample` module shows the default picker, a fully customized one (font, colors, texts,
custom year range) and the touch callback. Open the project in Android Studio and run `sample`.

## Building

```bash
./gradlew :sample:installDebug                 # run the sample on a device
./gradlew :shamsa:testDebugUnitTest            # unit tests
./gradlew :shamsa:assembleRelease              # AAR: shamsa/build/outputs/aar/shamsa-release.aar
```

An AAR does not contain its dependencies. If you use the AAR directly instead of the Maven
artifact, your app needs Compose and Material 3.

## Releasing

Publishing uses the [vanniktech maven-publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
and the Sonatype Central Portal. Put your credentials and signing key in `~/.gradle/gradle.properties`
(never in the repository):

```properties
mavenCentralUsername=<Central Portal token username>
mavenCentralPassword=<Central Portal token password>
signingInMemoryKey=<armored private key without header, footer and newlines>
signingInMemoryKeyId=<last 8 characters of the key id>
signingInMemoryKeyPassword=<key password>
```

```bash
./gradlew :shamsa:publishToMavenCentral --no-configuration-cache
```

Then publish the deployment from the Central Portal UI, or use `publishAndReleaseToMavenCentral`
to release automatically. Versions on Maven Central are immutable.

## License

```
Copyright 2026 Milad Mohammadi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
