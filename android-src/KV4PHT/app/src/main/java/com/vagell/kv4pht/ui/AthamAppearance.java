/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Copyright (C) 2026 Atley LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package com.vagell.kv4pht.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;

/**
 * Theme and accent choices shared with Anchor: Dark, Light, System, and the five accents.
 * Text size is left to the system accessibility font scale.
 */
public final class AthamAppearance {
    public static final int THEME_DARK = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_SYSTEM = 2;

    public static final int ACCENT_ANCHOR = 0;
    public static final int ACCENT_CLAY = 1;
    public static final int ACCENT_ORANGE = 2;
    public static final int ACCENT_PURPLE = 3;
    public static final int ACCENT_BLUE = 4;

    private static final String PREFS = "atham_appearance";
    private static final String KEY_THEME = "theme";
    private static final String KEY_ACCENT = "accent";

    private AthamAppearance() {
    }

    public static int theme(Context context) {
        return prefs(context).getInt(KEY_THEME, THEME_LIGHT);
    }

    public static int accent(Context context) {
        return prefs(context).getInt(KEY_ACCENT, ACCENT_ANCHOR);
    }

    public static void setTheme(Context context, int theme) {
        prefs(context).edit().putInt(KEY_THEME, theme).apply();
    }

    public static void setAccent(Context context, int accent) {
        prefs(context).edit().putInt(KEY_ACCENT, accent).apply();
    }

    public static boolean dark(Context context) {
        int theme = theme(context);
        if (theme == THEME_DARK) {
            return true;
        }
        if (theme == THEME_LIGHT) {
            return false;
        }
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int page(boolean dark) {
        return dark ? 0xFF10141C : 0xFFF3F5F8;
    }

    public static int card(boolean dark) {
        return dark ? 0xFF1B2130 : 0xFFFFFFFF;
    }

    public static int ink(boolean dark) {
        return dark ? 0xFFF4F6F8 : 0xFF1C2430;
    }

    public static int muted(boolean dark) {
        return dark ? 0xFF9AA3B2 : 0xFF64748B;
    }

    public static int line(boolean dark) {
        return dark ? 0xFF2A3142 : 0xFFE6EAF0;
    }

    public static int accentColor(int accent) {
        switch (accent) {
            case ACCENT_CLAY:
                return 0xFFB86B64;
            case ACCENT_ORANGE:
                return 0xFFF97316;
            case ACCENT_PURPLE:
                return 0xFF8B6FD4;
            case ACCENT_BLUE:
                return 0xFF3B82F6;
            case ACCENT_ANCHOR:
            default:
                return 0xFF14B8A6;
        }
    }

    public static int accentWash(int accent, boolean dark) {
        if (dark) {
            return 0xFF243044;
        }
        switch (accent) {
            case ACCENT_CLAY:
                return 0xFFF8EBE9;
            case ACCENT_ORANGE:
                return 0xFFFFF1E6;
            case ACCENT_PURPLE:
                return 0xFFF1ECFA;
            case ACCENT_BLUE:
                return 0xFFE8F1FE;
            case ACCENT_ANCHOR:
            default:
                return 0xFFE7F8F4;
        }
    }

    public static String accentCaption(int accent) {
        switch (accent) {
            case ACCENT_CLAY:
                return "Clay is a warm muted red. Other colors stay optional.";
            case ACCENT_ORANGE:
                return "Orange matches the StrikeNote field accent. Other colors stay optional.";
            case ACCENT_PURPLE:
                return "Purple is an optional accent. Anchor remains the default.";
            case ACCENT_BLUE:
                return "Blue is an optional accent. Anchor remains the default.";
            case ACCENT_ANCHOR:
            default:
                return "Anchor is a sea-glass emerald accent. Other colors stay optional.";
        }
    }

    public static GradientDrawable rounded(int color, float radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusPx);
        return drawable;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
