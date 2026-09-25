package com.vagell.kv4pht.radio;

/** Pure check before voice PTT. The service still enforces HOST_STATE_TX_ALLOWED. */
public final class TransmitGate {
    private TransmitGate() {}

    public static boolean mayStartVoice(boolean connected, boolean txAllowed, boolean scanning, boolean alreadyTransmitting) {
        return connected && txAllowed && !scanning && !alreadyTransmitting;
    }
}
