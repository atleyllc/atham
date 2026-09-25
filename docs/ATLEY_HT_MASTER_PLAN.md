# Atley HT master product plan

Status: product architecture. September 2026. Product owner: Atley LLC.

This is the plan. Implementation starts only after [phase0/AUDIT.md](phase0/AUDIT.md) is accepted. Cursor does not build the whole plan in one change.

## Vision

Atley HT is a phone-first amateur-radio workstation: radio control, scan, packet, Winlink, APRS, digital modes, logging, maps, memories, and spectrum. It starts from KV4P HT and keeps the proven device and firmware path. It is not a reskin and it is not a phone full of desktop daemons.

Connect a supported radio or SDR and the app shows what that hardware can safely do. USB is only a connector. Device profiles and capability flags decide the UI.

## Principles

- Phone-first workflows, not a desktop UI shrunk to a touchscreen.
- Local-first. Memories, logs, composed messages, downloaded maps, and device control work offline.
- Standards. Winlink means B2F. Packet means AX.25. Logs mean ADIF. APRS interoperates with other stations.
- Progressive disclosure. Plain language first. Modem settings, frame logs, and waterfalls on demand.
- Capability-driven UI. Disabled features say why.
- Safe transmit. Explicit intent, band-plan check, timeout, cancel, and guaranteed unkey.
- One station model. Callsign, grid, devices, memories, contacts, gateways, messages, and logs are shared.
- New radios are new profiles, not edits in every feature.
- RadioReference and RepeaterBook use approved access and attribution. No scraping.
- An audio FFT is labeled as audio. A true waterfall needs IQ hardware.

## Navigation

Radio, Spectrum, Data (APRS, packet, BBS, digital), Mail (Winlink), Log.

Global: device drawer, session status, station identity, settings, emergency unkey, and a beginner Activity launcher (repeater, Winlink, APRS, FT8, JS8, packet, scan, spectrum, log). Each activity checks the connected profile before it starts.

## First hardware profiles

| Device | Role | Honest limit |
| --- | --- | --- |
| KV4P HT | First radio. FM, memories, scan, APRS, KISS, later Winlink Packet | Narrowband. Not an SDR |
| QRP Labs QDX | First HF digital radio. CAT, USB audio, FT8 family | No general voice, no wideband IQ |
| QMX | Later, after firmware IQ is verified | Do not assume IQ |
| USB audio plus CAT | Digital modes and an audio spectrum | One profile per radio |
| Network rigctl | Compatibility | Needs another computer |
| RTL-SDR | First real receive waterfall | Receive only |
| External KISS TNC | Packet and Winlink Packet | Tuning may be separate |

## Architecture to build toward

Operator modules sit on shared services: session coordinator, memories, audio router, transmit safety, location, logging, diagnostics.

Engines: KISS, AX.25, APRS, B2F, then FT8, JS8, selected FLDigi modes, SSTV. Android orchestration stays the app layer. Native code is allowed behind small interfaces. Do not embed WSJT-X, Pat, Direwolf, or FLDigi as desktop processes.

DigiPi is a capability reference only.

## Delivery order

| Phase | Exit |
| --- | --- |
| 0 Audit and platform seams | Voice and basic APRS still work through documented abstractions. This audit is the first review, not that exit |
| 1 KV4P radio | Memories, scan, tones, recording, RepeaterBook or an approval path |
| 2 Packet and APRS | Interoperable with other TNCs, not only Atley-to-Atley |
| 3 Winlink slice | Real mail via Telnet CMS and one RMS Packet gateway |
| 4 Winlink parity | RadioMail-class mailbox, forms, P2P, external KISS |
| 5 QDX and FT8 | Logged FT8 without a laptop. Clock quality is visible |
| 6 RTL-SDR receive | Waterfall labeled as IQ, with thermal limits |
| 7 More digital modes and ARDOP | One mode at a time, each with vectors |
| 8 Trunk tracking, IGate, node, scheduler | Separate programs, not checkboxes |

## Non-goals for the first releases

- Universal USB-radio support
- Trunk tracking on KV4P
- A wideband waterfall from speaker audio
- All of DigiPi hosted on Android
- Unattended transmit by default
- A cloud account required to key the radio
- Proprietary replacements for AX.25, APRS, or Winlink

## Current code versus this plan

As of radio-mail 0.1.9 the app is still a KV4P controller plus APRS and a local packet mailbox. Numbered APRS lines are not Winlink. See the audit for the file map, the license gate, and the tasks that have to land before Phase 1 feature work expands.
