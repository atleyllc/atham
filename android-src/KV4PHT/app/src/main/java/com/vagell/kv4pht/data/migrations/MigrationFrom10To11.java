package com.vagell.kv4pht.data.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrationFrom10To11 extends Migration {
    public MigrationFrom10To11() {
        super(10, 11);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS qsos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + "callsign TEXT, "
                + "frequency TEXT, "
                + "mode TEXT, "
                + "notes TEXT, "
                + "created_at INTEGER NOT NULL)");
    }
}
