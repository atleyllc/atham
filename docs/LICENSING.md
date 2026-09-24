# Licensing and attribution

## Upstream

The official project is **kv4p HT** by Vance Vagell and contributors:

- https://github.com/VanceVagell/kv4p-ht
- License: GNU GPL version 3 (`LICENSE` in the repository root)

This Atley LLC checkout is a fork. The name **AtleyHT** does not replace upstream authorship. Do not remove copyright headers, `LICENSE`, or protocol/vendor identifiers that credit KV4P.

## What is upstream vs Atley

**Upstream (Vance Vagell and contributors), unless a file header says otherwise:**

- Entire tree at `6f3265e81abf64e812de7c653486c79e1903b2e8`
- Android app under `android-src/KV4PHT` except files whose headers name Atley LLC
- Firmware under `microcontroller-src`
- PCB, case, website-src, and FAQ content

**Atley fork additions (this branch):**

- `AGENTS.md`
- `docs/*` (development, architecture, licensing, identifiers, baseline, roadmap)
- README fork notice
- Connection-state types under `com.vagell.kv4pht.radio.RadioConnection*`
- Wiring in `RadioAudioService` and `MainActivity` that publishes/displays that state
- Connection-state strings in `strings.xml`

Modified upstream files must keep the original copyright and note the modification date and Atley LLC.

## GPL implications for APKs

This program is a GPL-3.0 covered work. If you convey an APK (object code) you must also convey Corresponding Source under GPL-3.0. Practical options:

1. Ship the APK with a written offer (valid at least three years) pointing to the public Git repository and commit hash used to build it.
2. Publish the exact source tag/commit alongside every distributed APK.
3. Include source on the same download page or release asset.

Also required:

- Preserve copyright and license notices.
- Mark modified versions as modified (this fork already does).
- Do not add further restrictions.
- Installation information if you convey a User Product where you retain the only install path. In-app USB flashing already allows users to load firmware; keep that path available.

The public CI debug keystore in `android-src/KV4PHT/config/` is **not** a production signing key and provides no authenticity.

## Third-party dependencies

Do not add a dependency unless its license is GPL-3.0-compatible (GPL-3, Apache-2.0, MIT, BSD-2/3, LGPL with appropriate linking, etc.).

### Android (`android-src/KV4PHT/app/build.gradle`)

| Dependency | Version | Typical license | Notes |
|------------|---------|-----------------|-------|
| `:usbSerialForAndroid` | project (mik3y/usb-serial-for-android lineage) | Apache-2.0 / original Google+mike wakerly headers | Vendored |
| AndroidX libraries | various | Apache-2.0 | |
| Material | 1.14.0 | Apache-2.0 | |
| Room | 2.8.4 | Apache-2.0 | |
| commons-math3 / commons-lang3 | 3.6.1 / 3.20.0 | Apache-2.0 | |
| ZXing | 3.5.4 | Apache-2.0 | |
| Play Services Location | 21.4.0 | Google Play services terms + Apache client | Already upstream; keep optional/local function if possible |
| Lombok | 1.18.46 | MIT | |
| esp32-flash-lib | 1.1.17 | Check Maven POM before changing | Already upstream |
| slf4android | 0.1.8 | Check Maven POM before changing | Already upstream |
| JUnit / Espresso | test | EPL-1.0 / Apache-2.0 | Test-only |

### Firmware (`microcontroller-src/platformio.ini`)

| Library | Pin | Notes |
|---------|-----|-------|
| fatpat/arduino-dra818 | commit `89582e3` | Required SA818 definitions |
| pschatzmann/arduino-audio-tools | v1.2.3 | Confirm GPL compatibility before upgrading |
| pschatzmann/adpcm | v1.2.1 | |
| dkaukov/esp32-afsk | ^0.1.3 (resolved 0.1.4 here) | KISS AFSK TNC |

Arduino core and Espressif toolchain are installed by PlatformIO (`espressif32 @ 6.10.0`).

## Privacy

No analytics, accounts, advertising, or telemetry should be added. RepeaterBook uses the user's existing website session in a WebView; that is an upstream feature, not an Atley account system.
