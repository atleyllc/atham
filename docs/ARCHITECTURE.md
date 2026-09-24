# Architecture assessment

Inspected from commit `6f3265e81abf64e812de7c653486c79e1903b2e8` (`upstream/main`, 2026-09-24). This is the official kv4p HT tree. Atley changes are additive unless a file header says otherwise.

## Languages and frameworks

| Area | Stack |
|------|--------|
| Android app | Java 8 bytecode, XML layouts, ViewBinding, DataBinding |
| Kotlin in app code | None (`android.builtInKotlin=false`; `kotlin-bom:1.8.22` is transitive only) |
| UI toolkit | Views / XML. No Jetpack Compose. Dead `nav_graph.xml` references missing Fragments |
| Architecture | Activities + one foreground `Service`; `MainViewModel` + LiveData |
| Firmware | C++/Arduino on ESP32-WROOM-32 via PlatformIO (`esp32dev`) |
| Protocol | KV4P KISS over USB serial 115200, with BLE GATT KISS fallback |

`minSdk` 26, `compileSdk`/`targetSdk` 36, `applicationId` `com.vagell.kv4pht`, `versionName` `2.0.0.1`.

## Important Android types

| Role | Class | Path |
|------|-------|------|
| Launcher / PTT / memories / APRS UI | `MainActivity` | `android-src/KV4PHT/app/src/main/java/com/vagell/kv4pht/ui/MainActivity.java` |
| Settings | `SettingsActivity` | `.../ui/SettingsActivity.java` |
| Firmware flash UI | `FirmwareActivity` | `.../ui/FirmwareActivity.java` |
| RepeaterBook import | `FindRepeatersActivity` | `.../ui/FindRepeatersActivity.java` |
| Memory editor | `AddEditMemoryActivity` | `.../ui/AddEditMemoryActivity.java` |
| Foreground radio service | `RadioAudioService` | `.../radio/RadioAudioService.java` |
| USB/BLE reconnect loop | `ConnectionController` | `.../radio/ConnectionController.java` |
| Desired/applied radio state | `RadioModuleController` | `.../radio/RadioModuleController.java` |
| KISS + vendor frames | `Protocol` | `.../radio/Protocol.java` |
| USB transport | `UsbSerialRadioTransport` | `.../radio/UsbSerialRadioTransport.java` |
| BLE transport | `BleKissRadioTransport` | `.../radio/BleKissRadioTransport.java` |
| Voice codec | `ImaAdpcm` | `.../radio/ImaAdpcm.java` |
| ESP32 flasher | `FirmwareUtils` | `.../firmware/FirmwareUtils.java` |
| Legacy mode enum | `RadioMode` | `.../radio/RadioMode.java` |
| Derived UI/service state | `RadioConnectionState` | `.../radio/RadioConnectionState.java` (Atley) |

No Fragment classes exist despite the Navigation dependency.

## USB serial

Vendored module `:usbSerialForAndroid` (`com.hoho.android.usbserial`).

Runtime IDs in `RadioAudioService.isESP32Device()`:

- 4292 / 60000 (Silicon Labs CP210x)
- 6790 / 29987 (CH340)

Manifest `device_filter.xml` also lists 9114 / 33041 (Adafruit), which is not accepted at runtime.

Flow: `ConnectionController.reconcileConnections()` every 500 ms → `setupSerialConnection()` → `UsbManager.requestPermission()` with action `com.vagell.kv4pht.USB_PERMISSION` → open 115200 8N1, RTS/DTR on → `UsbSerialRadioTransport` → HELLO.

## Audio

16 kHz PCM 16-bit mono; 249 samples → 128-byte IMA ADPCM.

RX: firmware `COMMAND_RX_AUDIO` (`0x0C`) → `RadioAudioService.handleRxAudio()` → `ImaAdpcm.decodeBlock()` → `AudioTrack` (`USAGE_MEDIA` / `CONTENT_TYPE_SPEECH`).

TX: `AudioRecord` (`MIC`) → `captureVoiceAudio()` → gain → `ImaAdpcm.Encoder` → `Protocol.Sender.txAudio()`.

Firmware hardware path is 48 kHz ADC/PDM with 16 kHz on the wire (`globals.h`, `rxAudio.h`, `txAudio.h`).

## PTT

`MainActivity.handlePttPress()` → `RadioAudioService.startPtt()` → `RadioModuleController.pttDown()` (`HOST_STATE_PTT_REQUESTED`) + voice capture. Firmware `reconcileDesiredState()` keys `pinPtt` LOW only if `HOST_STATE_TX_ALLOWED`. Physical PTT is reported as `DEVICE_STATE_PHYS_PTT_DOWN`; Android starts/ends PTT. App timeout 180 s; firmware watchdog ~778 ms without audio and 200 s runaway.

## Radio protocol

Documented in `microcontroller-src/kv4p_ht_esp32_wroom_32/readme.md` and implemented in `protocol.h` / `Protocol.java`.

Vendor frame: `FEND 0x06 "KV4P" 0x01 <command> <payload> FEND`.

Host → ESP32: `0x0C` TX audio, `0x0D` `HostDesiredState`.  
ESP32 → host: `0x06` HELLO, `0x0B` `DeviceState`, `0x0C` RX audio, `0x09` window update, `0x01`–`0x05` debug.

SA818/DRA818 via UART 9600. CTCSS TX is hardware. CTCSS RX is software. DCS is not sent (`0, 0`). Scanning is Android-owned.

## Firmware flashing

No in-band OTA command. Android uses `esp32-flash-lib` 1.1.17 after DTR/RTS bootloader entry. BLE cannot flash. Web fallback: kv4p.com.

## APRS

Firmware is a KISS TNC (`esp32-afsk`). Android builds AX.25 (`Protocol.Sender.txAx25()`) and parses RX with `com.vagell.kv4pht.aprs.parser`. Location beacons use Play Services `FusedLocationProviderClient`. Legacy `javAX25` AFSK classes are not on the hot path.

## Persistence

Room database `kv4pht-db` v7: `AppSetting`, `ChannelMemory`, `APRSMessage`. Radio tune state lives in firmware NVS, not Android prefs.

## Networking

RepeaterBook CSV via WebView/DownloadManager. Play Services location. SonarCloud in CI. No accounts or analytics in the app itself. RepeaterBook sign-in is an external website.

## Tests and CI

- App unit: `ProtocolKissTest`, `ImaAdpcmTest` (42 tests in baseline)
- usbSerialForAndroid unit: 9 tests
- Firmware native: `test_kiss_protocol`, `test_audio_codec`, `test_tx_audio_watchdog` (20 cases)
- Workflows: `.github/workflows/android-build.yml`, `arduino.yml`, `nightly.yml`, `sonarcloud-android.yml`

## Operation traces

### 1. Launch to hardware detection

`MainActivity.onStart()` → `startAndBindRadioAudioService()` → `RadioAudioService.onStartCommand()` → `start()` → `ConnectionController` → `reconcileConnections()` → USB `isESP32Device()` or `attemptBleConnect()`.

### 2. USB permission to connection

`setupSerialConnection()` → `requestPermission()` → `MainActivity.usbReceiver` → `reconnectViaUSB()` → open port → `onReady()` → `startProtocolHandshake()` → `validateHello()` → `setMode(RX)` → `radioConnected()`.

### 3. PTT to RF

`handlePttPress()` → `startPtt()` → `pttDown()` + ADPCM frames → firmware `setMode(MODE_TX)` → I2S PDM into SA818.

### 4. RF to speaker

Firmware ADC pipeline → `COMMAND_RX_AUDIO` → `handleRxAudio()` → `AudioTrack`. Routing is the Android media path (speaker/headset).

### 5. Frequency / memory

`tuneToMemory()` / `tuneToFreq()` → `RadioModuleController` snapshot → `COMMAND_HOST_DESIRED_STATE` → firmware `sa818.group()` → `COMMAND_DEVICE_STATE` → `syncRadioUiFromDeviceState()`.

### 6. APRS

TX: `sendChatMessage()` / `sendPositionBeacon()` → `txAX25Packet()`.  
RX: KISS DATA → `handleEsp32Ax25Packet()` → `Parser.parseAX25()` → Room + `APRSAdapter`.

### 7. Firmware detect and flash

HELLO `ver` vs `PACKAGED_FIRMWARE_VER`. Flash: `FirmwareActivity` → `FirmwareUtils.flashFirmware()` → `renegotiateAfterFlashing()`.

## Areas that must not be casually rewritten

Protocol structs, KISS escaping, ADPCM frame layout, HELLO validation, flash partition map, Room migrations, BLE GATT UUIDs (`00000001-ba2a-46c9-ae49-01b0961f68bb`), TX-allowed safety flag, NVS keys.

## Modernization recommendation

**Keep the native Java radio stack. Adopt Kotlin incrementally. Do not introduce Compose in the first stages.**

Reasons, from this tree:

- The radio path is a large, working Java service (`RadioAudioService`) with firmware-coupled binary protocol. A rewrite would create unmatched-firmware and stuck-TX risk.
- There is no Kotlin source yet, and `android.builtInKotlin=false` is intentional. Enabling Kotlin later is reasonable for **new** files; doing it in the first connection-state slice would mix toolchain change with behavior change.
- Compose can coexist with Views, but this app is one main activity with ViewBinding, DataBinding, and tight service callbacks. A second UI toolkit now would split navigation, theming, and PTT lifecycle without helping USB/audio correctness.
- React Native, Expo, Flutter, and web wrappers are rejected: they cannot safely own USB serial, foreground-service audio, and 16 kHz ADPCM latency.

Safer sequence:

1. Keep Java protocol/audio/USB/APRS.
2. Publish derived UI state (started: `RadioConnectionState`).
3. Add Kotlin when the first new non-critical screen or helper is written, enabling the Kotlin plugin then.
4. Consider Compose only for new secondary screens after the View-based primary radio screen is stable.

## Build risks and debt

Monolithic service, destructive Room fallback, dead Navigation/AFSK code, USB ID mismatch, public CI debug keystore, unminified release, lint skipped in CI, source-build firmware bypass.
