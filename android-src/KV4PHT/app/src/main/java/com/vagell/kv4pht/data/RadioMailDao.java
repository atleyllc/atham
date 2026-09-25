package com.vagell.kv4pht.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.vagell.kv4pht.data.RadioMailStation;

@Dao
public interface RadioMailDao {
    @Query("SELECT * FROM radio_mail WHERE folder = :folder ORDER BY created_at DESC")
    List<RadioMailMessage> folder(int folder);

    @Query("SELECT * FROM radio_mail WHERE flagged = 1 AND folder != 4 ORDER BY created_at DESC")
    List<RadioMailMessage> flagged();

    @Query("SELECT COUNT(*) FROM radio_mail WHERE folder = :folder")
    int count(int folder);

    @Query("SELECT COUNT(*) FROM radio_mail WHERE flagged = 1 AND folder != 4")
    int flaggedCount();

    @Insert
    long insert(RadioMailMessage message);

    @Update
    void update(RadioMailMessage message);

    @Query("SELECT * FROM radio_mail_stations ORDER BY favorite DESC, callsign COLLATE NOCASE")
    List<RadioMailStation> stations();

    @Query("SELECT * FROM radio_mail_stations WHERE favorite = 1 ORDER BY callsign COLLATE NOCASE")
    List<RadioMailStation> favorites();

    @Insert
    long insert(RadioMailStation station);

    @Update
    void update(RadioMailStation station);
}
