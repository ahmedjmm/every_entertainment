package com.dev.xapp.database.history;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(indices = @Index(value = {"url"}, unique = true))
public class HistoryContents {
    @PrimaryKey(autoGenerate = true)
    public long contentsId;
    public String title, url;

    @ForeignKey(entity = HistoryHeader.class, parentColumns="headerId", childColumns = "contentsId", onDelete = CASCADE)
    public long headerId;

    public HistoryContents(long headerId, String title, String url){
        this.headerId = headerId;
        this.title = title;
        this.url = url;
    }
}
