package com.vagell.kv4pht.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "radio_mail_stations")
public class RadioMailStation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "callsign")
    public String callsign;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "favorite", defaultValue = "0")
    public boolean favorite;

    @ColumnInfo(name = "transport")
    public String transport;
}
