package com.vagell.kv4pht.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RadioShellTest {
    @Test
    public void voicePttRequiresAReadyRadio() {
        assertFalse(TransmitGate.mayStartVoice(false, true, false, false));
        assertFalse(TransmitGate.mayStartVoice(true, false, false, false));
        assertFalse(TransmitGate.mayStartVoice(true, true, true, false));
        assertFalse(TransmitGate.mayStartVoice(true, true, false, true));
        assertTrue(TransmitGate.mayStartVoice(true, true, false, false));
    }

    @Test
    public void connectionStateDrivesTheShell() {
        assertEquals(RadioUiState.Operating.DISCONNECTED, RadioUiState.from(RadioConnectionState.DISCONNECTED, false, false).operating);
        assertEquals("RX", RadioUiState.from(RadioConnectionState.RECEIVING, true, true).statusLabel);
        assertTrue(RadioUiState.from(RadioConnectionState.TRANSMITTING, false, true).unkeyVisible);
        assertFalse(RadioUiState.from(RadioConnectionState.SCANNING, false, true).pttAvailable);
        assertEquals(RadioUiState.Operating.ERROR, RadioUiState.from(RadioConnectionState.FIRMWARE_INCOMPATIBLE, false, false).operating);
    }

    @Test
    public void traceExportOmitsMessageBodies() {
        RadioTrace trace = new RadioTrace();
        trace.add("tx-request", "voice");
        trace.add("tune", "146.5200");
        String text = trace.exportText();
        assertTrue(text.contains("tx-request"));
        assertTrue(text.contains("146.5200"));
        assertFalse(text.contains("message body"));
    }
}
