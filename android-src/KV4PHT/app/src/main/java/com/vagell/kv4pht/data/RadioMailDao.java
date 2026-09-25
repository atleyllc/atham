package com.vagell.kv4pht.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RadioMailDao {
    @Query("SELECT * FROM radio_mail WHERE folder = :folder ORDER BY created_at DESC")
    List<RadioMailMessage> folder(int folder);

    @Insert
    long insert(RadioMailMessage message);

    @Update
    void update(RadioMailMessage message);
}
