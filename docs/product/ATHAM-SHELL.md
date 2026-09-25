# ATHAM shell

**Product:** ATHAM  
**Expanded name:** Atley Ham  
**Description:** Mobile radio operations platform

This document amends navigation in [ATHAM-MASTER-PLAN.md](ATHAM-MASTER-PLAN.md) section 3.3. It does not change hardware, protocol, licensing, safety, or phase requirements elsewhere in that plan.

The shell is a proposal. It is not implemented in the Android project. Package `com.vagell.kv4pht` and `applicationId "com.vagell.kv4pht"` stay as they are in upstream commit `6f3265e81abf64e812de7c653486c79e1903b2e8`.

## Persistent navigation

Four destinations:

1. **Home** — station overview, alerts, recent activity, and guided launchers for work the connected station can start.
2. **Radio** — live VFO, memories, scan, and PTT.
3. **Log** — the shared contact list.
4. **Modules** — the catalog for every other workspace.

Home does not list disabled future modules. Radio does not list them either. Later modules stay in the Modules catalog with an honest capability or roadmap status.

## What the shell owns

- Current station identity
- Connected device
- Current frequency and mode
- Active session
- Global receive, transmit, scan, and decode state
- Emergency UNKEY
- Notifications and diagnostics

UNKEY stays available whenever a transmit-capable path exists, including ready, receive, scan, and transmit. It is the forced release, separate from letting go of PTT.

## Modules catalog

VoiceLog, Digital, Packet, Mail, Mesh, Spectrum, Maps, Tools, and Devices.

Home, Radio, and Log are the docked destinations. They also appear in the catalog so the operator can see which one owns the path.

Selecting a module replaces the workspace. It does not disconnect the radio, reset the VFO, or discard an active QSO draft.

If the selected module needs the transmit path and another module owns it, the shell asks the operator to take over or to observe. It does not take PTT, CAT, or the modem silently.

VoiceLog may observe Radio. Observation reads device, frequency, band, mode, memory, time, and location when location is authorized. It does not take transmit ownership.

## Legacy recovery

The current launcher remains `com.vagell.kv4pht.ui.MainActivity`, with Voice and APRS chat in `bottom_nav_menu.xml`. This package does not add a second launcher or change that navigation.

When a recovery entry is added later, it will be labeled **KV4P Classic** and reached from Devices. It stays until the new Radio slice passes hardware tests for connect, firmware compatibility, tune, receive, transmit, forced unkey, USB detach and reconnect, and logging context. Those tests have not been run. Compiling the debug APK does not satisfy them.

## Session ownership

No session coordinator class exists in this checkout. The rule below is the intended contract, not a description of current code.

Only one module may own a conflicting transmit path. Other modules may read shared frequency, mode, squelch, and the open QSO draft. The owner releases the transmitter on cancel, USB removal, timeout, or process death.
