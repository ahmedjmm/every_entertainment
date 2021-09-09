package com.dev.xapp;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageVideo {
    public String albumName;
    public List<String> imagesVideosPath;

    public ImageVideo(String albumName, List<String> imagesVideosPath) {
        this.albumName = albumName;
        this.imagesVideosPath = imagesVideosPath;
    }

    //video duration conversion
    public static String durationConversion(int songDuration){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(songDuration),
                TimeUnit.MILLISECONDS.toMinutes(songDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(songDuration)),
                TimeUnit.MILLISECONDS.toSeconds(songDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration)));
    }
}
