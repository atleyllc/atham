# AtleyHT

**AtleyHT** is Atley LLC’s fork of [kv4p HT](https://github.com/VanceVagell/kv4p-ht), the open-source handheld ham radio that pairs an ESP32 + SA818 with an Android phone over USB.

This is not a rewrite from scratch. The official project already has working USB serial, receive and transmit audio, PTT, firmware flashing, radio control, memories, scanning, and APRS. AtleyHT keeps that foundation and turns the Android side into a modern, dependable handheld-radio app.

- This repository: https://github.com/atleyllc/atleyht
- Official upstream: https://github.com/VanceVagell/kv4p-ht
- Hardware and original project site: https://kv4p.com
- License: GNU GPL-3.0. Vance Vagell and the kv4p HT contributors remain the upstream authors. That credit stays.

## What it started as

kv4p HT is a community ham-radio project: phone as the face of the radio, ESP32 as the controller, SA818 as the RF deck. The Android app in this tree is the official one — Java, XML layouts, a foreground `RadioAudioService`, KISS/KV4P protocol, in-app firmware flashing, Room memories, and APRS over the firmware TNC.

AtleyHT began as a straight fork of that repository. The first goal is to prove the radio still works on this fork before larger interface work.

## What AtleyHT is going to do

Build a first-class modern handheld experience on the same hardware, including:

- Clear connection, receive, transmit, scanning, and error states
- A dependable, prominent PTT
- Better frequency, memory, favorites, banks/zones, and scan lists
- Priority scan and nuisance-channel delete where the radio can support them
- Clear repeater offset and tone controls
- Better nearby-repeater discovery
- Bluetooth headset and microphone support where Android allows it
- Better audio routing and signal feedback
- Improved APRS messaging, mapping, history, and export
- A simple beginner mode and an advanced mode
- Accessibility-conscious controls
- Optional local handoff to Atley VoiceLog for logging contacts — no required cloud account

Work is staged. Radio-critical USB, audio, PTT, firmware, and protocol code is not being casually rewritten. The current development branch is `atley/android-foundation`. The detailed plan is in [docs/ATLEY_ANDROID_ROADMAP.md](docs/ATLEY_ANDROID_ROADMAP.md).

## Current test build

The first AtleyHT GitHub release is a **baseline debug APK** so the radio can be checked on a real phone and KV4P-HT **before** the larger UI work.

It is still the official kv4p HT app (`com.vagell.kv4pht`) plus a small connection-state label and this fork’s documentation. Install it, plug in the radio, and confirm USB connect, receive, PTT, memories, and APRS the way you already use kv4p HT.

This APK is a debug build signed with the project’s public CI debug key. It is for testing, not a Play Store release. Source for every APK is this repository at the matching release tag.

## More documentation

- [AGENTS.md](AGENTS.md) — rules for coding agents
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — remotes, tools, build and test commands
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — Android and firmware assessment
- [docs/LICENSING.md](docs/LICENSING.md) — GPL and third-party licenses
- [docs/IDENTIFIERS.md](docs/IDENTIFIERS.md) — application IDs and branding
- [docs/BASELINE.md](docs/BASELINE.md) — recorded baseline build results
- [FAQ.md](FAQ.md) — common hardware problems from upstream

## Building from source

Compiling this repo yourself means building **both** the ESP32 firmware and the Android app from the same git revision.

The firmware resource bundled in the Android app is from the last public kv4p HT release and is not updated until the next one. In a source build there is also no firmware compatibility check, so the app and radio will still connect even if they are out of sync. Flash the firmware and build the Android app from the same commit.

Do not flash hardware or transmit RF unless you intend to. Custom firmware is safety-critical.

### Clone this fork

```bash
git clone https://github.com/atleyllc/atleyht.git
cd atleyht
git remote add upstream https://github.com/VanceVagell/kv4p-ht.git
git checkout atley/android-foundation
```

### Firmware (ESP32)

See [microcontroller-src/README.md](microcontroller-src/README.md). PlatformIO is the preferred environment; Arduino IDE is documented there too.

Open `microcontroller-src/` as the project folder (it contains `platformio.ini`). Do not open the repo root in PlatformIO.

### Android app

Use JDK 17 (JDK 21 also built this fork). Open `android-src/KV4PHT/` in Android Studio, or:

```bash
cd android-src/KV4PHT
./gradlew assembleDebug
```

Debug APK: `android-src/KV4PHT/app/build/outputs/apk/debug/app-debug.apk`.

### Hardware and other docs

- PCB files: `pcb/`
- 3D-printed cases: `3d-print-case/`
- Common problems: [FAQ.md](FAQ.md)
