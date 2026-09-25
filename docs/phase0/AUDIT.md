# Phase 0 audit

Status: review document. No driver interfaces, session coordinator, or new protocols are implemented by this package.

Date: September 2026. Tree: `cursor/aprs-operator-53f2` at `382f713`, plus this audit branch.

Product spec: [ATLEY_HT_MASTER_PLAN.md](../ATLEY_HT_MASTER_PLAN.md). Existing engineering notes: [ARCHITECTURE.md](../ARCHITECTURE.md), [ATLEY_ANDROID_ROADMAP.md](../ATLEY_ANDROID_ROADMAP.md), [LICENSING.md](../LICENSING.md).

## 1. License and distribution

The repository root `LICENSE` is GNU GPL version 3. Upstream is Vance Vagell’s kv4p HT. Atley LLC’s fork must stay GPL-3.0-or-later compatible. A UI rewrite does not drop that obligation. Every APK release must point at the commit that built it. Current practice does that on GitHub releases `atleyht-0.1.x`.

Third-party code already in tree:

| Piece | Where | License posture |
| --- | --- | --- |
| usb-serial-for-android | vendored module | MIT / Apache headers in `other-licenses.txt` |
| AndroidX, Material, Room | Gradle | Apache-2.0 |
| javAX25 parser | `app/src/main/java/com/vagell/kv4pht/javAX25` and `aprs/parser` | Upstream-vendored; keep its notices |
| esp32-afsk | `platformio.ini` `dkaukov/esp32-afsk` | Firmware modem; do not move AFSK back to the phone |
| arduino-audio-tools, adpcm, arduino-dra818 | PlatformIO pins | Confirm before any upgrade |
| Concentus | Gradle `io.github.jaredmdobson:concentus` | Used only to play public-firmware Opus RX |
| Jost font, ZXing, Play Services Location | existing | See `docs/LICENSING.md` and `other-licenses.txt` |

Not in the tree, and not approved for import:

| Engine | Why it is interesting | Gate before any code |
| --- | --- | --- |
| Pat / wl2k-go | MIT Winlink B2F reference | Pin a commit, Android build proof, test vectors, notice in the APK |
| Direwolf | AX.25/APRS reference | GPL; linking keeps the app GPL. Do not copy algorithms until the license file is recorded |
| WSJT-X | FT8 family | GPL plus its own notices. Phase 5, not now |
| JS8Call, FLDigi | later modes | Same ledger. Mode by mode |
| Hamlib | CAT definitions | Optional bridge. Curated native profiles for phones |
| RadioReference, RepeaterBook | frequency data | Contract and attribution. No scrape, no silent mirror |

`docs/LICENSING.md` is the current ledger and is incomplete for future engines. Phase 0 acceptance is a row in that file before the first import, not an import itself.

GPL distribution gap: debug APKs are published. Source tags match. The in-app flasher must keep working so users can load firmware they build. Do not ship a locked boot path.

## 2. Firmware and app interface

USB serial is a KISS TNC at 115200 8N1. The phone does not own the SA818 modem.

Frame layout, identical in `radio/Protocol.java` and `microcontroller-src/kv4p_ht_esp32_wroom_32/protocol.h`:

```
FEND  0xC0
command byte: KISS DATA 0x00, or SETHARDWARE 0x06
vendor commands only: "KV4P" 0x01 <kv4pCommand> <payload>
FEND
```

Escapes: `FESC 0xDB`, `TFEND 0xDC`, `TFESC 0xDD`.

| Command | Direction | Bytes | What it does |
| --- | --- | --- | --- |
| KISS DATA `0x00` | both | raw AX.25 | Firmware AFSK modem. Phone builds and parses frames |
| DEBUG info/error/warn/debug/trace | device to host | `0x01`–`0x05` | Logs |
| HELLO | device to host | `0x06` | Version, band, feature bits (`hasHl`, physical PTT) |
| RX audio Opus | device to host | `0x07` | Public firmware, 48 kHz Opus. App plays it. Source firmware does not send this |
| WINDOW_UPDATE | device to host | `0x09` | Flow control |
| DEVICE_STATE | device to host | `0x0B` | Squelch, RSSI, physical PTT, mode |
| HOST_TX_AUDIO | host to device | `0x0C` | 16 kHz IMA ADPCM microphone |
| RX audio ADPCM | device to host | `0x0C` | Source firmware voice |
| HOST_DESIRED_STATE | host to device | `0x0D` | Frequency, tones, bandwidth, flags |

Host flags that matter (`Protocol.java`):

- `HOST_STATE_RADIO_CONFIG_VALID`
- `HOST_STATE_PTT_REQUESTED`
- `HOST_STATE_RX_AUDIO_OPEN`
- `HOST_STATE_HIGH_POWER`
- `HOST_STATE_TX_ALLOWED` (bit 11). Firmware refuses RF, including KISS DATA, when this is clear
- filters, RSSI, status reports

Transports today:

- `UsbSerialRadioTransport` — what the phone uses
- `BleKissRadioTransport` — firmware GATT exists (`BluedroidBleKissGattStream.h`, hessu BLE-KISS UUIDs). The phone still prefers USB. One USB owner at a time, so APRSdroid cannot share the cable

`RadioAudioService` is the only object that speaks this protocol: tune, PTT, RX playback (Opus and ADPCM), mic TX, KISS AX.25, digipeat, HELLO version check (`FirmwareUtils.PACKAGED_FIRMWARE_VER`). `RadioModuleController` holds desired state. `MainActivity` calls the service directly.

There is no `RadioDriver`, `KissEndpoint`, or `IqSource`. KV4P is hard-wired.

Audio fact that must not regress: bundled `firmware_v17.bin` is Opus on `0x07`. Source firmware is ADPCM on `0x0C`. The app plays both. Do not delete the Opus path while that bin is what users flash.

## 3. KISS, AX.25, and APRS path

```
SA818 audio
  -> esp32-afsk (firmware)
  -> KISS DATA frame on USB
  -> Protocol.parse
  -> RadioAudioService
  -> javAX25 Parser.parseAX25
  -> MainActivity stores APRSMessage
  -> APRS chat UI, station list, and radio-mail reassembly

Operator TX
  -> RadioAudioService.sendChatMessage / beacon / ack
  -> javAX25 Packet.toAX25Frame
  -> txAX25Packet
  -> KISS DATA
  -> firmware AFSK
  -> SA818
```

Gates on every TX path in `txAX25Packet`: radio connected and `isTxAllowed()`. Radio mail adds a confirm dialog and will not send email. It is not Winlink.

What exists:

- Message packets, acks, rejects, delivery column (`AprsOperator`)
- Position beacons, manual only
- Station list and 6-character Maidenhead
- Digipeat when the operator enabled it and TX is allowed (`RadioAudioService` around the digipeat call)
- `ProtocolKissTest` for framing

What does not exist:

- Connected-mode AX.25 (SABM/UA/I/RR)
- BBS or node client
- CSMA (`TXDELAY` / `PERSIST` / `SLOTTIME`). Upstream KV4P PR 470 is not merged. The phone bursts packets
- APRS-IS, IGate, objects as a first-class editor, weather station, telemetry UI
- Map
- Winlink B2F, CMS telnet, RMS session, forms, attachments

Radio mail (`mail/RadioMail.java`, `ui/RadioMailActivity.java`) splits text into `n/m` APRS message lines and files numbered lines addressed to this station into `radio_mail`. That is a local mailbox. The master plan’s Mail module replaces this transport. Keep the mailbox UI ideas. Do not call the current transport Winlink.

## 4. Capability interfaces, mapped to files

These types do not exist yet. Phase 0 only names them and the KV4P mapping. Implementation waits for audit approval.

| Contract | KV4P today | First file to wrap, not rewrite |
| --- | --- | --- |
| `RadioControl` | `RadioModuleController` desired frequency, tone, bandwidth, power, PTT | `radio/RadioModuleController.java` |
| `AudioEndpoint` | ADPCM TX `0x0C`, Opus and ADPCM RX | `RadioAudioService` playback and mic methods |
| `KissEndpoint` | KISS DATA in `Protocol` plus `txAX25Packet` | `radio/Protocol.java` |
| `SignalMetrics` | `COMMAND_DEVICE_STATE` squelch and RSSI | `RadioAudioService` state callback |
| `FirmwareManager` | `ui/FirmwareActivity.java`, esp32-flash-lib | leave the flasher alone |
| `TransmitGate` | `HOST_STATE_TX_ALLOWED`, `isTxAllowed()` | one method every TX caller already hits |
| `IqSource` | none | no KV4P implementation. Spectrum stays labeled “audio” until an IQ driver exists |

`RadioTransport` is the only seam (`UsbSerialRadioTransport`, `BleKissRadioTransport`). A later `Kv4pDriver` should sit on that seam. Feature screens keep calling the service until the wrapper passes the same protocol tests.

QDX, generic CAT, RTL-SDR, and external KISS are empty profiles. The UI must not offer FT8, a wideband waterfall, or ARDOP on a KV4P connection.

## 5. Session coordinator

There is no coordinator. `RadioAudioService` runs voice, scan, APRS, digipeat, and radio mail on one USB pipe. Scan is an app loop. PTT and packet TX both depend on `HOST_STATE_TX_ALLOWED`, but nothing stops scan, voice, and mail from wanting the radio in the same second except accidental single-threading.

Design to approve before code:

- One owner: Voice, Scan, APRS beacon, APRS message, Radio mail, or later Winlink/FT8.
- Taking ownership stops scan and suspends the other TX callers.
- CAT/desired-state writes are serialized on the existing module controller.
- Unkey on cancel, USB unplug, activity destroy, and timeout. Firmware already drops TX when the host clears PTT and when `TX_ALLOWED` clears. The coordinator’s job is to clear those bits on every exit path.
- A session row: who keyed, frequency, why it stopped. No message body in the default log.

Acceptance when it is eventually built: protocol golden test plus a unit test that a second module’s TX call is refused while the first owns the radio, and that unkey runs after a simulated USB removal. Not in this package.

## 6. Database

Room `AppDatabase` version 10. `fallbackToDestructiveMigration(true)` is still on. A missing migration wipes memories, APRS, and mail. Phase 0 does not turn that off until migration tests exist.

| Version | Change |
| --- | --- |
| 1→2 | `aprs_messages` |
| 2→4 | `channel_memories` rebuilt |
| 6→7 | `relay_callsign` |
| 7→8 | `delivery` on APRS messages; acks backfilled |
| 8→9 | `radio_mail` |
| 9→10 | `unread`, `flagged`, `radio_mail_stations` |

Entities today: `AppSetting`, `ChannelMemory`, `APRSMessage`, `RadioMailMessage`, `RadioMailStation`.

The master plan’s `StationIdentity`, `Qso`, `MailSession`, `DeviceProfile`, and `ScanHit` tables are new. They get their own migrations after this audit. Do not overload `radio_mail` into a Winlink message table. Winlink needs message id, MIME parts, and session state that this schema does not have.

Callsign and location still live in `AppSetting` and are read by APRS and mail separately. That is the seed of the shared station model.

## 7. Test harness that exists

| Test | Covers |
| --- | --- |
| `ProtocolKissTest` | KISS framing |
| `ImaAdpcmTest`, `OpusUtilsTest` | Voice codecs. Opus test must not load `RadioAudioService` |
| `RadioConnectionStateResolverTest` | Connection labels |
| `AprsOperatorTest` | Delivery and Maidenhead |
| `RadioMailTest` | Callsign, split, reassembly, search |

No instrumentation test drives a session. No USB-removal fixture. No B2F vectors. Hardware tests stay manual: dummy load, no casual RF, no firmware flash unless the operator asks.

Phase 0 harness work, after approval, is contract tests around the current `Protocol` and `isTxAllowed` gate. Not a new modem.

## 8. Keep, wrap, rewrite, remove

Keep, and do not casually edit:

- `microcontroller-src` KISS and SA818 control
- `Protocol` command bytes and `HOST_STATE_TX_ALLOWED`
- USB serial transport and the firmware flasher
- Opus `0x07` playback while `firmware_v17.bin` is the bundled image
- ADPCM `0x0C` path for source firmware
- javAX25 encode/decode on the phone, AFSK on the ESP32
- Explicit confirm before packet or mail TX
- GPL headers and `LICENSE`

Wrap later:

- `RadioAudioService` behind the contracts in section 4
- `RadioModuleController` as `RadioControl`
- APRS parse path in `MainActivity` so mail, chat, and a future session owner share one decoder

Rewrite only when a phase starts:

- Radio mail’s on-air format, when Phase 3 replaces it with B2F. The mailbox screens can be reused
- `MainActivity` navigation, when the product shell (Radio, Spectrum, Data, Mail, Log) is an approved epic
- Scan, as its own owner under the coordinator

Remove or stop extending:

- Any UI string that implies ARDOP, VARA, or a Winlink session. The device row already says those modes are absent. Keep it that way
- Numbered-line mail as a protocol once B2F exists. Until then it may stay behind the packet mailbox
- Destructive migration once versioned tests cover 7→10

Do not start: QDX, RTL-SDR, FT8, JS8, trunk tracking, RadioReference scraping, or a Linux-in-a-phone DigiPi port.

## 9. Phase 0 task breakdown

Each item is a reviewable change after this audit is accepted. None of them are done here.

| ID | Work | Acceptance |
| --- | --- | --- |
| P0-1 | Dependency ledger rows for Pat, Direwolf, WSJT-X, Hamlib with “not imported” | `docs/LICENSING.md` lists license, owner, and “no code yet” |
| P0-2 | `Kv4pCapabilities` documented as a table the UI can read | Unit test: KV4P profile enables FM, KISS, packet Winlink-later; disables IQ, SSB, ARDOP |
| P0-3 | Session owner interface with a fake clock and a fake transport | Second TX is refused; unkey flag sets on cancel and on disconnect |
| P0-4 | Migration tests for 7→8, 8→9, 9→10 on a temp database | Tests fail if a column is missing; destructive fallback unchanged until those tests exist |
| P0-5 | Redaction checklist for a future support bundle | Fixture log contains no callsign, body, or GPS |
| P0-6 | Shell map only: Radio, Data, Mail entry points to existing screens | No new protocol. Spectrum entry hidden on KV4P or labeled audio-only |
| P0-7 | Winlink epic spec | Names Pat commit candidate, telnet fixture, and RMS Packet gateway test. No library added |

Exit for Phase 0: voice receive, PTT, and basic APRS still pass on the current service, and the documents above match the source. That exit is not claimed by this commit. This commit is the map.
