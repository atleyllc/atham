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

import androidx.annotation.NonNull;

/**
 * Pure mapping from existing radio-service facts to {@link RadioConnectionState}.
 * Safe to unit-test without Android or USB hardware.
 */
public final class RadioConnectionStateResolver {
    private RadioConnectionStateResolver() {}

    @NonNull
    public static RadioConnectionSnapshot resolve(@NonNull RadioConnectionInputs inputs) {
        if (inputs.cause == RadioConnectionCause.RADIO_MODULE_NOT_FOUND) {
            return snapshot(RadioConnectionState.FATAL_ERROR, inputs.cause);
        }
        if (isFirmwareIncompatible(inputs)) {
            return snapshot(RadioConnectionState.FIRMWARE_INCOMPATIBLE, firmwareCause(inputs));
        }
        if (inputs.usbPermissionPending) {
            return snapshot(RadioConnectionState.REQUESTING_USB_PERMISSION, RadioConnectionCause.NONE);
        }
        if (isRecoverableError(inputs)) {
            return snapshot(RadioConnectionState.RECOVERABLE_ERROR, inputs.cause);
        }
        if (isConnecting(inputs)) {
            RadioConnectionCause cause = inputs.mode == RadioMode.FLASHING
                ? RadioConnectionCause.FLASHING
                : RadioConnectionCause.NONE;
            return snapshot(RadioConnectionState.CONNECTING, cause);
        }
        if (inputs.connectionReady) {
            if (inputs.mode == RadioMode.TX || inputs.deviceTxActive) {
                return snapshot(RadioConnectionState.TRANSMITTING, RadioConnectionCause.NONE);
            }
            if (inputs.mode == RadioMode.SCAN) {
                return snapshot(RadioConnectionState.SCANNING, RadioConnectionCause.NONE);
            }
            if (inputs.mode == RadioMode.RX && !inputs.squelched) {
                return snapshot(RadioConnectionState.RECEIVING, RadioConnectionCause.NONE);
            }
            return snapshot(RadioConnectionState.CONNECTED_IDLE, RadioConnectionCause.NONE);
        }
        return snapshot(RadioConnectionState.DISCONNECTED, RadioConnectionCause.NONE);
    }

    private static boolean isFirmwareIncompatible(RadioConnectionInputs inputs) {
        return inputs.mode == RadioMode.BAD_FIRMWARE
            || inputs.cause == RadioConnectionCause.HELLO_TIMEOUT
            || inputs.cause == RadioConnectionCause.INVALID_HELLO
            || inputs.cause == RadioConnectionCause.OUTDATED_FIRMWARE;
    }

    private static RadioConnectionCause firmwareCause(RadioConnectionInputs inputs) {
        if (inputs.cause == RadioConnectionCause.HELLO_TIMEOUT
            || inputs.cause == RadioConnectionCause.INVALID_HELLO
            || inputs.cause == RadioConnectionCause.OUTDATED_FIRMWARE) {
            return inputs.cause;
        }
        return RadioConnectionCause.OUTDATED_FIRMWARE;
    }

    private static boolean isRecoverableError(RadioConnectionInputs inputs) {
        return inputs.cause == RadioConnectionCause.USB_PERMISSION_DENIED
            || inputs.cause == RadioConnectionCause.TRANSPORT_ERROR;
    }

    private static boolean isConnecting(RadioConnectionInputs inputs) {
        if (inputs.mode == RadioMode.FLASHING) {
            return true;
        }
        if (inputs.waitingForHello) {
            return true;
        }
        return inputs.transportPresent && !inputs.connectionReady;
    }

    private static RadioConnectionSnapshot snapshot(
            RadioConnectionState state,
            RadioConnectionCause cause
    ) {
        return new RadioConnectionSnapshot(state, cause);
    }
}
