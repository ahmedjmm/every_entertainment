package com.dev.xapp.database.history;

import androidx.room.Embedded;
import androidx.room.Relation;

public class Wrapper {
    @Embedded
    public HistoryHeader historyHeader;
    @Relation(parentColumn = "headerId", entityColumn = "contentsId")
    public HistoryContents historyContents;

    public Wrapper(HistoryHeader historyHeader, HistoryContents historyContents){
        this.historyHeader = historyHeader;
        this.historyContents = historyContents;
    }
}