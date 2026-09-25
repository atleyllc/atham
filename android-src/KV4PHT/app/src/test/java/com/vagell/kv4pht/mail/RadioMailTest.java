package com.vagell.kv4pht.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class RadioMailTest {
    @Test
    public void recognizesCallsignAndEmail() {
        assertTrue(RadioMail.isCallsign("KO6SAB"));
        assertTrue(RadioMail.isCallsign("W6MOW-1"));
        assertFalse(RadioMail.isCallsign("not a call"));
        assertTrue(RadioMail.isEmail("op@example.com"));
        assertFalse(RadioMail.isEmail("KO6SAB"));
    }

    @Test
    public void splitsLongMailIntoNumberedPackets() {
        String body = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        List<String> packets = RadioMail.packets("Net", body);
        assertTrue(packets.size() > 1);
        assertTrue(packets.get(0).startsWith("1/" + packets.size()));
        assertTrue(packets.get(packets.size() - 1).startsWith(packets.size() + "/" + packets.size()));
    }

    @Test
    public void emptyMailSendsNothing() {
        assertEquals(0, RadioMail.packets("", "  ").size());
    }
}
