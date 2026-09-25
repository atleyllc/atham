package com.vagell.kv4pht.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vagell.kv4pht.R;
import com.vagell.kv4pht.radio.DeviceProfiles;
import com.vagell.kv4pht.radio.RadioCapabilities;
import com.vagell.kv4pht.session.ActivityCatalog;

public class ActivitiesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Appearance.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);
        RadioCapabilities radio = DeviceProfiles.connected();
        ((TextView) findViewById(R.id.activityDevice)).setText(radio.displayName);
        LinearLayout list = findViewById(R.id.activityList);
        for (ActivityCatalog.Item item : ActivityCatalog.forDevice(radio)) {
            TextView row = new TextView(this);
            row.setText(item.title + "\n" + item.reason);
            row.setTextSize(16);
            row.setPadding(32, 28, 32, 28);
            row.setBackgroundResource(R.drawable.card_surface);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 16;
            row.setLayoutParams(params);
            if (item.enabled) {
                row.setOnClickListener(v -> open(item.title));
            }
            list.addView(row);
        }
    }

    public void finishClicked(View view) {
        finish();
    }

    private void open(String title) {
        if ("Open packet mail".equals(title)) {
            startActivity(new Intent(this, RadioMailActivity.class));
        } else if ("Log a contact".equals(title)) {
            startActivity(new Intent(this, LogActivity.class));
        } else {
            finish();
        }
    }
}
