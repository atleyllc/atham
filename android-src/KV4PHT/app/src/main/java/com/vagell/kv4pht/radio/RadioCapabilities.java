package com.vagell.kv4pht.radio;

/** What a connected radio can actually do. Feature screens read this, not the device name. */
public final class RadioCapabilities {
    public final String id;
    public final String displayName;
    public final boolean receive;
    public final boolean transmit;
    public final boolean analogFm;
    public final boolean ssb;
    public final boolean cat;
    public final boolean usbAudio;
    public final boolean iq;
    public final boolean kiss;
    public final boolean ax25Modem;
    public final boolean rssi;
    public final boolean tone;
    public final boolean scan;

    public RadioCapabilities(String id, String displayName, boolean receive, boolean transmit,
                             boolean analogFm, boolean ssb, boolean cat, boolean usbAudio, boolean iq,
                             boolean kiss, boolean ax25Modem, boolean rssi, boolean tone, boolean scan) {
        this.id = id;
        this.displayName = displayName;
        this.receive = receive;
        this.transmit = transmit;
        this.analogFm = analogFm;
        this.ssb = ssb;
        this.cat = cat;
        this.usbAudio = usbAudio;
        this.iq = iq;
        this.kiss = kiss;
        this.ax25Modem = ax25Modem;
        this.rssi = rssi;
        this.tone = tone;
        this.scan = scan;
    }
}
