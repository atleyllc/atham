# Source audit

Checked out source, not an earlier written claim.

| Item | Value |
| --- | --- |
| Fork | `atleyllc/atham` |
| Origin | `https://github.com/atleyllc/atham.git` |
| Upstream | `https://github.com/VanceVagell/kv4p-ht.git` |
| Branch | `main` |
| Commit | `6f3265e81abf64e812de7c653486c79e1903b2e8` |
| Subject | Merge pull request #466 from dkaukov/fix/audio-focus |
| Match | `HEAD` equals `upstream/main` |
| License | GPL-3.0 (`LICENSE`) |
| Application ID | `com.vagell.kv4pht` |
| Version | `versionName "2.0.0.1"`, `versionCode 54` |
| Language | Java. No Kotlin sources under `android-src/KV4PHT/app/src/main`. |

`atleyllc/atham` is a GitHub fork. `gh repo view` reports `isFork: true` and parent `VanceVagell/kv4p-ht`. An older name `atleyht` resolves to this same repository.

## Corrected assumptions

| Earlier claim | What this commit contains |
| --- | --- |
| `SessionCoordinator` already exists | No class, file, or reference named SessionCoordinator |
| `RadioCapabilities` already exists | No class or file named RadioCapabilities. `RadioTransport` is a KV4P byte-stream interface, not a capability model |
| QSO entities exist | No QSO, contact-log, or ADIF type. Room entities are only `AppSetting`, `ChannelMemory`, and `APRSMessage` |
| Room database version 11 | `@Database(version = 7)` in `AppDatabase.java` |
| Room migrations 1–7 as a vague range | Six migration classes, `MigrationFrom1To2` through `MigrationFrom6To7`, all registered |

`RadioModuleController` is the Android-side desired-state writer for this radio. It is not a multi-module session owner.

Which of these files belong to each future module is listed in [MODULE-MAP.md](MODULE-MAP.md).

## Room

Database name: `kv4pht-db`. Version 7.

| Migration | Change |
| --- | --- |
| 1 → 2 | Creates `aprs_messages`. Pads 7-character memory frequencies with a trailing zero |
| 2 → 3 | Rebuilds `channel_memories` |
| 3 → 4 | Rebuilds `channel_memories`; `rx_tone` default becomes `None` |
| 4 → 5 | Renames setting `maxFreq` to `max2mTxFreq` |
| 5 → 6 | Normalizes `aprsPositionAccuracy` and `bandwidth` setting values |
| 6 → 7 | Adds `aprs_messages.relay_callsign` |

`ChannelMemory` fields: name, frequency, offset, transmit tone, group, receive tone, offset kHz, skip-during-scan.

## RadioMail, APRS, KISS, AX.25

**RadioMail.** No RadioMail or Winlink types, screens, or dependencies. Nothing in this tree is a Winlink client.

**APRS.** The app has an APRS chat tab (`text_chat_mode` in `bottom_nav_menu.xml`), parser package `com.vagell.kv4pht.aprs.parser`, and Room storage for message, object, position, and weather packets. `RadioAudioService` parses received AX.25 into APRS, sends APRS messages, beacons position, and can digipeat when the operator enables it. That is APRS messaging and beaconing, not a connected AX.25 terminal, BBS, or packet node.

**KISS.** `Protocol` frames standard KISS data and KV4P vendor commands inside KISS `SETHARDWARE`. `UsbSerialRadioTransport` and `BleKissRadioTransport` carry those frames to the radio. `ProtocolKissTest` covers framing. The Android app is not a general KISS TNC client for an arbitrary external TNC. Firmware in `microcontroller-src` can also expose the radio as a KISS serial device to a computer; that path was not exercised.

**AX.25.** `javAX25` contains an AFSK 1200 modem and packet types. The app sends and receives AX.25 frames for APRS through `RadioAudioService.txAX25Packet`. There is no connected-mode terminal, SABM session, or BBS client in the Java UI.

## What is present

| Area | Files |
| --- | --- |
| Live radio | `radio/RadioAudioService.java`, `RadioModuleController.java`, `RadioMode.java` (`UNKNOWN`, `STARTUP`, `RX`, `TX`, `SCAN`, `BAD_FIRMWARE`, `FLASHING`) |
| USB and BLE | `UsbSerialRadioTransport.java`, `BleKissRadioTransport.java`, `ConnectionController.java` |
| Firmware | `firmware/FirmwareUtils.java`, `ui/FirmwareActivity.java` |
| Memories and repeaters | `data/ChannelMemory.java`, `ui/AddEditMemoryActivity.java`, `ui/FindRepeatersActivity.java`, `ui/ToneHelper.java` |
| Current UI | `ui/MainActivity.java` launcher. Bottom destinations are Voice and APRS chat only |
| Settings | `ui/SettingsActivity.java`, `data/AppSetting.java` |

## What is absent

VoiceLog, a QSO draft, Winlink mail, FT8 or other sound-card modes, Reticulum, an IQ/SDR source, a shared map workspace, a band-plan tool, and a session coordinator. Those names in the product plan are future work.
