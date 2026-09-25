package com.vagell.kv4pht.data.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrationFrom7To8 extends Migration {
    public MigrationFrom7To8() {
        super(7, 8);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE aprs_messages ADD COLUMN delivery INTEGER NOT NULL DEFAULT 0");
        database.execSQL("UPDATE aprs_messages SET delivery = 2 WHERE ack = 1");
    }
}
