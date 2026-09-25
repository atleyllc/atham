package com.vagell.kv4pht.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vagell.kv4pht.R;
import com.vagell.kv4pht.data.AppDatabase;
import com.vagell.kv4pht.data.Qso;
import com.vagell.kv4pht.mail.RadioMail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final List<Qso> rows = new ArrayList<>();
    private RecyclerView.Adapter<?> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Appearance.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        RecyclerView list = findViewById(R.id.logList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        list.setAdapter(adapter);
        reload();
    }

    @Override
    protected void onDestroy() {
        io.shutdown();
        super.onDestroy();
    }

    public void finishClicked(View view) {
        finish();
    }

    public void saveQsoClicked(View view) {
        String call = text(R.id.logCall).trim().toUpperCase();
        if (!RadioMail.isCallsign(call)) {
            return;
        }
        Qso qso = new Qso();
        qso.callsign = call;
        qso.frequency = text(R.id.logFrequency).trim();
        qso.mode = text(R.id.logMode).trim();
        qso.notes = text(R.id.logNotes).trim();
        qso.createdAt = System.currentTimeMillis();
        io.execute(() -> {
            AppDatabase.getInstance(this).qsoDao().insert(qso);
            runOnUiThread(() -> {
                ((EditText) findViewById(R.id.logCall)).setText("");
                ((EditText) findViewById(R.id.logNotes)).setText("");
                reload();
            });
        });
    }

    private void reload() {
        io.execute(() -> {
            List<Qso> loaded = AppDatabase.getInstance(this).qsoDao().all();
            runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private String text(int id) {
        return ((EditText) findViewById(id)).getText().toString();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            view.setPadding(8, 24, 8, 24);
            view.setTextSize(16);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Qso qso = rows.get(position);
            holder.label.setText(qso.callsign + "  " + qso.frequency + "  " + qso.mode
                    + (qso.notes == null || qso.notes.isEmpty() ? "" : "\n" + qso.notes));
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView label;

            Holder(@NonNull View itemView) {
                super(itemView);
                label = (TextView) itemView;
            }
        }
    }
}
