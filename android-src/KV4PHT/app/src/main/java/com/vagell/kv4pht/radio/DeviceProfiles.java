package com.vagell.kv4pht.radio;

/** Known radios. Only KV4P is connected in this build. */
public final class DeviceProfiles {
    private DeviceProfiles() {}

    public static RadioCapabilities kv4p() {
        return new RadioCapabilities(
                "kv4p", "KV4P HT",
                true, true, true, false, false, true, false, true, true, true, true, true);
    }

    public static RadioCapabilities qdx() {
        return new RadioCapabilities(
                "qdx", "QRP Labs QDX",
                true, true, false, false, true, true, false, false, false, false, false, false);
    }

    public static RadioCapabilities rtlSdr() {
        return new RadioCapabilities(
                "rtl-sdr", "RTL-SDR",
                true, false, false, false, false, false, true, false, false, true, false, true);
    }

    public static RadioCapabilities connected() {
        return kv4p();
    }
}
