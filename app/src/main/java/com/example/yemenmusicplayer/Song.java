package com.example.yemenmusicplayer;

public class Song {
    private String title;
    private String streamUrl;
    private String downloadUrl;

    public Song(String title, String streamUrl, String downloadUrl) {
        this.title = title;
        this.streamUrl = streamUrl;
        this.downloadUrl = downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}