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

/**
 * Why the current {@link RadioConnectionState} was selected. Used for UI copy
 * and diagnostics; never sent to firmware.
 */
public enum RadioConnectionCause {
    NONE,
    USB_PERMISSION_DENIED,
    TRANSPORT_ERROR,
    HELLO_TIMEOUT,
    INVALID_HELLO,
    OUTDATED_FIRMWARE,
    RADIO_MODULE_NOT_FOUND,
    FLASHING
}
