package com.vagell.kv4pht.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "qsos")
public class Qso {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "callsign")
    public String callsign;

    @ColumnInfo(name = "frequency")
    public String frequency;

    @ColumnInfo(name = "mode")
    public String mode;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
