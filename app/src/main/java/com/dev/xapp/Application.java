package com.dev.xapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.dev.everyEntertainment.R;

public class Application extends android.app.Application {
    public static final String media_player_channel_id = "mediaPlayerChannel";
//    public static final String channel_2_id = "channel2";
    public static final String play = "play";
    public static final String next = "next";
    public static final String previous = "previous";
    public static final String dismiss = "dismiss";
    public NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mediaPlayerNotification = new NotificationChannel(media_player_channel_id, "media player", NotificationManager.IMPORTANCE_HIGH);
            mediaPlayerNotification.setDescription(getResources().getString(R.string.media_player_notification_desc));
//            NotificationChannel notificationChannel2 = new NotificationChannel(channel_2_id, "channel 2", NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel2.setDescription("channel 2 Desc");
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mediaPlayerNotification);
//            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }
}
