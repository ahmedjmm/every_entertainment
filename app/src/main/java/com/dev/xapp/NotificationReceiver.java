package com.dev.xapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dev.xapp.activities.MusicPlayerActivity;

import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MusicPlayerActivity.MusicService.class);
        String actionName = intent.getAction();
        switch (Objects.requireNonNull(actionName)){
            case Application.play:
                serviceIntent.putExtra("actionName", "play");
                context.startService(serviceIntent);
                break;
            case Application.next:
                serviceIntent.putExtra("actionName", "next");
                context.startService(serviceIntent);
                break;
            case Application.previous:
                serviceIntent.putExtra("actionName", "previous");
                context.startService(serviceIntent);
                break;
            case Application.dismiss:
                serviceIntent.putExtra("actionName", "dismiss");
                context.startService(serviceIntent);
                break;
        }
    }
}
