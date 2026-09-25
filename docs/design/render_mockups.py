#!/usr/bin/env python3
"""Render ATHAM phone-screen mockups. Visual artifacts only; not app code."""
from pathlib import Path
OUT = Path(__file__).resolve().parent
BG, GRAPHITE, GRAPHITE_2 = "#0C0D0B", "#181A17", "#22241F"
LINE, GOLD, GOLD_DIM = "#34362F", "#C4A46A", "#8A7044"
TEXT, MUTED, RX, TX, ERR = "#F3F0E8", "#9A968C", "#8FAF7A", "#E0B15A", "#C46B5A"
W, H, LW, LH = 390, 844, 844, 390

def esc(text):
    return str(text).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

class Svg:
    def __init__(self, width, height):
        self.w, self.h = width, height
        self.parts = [
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
            "<style>text{font-family:'Avenir Next','Segoe UI',Helvetica,Arial,sans-serif}</style>",
        ]
    def rect(self, x, y, w, h, fill, rx=0, stroke=None, sw=1):
        extra = f' stroke="{stroke}" stroke-width="{sw}"' if stroke else ""
        self.parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}"{extra}/>')
    def text(self, x, y, value, size=14, fill=TEXT, weight=500, anchor="start"):
        self.parts.append(f'<text x="{x}" y="{y}" fill="{fill}" font-size="{size}" font-weight="{weight}" text-anchor="{anchor}">{esc(value)}</text>')
    def line(self, x1, y1, x2, y2, stroke=LINE, sw=1):
        self.parts.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{stroke}" stroke-width="{sw}"/>')
    def circle(self, cx, cy, r, fill):
        self.parts.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{fill}"/>')
    def save(self, name):
        self.parts.append("</svg>")
        path = OUT / name
        path.write_text("\n".join(self.parts) + "\n", encoding="utf-8")

def chrome(svg, active, width=W):
    svg.rect(0, 0, width, svg.h, BG)
    svg.text(20, 28, "9:41", 13, MUTED, 600)
    svg.text(width - 20, 28, "LTE", 12, MUTED, 500, "end")
    svg.rect(0, 40, width, 56, GRAPHITE)
    svg.line(0, 96, width, 96)
    svg.text(16, 62, "N0CALL", 13, GOLD, 650)
    svg.text(16, 82, "KV4P HT  ·  FM", 12, MUTED, 500)
    svg.text(width / 2, 74, "146.5200", 18, TEXT, 650, "middle")
    svg.rect(width - 92, 52, 76, 32, "#2A2418", 6, GOLD, 1.5)
    svg.text(width - 54, 73, "UNKEY", 12, GOLD, 700, "middle")
    nav_y = svg.h - 72
    svg.rect(0, nav_y, width, 72, GRAPHITE)
    svg.line(0, nav_y, width, nav_y)
    for i, label in enumerate(["Home", "Radio", "Log", "Modules"]):
        cx = width * (i + 0.5) / 4
        fill = GOLD if label == active else MUTED
        svg.circle(cx, nav_y + 22, 3, fill)
        svg.text(cx, nav_y + 46, label, 13, fill, 650 if label == active else 500, "middle")
    return 108, nav_y

def chip(svg, x, y, label, fill, text=BG):
    width = max(64, 8 * len(label) + 20)
    svg.rect(x, y, width, 26, fill, 13)
    svg.text(x + width / 2, y + 17, label, 11, text, 700, "middle")

def card(svg, x, y, w, h):
    svg.rect(x, y, w, h, GRAPHITE, 12)

def home(name, connected=True):
    svg = Svg(W, H)
    top, nav = chrome(svg, "Home")
    y = top + 16
    card(svg, 16, y, 358, 132)
    svg.circle(36, y + 28, 5, RX if connected else ERR)
    svg.text(50, y + 32, "Connected" if connected else "Disconnected", 13, RX if connected else ERR, 650)
    svg.text(32, y + 68, "146.5200 MHz" if connected else "No frequency", 28, TEXT if connected else MUTED, 650)
    svg.text(32, y + 94, "FM   ·   SIMPLEX   ·   Squelch 2   ·   Tone off", 13, MUTED, 500)
    svg.text(32, y + 116, "VFO A   ·   Radio owns the path" if connected else "USB detached   ·   PTT blocked", 12, MUTED, 500)
    y += 148
    svg.text(16, y, "Station", 12, GOLD, 650)
    y += 12
    card(svg, 16, y, 358, 78)
    svg.text(32, y + 30, "N0CALL", 20, TEXT, 650)
    svg.text(32, y + 54, "Grid CN87   ·   UTC 16:07   ·   Clock quality good", 13, MUTED, 500)
    y += 98
    svg.text(16, y, "Launch", 12, GOLD, 650)
    y += 12
    for title, detail in (
        ("Talk on a repeater", "Opens Radio on the current VFO"),
        ("Scan a saved bank", "Opens Radio scan on this device"),
        ("Log a contact", "Opens VoiceLog on this session"),
    ):
        card(svg, 16, y, 358, 64)
        svg.text(32, y + 28, title, 16, TEXT, 600)
        svg.text(32, y + 48, detail, 12, MUTED, 500)
        y += 72
    svg.text(16, nav - 28, "Recent   ·   SIMPLEX monitored 2 min", 12, MUTED, 500)
    svg.save(name)

def radio(name, state):
    svg = Svg(W, H)
    top, nav = chrome(svg, "Radio")
    y = top + 18
    states = {
        "ready": ("READY", GRAPHITE_2, MUTED),
        "rx": ("RECEIVE", "#243024", RX),
        "tx": ("TRANSMIT", "#3A2E14", TX),
        "scan": ("SCAN", "#2A2418", GOLD),
        "error": ("ERROR", "#3A221C", ERR),
    }
    chip(svg, 16, y, *states[state])
    y += 44
    svg.text(195, y + 48, "146.5200", 52, ERR if state == "error" else TEXT, 650, "middle")
    svg.text(195, y + 76, "MHz", 14, MUTED, 600, "middle")
    y += 100
    svg.text(195, y, "FM    SIMPLEX    Tone off    Squelch 2", 13, MUTED, 500, "middle")
    y += 28
    card(svg, 16, y, 174, 72)
    card(svg, 200, y, 174, 72)
    svg.text(32, y + 28, "VFO A", 12, GOLD, 650)
    svg.text(32, y + 50, "Scanning" if state == "scan" else "Active", 16, TEXT, 600)
    svg.text(216, y + 28, "Bank" if state == "scan" else "VFO B", 12, MUTED, 650)
    svg.text(216, y + 50, "SIMPLEX" if state == "scan" else "446.000", 16, TEXT if state == "scan" else MUTED, 600)
    y += 88
    copy = {
        "ready": ("Quiet", TEXT, "Squelch closed    ·    no carrier"),
        "rx": ("Carrier", RX, "S 7    ·    audio open    ·    squelch open"),
        "tx": ("Keyed    00:04", TX, "Timeout 2:56    ·    release or tap UNKEY"),
        "scan": ("Memory bank", GOLD, "SIMPLEX held    ·    next 146.940"),
        "error": ("USB audio stalled", ERR, "Radio still connected    ·    PTT blocked"),
    }
    title, color, detail = copy[state]
    card(svg, 16, y, 358, 64)
    svg.text(32, y + 26, title, 14, color, 650)
    svg.text(32, y + 46, detail, 13, MUTED, 500)
    y += 84
    svg.text(16, y, "Signal", 12, GOLD, 650)
    bars = {"ready": [6, 8, 5, 7, 4, 6, 5, 4], "rx": [18, 28, 40, 34, 22, 16, 12, 8], "tx": [8, 8, 8, 8, 8, 8, 8, 8], "scan": [10, 14, 18, 12, 8, 8, 8, 8], "error": [4, 4, 4, 4, 4, 4, 4, 4]}[state]
    y += 12
    for i, h in enumerate(bars):
        svg.rect(16 + i * 46, y + 36 - h, 36, h, GOLD if state in ("rx", "tx", "scan") else GRAPHITE_2, 2)
    y += 56
    for i, (label, value) in enumerate((("Offset", "None"), ("Step", "5 kHz"), ("Power", "High"), ("Filter", "25 kHz"))):
        x = 16 + i * 92
        svg.text(x, y, label, 11, MUTED, 500)
        svg.text(x, y + 18, value, 13, TEXT, 600)
    ptt_y = nav - 88
    if state == "tx":
        svg.rect(16, ptt_y, 358, 72, "#3A2E14", 12, TX, 2)
        svg.text(195, ptt_y + 42, "PTT HELD", 18, TX, 700, "middle")
    elif state == "error":
        svg.rect(16, ptt_y, 358, 72, GRAPHITE, 12, LINE, 1)
        svg.text(195, ptt_y + 42, "PTT BLOCKED", 18, MUTED, 700, "middle")
    else:
        svg.rect(16, ptt_y, 358, 72, "#2A2418", 12, GOLD, 1.5)
        svg.text(195, ptt_y + 42, "HOLD TO TALK", 18, GOLD, 700, "middle")
    svg.save(name)

def modules():
    svg = Svg(W, H)
    top, nav = chrome(svg, "Modules")
    y = top + 20
    svg.text(16, y, "Modules", 22, TEXT, 650)
    svg.text(16, y + 24, "Radio stays on 146.5200", 13, MUTED, 500)
    y += 52
    rows = [
        ("Home", "Docked", "Station overview", GOLD),
        ("Radio", "Docked · owns path", "VFO, scan, PTT", GOLD),
        ("Log", "Docked", "Shared contacts", GOLD),
        ("VoiceLog", "Pinned · can observe", "Draft on this frequency", TEXT),
        ("Packet", "Available", "APRS and AX.25 on KV4P", TEXT),
        ("Devices", "Available", "KV4P USB, firmware, classic UI", TEXT),
        ("Digital", "Needs USB audio + CAT", "Not on this KV4P path", MUTED),
        ("Mail", "Needs Winlink transport", "Not on this KV4P path yet", MUTED),
        ("Mesh", "Needs a Reticulum carrier", "No mesh interface", MUTED),
        ("Spectrum", "Audio FFT only", "KV4P has no IQ source", MUTED),
        ("Maps", "Needs location", "Shared map not built", MUTED),
        ("Tools", "Later", "Band plan and time tools", MUTED),
    ]
    for title, status, detail, color in rows:
        svg.text(16, y, title, 15, color, 650)
        svg.text(374, y, status, 11, MUTED, 500, "end")
        svg.text(16, y + 16, detail, 11, MUTED, 500)
        svg.line(16, y + 26, 374, y + 26)
        y += 38
    svg.save("06-module-switcher.svg")

def voicelog():
    svg = Svg(W, H)
    top, nav = chrome(svg, "Modules")
    y = top + 16
    chip(svg, 16, y, "OBSERVING", "#243024", RX)
    svg.text(140, y + 17, "Does not own transmit", 12, MUTED, 500)
    y += 42
    svg.text(16, y, "VoiceLog", 22, TEXT, 650)
    svg.text(16, y + 24, "2 m  ·  FM  ·  SIMPLEX  ·  16:07 UTC", 13, MUTED, 500)
    y += 48
    card(svg, 16, y, 358, 78)
    svg.text(32, y + 26, "Hearing", 12, GOLD, 650)
    svg.text(32, y + 52, "K7T, you are five nine, name is Alex.", 14, TEXT, 500)
    y += 94
    svg.text(16, y, "Draft", 12, GOLD, 650)
    y += 12
    fields = [("Callsign", "K7T", "Confirm"), ("Report", "59", "Heard"), ("Name", "Alex", "Heard"), ("Frequency", "146.5200", "Radio"), ("Mode", "FM", "Radio"), ("Grid", "CN87", "Station")]
    for i, (label, value, source) in enumerate(fields):
        x = 16 + (i % 2) * 184
        yy = y + (i // 2) * 72
        card(svg, x, yy, 174, 64)
        svg.text(x + 14, yy + 20, label, 11, MUTED, 500)
        svg.text(x + 14, yy + 42, value, 16, TEXT, 650)
        svg.text(x + 160, yy + 42, source, 10, GOLD_DIM, 500, "end")
    svg.text(16, nav - 112, "Uncertain fields stay marked until you correct them.", 12, MUTED, 500)
    svg.rect(16, nav - 88, 170, 56, GRAPHITE_2, 10, LINE, 1)
    svg.text(101, nav - 54, "Correct", 16, TEXT, 650, "middle")
    svg.rect(204, nav - 88, 170, 56, "#2A2418", 10, GOLD, 1.5)
    svg.text(289, nav - 54, "Finalize", 16, GOLD, 650, "middle")
    svg.save("07-voicelog.svg")

def sequence():
    svg = Svg(1170, 844)
    svg.rect(0, 0, 1170, 844, "#070807")
    for origin, label, active, body in (
        (0, "1  Radio", "Radio", "radio"),
        (390, "2  Modules", "Modules", "modules"),
        (780, "3  VoiceLog", "Modules", "voice"),
    ):
        svg.text(origin + 16, 24, label, 13, GOLD, 650)
        svg.rect(origin, 36, 390, 808, BG)
        svg.rect(origin, 76, 390, 56, GRAPHITE)
        svg.text(origin + 16, 98, "N0CALL", 13, GOLD, 650)
        svg.text(origin + 16, 118, "KV4P HT  ·  FM", 12, MUTED, 500)
        svg.text(origin + 195, 108, "146.5200", 18, TEXT, 650, "middle")
        svg.rect(origin + 298, 88, 76, 32, "#2A2418", 6, GOLD, 1.5)
        svg.text(origin + 336, 109, "UNKEY", 12, GOLD, 700, "middle")
        nav = 772
        svg.rect(origin, nav, 390, 72, GRAPHITE)
        for i, item in enumerate(["Home", "Radio", "Log", "Modules"]):
            cx = origin + 390 * (i + 0.5) / 4
            fill = GOLD if item == active else MUTED
            svg.text(cx, nav + 42, item, 13, fill, 650 if item == active else 500, "middle")
        if body == "radio":
            svg.text(origin + 195, 240, "146.5200", 48, TEXT, 650, "middle")
            svg.text(origin + 195, 274, "READY  ·  FM  ·  SIMPLEX", 14, MUTED, 500, "middle")
            svg.rect(origin + 16, 660, 358, 72, "#2A2418", 12, GOLD, 1.5)
            svg.text(origin + 195, 702, "HOLD TO TALK", 18, GOLD, 700, "middle")
        elif body == "modules":
            svg.text(origin + 16, 180, "VoiceLog", 20, TEXT, 650)
            svg.text(origin + 16, 208, "Observe 146.5200", 14, MUTED, 500)
            svg.text(origin + 16, 248, "Radio still owns the path", 15, GOLD, 600)
            svg.text(origin + 16, 276, "VFO and draft stay put", 13, MUTED, 500)
        else:
            svg.text(origin + 16, 180, "VoiceLog", 22, TEXT, 650)
            svg.text(origin + 16, 208, "Observing  ·  draft kept", 14, RX, 600)
            svg.text(origin + 16, 252, "Callsign   K7T", 16, TEXT, 600)
            svg.text(origin + 16, 278, "Frequency still 146.5200", 14, MUTED, 500)
            svg.text(origin + 16, 304, "Mode FM   ·   SIMPLEX", 14, MUTED, 500)
    svg.save("08-radio-modules-voicelog.svg")

def landscape():
    svg = Svg(LW, LH)
    svg.rect(0, 0, LW, LH, BG)
    svg.rect(0, 0, LW, 48, GRAPHITE)
    svg.text(16, 30, "N0CALL", 13, GOLD, 650)
    svg.text(100, 30, "KV4P HT", 13, MUTED, 500)
    svg.text(330, 30, "146.5200  FM  SIMPLEX", 15, TEXT, 650, "middle")
    svg.rect(LW - 108, 10, 92, 28, "#2A2418", 6, GOLD, 1.5)
    svg.text(LW - 62, 29, "UNKEY", 12, GOLD, 700, "middle")
    svg.text(28, 120, "146.5200", 56, TEXT, 650)
    svg.text(28, 154, "VFO A     Squelch 2     Tone off     Quiet", 15, MUTED, 500)
    svg.rect(28, 180, 140, 72, GRAPHITE, 10)
    svg.text(44, 208, "VFO A", 12, GOLD, 650)
    svg.text(44, 232, "Active", 16, TEXT, 600)
    svg.rect(180, 180, 140, 72, GRAPHITE, 10)
    svg.text(196, 208, "VFO B", 12, MUTED, 650)
    svg.text(196, 232, "446.000", 16, MUTED, 600)
    svg.rect(28, 270, 400, 56, GRAPHITE, 10)
    svg.text(44, 304, "Offset none    Step 5 kHz    Power high", 14, TEXT, 500)
    svg.rect(500, 72, 316, 250, GRAPHITE, 12)
    svg.text(658, 160, "HOLD TO TALK", 20, GOLD, 700, "middle")
    svg.text(658, 192, "Radio owns the path", 13, MUTED, 500, "middle")
    svg.text(658, 240, "Squelch 2  ·  signal quiet", 14, TEXT, 600, "middle")
    svg.rect(0, LH - 48, LW, 48, GRAPHITE)
    for i, label in enumerate(["Home", "Radio", "Log", "Modules"]):
        fill = GOLD if label == "Radio" else MUTED
        svg.text(120 + i * 180, LH - 18, label, 14, fill, 650 if label == "Radio" else 500, "middle")
    svg.save("09-radio-landscape.svg")

if __name__ == "__main__":
    home("01-home-station.svg", True)
    home("01b-home-disconnected.svg", False)
    radio("02-radio-ready.svg", "ready")
    radio("03-radio-receiving.svg", "rx")
    radio("04-radio-transmitting.svg", "tx")
    radio("05-radio-scanning.svg", "scan")
    radio("05b-radio-error.svg", "error")
    modules()
    voicelog()
    sequence()
    landscape()
