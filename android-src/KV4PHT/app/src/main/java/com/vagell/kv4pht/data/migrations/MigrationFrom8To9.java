package com.vagell.kv4pht.data.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrationFrom8To9 extends Migration {
    public MigrationFrom8To9() {
        super(8, 9);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS radio_mail ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + "folder INTEGER NOT NULL, "
                + "address TEXT, "
                + "subject TEXT, "
                + "body TEXT, "
                + "created_at INTEGER NOT NULL, "
                + "status TEXT)");
    }
}
