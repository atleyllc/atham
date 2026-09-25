# ATHAM mockups

Rendered phone screens for the proposed shell. These are drawings, not Android layouts.

Shared state on every connected screen: N0CALL, KV4P HT, 146.5200 MHz, FM, SIMPLEX, squelch 2, tone off.

| File | Screen |
| --- | --- |
| `01-home-station.svg` | Home / Station, connected |
| `01b-home-disconnected.svg` | Home, USB detached |
| `02-radio-ready.svg` | Radio, ready |
| `03-radio-receiving.svg` | Radio, receiving |
| `04-radio-transmitting.svg` | Radio, transmitting, UNKEY visible |
| `05-radio-scanning.svg` | Radio, scanning |
| `05b-radio-error.svg` | Radio, USB audio stalled, PTT blocked |
| `06-module-switcher.svg` | Modules catalog |
| `07-voicelog.svg` | VoiceLog observing the Radio session |
| `08-radio-modules-voicelog.svg` | Radio, then Modules, then VoiceLog, same frequency |
| `09-radio-landscape.svg` | Landscape Radio |

Tokens are in [tokens.md](tokens.md). Regenerate with `python3 docs/design/render_mockups.py`.
