# ATHAM — Mobile Radio Operations Platform

**Expanded name:** Atley Ham  
**Product model:** A modular, phone-first radio operating environment

**Status:** Product architecture and phased implementation plan  
**Date:** September 2026  
**Product owner:** Atley LLC

## 1. Product vision

ATHAM is a phone-first, modular radio-operations platform: a mobile one-stop shop for operating, controlling and understanding connected radios. It combines a feature-rich HT controller, VoiceLog, digital modes, packet, Winlink mail, Reticulum mesh, spectrum tools, maps, memories and station management inside one coherent application.

The product should occupy the space between a premium radio controller, a mobile equivalent of a station-management suite, and the modular operating model of AtleyOS. Each capability is an ATHAM module with its own focused workspace, while all modules share the same connected devices, station identity, frequencies, memories, contacts, logs, location, time quality, audio paths, transmit safety and diagnostics.

ATHAM is not a bundle of unrelated ham applications placed behind a menu. It should feel like one radio operating system whose workspace changes with the operator's task.

It begins with the open-source KV4P HT project, but it is not merely a reskin or incremental fork. ATHAM should retain only the proven low-level KV4P device and firmware integration that is useful, then build a modular radio platform around it.

The long-term promise is:

> Connect a supported radio, modem, TNC or SDR to an Android phone, and ATHAM assembles the right mobile operating station from its available modules.

The word **supported** matters. USB is only a connector; USB radios expose different combinations of audio, CAT control, serial data, KISS, IQ samples, PTT, firmware control, and power requirements. ATHAM must use device profiles and capability detection rather than imply that every USB radio can perform every function.

ATHAM should feel like one purpose-built radio rather than a collection of desktop ham applications squeezed onto a phone.

## 2. Product principles

1. **Phone-first, not desktop-in-a-phone.** Workflows are task-oriented, touch-friendly, and understandable without knowing which Linux daemon traditionally performs the task.
2. **Local-first and field-ready.** Core radio operation, memories, logs, maps already downloaded, message composition, and device control work without internet access.
3. **Standards-compatible.** Winlink is actual Winlink; packet is actual AX.25; APRS interoperates with other stations; logs use standard formats.
4. **Progressive disclosure.** Beginners see guided actions and plain-language status. Experienced operators can open modem settings, frame logs, waterfalls, transcripts, and diagnostics.
5. **Capability-driven UI.** The app only enables features supported by the connected hardware and explains why another feature is unavailable.
6. **Safe transmit control.** Every transmit path has explicit operator intent, band-plan validation, timeout protection, cancellation, and guaranteed PTT release.
7. **One shared station model.** Callsign, location, grid, devices, memories, contacts, gateways, messages, and logs are shared across modules.
8. **Extensible adapters.** Adding a radio should normally require a new driver/profile, not changes throughout every feature.
9. **Respect data licensing.** RadioReference and RepeaterBook integrations use approved access methods and attribution; their databases are not scraped or silently mirrored.
10. **Honest radio limits.** An audio spectrum is labeled as an audio spectrum. A true RF waterfall requires IQ-capable hardware.
11. **Modular, but never fragmented.** Each major capability has a focused workspace, but modules use one shared station model and coordinated radio session.
12. **One operation, one source of truth.** A contact heard in Radio, decoded in FT8, exchanged over packet or captured by VoiceLog becomes part of the same activity and log history.
13. **Controller-grade immediacy.** Live radio control should match the operational depth expected from strong mobile HT controller applications such as the VGC/UV-PRO ecosystem, while improving clarity, consistency and extensibility.
14. **Mobile station management.** ATHAM should deliver the coordination and overview of a station-management suite without reproducing a desktop dashboard on a phone.

## 3. Product map and module system

### 3.1 ATHAM shell

ATHAM uses an AtleyOS-like shell rather than treating every capability as an equal permanent tab. The shell owns:

- Current station identity and operating profile
- Connected devices and their combined capabilities
- Global RX, TX, scan, decode and transfer state
- Active session and emergency unkey
- Workspace/module switcher
- Notifications and background activity
- Search and quick actions
- Settings, diagnostics and support export

Each module publishes its required capabilities, current activity, resumable state, quick actions and deep links. The shell only presents modules that can operate with the current hardware or clearly offers the device needed to enable them.

### 3.2 Core modules

| Module | Purpose |
| --- | --- |
| **Home / Station** | Connected station overview, active operations, recent activity, alerts and guided launchers |
| **Radio** | Full HT/mobile rig controller: VFOs, channels, memories, banks, scan, tones, duplex, power, filters, audio and PTT |
| **VoiceLog** | Built-in voice-assisted contact capture, live transcription, callsign/report parsing, review and final logging |
| **Digital** | FT8, FT4, WSPR, JS8, PSK31, RTTY and later supported sound-card modes |
| **Packet** | AX.25/KISS monitor, connected terminal, BBS/node sessions, APRS and raw frame tools |
| **Mail** | Full Winlink mailbox, composer, forms, gateway directory and live radio sessions |
| **Mesh** | Reticulum identities, LXMF, LXST, files, telemetry, maps, interfaces and transport status |
| **Spectrum** | Audio spectrum, narrowband views or true SDR panadapter/waterfall according to hardware capability |
| **Log** | Unified QSO history, drafts, recordings, transcripts, maps, statistics and ADIF interchange |
| **Maps** | Repeaters, APRS, Reticulum telemetry, gateways, memories and location-aware operating context |
| **Tools** | Band plan, propagation, calculators, time quality, tone tools, audio calibration and other mobile field utilities |
| **Devices** | Radio/modem discovery, profiles, firmware, CAT/audio/KISS/IQ endpoints and capability diagnostics |

Modules are product boundaries and navigation workspaces, not necessarily separate APKs or Gradle modules. Code boundaries should follow dependency and lifecycle needs rather than mirror the visible menu mechanically.

### 3.3 Primary navigation

1. **Radio** — VFO, channels, memories, tuning, PTT, audio, scan, monitor
2. **Spectrum** — audio spectrum or true SDR spectrum based on device capability
3. **Data** — APRS, packet terminal, BBS/node, digital modes
4. **Mail** — full Winlink mailbox, forms, stations, and sessions
5. **Log** — contacts, automatic capture, ADIF import/export, map and statistics
6. **Mesh** — Reticulum identity, LXMF messaging, LXST voice, files, telemetry, maps, interfaces and transport status

The final mobile navigation must not expose every module as an equal bottom tab. Use a radio-first shell with a module/workspace switcher, four or fewer persistent destinations if needed, contextual actions and resumable active sessions. Navigation testing must establish the clearest portrait, landscape and one-handed model.

Global access:

- Connected-device drawer
- Notifications and active-session status
- Station/callsign identity
- Settings and diagnostics
- Emergency stop/force unkey while transmitting

### Guided “Activity” launcher

Alongside expert navigation, ATHAM should offer a beginner-friendly launcher:

- Talk on a repeater
- Monitor nearby public-safety frequencies
- Send a Winlink message
- Check Winlink mail
- Send or view APRS
- Make an FT8 contact
- Start a JS8 conversation
- Open a packet BBS/node
- Scan a saved bank
- Explore the spectrum
- Log a contact
- Open Reticulum conversations
- Check nearby Reticulum destinations
- Start a private business/ISM mesh session

Each activity selects the appropriate device, configures the mode, checks prerequisites, and walks the user through operation.

## 4. Hardware capability model

Every driver publishes a `RadioCapabilities` description rather than relying on device-name conditionals.

Core capability flags include:

- Frequency ranges and allowed step sizes
- Receive and transmit availability
- Analog FM/AM/SSB/CW support
- CAT tuning and mode control
- Hardware or software PTT
- USB audio input/output
- Raw IQ input/output
- KISS TNC
- AX.25 in firmware
- RSSI/signal metrics
- Squelch control
- Duplex/offset/tone support
- Scan/tuning speed
- GPS or timing support
- Firmware update support
- Maximum safe duty cycle when known

### Initial device targets

| Device class | ATHAM role | Realistic first capabilities | Important limitation |
|---|---|---|---|
| KV4P HT | First deeply integrated VHF/UHF radio | Analog FM, memories, scanning, APRS, KISS/AX.25, Winlink Packet, direct packet | Narrowband path; not a true wideband IQ SDR |
| QRP Labs QDX | First HF USB digital transceiver | CAT, USB audio, FT8/FT4/WSPR and compatible single-tone digital modes | Digital-only design; no general voice and no wideband IQ waterfall |
| QRP Labs QMX | Later HF multi-mode target | CAT, USB audio, digital modes, possible richer receiver integration | Capabilities vary by firmware; IQ support must be verified per release |
| Generic USB Audio + CAT radio | Broad conventional-radio target | Digital modes, rig control, audio spectrum, PTT | Requires per-radio CAT/PTT profile and compatible Android USB audio |
| Network rigctl/hamlib bridge | Compatibility target | Remote CAT/PTT and network audio where available | Requires another computer or appliance |
| RTL-SDR | First true receive-only SDR | Wideband spectrum/waterfall, demodulation, scanning | Receive only; Android USB throughput and power constraints |
| External KISS TNC | Packet interoperability | APRS, AX.25, Winlink Packet, terminal | Separate radio tuning/PTT may still be required |
| RNode / compatible LoRa interface | Reticulum mesh target | Encrypted LXMF, telemetry, files, store-and-forward and supported LXST voice on legally configured ISM or other authorized spectrum | Regional frequency, power, bandwidth and duty-cycle rules apply |
| External Reticulum node | Initial Reticulum integration target | Reticulum over local TCP/IP, Wi-Fi, Ethernet or a separately managed radio transport | Depends on node availability and configured interfaces |
| Part 90 certified data radio/TNC | Business mesh target | Full encrypted Reticulum where the license, emission, bandwidth and equipment authorization permit it | A business license does not authorize arbitrary frequencies, emissions or uncertified transmitters |

## 5. System architecture

### 5.1 Major layers

```text
Operator experiences
  Radio | Spectrum | APRS | Packet | Winlink | Digital | Log

Shared radio services
  Session coordinator | frequency/memory service | audio router
  transmit safety | location/time | logging | diagnostics

Protocol and modem engines
  KISS | AX.25 | APRS | B2F/Winlink | RNS | LXMF | LXST
  FT8 family | JS8
  FLDigi-family modes | SSTV | audio demodulators

Hardware abstraction
  RadioDriver | CatDriver | AudioEndpoint | KissEndpoint | IqSource

Transports
  USB serial | USB audio | USB bulk/IQ | Bluetooth | TCP/UDP | built-in KV4P
```

### 5.2 Radio driver contract

Each driver should implement only the interfaces it supports:

- `RadioControl`: frequency, mode, filters, power, tones, duplex, PTT
- `AudioEndpoint`: RX and TX PCM streams plus format negotiation
- `KissEndpoint`: framed KISS byte stream
- `IqSource`: complex IQ stream plus sample-rate and gain controls
- `SignalMetrics`: RSSI, S-meter, squelch, SWR/power where available
- `FirmwareManager`: identify, validate, flash, recover
- `DeviceDiagnostics`: health, permissions, USB state, logs
- `ReticulumInterface`: Reticulum carrier lifecycle, status, MTU, bitrate, regulatory profile and diagnostics

Feature modules depend on these contracts, not on specific radios.

### 5.3 Session coordinator

Only one module may own a transmit-capable radio path at a time. The coordinator:

- Arbitrates Radio, APRS, Winlink, FT8, JS8 and other sessions
- Stops scanning before transmit
- Suspends conflicting audio consumers
- Prevents simultaneous CAT commands from multiple modules
- Applies band-plan and license settings
- Enforces transmit timeouts and duty-cycle warnings
- Guarantees unkey on cancellation, crash, USB removal, lifecycle transition, or timeout
- Records a human-readable and technical session history

### 5.4 Native engine boundary

The Android UI and orchestration layer should remain Kotlin-first. Signal processing and proven protocol implementations may be C/C++/Rust/Go behind small stable interfaces.

Do not launch full Linux desktop applications inside the app as the long-term architecture. DigiPi is an excellent capability reference, but it achieves its breadth by running separate Linux applications such as WSJT-X, JS8Call, FLDigi, Direwolf and Pat. ATHAM needs reusable engines with native phone workflows.

Where reuse is legally and technically practical:

- Use Pat / `wl2k-go` as the reference or reusable core for Winlink B2F.
- Evaluate Direwolf algorithms and tests for AX.25/APRS, respecting its license.
- Evaluate the WSJT-X decoder/encoder core and licenses for FT8-family modes.
- Evaluate JS8Call code and protocol compatibility for JS8.
- Evaluate FLDigi modem implementations mode by mode rather than promising its entire catalog immediately.
- Use Hamlib rig definitions as an optional bridge/reference, while keeping a curated native driver set for dependable mobile operation.

Every imported engine requires a written license decision, Android cross-compilation proof, CPU/battery benchmark, deterministic test vectors, and a maintained upstream-update strategy.

## 6. Radio and memory experience

### 6.1 VFO and live radio

- Large current frequency and channel identity
- Direct numeric entry and tuning wheel
- VFO A/B, swap, split where supported
- Mode, bandwidth, step and power
- Repeater offset and direction
- CTCSS/DCS encode/decode where supported
- Squelch, volume and filters
- PTT with slide/hold safety option
- TX timer and duty-cycle indicator
- Signal history and audio recording
- One-tap add to memory
- Nearby known-frequency context
- Device temperature/voltage/SWR when hardware supplies it

### 6.2 Unified memories

A memory is not just a frequency. It may contain:

- Name, service and group/bank
- RX and TX frequency
- Mode, step and bandwidth
- Offset, tone and DCS
- Scan inclusion and priority
- Geographic region/geofence
- Source and source record ID
- Last source refresh time
- Notes and tags
- Transmit permission: allowed, receive-only, or unknown
- Device-specific programming metadata

Memory sources:

- Manual entry
- Current VFO
- RepeaterBook
- RadioReference
- CSV/CHIRP-compatible import where licensing permits
- Previous scan hits
- Shared ATHAM export

### 6.3 Scan and monitor

- Memory-bank scan
- Frequency-range scan
- Priority channel/watch
- Tone search where hardware allows
- Resume modes: time, carrier, delay, manual
- Nuisance delete / temporary lockout
- Hit counter and last-heard time
- Signal-strength history
- Audio recording with timestamps
- Optional speech-to-text notes, clearly labeled as non-authoritative
- Quick save of discovered channels
- Geographic scan lists

Scanning performance depends on radio tuning speed. KV4P should provide useful conventional scanning, but it should not be marketed as a trunk-tracking scanner unless that functionality is separately engineered and supported by suitable hardware.

### 6.4 Built-in VoiceLog operating layer

VoiceLog is an ATHAM module, not merely an external handoff. It can run beside compatible Radio, Digital and Packet sessions and use their authoritative context instead of asking the operator to repeat known information.

- Voice-first start, pause, resume, correct and finalize workflow
- Live transcription with ham-specific recognition bias
- Callsign, signal report, frequency, band, mode, grid, name and QTH extraction
- Current device, VFO, mode, memory and location supplied automatically
- Draft contact created during the operation and finalized only after review
- Clear confidence and validation indicators
- QRZ/LoTW/eQSL/POTA/SOTA integrations later through shared log services
- Audio or transcript attachment only with explicit operator choice
- Works as an overlay, compact side panel or full module workspace
- Never lets transcription inferred data silently overwrite authoritative radio data

The existing standalone VoiceLog product may remain useful during migration, but ATHAM owns the canonical integrated experience and unified QSO data model.

## 7. RadioReference and RepeaterBook integrations

### 7.1 RadioReference

ATHAM should provide a first-class account connection:

- User signs in or supplies approved credentials through a secure authorization flow
- App uses an approved RadioReference application identity and official web service
- Search by current location, state/county, ZIP, route or manual location
- Browse agencies, categories, conventional frequencies, trunked systems and talkgroups
- Build local receive lists and memory banks
- Preserve source attribution and update timestamps
- Refresh records without destroying user edits
- Clearly distinguish conventional channels that the connected radio can monitor from trunked or digital systems it cannot decode
- Store credentials in Android Keystore and never include them in support bundles

An account connection does not itself make ATHAM a trunk tracker. Phase-one RadioReference value is discovery and programming of compatible conventional channels. Trunked-system monitoring requires a separate SDR decoding project.

### 7.2 RepeaterBook

Preferred order:

1. Integrate with RepeaterBook Connect on Android after approval, using its on-device interface and offline data.
2. Use approved per-user API access only where Connect cannot meet the workflow.
3. Never scrape public pages, mirror the database, bypass limits, or redistribute cached data outside permitted use.

Features:

- Nearby, route and destination searches
- Filters for band, mode, operational status, distance and bearing
- Details including offset, tone, notes and linked systems when available
- Save to ATHAM memories
- Source-linked refresh and change review
- Favorites independent of device-specific memory slots
- One-tap tune and monitor

## 8. Spectrum and SDR

ATHAM exposes three deliberately distinct views.

### 8.1 Audio spectrum

Available for radios exposing demodulated USB or internal audio:

- Audio FFT and waterfall
- Filter passband overlay
- Peak frequency
- Modem tones and decode markers
- Recording and playback

This is useful for digital-mode tuning but is not an RF-band waterfall.

### 8.2 Narrowband RF view

Available when a radio exposes limited IF/IQ or reports signal samples around the tuned channel. Features depend entirely on the driver.

### 8.3 Wideband SDR view

Requires an `IqSource`, initially RTL-SDR:

- Configurable sample rate, center frequency and gain
- Panadapter and waterfall
- AM, narrow FM, wide FM, USB, LSB and CW receive demodulation
- Tap-to-tune and draggable passband
- Frequency bookmarks and memory overlay
- Peak detection and scan ranges
- Recording IQ or demodulated audio with storage warnings
- Optional multiple demodulators if phone performance allows
- RadioReference/memory labels on visible signals

Later milestones may add trunked-control-channel decoding and following, but this is a separate program of work—not a checkbox under the initial waterfall.

## 9. APRS and packet suite

### 9.1 APRS

- 1200-baud VHF APRS through KV4P/KISS
- Station map and list
- Position beaconing with smart-beacon controls
- Messaging with acknowledgements and retries
- Conversation-style message view
- Objects, items, bulletins and announcements
- Weather packets
- Telemetry display
- Path configuration with safe presets
- APRS-IS optional connection and gating policies
- Digipeater mode only with explicit operator configuration
- Offline map support
- Raw packet inspector
- Export/share individual packets and diagnostics

### 9.2 General AX.25 packet

- KISS TNC console and monitor
- Connected-mode terminal
- Connect to local BBS/node
- Digipeater paths
- Heard stations list
- Frame inspector
- Saved connection profiles
- Session transcript
- File/text transfer where interoperable
- Optional AX.25 node/BBS functions in a later phase
- IP-over-AX.25 experimentation only after core packet reliability

## 10. Winlink Mail — first-class product

ATHAM Mail is a real Winlink client, not numbered lines transmitted on the current frequency.

### 10.1 Mail experience

- Inbox, Outbox, Drafts, Sent, Archive, Trash and Flagged
- Search, filters and bulk actions
- Reply, Reply All and Forward
- Callsign and email recipients
- Contacts and recent recipients
- Attachments with image resizing/compression
- Transfer-size and estimated airtime warning
- Draft autosave
- Read/unread, flags and message status
- Import/export and share
- Offline composition and reading

### 10.2 Winlink protocol core

- B2F proposals and exchanges
- Standard message IDs and duplicate prevention
- LZHUF compatibility and supported negotiated extensions
- Secure-login challenge handling
- Attachments
- Partial/failure-safe Outbox behavior
- Telnet CMS for development and internet sessions
- RMS Packet over AX.25/KISS
- Packet P2P send and listen
- Auxiliary callsigns when supported
- Complete redacted technical transcript

### 10.3 Station directory

- Official Winlink gateway data
- Packet/ARDOP/VARA/etc. filters
- Map and list
- Distance, bearing and favorites
- Recent and successful stations
- Compatibility with connected hardware
- User-observed success history
- One-tap tune and connect
- Cached offline directory with last-updated status

### 10.4 Forms and emergency communications

- Standard Winlink forms
- ICS and situation-report workflows
- Compose, view, reply and forward
- Standards-compatible XML payloads
- Automatic form catalog updates when connected
- Custom form-package import where supported
- Favorite and recent forms
- Saved operator/agency defaults
- Position insertion
- PDF or printable export

### 10.5 Transport roadmap

1. Telnet CMS
2. KV4P RMS Packet
3. External KISS Packet
4. Packet P2P
5. ARDOP with USB audio radios
6. Networked VARA FM/HF host where legally and technically appropriate
7. Other transports only after interoperability testing

## 11. Digital modes

### 11.1 FT8-family first release

QDX is the priority reference device because it supplies 24-bit/48 kHz USB audio and CAT over USB.

- FT8 receive/decode
- Band activity list
- Waterfall and decode markers
- Standard QSO sequencing with operator confirmation
- Fox/Hound deferred unless engine support is mature
- FT4 after FT8 core stability
- WSPR receive, reporting and controlled beaconing
- Callsign lookup/cache integration where available
- Automatic UTC time-quality check
- CAT frequency/mode control
- TX audio calibration and ALC guidance
- QSO logging with ADIF export
- PSK Reporter integration as opt-in
- POTA/SOTA-friendly exchange helpers later

Android time accuracy is critical. The app should show clock offset/quality and block or strongly warn on FT8 transmit if synchronization is inadequate.

### 11.2 JS8

- JS8 decoding and calling
- Free-text conversations
- Inbox/store-and-forward behavior supported by the protocol
- Groups and directed messages
- Heard stations and activity
- Saved macros
- Logging and map integration

### 11.3 FLDigi-family roadmap

Add modes in value/complexity order rather than claiming all FLDigi modes at launch:

1. PSK31
2. RTTY
3. Olivia
4. MFSK
5. Contestia
6. Hellschreiber
7. CW decode/encode assistance
8. MT63 and emergency-communications workflows
9. FLMSG/FLWRAP-compatible form and file workflows
10. SSTV receive, then transmit
11. Weather fax receive

Each mode requires a usability workflow, test vectors, and radio compatibility matrix—not only a DSP decoder.

## 12. Reticulum mesh and resilient communications

Reticulum should be a first-class ATHAM capability rather than an amateur-radio-only feature. It provides a carrier-independent network layer for encrypted, disruption-tolerant and multi-hop communication over RNode/LoRa, Wi-Fi, local IP, the Internet, serial/KISS devices, external transport nodes and appropriately authorized business-radio systems.

### 12.1 Mesh workspace

- Reticulum identity creation, backup, restore and explicit switching
- LXMF conversations, contacts and announce stream
- Store-and-forward messages and propagation-node support
- Images, files and compressed voice messages
- LXST half/full-duplex voice and PTT where bandwidth and hardware allow
- Nearby destinations and path status
- Telemetry and location sharing with per-contact permissions
- Offline maps and situation display
- Interface status, traffic, MTU, bitrate and path diagnostics
- Optional phone transport-node operation with clear battery/thermal warnings
- Clear carrier label on every active session

### 12.2 Supported carrier profiles

ATHAM must decide availability from both technical capability and the operator's selected regulatory profile.

| Profile | Full encrypted Reticulum | Product behavior |
| --- | --- | --- |
| Wi-Fi, Ethernet or Internet | Yes | Normal Reticulum operation |
| RNode/LoRa on license-free spectrum | Yes, when configured within regional technical limits | Require region, frequency, power, bandwidth and duty-cycle configuration |
| External Reticulum transport node | Yes | ATHAM acts as a client unless transport mode is explicitly enabled |
| Properly authorized Part 90/business system | Yes, when the station license, frequencies, emissions, bandwidth and certified equipment allow it | Require a business station profile and compatible certified radio/interface |
| US Part 97 amateur spectrum | Default encrypted Reticulum transmission blocked | Explain the encryption/obscured-message issue and offer legal carriers; do not imply that callsign identification alone resolves it |

A business radio license is not a universal transmit permission. The profile must record or confirm the licensee, authorized frequencies/area, station class, emission and bandwidth, equipment identity/certification, power limits and permissible operational purpose. ATHAM is an operating aid, not a substitute for frequency coordination or legal review.

The existing KV4P hardware must not be represented as Part 90 compliant unless its exact transmitter configuration has the required equipment authorization. A Part 90 license alone does not make uncertified hardware legal.

### 12.3 Implementation strategy

Do not create an LLM-generated clean-room Reticulum reimplementation. The Reticulum reference implementation is authoritative, and compatibility and cryptographic correctness are safety-critical.

Preferred sequence:

1. Connect ATHAM to an external/local Reticulum instance and prove identity, announce, path and LXMF workflows.
2. Add RNode USB and Bluetooth interface support.
3. Deliver a first-class native Atley LXMF interface.
4. Add files, voice messages, telemetry and maps.
5. Evaluate embedding the official Python reference implementation versus a mature recognized mobile implementation such as Reticulum-Go.
6. Add LXST voice/PTT after messaging and interface lifecycle reliability.
7. Add certified business-radio/KISS adapters and regulatory station profiles.

Keep the Reticulum engine behind a stable `ReticulumService` boundary. The Kotlin UI should consume typed state and commands rather than importing protocol internals throughout feature screens. Reticulum sessions still participate in the shared session coordinator when they own a transmit-capable radio path.

### 12.4 Licensing and security

- The Reticulum protocol is public domain, but the reference implementation uses the custom Reticulum License.
- Record the exact implementation/version, license obligations and distribution boundary in the dependency ledger before integration.
- Prefer an external-process/service boundary for the first implementation while licensing and Android packaging are evaluated.
- Never weaken or substitute cryptographic primitives to simplify Android packaging.
- Store identity private keys in app-private storage protected by Android Keystore-backed encryption where technically appropriate.
- Identity export must require explicit user action and warn that possession grants control of that identity.
- Default diagnostics must redact identities, destination hashes where linkable, message/file contents, precise locations and cryptographic material.
- Interoperability tests must include the authoritative reference implementation and established clients such as Sideband.

## 13. Logging and shared station intelligence

ATHAM should not create an isolated log inside each mode.

- Unified QSO database
- Automatic draft log from voice, FT8, JS8, packet and Winlink activity
- Manual confirmation before finalizing inferred data
- ADIF import/export
- Callsign, frequency, mode, band, grid, signal reports and notes
- Location/profile used for each contact
- Attachment of session transcript or recording references
- Map, statistics and awards-ready fields
- Optional integrations later: QRZ, LoTW, eQSL, POTA, SOTA
- Built-in VoiceLog module using the shared QSO domain model
- Optional import/export bridge to the standalone VoiceLog application during migration

## 14. Data and security

- SQLite/Room with explicit migrations
- Encrypted credentials in Android Keystore
- Separate large-attachment and recording storage
- User-controlled retention policies
- Exportable local backup
- No cloud dependency for core operation
- Optional Atley Account sync only after local workflows are stable
- Diagnostic bundle with configurable redaction
- No passwords, Winlink responses, API secrets, precise saved home locations, or message content in default logs
- Per-integration privacy disclosure

Core entities:

- StationIdentity
- Device and DeviceProfile
- RadioCapability and DriverBinding
- Memory and MemoryBank
- FrequencySourceRecord
- ScanSession and ScanHit
- Qso
- AprsStation, AprsMessage and PacketFrame
- WinlinkAccount, MailMessage, Attachment, Form and MailSession
- DigitalDecode and DigitalSession
- Recording
- LocationProfile
- DiagnosticTrace
- ReticulumIdentity, ReticulumContact and ReticulumDestination
- ReticulumInterfaceProfile, ReticulumPath and ReticulumSession
- LxmfConversation, LxmfMessage and ReticulumAttachment
- TelemetryRecord and TelemetryPermission
- RegulatoryProfile and BusinessStationProfile

## 15. Licensing and product-boundary decisions

Before implementation, create a dependency ledger recording:

- Upstream project and exact version/commit
- License
- Static/dynamic linking implications
- Required notices and source obligations
- Changes made by Atley LLC
- Update path and security owner

The upstream KV4P project is GPL-3.0. ATHAM must be structured and distributed in a way that complies with that license. Pat and `wl2k-go` are MIT-licensed, but other modem engines may be GPL or have additional constraints. Do not assume that a clean UI wrapper removes copyleft obligations.

The Reticulum protocol is public domain, while its reference implementation uses the custom Reticulum License. The dependency decision must separately evaluate protocol rights, reference-code distribution, Android embedding, modifications, notices and compatibility with ATHAM's GPL-derived distribution obligations.

RadioReference and RepeaterBook data have contractual/API restrictions separate from software licenses. Their data must not be treated as an open dataset.

## 16. Phased delivery plan

### Phase 0 — Fork audit and platform foundation

**Goal:** Establish legal, architectural and hardware truth before feature expansion.

- Audit existing ATHAM fork and upstream KV4P delta
- Inventory reusable USB, firmware, audio, KISS, AX.25 and UI code
- Confirm GPL distribution strategy
- Introduce capability-based driver interfaces
- Build session coordinator and transmit safety service
- Establish Room database and migrations
- Establish structured diagnostics and support bundle
- Add device lab and automated hardware-test checklist
- Create product design system and new navigation shell

**Exit:** Existing KV4P voice and basic APRS work through the new abstractions with no regression.

### Phase 1 — Best-in-class KV4P radio

- Radio/VFO redesign
- Memories and banks
- Manual and range scanning
- Tone/offset workflow
- Recording and scan history
- RepeaterBook Connect integration or approval path
- Import/export
- RadioReference developer-access application and integration spike
- VoiceLog module shell and shared QSO draft model
- Radio-to-VoiceLog context handoff for frequency, mode, channel, time and location

**Exit:** ATHAM is already a substantially better daily KV4P radio app and can create a reviewed voice-assisted QSO draft without leaving the application.

### Phase 2 — Packet and APRS suite

- Harden KISS and AX.25
- Full APRS map, messaging and station detail
- Packet monitor and connected terminal
- BBS/node connection profiles
- Frame-level diagnostics
- Long-duration receive and reconnect testing

**Exit:** Reliable interoperable packet operation, not only Atley-to-Atley messaging.

### Phase 3 — Winlink vertical slice

- Winlink account and secure credentials
- Local mailbox and composer
- Pat/`wl2k-go` reuse decision
- Telnet CMS interoperability
- KV4P RMS Packet
- Station directory
- Clear live session UI and transcript
- Attachments and size/airtime estimates

**Exit:** Send and receive standard Winlink mail through both Telnet and a real RMS Packet gateway.

### Phase 4 — First-class Winlink parity

- Full mailbox management/search
- Forms and emergency workflows
- Packet P2P
- Digipeater paths and scripts
- External KISS TNCs
- Position reports
- Advanced station selection/history
- Import/export and exhaustive failure recovery

**Exit:** RadioMail-class mail experience optimized for Android and KV4P.

### Phase 5 — USB audio/CAT platform and QDX FT8

- Generic Android USB audio endpoint
- USB serial/CAT framework
- QDX driver
- Time-quality service
- FT8 receive, decode, sequence and transmit
- FT4 and WSPR
- Unified logging and ADIF
- Audio calibration wizard

**Exit:** A QDX can plug into a supported Android phone and make/log FT8 contacts without a laptop.

### Phase 6 — SDR receive platform

- RTL-SDR driver
- Wideband waterfall/panadapter
- AM/FM/SSB/CW receive
- Memory overlays and tap-to-tune
- Frequency-range discovery and recording
- Performance, thermal and battery controls

**Exit:** Genuine SDR visualization and receive operation with explicitly supported IQ hardware.

### Phase 7 — Broader digital suite

- JS8
- PSK31 and RTTY
- Olivia/MFSK/Contestia
- SSTV
- FLMSG/FLWRAP workflows
- ARDOP for Winlink on compatible HF radios
- Additional native radio drivers and network bridges

### Phase 8 — Reticulum mesh vertical slice

- External/local Reticulum service connection
- Identity create/import/export safeguards
- Interface and path status
- Announce stream and destination discovery
- Native LXMF conversations and message status
- RNode USB/Bluetooth integration spike
- Wi-Fi/TCP and external-node carrier profiles
- Regulatory profile framework
- Part 97 encrypted-RF transmit guard
- Interoperability testing against the reference implementation and Sideband

**Exit:** ATHAM can exchange reliable LXMF messages over IP and a supported RNode path, clearly identify the active carrier, preserve identity securely, and prevent prohibited carrier/profile combinations by default.

### Phase 9 — Reticulum business and media suite

- Part 90/business station profiles
- Certified business-radio/KISS adapters
- File, image and voice-message transfer
- Telemetry permissions and situation map
- Propagation-node workflows
- LXST voice/PTT feasibility and first supported path
- Transport-node controls with battery/thermal safeguards
- Long-duration disruption/reconnection tests

**Exit:** A properly authorized business or ISM deployment can use the full Reticulum experience through supported hardware, while amateur operation remains clearly separated and guarded.

### Phase 10 — Advanced monitoring and station services

- Evaluate trunked control-channel decode with SDR hardware
- Multi-VFO/multi-demodulator receive where performance allows
- Optional APRS IGate/digipeater
- Optional packet node/BBS
- Scheduled receive tasks within Android limits
- Companion ATHAM desktop/server bridge if certain engines remain impractical on-phone

## 17. Priority order and non-goals

The highest-value order is:

1. Stabilize and improve KV4P voice/radio basics
2. Build reliable packet/APRS foundations
3. Deliver real Winlink Packet
4. Add frequency data and excellent memory/scan workflows
5. Add QDX + FT8
6. Add true SDR through dedicated IQ hardware
7. Expand the digital-mode catalog
8. Add Reticulum/LXMF first over external nodes and RNode/ISM
9. Add authorized business-radio Reticulum transports and LXST

Do not begin by porting every DigiPi application. That would create maximum integration risk before ATHAM has a stable radio/session foundation.

Initial non-goals:

- Claiming universal USB-radio compatibility
- Trunk tracking through KV4P
- Wideband SDR from demodulated audio
- Hosting all DigiPi server services on Android in the first releases
- Automatic unattended transmission by default
- Cloud account requirement for local radio operation
- Proprietary Atley-only replacements for interoperable ham protocols
- Encrypted Reticulum transmission over US amateur spectrum by default
- Treating possession of a business license as blanket authority for arbitrary frequencies, emissions or hardware
- Reimplementing Reticulum cryptography or routing without demonstrated interoperability and expert review

## 18. Testing strategy

### Automated

- Protocol golden vectors for KISS, AX.25, APRS, B2F and digital modes
- Recorded audio/IQ fixtures with expected decodes
- Driver contract tests
- Database migration tests
- Duplicate/retry/reconnection tests
- USB removal at every session state
- Forced lifecycle/background transitions
- Transmit cancellation and unkey verification
- Redaction tests
- UI state restoration
- Reticulum reference-implementation interoperability
- LXMF duplicate, retry, store-and-forward and delayed-delivery tests
- Regulatory-profile carrier gating tests
- Identity backup/restore and key-redaction tests

### Hardware lab matrix

For every supported phone/radio combination record:

- Android version and USB chipset
- Power behavior and need for powered hub
- Audio formats and sample rates
- CAT stability
- PTT/unkey safety
- Sustained receive and transmit duration
- Thermal and battery impact
- RF interoperability result
- Known limitations

### Over-the-air validation

- Cross-test against independent APRS clients/TNCs
- Connect to multiple real RMS Packet gateways
- Exchange Winlink messages with RadioMail, Pat and Winlink Express
- FT8 decode comparison against WSJT-X using identical recordings
- Controlled on-air transmit tests into dummy load before antenna testing

## 19. Success measures

- Time from plugging in a known device to ready-to-operate state
- Successful session rate by protocol and device
- Zero stuck-PTT defects
- Winlink interoperability and duplicate-message rate
- APRS acknowledgement success
- FT8 decode parity against reference fixtures
- Crash-free long-duration monitor sessions
- Battery and thermal consumption per hour
- Percentage of failures producing an actionable plain-language explanation
- Number of user actions required for common workflows
- LXMF delivery success across interruption and reconnection
- Reticulum path discovery time and carrier handoff reliability
- Zero encrypted RF transmissions on blocked regulatory profiles

## 20. Immediate next build package for Cursor

Cursor should not implement this entire plan in one prompt. The first engineering package should request:

1. Full repository and license audit
2. Current KV4P firmware/app interface map
3. Existing KISS/AX.25/APRS path diagram
4. Proposed capability interfaces mapped to actual source files
5. Session coordinator design
6. Database migration plan
7. Device and protocol test harness
8. Phase 0 task breakdown with acceptance tests
9. Identification of code that should be preserved, wrapped, rewritten, or removed
10. No broad feature implementation until the audit is approved

The master plan should then be converted into separately reviewable epics. Each epic must include supported hardware, user workflow, protocol standard, licensing decision, diagnostics, safety behavior, automated tests, hardware tests, and explicit exit criteria.

## 21. Reference foundation

- [DigiPi](https://digipi.org/) — capability inspiration: Direwolf/APRS, Pat/Winlink, WSJT-X, JS8Call, FLDigi, SSTV, AX.25 services and Hamlib.
- [DigiPi source/configuration repository](https://github.com/craigerl/digipi)
- [Pat](https://github.com/la5nta/pat) and [wl2k-go](https://github.com/la5nta/wl2k-go) — open Winlink implementation references.
- [QRP Labs QDX](https://qrp-labs.com/qdx.html) — USB audio and CAT digital transceiver.
- [RadioReference Database Web Service](https://support.radioreference.com/hc/en-us/articles/18844460198932-Database-Web-Service-API)
- [RepeaterBook API and Connect guidance](https://www.repeaterbook.com/wiki/doku.php?id=api)
- [RepeaterBook data-use requirements](https://www.repeaterbook.com/wiki/doku.php?id=data_use)
- [KV4P HT source](https://github.com/VanceVagell/kv4p-ht)
- [Reticulum](https://reticulum.network/) and [Reticulum source](https://github.com/markqvist/Reticulum)
- [Reticulum interfaces](https://reticulum.network/manual/interfaces.html)
- [Sideband](https://github.com/markqvist/Sideband) — Android proof and interoperability reference for LXMF/LXST, not the ATHAM UI foundation.
