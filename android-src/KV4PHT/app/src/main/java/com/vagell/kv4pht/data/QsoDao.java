package com.vagell.kv4pht.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QsoDao {
    @Query("SELECT * FROM qsos ORDER BY created_at DESC")
    List<Qso> all();

    @Insert
    long insert(Qso qso);
}
