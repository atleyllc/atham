/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Modified 2026 by Atley LLC: RadioMail-style mailbox.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package com.vagell.kv4pht.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vagell.kv4pht.R;
import com.vagell.kv4pht.data.AppDatabase;
import com.vagell.kv4pht.data.RadioMailDao;
import com.vagell.kv4pht.data.RadioMailMessage;
import com.vagell.kv4pht.data.RadioMailStation;
import com.vagell.kv4pht.mail.RadioMail;
import com.vagell.kv4pht.radio.RadioAudioService;
import com.vagell.kv4pht.radio.RadioServiceConnector;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RadioMailActivity extends AppCompatActivity {
    private static final int FLAGGED_VIEW = -1;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final List<RadioMailMessage> rows = new ArrayList<>();
    private final List<RadioMailStation> stations = new ArrayList<>();
    private RadioServiceConnector connector;
    private RadioAudioService radio;
    private int folder = RadioMailMessage.INBOX;
    private boolean favoritesOnly;
    private RadioMailMessage openMessage;
    private MailAdapter mailAdapter;
    private StationAdapter stationAdapter;
    private final List<RadioMailMessage> visible = new ArrayList<>();
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Appearance.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_mail);
        RecyclerView mailList = findViewById(R.id.mailList);
        mailList.setLayoutManager(new LinearLayoutManager(this));
        mailAdapter = new MailAdapter();
        mailList.setAdapter(mailAdapter);
        RecyclerView stationList = findViewById(R.id.stationList);
        stationList.setLayoutManager(new LinearLayoutManager(this));
        stationAdapter = new StationAdapter();
        stationList.setAdapter(stationAdapter);
        ((EditText) findViewById(R.id.mailSearch)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        connector = new RadioServiceConnector(this);
        connector.bind(service -> radio = service);
        refreshCounts();
    }

    @Override
    protected void onDestroy() {
        connector.unbind();
        io.shutdown();
        super.onDestroy();
    }

    public void showHome(View view) {
        show(R.id.mailHome);
        refreshCounts();
    }

    public void openInbox(View view) { openFolder(RadioMailMessage.INBOX, R.string.mail_inbox); }
    public void openDrafts(View view) { openFolder(RadioMailMessage.DRAFT, R.string.mail_drafts); }
    public void openOutbox(View view) { openFolder(RadioMailMessage.OUTBOX, R.string.mail_outbox); }
    public void openSent(View view) { openFolder(RadioMailMessage.SENT, R.string.mail_sent); }
    public void openTrash(View view) { openFolder(RadioMailMessage.TRASH, R.string.mail_trash); }
    public void openArchive(View view) { openFolder(RadioMailMessage.ARCHIVE, R.string.mail_archive); }

    public void openFlagged(View view) {
        folder = FLAGGED_VIEW;
        ((TextView) findViewById(R.id.mailListTitle)).setText(R.string.mail_flagged);
        show(R.id.mailListPane);
        reloadMail();
    }

    public void openDirectory(View view) {
        favoritesOnly = false;
        ((TextView) findViewById(R.id.directoryTitle)).setText(R.string.mail_directory);
        show(R.id.mailDirectory);
        reloadStations();
    }

    public void openFavorites(View view) {
        favoritesOnly = true;
        ((TextView) findViewById(R.id.directoryTitle)).setText(R.string.mail_favorites);
        show(R.id.mailDirectory);
        reloadStations();
    }

    public void composeClicked(View view) {
        openMessage = null;
        ((EditText) findViewById(R.id.mailTo)).setText("");
        ((EditText) findViewById(R.id.mailSubject)).setText("");
        ((EditText) findViewById(R.id.mailBody)).setText("");
        show(R.id.mailComposer);
    }

    public void backToList(View view) {
        show(R.id.mailListPane);
        reloadMail();
    }

    public void saveDraftClicked(View view) {
        storeComposer(RadioMailMessage.DRAFT, getString(R.string.mail_draft_saved));
    }

    public void mailSendClicked(View view) {
        String address = text(R.id.mailTo).trim();
        String subject = text(R.id.mailSubject).trim();
        String body = text(R.id.mailBody).trim();
        if (address.isEmpty() || (subject.isEmpty() && body.isEmpty())) {
            return;
        }
        List<String> recipients = RadioMail.recipients(address);
        boolean anyCall = false;
        for (String recipient : recipients) {
            if (RadioMail.isEmail(recipient)) {
                continue;
            }
            if (!RadioMail.isCallsign(recipient)) {
                new MaterialAlertDialogBuilder(this).setMessage(R.string.mail_bad_address).setPositiveButton(android.R.string.ok, null).show();
                return;
            }
            anyCall = true;
        }
        if (!anyCall) {
            store(address, subject, body, RadioMailMessage.OUTBOX, getString(R.string.mail_held_for_winlink));
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.mail_send)
                .setMessage(R.string.mail_send_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.mail_send, (d, w) -> transmit(address, subject, body))
                .show();
    }

    public void replyClicked(View view) {
        if (openMessage == null) {
            return;
        }
        String address = openMessage.address;
        String subject = openMessage.subject != null && openMessage.subject.startsWith("Re:") ? openMessage.subject : "Re: " + openMessage.subject;
        String body = "\n\n" + openMessage.body;
        openMessage = null;
        fillComposer(address, subject, body);
    }

    public void forwardClicked(View view) {
        if (openMessage == null) {
            return;
        }
        String subject = openMessage.subject != null && openMessage.subject.startsWith("Fwd:") ? openMessage.subject : "Fwd: " + openMessage.subject;
        String body = "\n\n" + openMessage.body;
        openMessage = null;
        fillComposer("", subject, body);
    }

    public void unreadClicked(View view) {
        if (openMessage == null) {
            return;
        }
        openMessage.unread = true;
        persist(openMessage, () -> backToList(null));
    }

    public void resendClicked(View view) {
        if (openMessage == null) {
            return;
        }
        transmit(openMessage.address, openMessage.subject, openMessage.body);
    }

    public void templateCheckIn(View view) {
        fillTemplate(R.string.mail_template_check_in_subject, R.string.mail_template_check_in_body);
    }

    public void templateWelfare(View view) {
        fillTemplate(R.string.mail_template_welfare_subject, R.string.mail_template_welfare_body);
    }

    public void templateWeather(View view) {
        fillTemplate(R.string.mail_template_weather_subject, R.string.mail_template_weather_body);
    }

    public void flagClicked(View view) {
        if (openMessage == null) {
            return;
        }
        openMessage.flagged = !openMessage.flagged;
        persist(openMessage, () -> showHome(null));
    }

    public void archiveClicked(View view) {
        moveOpen(RadioMailMessage.ARCHIVE);
    }

    public void trashClicked(View view) {
        moveOpen(openMessage != null && openMessage.folder == RadioMailMessage.TRASH ? RadioMailMessage.INBOX : RadioMailMessage.TRASH);
    }

    public void addStationClicked(View view) {
        String call = text(R.id.stationCall).trim().toUpperCase();
        if (!RadioMail.isCallsign(call)) {
            return;
        }
        RadioMailStation station = new RadioMailStation();
        station.callsign = call;
        station.note = text(R.id.stationNote).trim();
        station.favorite = favoritesOnly;
        station.transport = "Packet";
        io.execute(() -> {
            dao().insert(station);
            runOnUiThread(() -> {
                ((EditText) findViewById(R.id.stationCall)).setText("");
                ((EditText) findViewById(R.id.stationNote)).setText("");
                reloadStations();
            });
        });
    }

    private void openFolder(int next, int title) {
        folder = next;
        ((TextView) findViewById(R.id.mailListTitle)).setText(title);
        show(R.id.mailListPane);
        reloadMail();
    }

    private void moveOpen(int nextFolder) {
        if (openMessage == null) {
            return;
        }
        openMessage.folder = nextFolder;
        persist(openMessage, () -> showHome(null));
    }

    private void transmit(String address, String subject, String body) {
        List<String> calls = new ArrayList<>();
        boolean heldEmail = false;
        for (String recipient : RadioMail.recipients(address)) {
            if (RadioMail.isEmail(recipient)) {
                heldEmail = true;
            } else if (RadioMail.isCallsign(recipient)) {
                calls.add(recipient.toUpperCase());
            }
        }
        if (calls.isEmpty() || radio == null || !radio.isTxAllowed()) {
            store(address, subject, body, RadioMailMessage.OUTBOX,
                    getString(calls.isEmpty() ? R.string.mail_held_for_winlink : R.string.mail_tx_not_allowed));
            return;
        }
        List<String> packets = RadioMail.packets(subject, body);
        boolean emailNote = heldEmail;
        sendAt(address, subject, body, calls, packets, 0, 0, false, emailNote);
    }

    private void sendAt(String address, String subject, String body, List<String> calls, List<String> packets,
                         int callIndex, int packetIndex, boolean failed, boolean heldEmail) {
        if (failed || callIndex >= calls.size()) {
            String status = getString(failed ? R.string.mail_send_failed : R.string.mail_sent_on_frequency);
            if (!failed && heldEmail) {
                status = status + " " + getString(R.string.mail_held_for_winlink);
            }
            if (!failed) {
                status = status + " " + getString(R.string.mail_packet_count, packets.size() * calls.size());
            }
            store(address, subject, body, failed ? RadioMailMessage.OUTBOX : RadioMailMessage.SENT, status.trim());
            return;
        }
        if (packetIndex >= packets.size()) {
            sendAt(address, subject, body, calls, packets, callIndex + 1, 0, false, heldEmail);
            return;
        }
        int result = radio.sendChatMessage(calls.get(callIndex), packets.get(packetIndex));
        if (result < 0) {
            sendAt(address, subject, body, calls, packets, callIndex, packetIndex, true, heldEmail);
            return;
        }
        main.postDelayed(() -> sendAt(address, subject, body, calls, packets, callIndex, packetIndex + 1, false, heldEmail), 900);
    }

    private void fillComposer(String address, String subject, String body) {
        ((EditText) findViewById(R.id.mailTo)).setText(address);
        ((EditText) findViewById(R.id.mailSubject)).setText(subject);
        ((EditText) findViewById(R.id.mailBody)).setText(body);
        show(R.id.mailComposer);
    }

    private void fillTemplate(int subject, int body) {
        if (text(R.id.mailSubject).trim().isEmpty()) {
            ((EditText) findViewById(R.id.mailSubject)).setText(subject);
        }
        EditText bodyField = findViewById(R.id.mailBody);
        if (bodyField.getText().toString().trim().isEmpty()) {
            bodyField.setText(body);
        }
    }

    private void storeComposer(int target, String status) {
        store(text(R.id.mailTo).trim(), text(R.id.mailSubject).trim(), text(R.id.mailBody).trim(), target, status);
    }

    private void store(String address, String subject, String body, int target, String status) {
        RadioMailMessage message = openMessage == null ? new RadioMailMessage() : openMessage;
        message.address = address;
        message.subject = subject;
        message.body = body;
        message.folder = target;
        message.status = status;
        message.unread = target == RadioMailMessage.INBOX;
        if (message.createdAt == 0) {
            message.createdAt = System.currentTimeMillis();
        }
        io.execute(() -> {
            if (message.id == 0) {
                dao().insert(message);
            } else {
                dao().update(message);
            }
            runOnUiThread(() -> {
                openMessage = null;
                showHome(null);
            });
        });
    }

    private void persist(RadioMailMessage message, Runnable after) {
        io.execute(() -> {
            dao().update(message);
            runOnUiThread(after);
        });
    }

    private void reloadMail() {
        io.execute(() -> {
            List<RadioMailMessage> loaded = folder == FLAGGED_VIEW ? dao().flagged() : dao().folder(folder);
            runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                applyFilter();
            });
        });
    }

    private void reloadStations() {
        io.execute(() -> {
            List<RadioMailStation> loaded = favoritesOnly ? dao().favorites() : dao().stations();
            runOnUiThread(() -> {
                stations.clear();
                stations.addAll(loaded);
                stationAdapter.notifyDataSetChanged();
            });
        });
    }

    private void refreshCounts() {
        io.execute(() -> {
            int inbox = dao().count(RadioMailMessage.INBOX);
            int inboxNew = dao().unreadCount(RadioMailMessage.INBOX);
            int drafts = dao().count(RadioMailMessage.DRAFT);
            int outbox = dao().count(RadioMailMessage.OUTBOX);
            int sent = dao().count(RadioMailMessage.SENT);
            int trash = dao().count(RadioMailMessage.TRASH);
            int archive = dao().count(RadioMailMessage.ARCHIVE);
            int flagged = dao().flaggedCount();
            runOnUiThread(() -> {
                String inboxLabel = getString(R.string.mail_inbox) + (inbox == 0 ? "" : "    " + inbox);
                if (inboxNew > 0) {
                    inboxLabel = inboxLabel + "  ·  " + getString(R.string.mail_new_count, inboxNew);
                }
                ((TextView) findViewById(R.id.rowInbox)).setText(inboxLabel);
                label(R.id.rowDrafts, R.string.mail_drafts, drafts);
                label(R.id.rowOutbox, R.string.mail_outbox, outbox);
                label(R.id.rowSent, R.string.mail_sent, sent);
                label(R.id.rowTrash, R.string.mail_trash, trash);
                label(R.id.rowArchive, R.string.mail_archive, archive);
                label(R.id.rowFlagged, R.string.mail_flagged, flagged);
            });
        });
    }

    private void applyFilter() {
        String query = text(R.id.mailSearch);
        visible.clear();
        for (RadioMailMessage message : rows) {
            if (RadioMail.matches(message.address, message.subject, message.body, query)) {
                visible.add(message);
            }
        }
        findViewById(R.id.mailEmpty).setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        mailAdapter.notifyDataSetChanged();
    }

    private void label(int id, int name, int count) {
        ((TextView) findViewById(id)).setText(getString(name) + (count == 0 ? "" : "    " + count));
    }

    private void show(int pane) {
        findViewById(R.id.mailHome).setVisibility(pane == R.id.mailHome ? View.VISIBLE : View.GONE);
        findViewById(R.id.mailListPane).setVisibility(pane == R.id.mailListPane ? View.VISIBLE : View.GONE);
        findViewById(R.id.mailReader).setVisibility(pane == R.id.mailReader ? View.VISIBLE : View.GONE);
        findViewById(R.id.mailComposer).setVisibility(pane == R.id.mailComposer ? View.VISIBLE : View.GONE);
        findViewById(R.id.mailDirectory).setVisibility(pane == R.id.mailDirectory ? View.VISIBLE : View.GONE);
        findViewById(R.id.mailComposeButton).setVisibility(pane == R.id.mailHome || pane == R.id.mailListPane ? View.VISIBLE : View.GONE);
    }

    private void openReader(RadioMailMessage message) {
        openMessage = message;
        message.unread = false;
        ((TextView) findViewById(R.id.readerSubject)).setText(message.subject);
        ((TextView) findViewById(R.id.readerAddress)).setText(message.address);
        String when = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(message.createdAt));
        ((TextView) findViewById(R.id.readerStatus)).setText(when + (message.status == null || message.status.isEmpty() ? "" : "  ·  " + message.status));
        ((TextView) findViewById(R.id.readerBody)).setText(message.body);
        show(R.id.mailReader);
        io.execute(() -> dao().update(message));
    }

    private RadioMailDao dao() {
        return AppDatabase.getInstance(this).radioMailDao();
    }

    private String text(int id) {
        return ((EditText) findViewById(id)).getText().toString();
    }

    private class MailAdapter extends RecyclerView.Adapter<MailAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_mail_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            RadioMailMessage message = visible.get(position);
            holder.address.setText((message.unread ? "● " : "") + message.address);
            holder.subject.setText(message.flagged ? "⚑ " + message.subject : message.subject);
            String when = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.createdAt));
            holder.status.setText(when + (message.status == null || message.status.isEmpty() ? "" : "  ·  " + message.status));
            holder.itemView.setOnClickListener(v -> openReader(message));
        }

        @Override
        public int getItemCount() {
            return visible.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView address;
            final TextView subject;
            final TextView status;

            Holder(@NonNull View itemView) {
                super(itemView);
                address = itemView.findViewById(R.id.mailRowAddress);
                subject = itemView.findViewById(R.id.mailRowSubject);
                status = itemView.findViewById(R.id.mailRowStatus);
            }
        }
    }

    private class StationAdapter extends RecyclerView.Adapter<StationAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            view.setPadding(8, 28, 8, 28);
            view.setTextSize(18);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            RadioMailStation station = stations.get(position);
            holder.label.setText((station.favorite ? "★ " : "") + station.callsign + "  " + station.transport
                    + (station.note == null || station.note.isEmpty() ? "" : "\n" + station.note));
            holder.label.setOnClickListener(v -> {
                openMessage = null;
                fillComposer(station.callsign, "", "");
            });
            holder.label.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(RadioMailActivity.this)
                        .setTitle(station.callsign)
                        .setItems(new CharSequence[]{getString(R.string.mail_favorite_station), getString(R.string.mail_remove_station)}, (d, which) -> {
                            if (which == 0) {
                                station.favorite = !station.favorite;
                                io.execute(() -> dao().update(station));
                            } else {
                                io.execute(() -> dao().delete(station));
                            }
                            reloadStations();
                        })
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return stations.size();
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
