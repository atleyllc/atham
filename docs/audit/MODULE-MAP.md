# KV4P preservation map

Source commit audited: `6f3265e81abf64e812de7c653486c79e1903b2e8`.  
Application ID remains `com.vagell.kv4pht`. This map records files that exist. It does not add classes.

A module is a future ATHAM workspace. Nothing here is a separate Gradle module.

## Shared, used by more than one workspace

| Existing code | Role |
| --- | --- |
| `data/AppDatabase.java` | Room database `kv4pht-db`, version 7. Entities: `AppSetting`, `ChannelMemory`, `APRSMessage` |
| `data/migrations/MigrationFrom1To2.java` through `MigrationFrom6To7.java` | The only migrations |
| `data/AppSetting.java` | Callsign and radio preferences, including APRS beacon and digipeat switches |
| `radio/RadioAudioService.java` | USB/BLE session, RX/TX audio, PTT, scan, APRS send and receive |
| `radio/RadioModuleController.java` | Desired radio state for this one device. Not a multi-module session owner |
| `radio/RadioMode.java` | `UNKNOWN`, `STARTUP`, `RX`, `TX`, `SCAN`, `BAD_FIRMWARE`, `FLASHING` |

There is no `SessionCoordinator`, no `RadioCapabilities`, and no QSO type.

## By future module

| Module | In this tree | Preserve and wrap | Not present |
| --- | --- | --- | --- |
| Home / Station | No station screen | `ui/MainActivity.java` is the current launcher and the place a later shell would start | Station overview, alerts, guided launchers |
| Radio | Voice tab in `res/menu/bottom_nav_menu.xml` | `RadioAudioService`, `RadioModuleController`, `RadioMode`, `Protocol`, `ui/MainActivity.java` scan and PTT, `data/ChannelMemory.java`, `ui/AddEditMemoryActivity.java`, `ui/MemoriesAdapter.java`, `ui/FindRepeatersActivity.java`, `ui/ToneHelper.java` | VFO A/B model, shared QSO draft |
| VoiceLog | Nothing | Radio frequency and mode can be read later from the service. No transcript or draft exists | Speech recognition, callsign extraction, QSO review |
| Digital | Nothing | USB audio inside `RadioAudioService` is voice PCM for this radio, not a sound-card modem | FT8, FT4, WSPR, JS8, PSK31, RTTY |
| Packet | APRS chat tab | `javAX25/ax25` AFSK modem and packet types, `aprs/parser`, `data/APRSMessage.java`, `ui/APRSAdapter.java`, KISS framing in `radio/Protocol.java` | Connected AX.25 terminal, BBS, frame inspector, Winlink |
| Mail | Nothing | Packet transport above is the only related code, and it is APRS | Winlink mailbox, forms, Telnet CMS, RMS Packet |
| Mesh | Nothing | None | Reticulum, LXMF, LXST, RNode |
| Spectrum | Nothing | Demodulated RX audio exists. No FFT and no IQ source | Audio spectrum, waterfall, panadapter |
| Log | Nothing | `APRSMessage` is an APRS packet record, not a contact log | Unified QSO database, ADIF, recordings |
| Maps | Location permission only | `ACCESS_FINE_LOCATION` in `AndroidManifest.xml`. APRS position packets in `aprs/parser/Position.java` | Map view, offline tiles, gateway or repeater map |
| Tools | Nothing | `aprs/parser/GridConverter.java` converts APRS positions. It is not a field-tool workspace | Band plan, propagation, calculators, clock-quality service |
| Devices | Firmware and connection screens | `radio/UsbSerialRadioTransport.java`, `radio/BleKissRadioTransport.java`, `radio/RadioTransport.java`, `radio/ConnectionController.java`, `firmware/FirmwareUtils.java`, `ui/FirmwareActivity.java`, `ui/SettingsActivity.java` | Capability profile, regulatory profile, diagnostics bundle |

## Legacy recovery

Keep these reachable as KV4P Classic until a later Radio slice is hardware-tested. This package does not change the launcher.

- `ui/MainActivity.java`
- `res/layout/activity_main.xml`
- `res/menu/bottom_nav_menu.xml` (Voice and APRS chat)

## Leave untouched in this package

Firmware under `microcontroller-src`, PCB files, the 3D-printed case, and `website-src` stay as upstream KV4P material. No Android activity, navigation graph, or application ID change belongs in this documentation package.
