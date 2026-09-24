/*
Atley KV4P connection-state model
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

/**
 * Authoritative radio connection states for UI and diagnostics.
 * Derived from existing {@link RadioMode} and transport flags; does not change
 * the USB/KISS/KV4P protocol.
 */
public enum RadioConnectionState {
    DISCONNECTED,
    REQUESTING_USB_PERMISSION,
    CONNECTING,
    CONNECTED_IDLE,
    RECEIVING,
    TRANSMITTING,
    SCANNING,
    FIRMWARE_INCOMPATIBLE,
    RECOVERABLE_ERROR,
    FATAL_ERROR
}
