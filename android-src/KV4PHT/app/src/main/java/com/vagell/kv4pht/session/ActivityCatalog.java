package com.vagell.kv4pht.session;

import com.vagell.kv4pht.radio.RadioCapabilities;

import java.util.ArrayList;
import java.util.List;

/** Beginner activities. Each row is enabled only when the connected radio can do it. */
public final class ActivityCatalog {
    public static final class Item {
        public final String title;
        public final boolean enabled;
        public final String reason;

        public Item(String title, boolean enabled, String reason) {
            this.title = title;
            this.enabled = enabled;
            this.reason = reason;
        }
    }

    private ActivityCatalog() {}

    public static List<Item> forDevice(RadioCapabilities radio) {
        List<Item> items = new ArrayList<>();
        items.add(row("Talk on a repeater", radio.analogFm && radio.transmit, "Needs an FM radio that can transmit."));
        items.add(row("Scan a memory bank", radio.scan, "This radio cannot scan."));
        items.add(row("Send or view APRS", radio.kiss && radio.ax25Modem, "Needs a KISS TNC."));
        items.add(row("Open packet mail", radio.kiss, "Packet mail needs KISS."));
        items.add(row("Check Winlink mail", false, "No Winlink session yet. Telnet CMS and RMS Packet are not connected."));
        boolean ft8Radio = radio.cat && radio.usbAudio && !radio.analogFm;
        items.add(row("Make an FT8 contact", false, ft8Radio
                ? "This radio can feed FT8, but the decoder is not in this build."
                : "FT8 needs a CAT radio with USB audio, such as a QDX. This connection is not that radio."));
        items.add(row("Start a JS8 conversation", false, "JS8 is not in this build."));
        items.add(row("Open a packet BBS", false, "Connected-mode AX.25 is not in this build."));
        items.add(row("Explore the spectrum", radio.iq, radio.iq ? "IQ waterfall." : "KV4P exposes demodulated audio, not an RF waterfall."));
        items.add(row("Log a contact", true, "Local log. Nothing is uploaded."));
        return items;
    }

    private static Item row(String title, boolean enabled, String reason) {
        return new Item(title, enabled, enabled ? "Ready on this radio." : reason);
    }
}
