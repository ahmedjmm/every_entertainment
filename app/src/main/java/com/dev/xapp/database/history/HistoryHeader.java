package com.dev.xapp.database.history;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HistoryHeader {
    @PrimaryKey(autoGenerate = true)
    public long headerId;
    public String date;

    public HistoryHeader(String date){
        this.date = date;
    }
}
