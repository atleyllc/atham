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

import java.util.Objects;

/**
 * Immutable view of the current radio connection state.
 */
public final class RadioConnectionSnapshot {
    @NonNull
    private final RadioConnectionState state;
    @NonNull
    private final RadioConnectionCause cause;

    public RadioConnectionSnapshot(
            @NonNull RadioConnectionState state,
            @NonNull RadioConnectionCause cause
    ) {
        this.state = state;
        this.cause = cause;
    }

    @NonNull
    public RadioConnectionState getState() {
        return state;
    }

    @NonNull
    public RadioConnectionCause getCause() {
        return cause;
    }

    public boolean isOperational() {
        return state == RadioConnectionState.CONNECTED_IDLE
            || state == RadioConnectionState.RECEIVING
            || state == RadioConnectionState.TRANSMITTING
            || state == RadioConnectionState.SCANNING;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RadioConnectionSnapshot)) {
            return false;
        }
        RadioConnectionSnapshot that = (RadioConnectionSnapshot) other;
        return state == that.state && cause == that.cause;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, cause);
    }

    @Override
    public String toString() {
        return "RadioConnectionSnapshot{state=" + state + ", cause=" + cause + '}';
    }
}
