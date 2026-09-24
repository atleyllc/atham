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
 * Observable facts from the existing radio service used to derive
 * {@link RadioConnectionState}. Protocol bytes are not included.
 */
public final class RadioConnectionInputs {
    @NonNull
    public final RadioMode mode;
    public final boolean usbPermissionPending;
    public final boolean waitingForHello;
    public final boolean transportPresent;
    public final boolean connectionReady;
    public final boolean deviceTxActive;
    public final boolean squelched;
    @NonNull
    public final RadioConnectionCause cause;

    public RadioConnectionInputs(
            @NonNull RadioMode mode,
            boolean usbPermissionPending,
            boolean waitingForHello,
            boolean transportPresent,
            boolean connectionReady,
            boolean deviceTxActive,
            boolean squelched,
            @NonNull RadioConnectionCause cause
    ) {
        this.mode = mode;
        this.usbPermissionPending = usbPermissionPending;
        this.waitingForHello = waitingForHello;
        this.transportPresent = transportPresent;
        this.connectionReady = connectionReady;
        this.deviceTxActive = deviceTxActive;
        this.squelched = squelched;
        this.cause = cause;
    }

    public static RadioConnectionInputs disconnected() {
        return new RadioConnectionInputs(
            RadioMode.STARTUP,
            false,
            false,
            false,
            false,
            false,
            true,
            RadioConnectionCause.NONE
        );
    }
}
