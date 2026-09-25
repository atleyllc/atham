package com.vagell.kv4pht.radio;

/** KV4P driver facade. Protocol code stays in RadioAudioService. */
public final class Kv4pDriver {
    private final RadioAudioService radio;
    private int squelch;

    public Kv4pDriver(RadioAudioService radio) {
        this.radio = radio;
    }

    public String frequency() {
        String value = radio.getActiveFrequencyStr();
        return value == null || value.isEmpty() ? "146.5200" : value;
    }

    public boolean connected() {
        return radio.isRadioConnected();
    }

    public boolean txAllowed() {
        return radio.isTxAllowed();
    }

    public int squelch() {
        return squelch;
    }

    public int meter() {
        return radio.getRadioModule().getSMeterBarValue();
    }

    public boolean squelchOpen() {
        return radio.isRadioConnected() && !radio.getRadioModule().isSquelched();
    }

    public void tune(String frequency) {
        radio.tuneToFreq(frequency);
    }

    public void setSquelch(int level) {
        int clamped = Math.max(0, Math.min(8, level));
        squelch = clamped;
        radio.getRadioModule().setSquelch(clamped);
    }

    public void startScan() {
        radio.setScanning(true);
    }

    public void stopScan() {
        radio.setScanning(false, true);
    }

    public boolean scanning() {
        return radio.getMode() == RadioMode.SCAN;
    }

    public boolean transmitting() {
        return radio.getMode() == RadioMode.TX;
    }

    public void holdPtt() {
        if (TransmitGate.mayStartVoice(connected(), txAllowed(), scanning(), transmitting())) {
            radio.startPtt();
        }
    }

    public void releasePtt() {
        radio.endPtt();
    }

    public void forceUnkey() {
        radio.forceUnkey();
    }
}
