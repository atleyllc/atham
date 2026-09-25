package com.vagell.kv4pht.radio;

/** What the new shell renders. Built from the service, not from view state. */
public final class RadioUiState {
    public enum Operating { DISCONNECTED, READY, RECEIVING, TRANSMITTING, SCANNING, ERROR }

    public final Operating operating;
    public final String statusLabel;
    public final String detail;
    public final boolean pttAvailable;
    public final boolean unkeyVisible;

    public RadioUiState(Operating operating, String statusLabel, String detail, boolean pttAvailable, boolean unkeyVisible) {
        this.operating = operating;
        this.statusLabel = statusLabel;
        this.detail = detail;
        this.pttAvailable = pttAvailable;
        this.unkeyVisible = unkeyVisible;
    }

    public static RadioUiState from(RadioConnectionState connection, boolean squelchOpen, boolean txAllowed) {
        if (connection == null) {
            return new RadioUiState(Operating.DISCONNECTED, "NO RADIO", "Connect the KV4P.", false, false);
        }
        switch (connection) {
            case TRANSMITTING:
                return new RadioUiState(Operating.TRANSMITTING, "TX", "Transmit frequency is the one on screen.", false, true);
            case SCANNING:
                return new RadioUiState(Operating.SCANNING, "SCAN", "PTT is hidden while scan owns the radio.", false, false);
            case RECEIVING:
                return new RadioUiState(Operating.RECEIVING, "RX", squelchOpen ? "Squelch open." : "Carrier.", txAllowed, false);
            case CONNECTED_IDLE:
                return new RadioUiState(Operating.READY, "READY", squelchOpen ? "Squelch open." : "Squelch closed.", txAllowed, false);
            case REQUESTING_USB_PERMISSION:
                return new RadioUiState(Operating.DISCONNECTED, "PERMISSION", "Allow USB access to the radio.", false, false);
            case CONNECTING:
                return new RadioUiState(Operating.DISCONNECTED, "CONNECTING", "Waiting for the radio.", false, false);
            case FIRMWARE_INCOMPATIBLE:
                return new RadioUiState(Operating.ERROR, "FIRMWARE", "Firmware does not match this app.", false, false);
            case RECOVERABLE_ERROR:
            case FATAL_ERROR:
                return new RadioUiState(Operating.ERROR, "ERROR", "Radio link failed. Reconnect USB.", false, true);
            case DISCONNECTED:
            default:
                return new RadioUiState(Operating.DISCONNECTED, "NO RADIO", "Connect the KV4P.", false, false);
        }
    }
}
