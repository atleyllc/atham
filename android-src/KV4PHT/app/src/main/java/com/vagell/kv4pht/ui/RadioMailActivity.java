/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Modified 2026 by Atley LLC: packet radio mail mailbox.

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
import com.vagell.kv4pht.data.RadioMailMessage;
import com.vagell.kv4pht.mail.RadioMail;
import com.vagell.kv4pht.radio.RadioAudioService;
import com.vagell.kv4pht.radio.RadioServiceConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RadioMailActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final List<RadioMailMessage> rows = new ArrayList<>();
    private RadioServiceConnector connector;
    private RadioAudioService radio;
    private int folder = RadioMailMessage.OUTBOX;
    private MailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Appearance.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_mail);
        RecyclerView list = findViewById(R.id.mailList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MailAdapter();
        list.setAdapter(adapter);
        connector = new RadioServiceConnector(this);
        connector.bind(service -> radio = service);
        reload();
    }

    @Override
    protected void onDestroy() {
        connector.unbind();
        io.shutdown();
        super.onDestroy();
    }

    public void mailInboxClicked(View view) {
        showFolder(RadioMailMessage.INBOX);
    }

    public void mailOutboxClicked(View view) {
        showFolder(RadioMailMessage.OUTBOX);
    }

    public void mailSentClicked(View view) {
        showFolder(RadioMailMessage.SENT);
    }

    public void mailSendClicked(View view) {
        String address = text(R.id.mailTo).trim();
        String subject = text(R.id.mailSubject).trim();
        String body = text(R.id.mailBody).trim();
        if (address.isEmpty() || (subject.isEmpty() && body.isEmpty())) {
            return;
        }
        if (RadioMail.isEmail(address)) {
            save(address, subject, body, RadioMailMessage.OUTBOX, getString(R.string.mail_held_for_winlink), RadioMailMessage.OUTBOX);
            clearComposer();
            return;
        }
        if (!RadioMail.isCallsign(address)) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.mail_bad_address)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.mail_send)
                .setMessage(R.string.mail_send_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.mail_send, (d, w) -> transmit(address, subject, body))
                .show();
    }

    private void transmit(String address, String subject, String body) {
        if (radio == null || !radio.isTxAllowed()) {
            save(address, subject, body, RadioMailMessage.OUTBOX, getString(R.string.mail_tx_not_allowed), RadioMailMessage.OUTBOX);
            clearComposer();
            return;
        }
        List<String> packets = RadioMail.packets(subject, body);
        boolean failed = false;
        for (String packet : packets) {
            if (radio.sendChatMessage(address.toUpperCase(), packet) < 0) {
                failed = true;
                break;
            }
        }
        int target = failed ? RadioMailMessage.OUTBOX : RadioMailMessage.SENT;
        save(address, subject, body, target,
                getString(failed ? R.string.mail_send_failed : R.string.mail_sent_on_frequency), target);
        clearComposer();
    }

    private void save(String address, String subject, String body, int targetFolder, String status, int showAfter) {
        RadioMailMessage message = new RadioMailMessage();
        message.address = address;
        message.subject = subject;
        message.body = body;
        message.folder = targetFolder;
        message.status = status;
        message.createdAt = System.currentTimeMillis();
        io.execute(() -> {
            AppDatabase.getInstance(this).radioMailDao().insert(message);
            runOnUiThread(() -> showFolder(showAfter));
        });
    }

    private void showFolder(int next) {
        folder = next;
        styleFolder();
        reload();
    }

    private void styleFolder() {
        stylePill(R.id.mailInbox, folder == RadioMailMessage.INBOX);
        stylePill(R.id.mailOutbox, folder == RadioMailMessage.OUTBOX);
        stylePill(R.id.mailSent, folder == RadioMailMessage.SENT);
    }

    private void stylePill(int id, boolean selected) {
        TextView pill = findViewById(id);
        pill.setBackgroundResource(selected ? R.drawable.pill_outline : R.drawable.pill_idle);
    }

    private void reload() {
        io.execute(() -> {
            List<RadioMailMessage> loaded = AppDatabase.getInstance(this).radioMailDao().folder(folder);
            runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void clearComposer() {
        ((EditText) findViewById(R.id.mailTo)).setText("");
        ((EditText) findViewById(R.id.mailSubject)).setText("");
        ((EditText) findViewById(R.id.mailBody)).setText("");
    }

    private String text(int id) {
        return ((EditText) findViewById(id)).getText().toString();
    }

    private class MailAdapter extends RecyclerView.Adapter<MailAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_mail_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            RadioMailMessage message = rows.get(position);
            holder.address.setText(message.address);
            holder.subject.setText(message.subject == null || message.subject.isEmpty() ? message.body : message.subject);
            holder.status.setText(message.status);
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
}
