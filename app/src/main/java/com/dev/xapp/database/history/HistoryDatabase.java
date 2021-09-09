package com.dev.xapp.database.history;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HistoryContents.class, HistoryHeader.class}, version = 1)
public abstract class HistoryDatabase extends RoomDatabase {
    public abstract HistoryDAO getHistoryDAO();
}