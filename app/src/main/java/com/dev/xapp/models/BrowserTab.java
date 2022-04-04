package com.dev.xapp.models;

public class BrowserTab {
    public String title, url;
    public byte[] bytes;
    public long tabId;

    public BrowserTab(long tabId, String title, String url, byte[] bytes){
        this.tabId = tabId;
        this.title = title;
        this.url = url;
        this.bytes = bytes;
    }

    public BrowserTab(String title, String url, byte[] bytes){
        this.title = title;
        this.url = url;
        this.bytes = bytes;
    }
}
