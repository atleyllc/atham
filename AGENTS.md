# Agent instructions for AtleyHT

This repository is a GPL-3.0 fork of [kv4p HT](https://github.com/VanceVagell/kv4p-ht) by Vance Vagell and contributors. The working product name is **AtleyHT**. Preserve visible upstream credit.

Read [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/LICENSING.md](docs/LICENSING.md), and [docs/ATLEY_ANDROID_ROADMAP.md](docs/ATLEY_ANDROID_ROADMAP.md) before making product changes.

## License and attribution

- Keep `LICENSE` (GPL-3.0) and existing copyright headers.
- New source files that belong to this program must also be GPL-3.0-compatible and name both Vance Vagell and Atley LLC where the file is based on upstream work.
- Do not remove or obscure upstream authorship.
- Do not add dependencies with licenses incompatible with GPL-3.0.
- Distributing APKs requires Corresponding Source. See [docs/LICENSING.md](docs/LICENSING.md).

## Git remotes

Keep these remotes:

- `origin` = this fork (`https://github.com/atleyllc/atleyht.git`)
- `upstream` = official project (`https://github.com/VanceVagell/kv4p-ht.git`)

Never force-push, rewrite upstream history, or change the `upstream` remote URL. Prefer upstream-compatible improvements when reasonable, and note changes that could be submitted back.

## Radio-critical code

Do not casually rewrite working USB, audio, PTT, firmware, protocol, or APRS code. The following must stay byte-compatible with firmware unless a paired firmware change is explicitly requested and tested:

- `android-src/KV4PHT/app/src/main/java/com/vagell/kv4pht/radio/Protocol.java`
- `android-src/KV4PHT/app/src/main/java/com/vagell/kv4pht/radio/RadioModuleController.java`
- `android-src/KV4PHT/app/src/main/java/com/vagell/kv4pht/radio/ImaAdpcm.java`
- `android-src/KV4PHT/app/src/main/java/com/vagell/kv4pht/radio/RadioAudioService.java` handshake and transport paths
- `android-src/KV4PHT/app/src/main/java/com/vagell/kv4pht/firmware/FirmwareUtils.java`
- `microcontroller-src/kv4p_ht_esp32_wroom_32/protocol.h`
- firmware audio frame sizes in `microcontroller-src/kv4p_ht_esp32_wroom_32/globals.h`

Wrap existing Java components instead of rewriting them without evidence.

## App and firmware compatibility

- Source builds must keep the Android app and ESP32 firmware from the **same git revision**.
- `FirmwareUtils.PACKAGED_FIRMWARE_VER` and firmware `FIRMWARE_VER` are currently **17**.
- Custom source builds can bypass public-release compatibility protection. Treat version matching as safety-critical.
- Separate UI changes from protocol and firmware changes.

## Hardware and RF safety

- Never flash physical hardware without explicit user authorization.
- Never transmit RF unless the user explicitly authorizes a hardware test.
- Do not invent hardware-test results. If a path needs a KV4P-HT, say so.
- Do not claim radio behavior was verified unless actual hardware was used.

## Product and privacy

- Keep the app local-first and usable without an account or internet connection.
- Do not add analytics, advertising, accounts, cloud services, or telemetry.
- Never log message contents, precise location, callsigns, recordings, or other sensitive data unless the user has an explicit, user-controlled reason.

## Engineering practice

- Inspect before editing.
- Make small, reviewable commits. Keep unrelated changes out of each commit.
- Prefer Kotlin for **new** Android code. Do not migrate the project to React Native, Expo, Flutter, or a web wrapper.
- Jetpack Compose may be considered later for new screens only; do not convert the existing UI in the first slices.
- Build and test before committing:
  - `cd android-src/KV4PHT && ./gradlew assembleDebug test`
  - `cd microcontroller-src && pio test -e native-tests` when firmware or protocol files change
- Do not suppress build errors. Do not use placeholders in radio-critical paths.
- Do not immediately rename packages, application IDs, USB IDs, protocol IDs, or firmware IDs. See [docs/IDENTIFIERS.md](docs/IDENTIFIERS.md).
- Do not change the app icon yet.

## Existing contributor instructions

Upstream build instructions in `README.md` and `microcontroller-src/README.md` remain authoritative for the original project flow. Extend them; do not overwrite them.
