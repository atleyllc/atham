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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Splits operator mail into APRS-sized packet lines. Does not transmit. */
public final class RadioMail {
    public static final int CHUNK = 48;
    private static final Pattern CALLSIGN = Pattern.compile("^[A-Z0-9]{1,2}[0-9][A-Z0-9]{1,3}(-[0-9]{1,2})?$");
    private static final Pattern PART = Pattern.compile("^(\\d+)/(\\d+) (.*)$", Pattern.DOTALL);
    private static final Assembler INBOX = new Assembler();

    private RadioMail() {}

    public static Assembler inbox() {
        return INBOX;
    }

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

    /** One numbered line, or null when the text is ordinary chat. */
    public static Part part(String line) {
        if (line == null) {
            return null;
        }
        Matcher matcher = PART.matcher(line.replace("\r", ""));
        if (!matcher.matches()) {
            return null;
        }
        int index = Integer.parseInt(matcher.group(1));
        int count = Integer.parseInt(matcher.group(2));
        if (count < 1 || count > 32 || index < 1 || index > count) {
            return null;
        }
        return new Part(index, count, matcher.group(3));
    }

    /** Subject and body joined the same way {@link #packets} writes them. */
    public static String[] subjectAndBody(String joined) {
        if (joined == null) {
            return new String[]{"", ""};
        }
        int bar = joined.indexOf(" | ");
        if (bar < 0) {
            return new String[]{"", joined};
        }
        return new String[]{joined.substring(0, bar), joined.substring(bar + 3)};
    }

    public static List<String> recipients(String address) {
        List<String> out = new ArrayList<>();
        if (address == null) {
            return out;
        }
        for (String piece : address.split("[,;]")) {
            String trimmed = piece.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    public static boolean matches(String address, String subject, String body, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.US);
        return contains(address, needle) || contains(subject, needle) || contains(body, needle);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.US).contains(needle);
    }

    public static final class Part {
        public final int index;
        public final int count;
        public final String text;

        Part(int index, int count, String text) {
            this.index = index;
            this.count = count;
            this.text = text;
        }
    }

    /** Reassembles numbered lines from one station. Thread-safe. */
    public static final class Assembler {
        private final Map<String, String[]> open = new LinkedHashMap<>();
        private final Map<String, Boolean> filed = new LinkedHashMap<>();

        public synchronized String offer(String from, String line) {
            Part piece = part(line);
            if (piece == null || from == null || from.trim().isEmpty()) {
                return null;
            }
            String key = from.trim().toUpperCase(Locale.US) + "#" + piece.count;
            String[] slots = open.get(key);
            if (slots == null || slots.length != piece.count) {
                slots = new String[piece.count];
                open.put(key, slots);
            }
            slots[piece.index - 1] = piece.text;
            StringBuilder all = new StringBuilder();
            for (String slot : slots) {
                if (slot == null) {
                    return null;
                }
                all.append(slot);
            }
            open.remove(key);
            String text = all.toString();
            String fingerprint = key + "|" + text;
            if (filed.containsKey(fingerprint)) {
                return null;
            }
            filed.put(fingerprint, Boolean.TRUE);
            while (filed.size() > 40) {
                filed.remove(filed.keySet().iterator().next());
            }
            return text;
        }
    }
}
