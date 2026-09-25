package com.vagell.kv4pht.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "radio_mail")
public class RadioMailMessage {
    public static final int DRAFT = 0;
    public static final int OUTBOX = 1;
    public static final int SENT = 2;
    public static final int INBOX = 3;

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "folder")
    public int folder;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "subject")
    public String subject;

    @ColumnInfo(name = "body")
    public String body;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "status")
    public String status;
}
