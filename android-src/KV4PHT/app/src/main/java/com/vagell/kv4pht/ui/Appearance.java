/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Modified 2026 by Atley LLC: light/dark/system theme and accent swatches.

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

package com.vagell.kv4pht.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.vagell.kv4pht.R;

/**
 * Persists theme and accent before Room loads so they apply before setContentView.
 */
public final class Appearance {
    public static final String PREFS = "atley_appearance";
    public static final String KEY_THEME = "theme";
    public static final String KEY_ACCENT = "accent";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    public static final String ACCENT_ORANGE = "orange";
    public static final String ACCENT_TEAL = "teal";
    public static final String ACCENT_ROSE = "rose";
    public static final String ACCENT_BLUE = "blue";

    private Appearance() {}

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String theme(Context context) {
        return prefs(context).getString(KEY_THEME, THEME_LIGHT);
    }

    public static String accent(Context context) {
        return prefs(context).getString(KEY_ACCENT, ACCENT_ORANGE);
    }

    public static void saveTheme(Context context, String theme) {
        prefs(context).edit().putString(KEY_THEME, theme).apply();
    }

    public static void saveAccent(Context context, String accent) {
        prefs(context).edit().putString(KEY_ACCENT, accent).apply();
    }

    /** Call before super.onCreate. */
    public static void apply(Activity activity) {
        String theme = theme(activity);
        int nightMode;
        if (THEME_DARK.equals(theme)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (THEME_SYSTEM.equals(theme)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);

        String accent = accent(activity);
        int themeRes;
        if (ACCENT_TEAL.equals(accent)) {
            themeRes = R.style.Theme_KV4PHT_Teal;
        } else if (ACCENT_ROSE.equals(accent)) {
            themeRes = R.style.Theme_KV4PHT_Rose;
        } else if (ACCENT_BLUE.equals(accent)) {
            themeRes = R.style.Theme_KV4PHT_Blue;
        } else {
            themeRes = R.style.Theme_KV4PHT_Orange;
        }
        activity.setTheme(themeRes);
    }
}
