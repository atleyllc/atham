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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        findViewById(R.id.moduleDevices).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
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
        section.setVisibility(View.VISIBLE);
        int muted = ContextCompat.getColor(this, R.color.atham_muted);
        int gold = ContextCompat.getColor(this, R.color.atham_gold);
        navHome.setTextColor(muted);
        navRadio.setTextColor(muted);
        navLog.setTextColor(muted);
        navModules.setTextColor(muted);
        nav.setTextColor(gold);
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
                radioSignal.setText("PTT blocked. Connect a KV4P HT first.");
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
        homeFrequency.setText(frequency + " MHz");
        radioFrequency.setText(frequency);
        modulesHint.setText("Radio stays on " + frequency);
        voiceContext.setText(bandLabel() + "  ·  FM  ·  SIMPLEX  ·  " + frequency + " MHz");
        boolean connected = radio != null && radio.isRadioConnected();
        RadioMode mode = radio == null ? null : radio.getMode();
        if (!connected) {
            deviceView.setText("KV4P HT  ·  FM");
            homeConnection.setText("Disconnected");
            homeConnection.setTextColor(ContextCompat.getColor(this, R.color.atham_err));
            radioState.setText(scanning ? "SCAN" : "DISCONNECTED");
            radioState.setTextColor(ContextCompat.getColor(this, scanning ? R.color.atham_gold : R.color.atham_err));
            radioSignal.setText(scanning
                    ? "Scan is armed. It runs after a KV4P HT connects."
                    : "USB detached. PTT stays blocked until a KV4P HT is connected.");
        } else if (mode == RadioMode.TX) {
            deviceView.setText("KV4P HT  ·  TX");
            homeConnection.setText("Transmitting");
            homeConnection.setTextColor(ContextCompat.getColor(this, R.color.atham_tx));
            radioState.setText("TRANSMIT");
            radioState.setTextColor(ContextCompat.getColor(this, R.color.atham_tx));
            radioSignal.setText("Keyed. Release PTT or tap UNKEY.");
            pttButton.setText("PTT HELD");
        } else if (mode == RadioMode.SCAN || scanning) {
            deviceView.setText("KV4P HT  ·  SCAN");
            homeConnection.setText("Scanning");
            homeConnection.setTextColor(ContextCompat.getColor(this, R.color.atham_gold));
            radioState.setText("SCAN");
            radioState.setTextColor(ContextCompat.getColor(this, R.color.atham_gold));
            radioSignal.setText("Scanning memories. UNKEY stops the scan.");
        } else {
            deviceView.setText("KV4P HT  ·  FM");
            homeConnection.setText("Connected");
            homeConnection.setTextColor(ContextCompat.getColor(this, R.color.atham_rx));
            radioState.setText(mode == RadioMode.RX ? "RECEIVE" : "READY");
            radioState.setTextColor(ContextCompat.getColor(this, mode == RadioMode.RX ? R.color.atham_rx : R.color.atham_muted));
            radioSignal.setText("Squelch 2. Tone off. Hold PTT to transmit on " + frequency + ".");
        }
        if (mode != RadioMode.TX) {
            pttButton.setText("HOLD TO TALK");
        }
        if (contacts.isEmpty()) {
            logBody.setText("No contacts yet. VoiceLog drafts stay here after you finalize them.");
        } else {
            StringBuilder builder = new StringBuilder();
            for (String contact : contacts) {
                builder.append(contact).append("\n\n");
            }
            logBody.setText(builder.toString().trim());
        }
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
