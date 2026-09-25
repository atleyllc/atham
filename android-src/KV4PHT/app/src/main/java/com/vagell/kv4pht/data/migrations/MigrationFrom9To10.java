package com.vagell.kv4pht.data.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrationFrom9To10 extends Migration {
    public MigrationFrom9To10() {
        super(9, 10);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE radio_mail ADD COLUMN unread INTEGER NOT NULL DEFAULT 1");
        database.execSQL("ALTER TABLE radio_mail ADD COLUMN flagged INTEGER NOT NULL DEFAULT 0");
        database.execSQL("CREATE TABLE IF NOT EXISTS radio_mail_stations ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + "callsign TEXT, "
                + "note TEXT, "
                + "favorite INTEGER NOT NULL DEFAULT 0, "
                + "transport TEXT)");
    }
}
