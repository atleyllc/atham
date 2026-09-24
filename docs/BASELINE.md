# Baseline build record

Recorded 2026-09-24 on macOS 15 (darwin 25.6.0), Apple Silicon. No KV4P-HT hardware was attached. No firmware was flashed. No RF was transmitted.

Upstream commit: `6f3265e81abf64e812de7c653486c79e1903b2e8`  
Branch: `atley/android-foundation`  
Fork: `https://github.com/atleyllc/kv4p-ht`  
Upstream remote: `https://github.com/VanceVagell/kv4p-ht.git`

## Tool versions used

```
openjdk 21.0.11 (Homebrew)
Gradle Wrapper 9.2.1
Android Gradle Plugin 9.0.1
ANDROID_HOME=/Users/KAT/Library/Android/sdk
PlatformIO Core 6.1.18
Python 3.12.6
espressif32 6.10.0
framework-arduinoespressif32 3.20017.241212
```

Project docs and CI specify JDK 17. JDK 21 built the app successfully. Prefer JDK 17 for CI parity when available.

## Commands and results

### Android debug APK — SUCCESS

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.11/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=$HOME/Library/Android/sdk
cd android-src/KV4PHT
./gradlew assembleDebug --stacktrace
```

Result: `BUILD SUCCESSFUL in 2m 48s`  
Artifact: `android-src/KV4PHT/app/build/outputs/apk/debug/app-debug.apk` (13,769,164 bytes)

Warnings: Java 8 source/target obsolete on JDK 21; AGP 9 flag deprecations; some deprecated API usage.

### ESP32 firmware — SUCCESS

```bash
export PATH=$HOME/Library/Python/3.12/bin:$PATH
cd microcontroller-src
pio run -e esp32dev -e esp32dev-release
```

Result: both environments SUCCESS in 2m 52s.

| Environment | Artifact | Size |
|-------------|----------|------|
| `esp32dev` (debug) | `microcontroller-src/.pio/build/esp32dev/firmware.bin` | 1,595,072 bytes |
| `esp32dev-release` | `microcontroller-src/.pio/build/esp32dev-release/firmware.bin` | 1,604,016 bytes |

Release flash usage: 81.3% (1,597,441 / 1,966,080). Third-party I2S `ADC_ATTEN_DB_11` deprecation warnings only.

### Android unit tests — SUCCESS

```bash
cd android-src/KV4PHT
./gradlew test --stacktrace
```

- `:app:testDebugUnitTest` — 42 tests, 0 failures
- `:usbSerialForAndroid:testDebugUnitTest` — 9 tests, 0 failures

### Firmware native tests — SUCCESS

```bash
cd microcontroller-src
pio test -e native-tests
```

20 test cases succeeded (`test_tx_audio_watchdog`, `test_audio_codec`, `test_kiss_protocol`).

### Android lint — FAILED (upstream)

```bash
cd android-src/KV4PHT
./gradlew lint --stacktrace
```

`app` report: **1 error, 330 warnings**.  
Error: `MissingPermission` at `RadioAudioService.java:352` (`sendPositionBeacon()` from the beacon scheduler).  
`usbSerialForAndroid`: 0 errors, 4 warnings.

Upstream `.github/workflows/android-build.yml` runs `./gradlew check -x lint` and `./gradlew build -x lint`. This failure is a known baseline issue, not introduced by Atley.

## Hardware-dependent items not tested

USB attach, permission, HELLO, RX/TX audio, PTT, scanning, APRS over RF, BLE KISS, in-app flashing, headset routing, physical PTT, VHF/UHF NVS, and RepeaterBook device conflict.

## Safety note

Locally built `firmware.bin` is **not** the same artifact as the last public `firmware_v17.bin` bundled in the APK until they are intentionally rebuilt together. Do not flash either image without authorization.
