# AtleyHT Android roadmap

Working name: **AtleyHT**. Repository: `atleyllc/atleyht`. Foundation: official kv4p HT (`VanceVagell/kv4p-ht`), GPL-3.0. This is not a greenfield rewrite.

Hardware facts that constrain the plan:

- Radio module is SA818 VHF **or** UHF, never dual-band.
- DCS is not implemented (`sa818.group(..., 0, 0)`).
- Scanning, memories, groups, and skip flags are Android-owned.
- Firmware reports a single `freq_rx` / `freq_tx` and squelch bit; no priority-scan engine in firmware.
- Voice is 16 kHz IMA ADPCM over USB/BLE KISS. Bluetooth **playback** is an Android audio-routing problem. Bluetooth **microphone TX** is not implemented and may be restricted by Android.
- Firmware has classic Bluetooth SPP; Android does not use it.
- High/low power requires `pinHl` / `FEATURE_HAS_HL`.
- Physical PTT is reported by firmware; Android keys RF.
- `HOST_STATE_TX_ALLOWED` is a safety interlock. Keep it.

Risk scale: **Low** / **Medium** / **High** (protocol, RF, or data-loss).

---

## Stage A — Foundation

| Feature | App / FW | Hardware support | Existing code | Risk | Tests | Dependencies | Order |
|---------|----------|------------------|---------------|------|-------|--------------|-------|
| Reproducible builds | App + FW docs | n/a | `README.md`, Gradle wrapper, `platformio.ini`, CI | Low | Record `assembleDebug`, `pio run`, hashes | JDK, PlatformIO 6.1.18 | A1 done (docs) |
| Architecture documentation | Docs | n/a | this file, `docs/ARCHITECTURE.md` | Low | Review against source | Inspection | A1 done |
| Testing seams / connection-state model | App | n/a | `RadioMode`, `RadioAudioService` callbacks | Low | Unit tests on resolver | None | A2 done this branch |
| Error and diagnostic reporting | App | Uses existing debug cmds `0x01`–`0x05` | `handleParsedCommand` firmware logs | Medium | Unit + USB reconnect fixture later | Connection state | A3 |
| Firmware compatibility handling | App (+ docs) | HELLO `ver` | `validateHello()`, `PACKAGED_FIRMWARE_VER` | High | Protocol tests; do not loosen source-build bypass without a visible warning | Version constants | A4 |
| Safe hardware-testing procedure | Docs + checklist | Physical radio | `docs/DEVELOPMENT.md` | High if ignored | Manual only | Dummy load, user auth | A5 |

**A3 notes:** Export logs without callsigns, message bodies, or precise location. Include connection state, firmware version, USB VID/PID, and last error code only.

**A4 notes:** Source builds currently connect even when bundled `firmware_v17.bin` differs from `pio` output. Add a non-blocking in-app warning when `Hello.ver` != packaged version, without changing HELLO bytes.

---

## Stage B — Interface shell

All items are **app-only** unless noted. Do not restyle by rewriting `RadioAudioService`.

| Feature | App / FW | Hardware | Existing code | Risk | Tests | Dependencies | Order |
|---------|----------|----------|---------------|------|-------|--------------|-------|
| Updated navigation | App | n/a | `MainActivity.showScreen()`, dead `nav_graph.xml` | Medium | UI smoke | Connection state | B1 |
| Modern visual system | App | n/a | `res/values`, `Theme.KV4PHT` | Low | Screenshot + a11y contrast | B1 | B2 |
| Clear connection state | App | USB/BLE | `RadioConnectionState` + `moduleStateLabel` | Low | Resolver tests | A2 | Started |
| Clear RX/TX state | App | DeviceState flags | `moduleStateChanged`, PTT drawables | Low | Service callback tests later | A2 | Started |
| Primary radio screen | App | n/a | `activity_main.xml` | Medium | Manual PTT hit-target | B1–B2 | B3 |
| Settings organization | App | n/a | `SettingsActivity` | Low | Settings persist in Room | Room keys | B4 |
| Accessibility | App | n/a | sparse `contentDescription`s | Medium | TalkBack pass | B3 | B5 |
| Large touch targets | App | n/a | 36dp menu, PTT drawable | Medium | Min 48dp | B3 | B5 |
| Dark theme for radio use | App | n/a | `values-night` exists | Low | Night mode | B2 | B6 |

Compose is **not** required for Stage B. Prefer XML/Views (and later Kotlin + Views). Compose only if a new screen cannot reasonably share the existing theme.

---

## Stage C — Core HT workflow

| Feature | App / FW | Hardware support | Existing code | Risk | Tests | Dependencies | Order |
|---------|----------|------------------|---------------|------|-------|--------------|-------|
| Frequency entry | App | SA818 range from HELLO | `tuneToFreq()`, `activeFrequency` | Medium | Invalid/out-of-band rejected | TX limits | C1 |
| VFO behavior | App + FW NVS | Single VFO | `memoryId = -1`, firmware NVS restore | Medium | Tune + reconnect keeps VFO | Desired-state | C2 |
| Memory channels | App | n/a | `ChannelMemory`, `AddEditMemoryActivity` | Medium | Room CRUD | Migrations | C3 |
| Favorites | App | n/a | none; can be a memory flag/group | Low | Room migration | C3 | C4 |
| Banks / zones | App | n/a | `memory.group` already exists | Low | Group filter | C3 | C5 |
| Scan lists | App | Firmware squelch only | `setScanning()`, `skipDuringScan` | Medium | Mock squelch advances | DeviceState `SQUELCHED` | C6 |
| Priority scan | App (maybe FW later) | **Not in firmware.** Single RX. App can dual-watch by time-slicing `freq_rx` | `RadioAudioService` scan loop | High (missed calls, choppy audio) | Hardware timed scan | C6, user-visible limitation | C7 after evidence |
| Nuisance delete | App | n/a | `skipDuringScan` is close | Low | Session vs persisted skip | C6 | C8 |
| Repeater offset | App | SA818 independent TX/RX freq | `ChannelMemory.offset`, `offsetKhz` | Medium | TX freq shown only while TX | C1 | C3 already partial |
| CTCSS | App + FW | TX via module; RX via `CtcssDetector` | `ToneHelper`, `ctcss_tx` / `ctcss_rx` | Medium | Tone tables | Do not claim DCS | C9 |
| DCS | — | **Unsupported** | `sa818.group` zeros | n/a | n/a | Requires firmware + SA818 verification | Do not schedule |
| Power / BW / squelch / filters | App + FW | Power needs `hasHL`; BW 12.5/25; squelch is software + SQ pin; filters split module/DSP | Settings + `HostDesiredState` flags | Medium | HELLO feature bits | Do not send unsupported flags | C10 |
| Hardware-button PTT | App + FW | `FEATURE_HAS_PHY_PTT` | `DEVICE_STATE_PHYS_PTT_DOWN` | High | Hardware | Sticky PTT setting | C11 |

---

## Stage D — Audio and reliability

| Feature | App / FW | Hardware / OS | Existing code | Risk | Tests | Dependencies | Order |
|---------|----------|---------------|---------------|------|-------|--------------|-------|
| Audio routing controls | App | Android audio APIs | `AudioTrack`, `AudioFocus` | Medium | Speaker vs wired headset | No protocol change | D1 |
| Bluetooth microphone | App investigation | Often blocked for VoIP/USB hybrids; may need `VOICE_COMMUNICATION` and BT SCO | `AudioRecord` `MIC` only | High | Device matrix; may be **impossible** | Honest "unsupported" if OS refuses | D2 investigate only |
| Bluetooth playback | App | A2DP usually works for `USAGE_MEDIA` | Current `AudioTrack` usage | Medium | BT speaker/headset RX | D1 | D3 |
| Wired headset | App | Analog/USB-C headset | System routing | Medium | Plug/unplug while RX | D1 | D4 |
| Speaker / earpiece | App | `AudioManager` | none | Medium | Earpiece legal/safety (don't hide TX) | D1 | D5 |
| PTT timeout | App + FW | Already 180 s app / 200 s FW | `RUNAWAY_TX_TIMEOUT_SEC`, `TxWatchDog` | High if weakened | Unit + hardware | Keep both | D6 harden UI |
| Stuck-transmit protection | App + FW | Watchdog releases host PTT | `releaseHostPtt()` | High | Existing firmware tests | Do not disable | D6 |
| Reconnect / USB recovery | App | USB detach | `reconcileConnections()`, `radioMissing()` | Medium | Unplug/replug | Connection state | D7 |
| Diagnostics export | App | n/a | firmware debug commands | Medium | Redaction tests | A3 | D8 |

Classic Bluetooth SPP on the ESP32 is **not** an Android headset path. Treat it as a future TNC/host option, not a mic.

---

## Stage E — APRS and packet

| Feature | App / FW | Hardware | Existing code | Risk | Tests | Dependencies | Order |
|---------|----------|----------|---------------|------|-------|--------------|-------|
| Improved messaging | App | Firmware KISS TNC | `sendChatMessage()`, `APRSAdapter` | Medium | Parser tests; no RF in CI | Callsign setting | E1 |
| Conversation history | App | n/a | Room `APRSMessage` | Low | DAO tests | Privacy: no log of bodies in logcat | E2 |
| Export / delete | App | n/a | DAOs exist | Low | File export redaction | E2 | E3 |
| Mapping | App | Location permission | Position packets, Play Services | Medium | Offline map option preferred | Do not require account | E4 |
| Beacon controls | App | Location | `aprsBeaconPosition`, 5 min scheduler | High (on-air) | UI disable default | TX allowed | E5 |
| KISS TNC handoff | App + FW already | USB serial is already KISS | `HOST_STATE_ENABLE_STATUS_REPORTS` | High | APRSdroid coexistence | USB exclusive access | E6 |
| APRSdroid interop | App | USB conflict possible (see FAQ RepeaterBook) | none | High | Manual | May need accessory protocol, not a rewrite | E6 |
| Digipeater / packet status | App | `digipeatPackets` | `maybeDigipeat()` | High | Unit tests on WIDE1-1 logic | Legal/operator education | E7 |

Do not move AFSK back to Android `javAX25` unless firmware AFSK is proven insufficient. Firmware TNC is the supported path.

---

## Stage F — Atley ecosystem

| Feature | App / FW | Hardware | Existing code | Risk | Tests | Dependencies | Order |
|---------|----------|----------|---------------|------|-------|--------------|-------|
| Optional VoiceLog handoff | App | n/a | none | Low | Contract tests | Local intent or file, no cloud | F1 |
| Contact logging | App | n/a | none | Low | Local DB | F1 | F2 |
| Frequency / mode metadata | App | DeviceState | `freq_rx`, BW, tones | Low | Snapshot fields | Connection + radio state | F2 |
| Timestamp / location handoff | App | Location optional | Play Services already used for APRS | Medium | Location omitted unless user enables | Same privacy rules | F3 |
| Local integration contract | Docs | n/a | none | Low | Example extra keys | No required account | F1 |
| No cloud account | Policy | n/a | already true | n/a | Review new deps | Permanent constraint | always |

Suggested local contract (draft, not implemented): an explicit user action sends `application/vnd.atley.voicelog-contact+json` or a documented `Intent` extra set (`frequencyHz`, `mode`, `timestampUtc`, `gridOptional`, `source=atleyht`). No automatic upload.

---

## Implementation architecture

See `docs/ARCHITECTURE.md` for the full argument.

- Keep proven native USB, firmware, APRS, and audio Java code.
- Improve the interface gradually.
- New Android code: Kotlin when the first new module is added; this foundation slice stayed Java to avoid a toolchain change.
- Compose: optional later for new screens only.
- Wrap `RadioAudioService` with state models and small controllers; do not replace it.
- Stay local-first.

## Branding

See `docs/IDENTIFIERS.md`. No package, icon, USB, or protocol rename in Stages A–B.

## Upstream contribution candidates

These are likely welcome upstream if kept protocol-compatible:

- Connection-state model and tests
- Lint `MissingPermission` fix on the beacon scheduler
- USB ID alignment between `device_filter.xml` and `isESP32Device()`
- In-app warning when source-build firmware does not match packaged version
- Accessibility labels on RX/TX state

Keep Atley-specific VoiceLog and branding out of upstream PRs.
