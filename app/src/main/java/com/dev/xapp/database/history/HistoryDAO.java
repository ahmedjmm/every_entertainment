package com.dev.xapp.database.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface HistoryDAO {
    @Insert
    @Transaction
    long addHistoryHeader(HistoryHeader historyHeader);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addHistoryContents(HistoryContents historyContents);

    @Query("SELECT * FROM HistoryHeader ORDER BY headerId DESC LIMIT 1")
    HistoryHeader getLastHistoryRecord();

    @Transaction
    @Query("SELECT * FROM HistoryHeader")
    List<Wrapper> getAllHistories();


    @Query("SELECT * FROM HistoryContents WHERE headerId=:headerId")
    List<HistoryContents> getHistoryContentsForHeader(final long headerId);

    //delete empty headers which has no HistoryContents
    @Query("DELETE FROM HistoryHeader WHERE headerId=:headerId")
    void deleteHeader(long headerId);

//    @Query("select * from HistoryContents")
//    List<HistoryContents> getHistoryContents();
//
//    @Query("SELECT * FROM HistoryHeader ORDER BY headerId DESC LIMIT 1")
//    HistoryHeader getLastHistoryRecord();
}
