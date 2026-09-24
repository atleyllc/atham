# kv4p-ht

Open source handheld ham radio project kv4p HT

Please see the main project site: https://kv4p.com

## Atley KV4P fork

This checkout is the Atley LLC fork of the official project. The provisional working name is **Atley KV4P**. Vance Vagell and the kv4p HT contributors remain the upstream authors. The project stays GPL-3.0.

- Official upstream: https://github.com/VanceVagell/kv4p-ht
- This fork: https://github.com/atleyllc/kv4p-ht
- Development branch: `atley/android-foundation`

Fork documentation (does not replace the upstream build steps below):

- [AGENTS.md](AGENTS.md) — rules for coding agents
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — remotes, tools, build and test commands
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — Android and firmware assessment
- [docs/LICENSING.md](docs/LICENSING.md) — GPL and third-party licenses
- [docs/IDENTIFIERS.md](docs/IDENTIFIERS.md) — application IDs and branding
- [docs/BASELINE.md](docs/BASELINE.md) — recorded baseline build results
- [docs/ATLEY_ANDROID_ROADMAP.md](docs/ATLEY_ANDROID_ROADMAP.md) — staged modernization plan

## Building from source

The site above is for installing a public release. Compiling this repo yourself means building **both** the ESP32 firmware and the Android app from the same git revision.

The firmware resource bundled in the Android app is from the last public release and is not updated until the next one. In a source build there is also no firmware compatibility check, so the app and radio will still connect even if they are out of sync. Flash the firmware and build the Android app from the same commit.

### Clone

```bash
git clone https://github.com/VanceVagell/kv4p-ht.git
cd kv4p-ht
```

### Firmware (ESP32)

See [microcontroller-src/README.md](microcontroller-src/README.md). PlatformIO is the preferred environment; Arduino IDE is documented there too.

Open `microcontroller-src/` as the project folder (it contains `platformio.ini`). Do not open the repo root in PlatformIO.

### Android app

Use JDK 17. Open `android-src/KV4PHT/` in Android Studio, or:

```bash
cd android-src/KV4PHT
./gradlew assembleDebug
```

Debug APK: `android-src/KV4PHT/app/build/outputs/apk/debug/app-debug.apk`.

### Hardware and other docs

- PCB files: `pcb/`
- 3D-printed cases: `3d-print-case/`
- Common problems: [FAQ.md](FAQ.md)
