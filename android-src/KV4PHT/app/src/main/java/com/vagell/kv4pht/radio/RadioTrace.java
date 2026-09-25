package com.vagell.kv4pht.radio;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Privacy-safe radio events. No message bodies, credentials, locations, or audio. */
public final class RadioTrace {
    public static final int LIMIT = 40;
    private final ArrayDeque<String> lines = new ArrayDeque<>();

    public synchronized void add(String kind, String detail) {
        String safeKind = kind == null ? "event" : kind.replace('\n', ' ');
        String safeDetail = detail == null ? "" : detail.replace('\n', ' ');
        lines.addLast(safeKind + " | " + safeDetail);
        while (lines.size() > LIMIT) {
            lines.removeFirst();
        }
    }

    public synchronized List<String> snapshot() {
        return new ArrayList<>(lines);
    }

    public synchronized String exportText() {
        StringBuilder out = new StringBuilder();
        out.append("Atley HT radio trace\n");
        for (String line : lines) {
            out.append(line).append('\n');
        }
        return out.toString();
    }
}
