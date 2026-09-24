# Identifiers and branding migration

Working product name: **Atley KV4P** (provisional). Visible upstream credit must remain. Do **not** change these identifiers until a dedicated migration is approved.

## What each identifier controls

| Identifier | Current value | Controls | Risk if changed |
|------------|---------------|----------|-----------------|
| `applicationId` | `com.vagell.kv4pht` | Installed-app identity, Play updates, USB permission `PendingIntent`s | Existing installs will not update in place; users keep two apps and two data stores |
| `namespace` / Java package | `com.vagell.kv4pht` | Source packages, `R`, manifest names | Safe only with a coordinated rename; easy to miss broadcasts |
| USB permission action | `com.vagell.kv4pht.USB_PERMISSION` | Runtime USB grant | Permission dialogs and attach flow break if mismatched |
| USB vendor/product IDs | 4292/60000, 6790/29987 (runtime); plus 9114/33041 in `device_filter.xml` | Device discovery and auto-launch | Radio will not attach |
| Service / UI intent actions | `com.vagell.kv4pht.OPEN_CHAT_ACTION`, `SETTINGS_ACTION`, `FIRMWARE_ACTION`, `ADD_MEMORY_ACTION`, `EDIT_MEMORY_ACTION`, `FIND_REPEATERS`, `STOP_RADIO_SERVICE`, `SERVICE_STOPPING` | Deep links and inter-activity intents | Settings, flash, memories, and chat entry break |
| Notification channels | `KV4P_HT_RADIO_AUDIO`, `aprs_message_notifications` | Android notification settings | Users lose channel preferences |
| Room DB file | `kv4pht-db` | Memories, settings, APRS history | Data appears empty unless migrated |
| AppSetting keys | `callsign`, TX limits, APRS keys, etc. | User preferences | Settings reset |
| Protocol vendor | ASCII `"KV4P"` + version `0x01` | USB/BLE framing | Handshake fails; do not change without firmware |
| Firmware version | `17` | Flash gate and HELLO | False "update required" or unsafe mismatch |
| BLE GATT UUIDs | `00000001-ba2a-46c9-ae49-01b0961f68bb` and TX/RX chars | BLE KISS | BLE devices disappear |
| APRS software / message token | `KV4P` string, `BLN1CQ`, `WIDE1-1`/`WIDE2-1` | On-air packet identity | Interop and user recognition; changing is an on-air behavior change |
| Debug keystore alias | `kv4p-ci-debug` | CI/debug install upgrades | Debug APKs will not update each other |
| Play listing / icon | Official kv4p HT branding | Store identity | Not changed in this phase |

## What is safe to change now

- User-visible title strings **in addition to** upstream credit (for example an about line: "Atley KV4P, based on kv4p HT by Vance Vagell").
- Documentation and repository description.
- Derived UI state labels (this branch).

## What must wait

- `applicationId` and package rename
- App icon
- USB and protocol identifiers
- Firmware identifiers
- Play listing as a different app unless a side-by-side install is an intentional product decision

## Proposed later migration (not in this phase)

1. Keep `com.vagell.kv4pht` while the fork is a development build of the same hardware protocol.
2. If a distinct Play app is required, use a **new** `applicationId` such as `com.atley.kv4p` and treat it as a new install. Provide an explicit memory/APRS export/import first.
3. Keep protocol `"KV4P"` and USB IDs forever unless hardware changes; those identify the radio, not the fork.
4. After data export exists, optionally migrate Room from `kv4pht-db` to a new name.
5. Change the launcher label only after an about screen still names kv4p HT and Vance Vagell.
6. Sign production builds with a private Atley key. Do not reuse the public CI debug keystore.

Until that plan is executed, this app remains protocol-compatible with official kv4p HT hardware and firmware.
