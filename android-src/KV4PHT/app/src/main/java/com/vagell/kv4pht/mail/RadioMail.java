/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Modified 2026 by Atley LLC: packet radio mail.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package com.vagell.kv4pht.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Splits operator mail into APRS-sized packet lines. Does not transmit. */
public final class RadioMail {
    public static final int CHUNK = 48;
    private static final Pattern CALLSIGN = Pattern.compile("^[A-Z0-9]{1,2}[0-9][A-Z0-9]{1,3}(-[0-9]{1,2})?$");

    private RadioMail() {}

    public static boolean isCallsign(String address) {
        if (address == null) {
            return false;
        }
        return CALLSIGN.matcher(address.trim().toUpperCase(Locale.US)).matches();
    }

    public static boolean isEmail(String address) {
        return address != null && address.contains("@") && address.indexOf('@') > 0;
    }

    public static List<String> packets(String subject, String body) {
        String safeSubject = subject == null ? "" : subject.replace('\n', ' ').trim();
        String safeBody = body == null ? "" : body.trim();
        String text = safeSubject.isEmpty() ? safeBody : safeSubject + " | " + safeBody;
        List<String> parts = new ArrayList<>();
        if (text.isEmpty()) {
            return parts;
        }
        int count = (text.length() + CHUNK - 1) / CHUNK;
        for (int i = 0; i < count; i++) {
            int start = i * CHUNK;
            int end = Math.min(text.length(), start + CHUNK);
            parts.add((i + 1) + "/" + count + " " + text.substring(start, end));
        }
        return parts;
    }
}
