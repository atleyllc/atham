# Atley KV4P development

This fork develops a modern Android experience on top of the official [kv4p HT](https://github.com/VanceVagell/kv4p-ht) hardware, firmware, and radio stack. The provisional product name is **Atley KV4P**. Upstream credit and GPL-3.0 remain in force.

## Clone with origin and upstream

If GitHub authentication works:

```bash
gh repo fork VanceVagell/kv4p-ht --clone=false
git clone https://github.com/atleyllc/kv4p-ht.git
cd kv4p-ht
git remote add upstream https://github.com/VanceVagell/kv4p-ht.git
git fetch origin
git fetch upstream
git checkout -b atley/android-foundation
```

Expected remotes:

```
origin    https://github.com/atleyllc/kv4p-ht.git
upstream  https://github.com/VanceVagell/kv4p-ht.git
```

This workspace was configured that way on 2026-09-24. The development branch is `atley/android-foundation`.

## Sync changes from upstream

```bash
git fetch upstream
git checkout atley/android-foundation
git merge upstream/main
```

Do not rebase onto `upstream/main` unless you intend to rewrite this fork's commits. Never force-push to `upstream`. After a merge, rebuild the Android app and firmware from the same revision.

## Required tools and versions

| Tool | Project / CI version | Notes |
|------|----------------------|-------|
| JDK | **17** documented and used in CI (Zulu) | Homebrew OpenJDK **21.0.11** successfully built this baseline |
| Gradle Wrapper | **9.2.1** | Do not replace the wrapper |
| Android Gradle Plugin | **9.0.1** | |
| Android `minSdk` / `targetSdk` | 26 / 36 | |
| Android SDK | Platforms 34–36 present on this machine | Create `android-src/KV4PHT/local.properties` with `sdk.dir=...` |
| PlatformIO | **6.1.18** | Preferred firmware build |
| espressif32 platform | **6.10.0** | From `microcontroller-src/platformio.ini` |
| Arduino ESP32 package | **2.0.17** | If using Arduino IDE; 3.x is not supported yet |
| Python | 3.12 used here | Required for PlatformIO |

Project-local Android config:

```
android-src/KV4PHT/local.properties
sdk.dir=/Users/KAT/Library/Android/sdk
```

`local.properties` is gitignored.

## Android build

```bash
export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || echo /opt/homebrew/opt/openjdk@21)"
export ANDROID_HOME="$HOME/Library/Android/sdk"
cd android-src/KV4PHT
./gradlew assembleDebug
```

Debug APK:

`android-src/KV4PHT/app/build/outputs/apk/debug/app-debug.apk`

Release APK (unsigned unless you add a private key):

`android-src/KV4PHT/app/build/outputs/apk/release/app-release-unsigned.apk`

CI debug builds use the intentionally public key in `android-src/KV4PHT/config/debug.keystore`. Never use that key for a store release. See `android-src/KV4PHT/config/README.md`.

## Firmware build

Open `microcontroller-src/` (the folder that contains `platformio.ini`), not the repo root.

```bash
export PATH="$HOME/Library/Python/3.12/bin:$PATH"
cd microcontroller-src
pio run -e esp32dev -e esp32dev-release
```

Firmware output:

- Debug: `microcontroller-src/.pio/build/esp32dev/firmware.bin`
- Release: `microcontroller-src/.pio/build/esp32dev-release/firmware.bin`

Arduino IDE steps remain in `microcontroller-src/README.md`. Do not flash hardware without explicit authorization.

The APK also bundles last-release images in `android-src/KV4PHT/app/src/main/res/raw/`:

- `bootloader.bin`
- `partitions.bin`
- `boot_app0.bin`
- `firmware_v17.bin`

PR CI overwrites `firmware_v17.bin` with the firmware built from the PR commit. Local source work should treat the bundled raw image and the PlatformIO output as potentially different until they are rebuilt together.

## Tests and lint

```bash
cd android-src/KV4PHT
./gradlew test
./gradlew lint

cd ../../microcontroller-src
pio test -e native-tests
```

Upstream Android CI runs `./gradlew check -x lint` and `./gradlew build -x lint`. Lint currently fails on an existing `MissingPermission` finding in `RadioAudioService.sendScheduledBeacon()`. Do not hide that error; record it.

There are no app instrumented tests. `usbSerialForAndroid` instrumented tests require physical USB hardware.

## App and firmware compatibility

From the root `README.md`:

> Compiling this repo yourself means building **both** the ESP32 firmware and the Android app from the same git revision.
>
> The firmware resource bundled in the Android app is from the last public release and is not updated until the next one. In a source build there is also no firmware compatibility check, so the app and radio will still connect even if they are out of sync.

Current versions:

- App `versionName` `2.0.0.1`, `versionCode` 54
- `FirmwareUtils.PACKAGED_FIRMWARE_VER = 17`
- Firmware `FIRMWARE_VER = 17`
- Protocol vendor prefix `"KV4P"`, `KV4P_PROTOCOL_VERSION = 0x01`

`RadioAudioService.validateHello()` rejects firmware older than the packaged version and rejects a missing radio module (`radioModuleStatus == 'x'`). Source builds can still talk to mismatched newer or same-major firmware if HELLO parses.

**Do not flash a locally built firmware.bin onto hardware unless the matching Android build is installed and the user has authorized flashing.**

## Hardware-testing checklist

None of the following were verified in the 2026-09-24 baseline environment. They require a physical KV4P-HT and, where noted, RF authorization.

1. USB attach, permission dialog, and HELLO handshake
2. BLE KISS fallback when USB is absent
3. RX audio to speaker, wired headset, and Bluetooth playback
4. PTT from on-screen button and physical PTT (if the board reports `FEATURE_HAS_PHY_PTT`)
5. Mic capture to RF (only with explicit RF authorization; prefer a dummy load)
6. Frequency entry, memory tune, offset, CTCSS TX, software CTCSS RX
7. Scan advance from firmware squelch reports
8. APRS position beacon, message TX/RX, optional digipeat
9. In-app firmware flash over USB (explicit authorization required)
10. USB detach/reconnect and RepeaterBook USB-conflict behavior
11. High/low power GPIO on boards that expose `pinHl`
12. VHF vs UHF NVS board-config detection

Until those are run, do not claim radio behavior has been tested.

## Known baseline issues

Recorded from the untouched upstream tree plus local tool versions. See [BASELINE.md](BASELINE.md) for exact commands.

- `./gradlew lint` fails: 1 error (`MissingPermission` at `RadioAudioService.java:352`), 330 warnings. Upstream CI skips lint.
- JDK 21 compiling Java 8 bytecode produces obsolete source/target warnings.
- AGP 9 deprecation warnings: `android.builtInKotlin=false`, `android.newDsl=false`, and related flags.
- `nav_graph.xml` references `FirstFragment` / `SecondFragment`, which do not exist.
- Room uses `fallbackToDestructiveMigration(true)`.
- `device_filter.xml` includes USB ID 9114/33041 that `RadioAudioService.isESP32Device()` does not accept.
- No dual-band hardware exists. DCS is not implemented. Scanning is Android-owned.
- Bluetooth microphone TX is not implemented; firmware has classic Bluetooth SPP but Android does not use it.
- Source builds can connect with unmatched firmware.

## Branding

Do not rename `applicationId` `com.vagell.kv4pht` yet. See [IDENTIFIERS.md](IDENTIFIERS.md).
