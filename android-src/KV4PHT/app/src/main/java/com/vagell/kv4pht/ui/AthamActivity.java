/*
kv4p HT (see http://kv4p.com)
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

package com.vagell.kv4pht.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.vagell.kv4pht.R;
import com.vagell.kv4pht.data.AppDatabase;
import com.vagell.kv4pht.data.AppSetting;
import com.vagell.kv4pht.radio.RadioAudioService;
import com.vagell.kv4pht.radio.RadioMode;
import com.vagell.kv4pht.radio.RadioServiceConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ATHAM shell. This is the phone launcher. KV4P Classic remains {@link MainActivity}.
 */
public class AthamActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 41;
    private static final String DEFAULT_FREQUENCY = "146.5200";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<String> contacts = new ArrayList<>();
    private RadioServiceConnector connector;
    private RadioAudioService radio;
    private boolean scanning;
    private String frequency = DEFAULT_FREQUENCY;
    private String callsign = "N0CALL";

    private TextView callsignView;
    private TextView deviceView;
    private TextView frequencyView;
    private TextView homeConnection;
    private TextView homeFrequency;
    private TextView radioState;
    private TextView radioFrequency;
    private TextView radioSignal;
    private TextView modulesHint;
    private TextView voiceContext;
    private TextView logBody;
    private EditText frequencyInput;
    private Button pttButton;
    private Button navHome;
    private Button navRadio;
    private Button navLog;
    private Button navModules;
    private View sectionHome;
    private View sectionRadio;
    private View sectionLog;
    private View sectionModules;
    private View sectionVoice;
    private View sectionAppearance;
    private Button activeNav;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            refreshFromRadio();
            handler.postDelayed(this, 400);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atham);
        bindViews();
        connector = new RadioServiceConnector(this);
        wireActions();
        showSection(sectionHome, navHome);
        loadCallsign();
        render();
    }

    private void bindViews() {
        callsignView = findViewById(R.id.athamCallsign);
        deviceView = findViewById(R.id.athamDevice);
        frequencyView = findViewById(R.id.athamFrequency);
        homeConnection = findViewById(R.id.homeConnection);
        homeFrequency = findViewById(R.id.homeFrequency);
        radioState = findViewById(R.id.radioState);
        radioFrequency = findViewById(R.id.radioFrequency);
        radioSignal = findViewById(R.id.radioSignal);
        modulesHint = findViewById(R.id.modulesHint);
        voiceContext = findViewById(R.id.voiceContext);
        logBody = findViewById(R.id.logBody);
        frequencyInput = findViewById(R.id.radioFrequencyInput);
        pttButton = findViewById(R.id.radioPtt);
        navHome = findViewById(R.id.navHome);
        navRadio = findViewById(R.id.navRadio);
        navLog = findViewById(R.id.navLog);
        navModules = findViewById(R.id.navModules);
        sectionHome = findViewById(R.id.sectionHome);
        sectionRadio = findViewById(R.id.sectionRadio);
        sectionLog = findViewById(R.id.sectionLog);
        sectionModules = findViewById(R.id.sectionModules);
        sectionVoice = findViewById(R.id.sectionVoice);
        sectionAppearance = findViewById(R.id.sectionAppearance);
        activeNav = navHome;
    }

    private void wireActions() {
        findViewById(R.id.athamUnkey).setOnClickListener(v -> unkey());
        navHome.setOnClickListener(v -> showSection(sectionHome, navHome));
        navRadio.setOnClickListener(v -> showSection(sectionRadio, navRadio));
        navLog.setOnClickListener(v -> showSection(sectionLog, navLog));
        navModules.setOnClickListener(v -> showSection(sectionModules, navModules));
        findViewById(R.id.launchRadio).setOnClickListener(v -> showSection(sectionRadio, navRadio));
        findViewById(R.id.launchScan).setOnClickListener(v -> {
            showSection(sectionRadio, navRadio);
            toggleScan();
        });
        findViewById(R.id.launchLog).setOnClickListener(v -> showSection(sectionVoice, navModules));
        findViewById(R.id.launchClassic).setOnClickListener(v -> openClassic());
        findViewById(R.id.moduleVoice).setOnClickListener(v -> showSection(sectionVoice, navModules));
        findViewById(R.id.modulePacket).setOnClickListener(v -> openClassic());
        findViewById(R.id.moduleAppearance).setOnClickListener(v -> showSection(sectionAppearance, navModules));
        findViewById(R.id.moduleDevices).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.themeDark).setOnClickListener(v -> chooseTheme(AthamAppearance.THEME_DARK));
        findViewById(R.id.themeLight).setOnClickListener(v -> chooseTheme(AthamAppearance.THEME_LIGHT));
        findViewById(R.id.themeSystem).setOnClickListener(v -> chooseTheme(AthamAppearance.THEME_SYSTEM));
        findViewById(R.id.accentAnchor).setOnClickListener(v -> chooseAccent(AthamAppearance.ACCENT_ANCHOR));
        findViewById(R.id.accentClay).setOnClickListener(v -> chooseAccent(AthamAppearance.ACCENT_CLAY));
        findViewById(R.id.accentOrange).setOnClickListener(v -> chooseAccent(AthamAppearance.ACCENT_ORANGE));
        findViewById(R.id.accentPurple).setOnClickListener(v -> chooseAccent(AthamAppearance.ACCENT_PURPLE));
        findViewById(R.id.accentBlue).setOnClickListener(v -> chooseAccent(AthamAppearance.ACCENT_BLUE));
        findViewById(R.id.radioConnect).setOnClickListener(v -> connectRadio());
        findViewById(R.id.radioTune).setOnClickListener(v -> tune());
        findViewById(R.id.radioScan).setOnClickListener(v -> toggleScan());
        findViewById(R.id.voiceFinalize).setOnClickListener(v -> finalizeContact());
        pttButton.setOnTouchListener((v, event) -> handlePtt(event));
    }

    private void loadCallsign() {
        new Thread(() -> {
            String loaded = "N0CALL";
            try {
                AppSetting setting = AppDatabase.getInstance(this)
                        .appSettingDao()
                        .getByName(AppSetting.SETTING_CALLSIGN);
                if (setting != null && setting.getValue() != null && !setting.getValue().trim().isEmpty()) {
                    loaded = setting.getValue();
                }
            } catch (Exception ignored) {
                loaded = "N0CALL";
            }
            String shown = loaded;
            runOnUiThread(() -> {
                callsign = shown;
                render();
            });
        }).start();
    }

    private void showSection(View section, Button nav) {
        sectionHome.setVisibility(View.GONE);
        sectionRadio.setVisibility(View.GONE);
        sectionLog.setVisibility(View.GONE);
        sectionModules.setVisibility(View.GONE);
        sectionVoice.setVisibility(View.GONE);
        sectionAppearance.setVisibility(View.GONE);
        section.setVisibility(View.VISIBLE);
        activeNav = nav;
        render();
    }

    private void chooseTheme(int theme) {
        AthamAppearance.setTheme(this, theme);
        render();
    }

    private void chooseAccent(int accent) {
        AthamAppearance.setAccent(this, accent);
        render();
    }

    private void connectRadio() {
        if (!hasRadioPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions(), PERMISSION_REQUEST);
            return;
        }
        Intent intent = new Intent(this, RadioAudioService.class);
        ContextCompat.startForegroundService(this, intent);
        connector.bind(service -> {
            radio = service;
            service.start();
            String current = service.getActiveFrequencyStr();
            if (current != null && !current.isEmpty()) {
                frequency = current;
            } else {
                service.tuneToFreq(frequency);
            }
            render();
        });
    }

    private boolean hasRadioPermissions() {
        for (String permission : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] requiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            return new String[] {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        }
        return new String[] {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST && hasRadioPermissions()) {
            connectRadio();
        }
    }

    private void tune() {
        String entered = frequencyInput.getText().toString().trim();
        if (!entered.isEmpty()) {
            frequency = entered;
        }
        if (radio != null) {
            radio.tuneToFreq(frequency);
            frequency = radio.getActiveFrequencyStr().isEmpty() ? frequency : radio.getActiveFrequencyStr();
        }
        scanning = false;
        render();
    }

    private void toggleScan() {
        scanning = !scanning;
        if (radio != null) {
            radio.setScanning(scanning);
        }
        render();
    }

    private boolean handlePtt(MotionEvent event) {
        if (radio == null || !radio.isRadioConnected()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                radioSignal.setText("Connect a KV4P HT before talking.");
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            scanning = false;
            radio.startPtt();
            render();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            radio.endPtt();
            render();
            return true;
        }
        return false;
    }

    private void unkey() {
        if (radio != null) {
            radio.endPtt();
            radio.setScanning(false);
        }
        scanning = false;
        render();
    }

    private void finalizeContact() {
        EditText call = findViewById(R.id.voiceCallsign);
        EditText report = findViewById(R.id.voiceReport);
        EditText name = findViewById(R.id.voiceName);
        EditText heard = findViewById(R.id.voiceHeard);
        String theirCall = textOf(call);
        if (theirCall.isEmpty()) {
            theirCall = "Unknown";
        }
        contacts.add(0, theirCall + "  ·  " + frequency + " MHz FM  ·  " + textOf(report)
                + "  ·  " + textOf(name) + (textOf(heard).isEmpty() ? "" : "  ·  " + textOf(heard)));
        showSection(sectionLog, navLog);
        render();
    }

    private static String textOf(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void openClassic() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void refreshFromRadio() {
        if (radio == null) {
            return;
        }
        String live = radio.getActiveFrequencyStr();
        if (live != null && !live.isEmpty()) {
            frequency = live;
        }
        render();
    }

    private void render() {
        callsignView.setText(callsign);
        frequencyView.setText(frequency);
        homeFrequency.setText(frequency);
        radioFrequency.setText(frequency);
        modulesHint.setText(frequency + " MHz stays with you");
        voiceContext.setText(bandLabel() + "  ·  FM  ·  Simplex");
        boolean dark = AthamAppearance.dark(this);
        int ink = AthamAppearance.ink(dark);
        int muted = AthamAppearance.muted(dark);
        boolean connected = radio != null && radio.isRadioConnected();
        RadioMode mode = radio == null ? null : radio.getMode();
        String homeStatus = "Not connected";
        String radioStatusText = scanning ? "Scanning" : "Not connected";
        int statusColor = muted;
        if (!connected) {
            radioSignal.setText(scanning
                    ? "Scan starts when a KV4P HT is connected."
                    : "Plug in a KV4P HT to tune, scan, and talk.");
        } else if (mode == RadioMode.TX) {
            homeStatus = "Transmitting";
            radioStatusText = "Transmitting";
            statusColor = ink;
            radioSignal.setText("Release or tap Unkey.");
            pttButton.setText("Talking");
        } else if (mode == RadioMode.SCAN || scanning) {
            homeStatus = "Scanning";
            radioStatusText = "Scanning";
            statusColor = ink;
            radioSignal.setText("Unkey stops the scan.");
        } else {
            homeStatus = "Connected";
            radioStatusText = mode == RadioMode.RX ? "Listening" : "Ready";
            radioSignal.setText("Hold to talk on " + frequency + ".");
        }
        if (mode != RadioMode.TX) {
            pttButton.setText("Hold to talk");
        }
        deviceView.setText("KV4P HT");
        homeConnection.setText(homeStatus);
        radioState.setText(radioStatusText);
        paintChrome(dark, ink, muted);
        homeConnection.setTextColor(statusColor);
        radioState.setTextColor(statusColor);
        if (contacts.isEmpty()) {
            logBody.setText("No contacts yet.");
            logBody.setTextColor(muted);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String contact : contacts) {
                builder.append(contact).append("\n\n");
            }
            logBody.setText(builder.toString().trim());
            logBody.setTextColor(ink);
        }
    }

    private void paintChrome(boolean dark, int ink, int muted) {
        int accent = AthamAppearance.accentColor(AthamAppearance.accent(this));
        int wash = AthamAppearance.accentWash(AthamAppearance.accent(this), dark);
        int page = AthamAppearance.page(dark);
        int card = AthamAppearance.card(dark);
        float radius = dp(16);
        float chipRadius = dp(22);

        findViewById(R.id.athamRoot).setBackgroundColor(page);
        findViewById(R.id.athamNav).setBackgroundColor(card);
        findViewById(R.id.athamNavLine).setBackgroundColor(AthamAppearance.line(dark));
        getWindow().setStatusBarColor(page);
        getWindow().setNavigationBarColor(card);
        WindowInsetsControllerCompat insets = WindowCompat.getInsetsController(getWindow(), findViewById(R.id.athamRoot));
        insets.setAppearanceLightStatusBars(!dark);
        insets.setAppearanceLightNavigationBars(!dark);

        paintTagged(findViewById(R.id.athamRoot), ink, muted, accent, wash, card, radius);

        Button[] navs = {navHome, navRadio, navLog, navModules};
        for (Button nav : navs) {
            nav.setTextColor(nav == activeNav ? accent : muted);
            nav.setTypeface(null, nav == activeNav ? Typeface.BOLD : Typeface.NORMAL);
        }

        int theme = AthamAppearance.theme(this);
        styleChoice(R.id.themeDark, theme == AthamAppearance.THEME_DARK, card, wash, ink, accent, chipRadius);
        styleChoice(R.id.themeLight, theme == AthamAppearance.THEME_LIGHT, card, wash, ink, accent, chipRadius);
        styleChoice(R.id.themeSystem, theme == AthamAppearance.THEME_SYSTEM, card, wash, ink, accent, chipRadius);

        int selected = AthamAppearance.accent(this);
        styleAccent(R.id.accentAnchorDot, R.id.accentAnchorLabel, AthamAppearance.ACCENT_ANCHOR, selected, card);
        styleAccent(R.id.accentClayDot, R.id.accentClayLabel, AthamAppearance.ACCENT_CLAY, selected, card);
        styleAccent(R.id.accentOrangeDot, R.id.accentOrangeLabel, AthamAppearance.ACCENT_ORANGE, selected, card);
        styleAccent(R.id.accentPurpleDot, R.id.accentPurpleLabel, AthamAppearance.ACCENT_PURPLE, selected, card);
        styleAccent(R.id.accentBlueDot, R.id.accentBlueLabel, AthamAppearance.ACCENT_BLUE, selected, card);
        TextView caption = findViewById(R.id.accentCaption);
        caption.setText(AthamAppearance.accentCaption(selected));
        caption.setTextColor(muted);
    }

    private void paintTagged(View view, int ink, int muted, int accent, int wash, int card, float radius) {
        Object tag = view.getTag();
        if (tag instanceof String) {
            String role = (String) tag;
            if ("card".equals(role) || "card-ink".equals(role) || "card-muted".equals(role)) {
                view.setBackground(AthamAppearance.rounded(card, radius));
                ViewCompat.setBackgroundTintList(view, null);
            } else if ("wash".equals(role)) {
                view.setBackground(AthamAppearance.rounded(wash, radius));
                ViewCompat.setBackgroundTintList(view, null);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(accent);
                }
            } else if ("field".equals(role) && view instanceof EditText) {
                EditText field = (EditText) view;
                field.setBackground(AthamAppearance.rounded(card, radius));
                ViewCompat.setBackgroundTintList(field, null);
                field.setTextColor(ink);
                field.setHintTextColor(muted);
            }
            if (view instanceof TextView) {
                TextView text = (TextView) view;
                if ("ink".equals(role) || "card-ink".equals(role)) {
                    text.setTextColor(ink);
                } else if ("muted".equals(role) || "card-muted".equals(role)) {
                    text.setTextColor(muted);
                } else if ("accent".equals(role)) {
                    text.setTextColor(accent);
                }
            }
        }
        if (view instanceof Button) {
            Button button = (Button) view;
            button.setAllCaps(false);
            button.setStateListAnimator(null);
            if (button.getText() != null && button.getText().toString().contains("\n")) {
                button.setSingleLine(false);
                button.setMaxLines(3);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                paintTagged(group.getChildAt(i), ink, muted, accent, wash, card, radius);
            }
        }
    }

    private void styleChoice(int id, boolean selected, int card, int wash, int ink, int accent, float radius) {
        Button button = findViewById(id);
        button.setBackground(AthamAppearance.rounded(selected ? wash : card, radius));
        ViewCompat.setBackgroundTintList(button, null);
        button.setTextColor(selected ? accent : ink);
        button.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void styleAccent(int dotId, int labelId, int accentId, int selected, int page) {
        View dot = findViewById(dotId);
        int color = AthamAppearance.accentColor(accentId);
        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.OVAL);
        fill.setColor(color);
        if (accentId == selected) {
            GradientDrawable ring = new GradientDrawable();
            ring.setShape(GradientDrawable.OVAL);
            ring.setColor(page);
            ring.setStroke(Math.round(dp(2)), color);
            int inset = Math.round(dp(5));
            LayerDrawable layers = new LayerDrawable(new android.graphics.drawable.Drawable[] {ring, fill});
            layers.setLayerInset(1, inset, inset, inset, inset);
            dot.setBackground(layers);
        } else {
            dot.setBackground(fill);
        }
        TextView label = findViewById(labelId);
        label.setTextColor(accentId == selected ? color : AthamAppearance.muted(AthamAppearance.dark(this)));
        label.setTypeface(null, accentId == selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private String bandLabel() {
        try {
            float mhz = Float.parseFloat(frequency);
            if (mhz >= 144 && mhz < 148) {
                return "2 m";
            }
            if (mhz >= 420 && mhz < 450) {
                return "70 cm";
            }
        } catch (NumberFormatException ignored) {
            return "VHF";
        }
        return String.format(Locale.US, "%s MHz", frequency);
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(poll);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(poll);
        super.onStop();
    }
}
