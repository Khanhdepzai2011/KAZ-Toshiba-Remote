# KAZ Toshiba Remote
Native Android app for phones with an **IR blaster**, including Xiaomi 11T.

## Download the APK
Open the **Actions** tab, select **Build Android APK**, and download the `KAZ-Toshiba-Remote-APK` artifact after the run succeeds. Unzip it to obtain `KAZ-Toshiba-Remote.apk`.

## Features
- Real `ConsumerIrManager.transmit()` at 38 kHz, no Wi-Fi connection needed.
- Built-in **Toshiba NEC1** codes (device 64, subdevice -1) covering POWER, MUTE, SOURCE, VOLUME, CHANNEL, MENU, numbers, and a few more keys.
- Long-press to edit any button's 8-digit NEC hexadecimal code.
- No internet permission, no analytics or ads.
- IRDB reference: https://github.com/probonopd/irdb/blob/master/codes/Toshiba/TV/64%2C-1.csv

## TV compatibility
**RC ERF3c98V1.00 is a TV remote-controller firmware revision, NOT an IR command ID.**
Toshiba uses multiple command sets. The built-in profile is documented in IRDB, but **not verified for this exact television**. HOME and directional keys need to be programmed manually because this IRDB profile does not document matching discrete functions.

## Build locally (optional)
Requires JDK 17 and Android SDK API 35:
`gradle :app:assembleDebug`
