package com.dev.xapp;

import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Song implements Parcelable{

    public String path, albumName, artistName, albumId, songName, tracksCount, artistId;
    public int songDuration;

    //all songs constructor
    public Song(String path, String songName, String artistName, int songDuration, String albumId, String artistId){
        this.path = path;
        this.songName = songName;
        this.artistName = artistName;
        this.songDuration = songDuration;
        this.albumId = albumId;
        this.artistId = artistId;
    }

    //albums constructor
    public Song(String albumName, String artistName, String tracksCount, String albumId){
        this.albumName = albumName;
        this.artistName = artistName;
        this.tracksCount = tracksCount;
        this.albumId = albumId;
    }

    //artist constructor
    public Song(String artistName, String artistId, String tracksCount){
        this.artistId = artistId;
        this.artistName = artistName;
        this.tracksCount = tracksCount;
    }

    protected Song(Parcel in) {
        path = in.readString();
        albumName = in.readString();
        artistName = in.readString();
        albumId = in.readString();
        songName = in.readString();
        tracksCount = in.readString();
        artistId = in.readString();
        songDuration = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public static void setAudioAlbumArt(List<Song> songList, HashMap<String, String> hashMap){
        for(int x = 0; x < songList.size(); x++)
            hashMap.put(songList.get(x).albumId, songList.get(x).path);
    }

    public static void setAudioArtistArt(List<Song> songList, HashMap<String, String> hashMap){
        for(int x = 0; x < songList.size(); x++)
            hashMap.put(songList.get(x).artistId, songList.get(x).path);
    }

    public static byte[] getAudioAlbumArt(String path){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] bytes = mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return bytes;
    }

    //song duration conversion
    public static String durationConversion(int songDuration){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(songDuration), TimeUnit.MILLISECONDS.toMinutes(songDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(songDuration)),
                TimeUnit.MILLISECONDS.toSeconds(songDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration)));
    }

    public static void sortAlbum(List<Song> songList, String sortType, boolean isReverse) {
        switch (sortType){
            case "album a to z":
                if (!isReverse)
                    Collections.sort(songList, (song, s2) -> song.albumName.toLowerCase().compareTo(s2.albumName.toLowerCase()));
                else {
                    Collections.sort(songList, (song, s2) -> song.albumName.toLowerCase().compareTo(s2.albumName.toLowerCase()));
                    Collections.reverse(songList);
                }
                break;
            case "artist a to z":
                if(!isReverse)
                    Collections.sort(songList, (song, s2) -> song.artistName.toLowerCase().compareTo(s2.artistName.toLowerCase()));
                else{
                    Collections.sort(songList, (song, s2) -> song.artistName.toLowerCase().compareTo(s2.artistName.toLowerCase()));
                    Collections.reverse(songList);
                }
                break;
            case "album count small to large":
                if(!isReverse)
                    Collections.sort(songList, (song, s2) -> Long.compare(Long.parseLong(song.tracksCount), Long.parseLong(s2.tracksCount)));
                else{
                    Collections.sort(songList, (song, s2) -> Long.compare(Long.parseLong(song.tracksCount), Long.parseLong(s2.tracksCount)));
                    Collections.reverse(songList);
                }
                break;
        }
    }

    public static void sortArtist(List<Song> songList, String sortType, boolean isReverse) {
        switch (sortType){
            case "artist a to z":
                if(!isReverse)
                    Collections.sort(songList, (song, s2) -> song.artistName.toLowerCase().compareTo(s2.artistName.toLowerCase()));
                else{
                    Collections.sort(songList, (song, s2) -> song.artistName.toLowerCase().compareTo(s2.artistName.toLowerCase()));
                    Collections.reverse(songList);
                }
                break;
            case "artist count small to large":
                if(!isReverse)
                    Collections.sort(songList, (song, s2) -> Long.compare(Long.parseLong(song.tracksCount), Long.parseLong(s2.tracksCount)));
                else{
                    Collections.sort(songList, (song, s2) -> Long.compare(Long.parseLong(song.tracksCount), Long.parseLong(s2.tracksCount)));
                    Collections.reverse(songList);
                }
                break;
        }
    }

    public static void sortSong(ArrayList<Song> songsList, String sortType, boolean isReverse) {
        switch (sortType){
            case "name a to z":
                if (!isReverse)
                    Collections.sort(songsList, (song, s2) -> song.songName.toLowerCase().compareTo(s2.songName.toLowerCase()));
                else {
                    Collections.sort(songsList, (song, s2) -> song.songName.toLowerCase().compareTo(s2.songName.toLowerCase()));
                    Collections.reverse(songsList);
                }
                break;
            case "album a to z":
                if (!isReverse)
                    Collections.sort(songsList, (song, s2) -> song.albumName.toLowerCase().compareTo(s2.albumName.toLowerCase()));
                else {
                    Collections.sort(songsList, (song, s2) -> song.albumName.toLowerCase().compareTo(s2.albumName.toLowerCase()));
                    Collections.reverse(songsList);
                }
                break;
            case "artist a to z":
                if(!isReverse)
                    Collections.sort(songsList, (song, s2) -> song.artistName.toLowerCase().compareTo(s2.artistName.toLowerCase()));
                else{
                    Collections.sort(songsList, (song, s2) -> song.artistName.toLowerCase().compareTo(s2.artistName.toLowerCase()));
                    Collections.reverse(songsList);
                }
                break;
            case "duration short to long":
                if(!isReverse)
                    Collections.sort(songsList, (song, s2) -> Integer.compare(song.songDuration, s2.songDuration));
                else{
                    Collections.sort(songsList, (song, s2) -> Integer.compare(song.songDuration, s2.songDuration));
                    Collections.reverse(songsList);
                }
                break;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeString(albumId);
        dest.writeString(songName);
        dest.writeString(tracksCount);
        dest.writeString(artistId);
        dest.writeInt(songDuration);
    }
}