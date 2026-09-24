/*
AtleyHT connection-state model
Based on kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Copyright (C) 2026 Atley LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.vagell.kv4pht.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RadioConnectionStateResolverTest {

    @Test
    public void disconnectedWhenNoTransportOrCause() {
        RadioConnectionSnapshot snapshot =
            RadioConnectionStateResolver.resolve(RadioConnectionInputs.disconnected());
        assertEquals(RadioConnectionState.DISCONNECTED, snapshot.getState());
        assertEquals(RadioConnectionCause.NONE, snapshot.getCause());
        assertFalse(snapshot.isOperational());
    }

    @Test
    public void requestingUsbPermissionTakesPriorityOverDisconnected() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.STARTUP, true, false, false, false, false, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.REQUESTING_USB_PERMISSION, snapshot.getState());
    }

    @Test
    public void connectingWhileWaitingForHello() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.STARTUP, false, true, true, false, false, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.CONNECTING, snapshot.getState());
        assertEquals(RadioConnectionCause.NONE, snapshot.getCause());
    }

    @Test
    public void connectingWhenTransportPresentButNotReady() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.STARTUP, false, false, true, false, false, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.CONNECTING, snapshot.getState());
    }

    @Test
    public void flashingMapsToConnecting() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.FLASHING, false, false, true, false, false, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.CONNECTING, snapshot.getState());
        assertEquals(RadioConnectionCause.FLASHING, snapshot.getCause());
    }

    @Test
    public void connectedIdleWhenRxAndSquelched() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.RX, false, false, true, true, false, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.CONNECTED_IDLE, snapshot.getState());
        assertTrue(snapshot.isOperational());
    }

    @Test
    public void receivingWhenRxAndUnsquelched() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.RX, false, false, true, true, false, false, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.RECEIVING, snapshot.getState());
    }

    @Test
    public void transmittingFromHostPttMode() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.TX, false, false, true, true, true, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.TRANSMITTING, snapshot.getState());
    }

    @Test
    public void transmittingFromFirmwareTxFlagDuringRx() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.RX, false, false, true, true, true, false, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.TRANSMITTING, snapshot.getState());
    }

    @Test
    public void scanningWhenScanMode() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.SCAN, false, false, true, true, false, true, RadioConnectionCause.NONE
        ));
        assertEquals(RadioConnectionState.SCANNING, snapshot.getState());
    }

    @Test
    public void firmwareIncompatibleOnOutdatedFirmware() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.BAD_FIRMWARE, false, false, true, false, false, true,
            RadioConnectionCause.OUTDATED_FIRMWARE
        ));
        assertEquals(RadioConnectionState.FIRMWARE_INCOMPATIBLE, snapshot.getState());
        assertEquals(RadioConnectionCause.OUTDATED_FIRMWARE, snapshot.getCause());
    }

    @Test
    public void firmwareIncompatibleOnHelloTimeout() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.BAD_FIRMWARE, false, false, true, false, false, true,
            RadioConnectionCause.HELLO_TIMEOUT
        ));
        assertEquals(RadioConnectionState.FIRMWARE_INCOMPATIBLE, snapshot.getState());
        assertEquals(RadioConnectionCause.HELLO_TIMEOUT, snapshot.getCause());
    }

    @Test
    public void firmwareIncompatibleOnInvalidHello() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.BAD_FIRMWARE, false, false, true, false, false, true,
            RadioConnectionCause.INVALID_HELLO
        ));
        assertEquals(RadioConnectionState.FIRMWARE_INCOMPATIBLE, snapshot.getState());
    }

    @Test
    public void fatalErrorWhenRadioModuleMissing() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.BAD_FIRMWARE, false, false, true, false, false, true,
            RadioConnectionCause.RADIO_MODULE_NOT_FOUND
        ));
        assertEquals(RadioConnectionState.FATAL_ERROR, snapshot.getState());
        assertEquals(RadioConnectionCause.RADIO_MODULE_NOT_FOUND, snapshot.getCause());
    }

    @Test
    public void recoverableErrorOnUsbPermissionDenied() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.STARTUP, false, false, false, false, false, true,
            RadioConnectionCause.USB_PERMISSION_DENIED
        ));
        assertEquals(RadioConnectionState.RECOVERABLE_ERROR, snapshot.getState());
        assertEquals(RadioConnectionCause.USB_PERMISSION_DENIED, snapshot.getCause());
    }

    @Test
    public void recoverableErrorOnTransportError() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.STARTUP, false, false, false, false, false, true,
            RadioConnectionCause.TRANSPORT_ERROR
        ));
        assertEquals(RadioConnectionState.RECOVERABLE_ERROR, snapshot.getState());
    }

    @Test
    public void moduleMissingOutranksFirmwareIncompatible() {
        RadioConnectionSnapshot snapshot = RadioConnectionStateResolver.resolve(inputs(
            RadioMode.BAD_FIRMWARE, true, true, true, false, false, true,
            RadioConnectionCause.RADIO_MODULE_NOT_FOUND
        ));
        assertEquals(RadioConnectionState.FATAL_ERROR, snapshot.getState());
    }

    @Test
    public void snapshotEqualityDependsOnStateAndCause() {
        RadioConnectionSnapshot a = new RadioConnectionSnapshot(
            RadioConnectionState.CONNECTING, RadioConnectionCause.NONE);
        RadioConnectionSnapshot b = new RadioConnectionSnapshot(
            RadioConnectionState.CONNECTING, RadioConnectionCause.NONE);
        RadioConnectionSnapshot c = new RadioConnectionSnapshot(
            RadioConnectionState.CONNECTING, RadioConnectionCause.FLASHING);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    private static RadioConnectionInputs inputs(
            RadioMode mode,
            boolean usbPermissionPending,
            boolean waitingForHello,
            boolean transportPresent,
            boolean connectionReady,
            boolean deviceTxActive,
            boolean squelched,
            RadioConnectionCause cause
    ) {
        return new RadioConnectionInputs(
            mode,
            usbPermissionPending,
            waitingForHello,
            transportPresent,
            connectionReady,
            deviceTxActive,
            squelched,
            cause
        );
    }
}
