/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Modified 2026 by Atley LLC: station list and message delivery status.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package com.vagell.kv4pht.aprs;

import com.vagell.kv4pht.data.APRSMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Phone-side APRS helpers. The ESP32 still modulates and demodulates AFSK.
 */
public final class AprsOperator {
    public static final int DELIVERY_NONE = 0;
    public static final int DELIVERY_PENDING = 1;
    public static final int DELIVERY_ACKED = 2;
    public static final int DELIVERY_REJECTED = 3;

    private AprsOperator() {}

    public static int deliveryAfterReport(boolean ack, boolean reject) {
        if (reject) {
            return DELIVERY_REJECTED;
        }
        if (ack) {
            return DELIVERY_ACKED;
        }
        return DELIVERY_PENDING;
    }

    /** Six-character Maidenhead locator. Empty when the coordinates are unusable. */
    public static String maidenhead(double latitude, double longitude) {
        if (Double.isNaN(latitude) || Double.isNaN(longitude)
                || latitude < -90.0 || latitude > 90.0
                || longitude < -180.0 || longitude > 180.0
                || (latitude == 0.0 && longitude == 0.0)) {
            return "";
        }
        double lon = longitude + 180.0;
        double lat = latitude + 90.0;
        char fieldLon = (char) ('A' + (int) (lon / 20.0));
        char fieldLat = (char) ('A' + (int) (lat / 10.0));
        char squareLon = (char) ('0' + (int) ((lon % 20.0) / 2.0));
        char squareLat = (char) ('0' + (int) (lat % 10.0));
        char subLon = (char) ('a' + (int) ((lon % 2.0) * 12.0));
        char subLat = (char) ('a' + (int) ((lat % 1.0) * 24.0));
        return "" + fieldLon + fieldLat + squareLon + squareLat + subLon + subLat;
    }

    /**
     * Newest position, object, or weather report for each callsign.
     * Chat messages without a position are skipped.
     */
    public static List<APRSMessage> latestStations(List<APRSMessage> messages) {
        Map<String, APRSMessage> newest = new LinkedHashMap<>();
        if (messages == null) {
            return new ArrayList<>();
        }
        for (APRSMessage message : messages) {
            if (message == null || message.fromCallsign == null) {
                continue;
            }
            if (message.positionLat == 0.0 && message.positionLong == 0.0) {
                continue;
            }
            if (message.type != APRSMessage.POSITION_TYPE
                    && message.type != APRSMessage.OBJECT_TYPE
                    && message.type != APRSMessage.WEATHER_TYPE) {
                continue;
            }
            String key = message.fromCallsign.trim().toUpperCase(Locale.US);
            APRSMessage previous = newest.get(key);
            if (previous == null || message.timestamp >= previous.timestamp) {
                newest.put(key, message);
            }
        }
        return new ArrayList<>(newest.values());
    }
}
