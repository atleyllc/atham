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

import java.util.ArrayList;
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
        if (RadioMail.isEmail(address)) {
            store(address, subject, body, RadioMailMessage.OUTBOX, getString(R.string.mail_held_for_winlink));
            return;
        }
        if (!RadioMail.isCallsign(address)) {
            new MaterialAlertDialogBuilder(this).setMessage(R.string.mail_bad_address).setPositiveButton(android.R.string.ok, null).show();
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
        ((EditText) findViewById(R.id.mailTo)).setText(openMessage.address);
        ((EditText) findViewById(R.id.mailSubject)).setText(openMessage.subject != null && openMessage.subject.startsWith("Re:") ? openMessage.subject : "Re: " + openMessage.subject);
        ((EditText) findViewById(R.id.mailBody)).setText("\n\n" + openMessage.body);
        show(R.id.mailComposer);
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
        if (radio == null || !radio.isTxAllowed()) {
            store(address, subject, body, RadioMailMessage.OUTBOX, getString(R.string.mail_tx_not_allowed));
            return;
        }
        boolean failed = false;
        for (String packet : RadioMail.packets(subject, body)) {
            if (radio.sendChatMessage(address.toUpperCase(), packet) < 0) {
                failed = true;
                break;
            }
        }
        store(address, subject, body, failed ? RadioMailMessage.OUTBOX : RadioMailMessage.SENT,
                getString(failed ? R.string.mail_send_failed : R.string.mail_sent_on_frequency));
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
                mailAdapter.notifyDataSetChanged();
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
            int drafts = dao().count(RadioMailMessage.DRAFT);
            int outbox = dao().count(RadioMailMessage.OUTBOX);
            int sent = dao().count(RadioMailMessage.SENT);
            int trash = dao().count(RadioMailMessage.TRASH);
            int archive = dao().count(RadioMailMessage.ARCHIVE);
            int flagged = dao().flaggedCount();
            runOnUiThread(() -> {
                label(R.id.rowInbox, R.string.mail_inbox, inbox);
                label(R.id.rowDrafts, R.string.mail_drafts, drafts);
                label(R.id.rowOutbox, R.string.mail_outbox, outbox);
                label(R.id.rowSent, R.string.mail_sent, sent);
                label(R.id.rowTrash, R.string.mail_trash, trash);
                label(R.id.rowArchive, R.string.mail_archive, archive);
                label(R.id.rowFlagged, R.string.mail_flagged, flagged);
            });
        });
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
        ((TextView) findViewById(R.id.readerStatus)).setText(message.status);
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
            RadioMailMessage message = rows.get(position);
            holder.address.setText((message.unread ? "● " : "") + message.address);
            holder.subject.setText(message.flagged ? "⚑ " + message.subject : message.subject);
            holder.status.setText(message.status);
            holder.itemView.setOnClickListener(v -> openReader(message));
        }

        @Override
        public int getItemCount() {
            return rows.size();
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
                ((EditText) findViewById(R.id.mailTo)).setText(station.callsign);
                show(R.id.mailComposer);
            });
            holder.label.setOnLongClickListener(v -> {
                station.favorite = !station.favorite;
                io.execute(() -> dao().update(station));
                reloadStations();
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
