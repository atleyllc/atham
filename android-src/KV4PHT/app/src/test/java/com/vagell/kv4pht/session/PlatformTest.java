package com.vagell.kv4pht.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.vagell.kv4pht.radio.DeviceProfiles;

import org.junit.Test;

public class PlatformTest {
    @Test
    public void voiceBlocksPacketUntilReleased() {
        SessionCoordinator sessions = new SessionCoordinator();
        assertTrue(sessions.tryAcquire(SessionCoordinator.Owner.VOICE));
        assertFalse(sessions.tryAcquire(SessionCoordinator.Owner.PACKET));
        sessions.release(SessionCoordinator.Owner.VOICE);
        assertTrue(sessions.tryAcquire(SessionCoordinator.Owner.PACKET));
    }

    @Test
    public void unkeyClearsTheOwner() {
        SessionCoordinator sessions = new SessionCoordinator();
        sessions.tryAcquire(SessionCoordinator.Owner.PACKET);
        sessions.forceUnkey();
        assertEquals(SessionCoordinator.Owner.NONE, sessions.owner());
        assertTrue(sessions.unkeyRequested());
        assertTrue(sessions.tryAcquire(SessionCoordinator.Owner.VOICE));
    }

    @Test
    public void kv4pEnablesFmPacketAndHidesSdrAndFt8() {
        ActivityCatalog.Item spectrum = find("Explore the spectrum");
        ActivityCatalog.Item ft8 = find("Make an FT8 contact");
        ActivityCatalog.Item winlink = find("Check Winlink mail");
        ActivityCatalog.Item aprs = find("Send or view APRS");
        assertFalse(DeviceProfiles.kv4p().iq);
        assertFalse(DeviceProfiles.kv4p().ssb);
        assertTrue(DeviceProfiles.kv4p().kiss);
        assertFalse(spectrum.enabled);
        assertFalse(ft8.enabled);
        assertFalse(winlink.enabled);
        assertTrue(aprs.enabled);
        assertTrue(find("Log a contact").enabled);
    }

    private static ActivityCatalog.Item find(String title) {
        for (ActivityCatalog.Item item : ActivityCatalog.forDevice(DeviceProfiles.kv4p())) {
            if (title.equals(item.title)) {
                return item;
            }
        }
        throw new AssertionError(title);
    }
}
