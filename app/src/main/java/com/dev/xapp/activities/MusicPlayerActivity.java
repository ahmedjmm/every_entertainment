package com.dev.xapp.activities;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.MailTo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.dev.everyEntertainment.R;
import com.dev.xapp.Application;
import com.dev.xapp.NotificationReceiver;
import com.dev.xapp.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements ServiceConnection {
    static MusicService musicService;
    TextView songName, artistName, albumName, durationPlayed, totalDuration, dragViewTitle;
    public static ImageView next;
    public static ImageView previous;
    public static FloatingActionButton floatingActionButton;
    ImageView songImage, shuffle, repeat;
    ListView listView;
    PlayListAdapter playListAdapter;
    LinearLayout linearLayout;
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    SeekBar seekBar;
    public static List<Song> songList = new ArrayList<>();
    Handler handler = new Handler();
    Runnable runnable;
    Thread playPauseThread, nextThread, previousThread;
    private static int position;
    boolean shuffleBoolean, repeatBoolean;
    public static Intent musicServiceIntent;
    public static boolean serviceState;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Objects.equals(intent.getAction(), "finishActivity")){
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("finishActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        listView = findViewById(R.id.list_view);
        songList = getIntent().getParcelableArrayListExtra("songsList");
        position = getIntent().getIntExtra("position", -1);
        serviceState = getIntent().getBooleanExtra("serviceState", false);
        if(serviceState){
            if(isMusicServiceRunning()) {
                musicService.stop();
                musicService.release();
                MusicService.songArrayList = (ArrayList<Song>) songList;
                musicService.createMediaPlayer(position);
                musicService.start();
            }
        }

        songImage = findViewById(R.id.album_cover);
        songName = findViewById(R.id.music_name);
        albumName = findViewById(R.id.album_name);
        artistName = findViewById(R.id.artist_name);
        seekBar = findViewById(R.id.seek_bar);
        durationPlayed = findViewById(R.id.played_duration);
        totalDuration = findViewById(R.id.music_duration);
        repeat = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);
        floatingActionButton = findViewById(R.id.play_pause);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        linearLayout = findViewById(R.id.linear_layout);
        dragViewTitle = findViewById(R.id.dragViewTitle);
        playMusic();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (sharedPreferences.getBoolean("playerShuffle", false)) {
            shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24);
            shuffleBoolean = true;
        }
        else {
            shuffle.setImageResource(R.drawable.ic_baseline_shuffle_off_24);
            shuffleBoolean = false;
        }
        if (sharedPreferences.getBoolean("playerRepeat", false)) {
            repeat.setImageResource(R.drawable.ic_baseline_repeat_24);
            repeatBoolean = true;
        }
        else {
            repeat.setImageResource(R.drawable.ic_baseline_repeat_24_off);
            repeatBoolean = false;
        }

        shuffle.setOnClickListener(v -> {
            if (sharedPreferences.getBoolean("playerShuffle", false)) {
                editor = sharedPreferences.edit();
                editor.putBoolean("playerShuffle", false);
                editor.commit();
                shuffleBoolean = false;
                shuffle.setImageResource(R.drawable.ic_baseline_shuffle_off_24);
                Toast.makeText(getApplicationContext(), R.string.shuffle_off, Toast.LENGTH_SHORT).show();
            }
            else {
                editor = sharedPreferences.edit();
                editor.putBoolean("playerShuffle", true);
                editor.commit();
                shuffleBoolean = true;
                shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24);
                Toast.makeText(getApplicationContext(), R.string.shuffle_on, Toast.LENGTH_SHORT).show();
            }
        });

        repeat.setOnClickListener(v -> {
            if (sharedPreferences.getBoolean("playerRepeat", false)) {
                editor = sharedPreferences.edit();
                editor.putBoolean("playerRepeat", false);
                editor.commit();
                repeatBoolean = false;
                repeat.setImageResource(R.drawable.ic_baseline_repeat_24_off);
                Toast.makeText(getApplicationContext(), R.string.repeat_off, Toast.LENGTH_LONG).show();
            }
            else {
                editor = sharedPreferences.edit();
                editor.putBoolean("playerRepeat", true);
                editor.commit();
                repeatBoolean = true;
                repeat.setImageResource(R.drawable.ic_baseline_repeat_24);
                Toast.makeText(getApplicationContext(), R.string.repeat_on, Toast.LENGTH_LONG).show();
            }
        });
    }

    public String durationConversion(int songDuration) {
        long s = songDuration % 60;
        long m = (songDuration / 60) % 60;
        long h = (songDuration / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    void playPauseButton() {
        playPauseThread = new Thread() {
            @Override
            public void run() {
                super.run();
                floatingActionButton.setOnClickListener(v -> {
                    if (musicService.isPlaying()) {
                        musicService.pause();
                        musicService.showNotification(R.drawable.ic_baseline_play_arrow_white_24);
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    }
                    else {
                        musicService.start();
                        musicService.showNotification(R.drawable.ic_baseline_pause__white24);
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                    seekBar.setMax(musicService.getDuration() / 1000);
                    MusicPlayerActivity.this.runOnUiThread(runnable);
                });
            }
        };
        playPauseThread.start();
    }

    private void nextButton() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                next.setOnClickListener(v -> {
                    musicService.stop();
                    musicService.release();
                    if(shuffleBoolean && !repeatBoolean)
                        position = random(songList.size()-1);
                    else if (!shuffleBoolean && !repeatBoolean)
                        position++;
                    if (position == songList.size())
                        position = 0;
                    musicService.createMediaPlayer(position);
                    metaDataRetriever(Uri.parse(songList.get(position).path));
                    seekBar.setMax(musicService.getDuration() / 1000);
                    MusicPlayerActivity.this.runOnUiThread(runnable);
                    floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    musicService.showNotification(R.drawable.ic_baseline_pause__white24);
                    musicService.onComplete();
                    musicService.start();
                });
            }
        };
        nextThread.start();
    }

    private void previousButton() {
        previousThread = new Thread() {
            @Override
            public void run() {
                super.run();
                previous.setOnClickListener(v -> {
                    musicService.stop();
                    musicService.release();
                    if(shuffleBoolean && !repeatBoolean)
                        position = random(songList.size()-1);
                    else if (!shuffleBoolean && !repeatBoolean)
                        position--;
                    if (position < 0)
                        position = 0;
                    musicService.createMediaPlayer(position);
                    metaDataRetriever(Uri.parse(songList.get(position).path));
                    seekBar.setMax(musicService.getDuration() / 1000);
                    MusicPlayerActivity.this.runOnUiThread(runnable);
                    musicService.onComplete();
                    musicService.showNotification(R.drawable.ic_baseline_pause__white24);
                    floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    musicService.start();
                });
            }
        };
        previousThread.start();
    }

    private int random(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public void metaDataRetriever(Uri uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri.getPath());
        byte[] bytes = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (bytes != null) {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            layoutAnimation(getApplicationContext(), songImage, bitmap);
            Palette.from(bitmap).generate(palette -> {
                Palette.Swatch swatch = Objects.requireNonNull(palette).getDominantSwatch();
                if (swatch != null) {
                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, swatch.getRgb()});
                    linearLayout.setBackground(gradientDrawable);
                    songName.setTextColor(swatch.getTitleTextColor());
                    albumName.setTextColor(swatch.getTitleTextColor());
                    artistName.setTextColor(swatch.getTitleTextColor());
                    durationPlayed.setTextColor(Color.WHITE);
                    totalDuration.setTextColor(Color.WHITE);
                    floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    floatingActionButton.getBackground().setTint(Color.WHITE);
                    next.setImageResource(R.drawable.ic_baseline_skip_next_white_24);
                    previous.setImageResource(R.drawable.ic_baseline_skip_previous_white_24);
                    seekBar.getThumb().setTint(Color.WHITE);
                }
            });
        } else {
            layoutAnimation(getApplicationContext(), songImage, null);
            linearLayout.setBackgroundResource(R.drawable.gradient_brown);
            songName.setTextColor(Color.DKGRAY);
            albumName.setTextColor(Color.DKGRAY);
            artistName.setTextColor(Color.DKGRAY);
            floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
            next.setImageResource(R.drawable.ic_baseline_skip_next_24);
            previous.setImageResource(R.drawable.ic_baseline_skip_previous_24);
            seekBar.getThumb().setTint(getResources().getColor(R.color.colorYellow));
            durationPlayed.setTextColor(getResources().getColor(R.color.colorYellow));
            totalDuration.setTextColor(getResources().getColor(R.color.colorYellow));
        }
        songName.setText(songList.get(position).songName);
        albumName.setText(songList.get(position).albumName);
        artistName.setText(songList.get(position).artistName);
        totalDuration.setText(Song.durationConversion(songList.get(position).songDuration));
    }

    private boolean isMusicServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MusicPlayerActivity.MusicService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void playMusic() {
        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
        if (songList != null)
            floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
        startService(musicServiceIntent);
    }

    public void layoutAnimation(final Context context, final ImageView imageView, final Bitmap bitmap) {
        final Animation animationIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        Animation animationOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        if (bitmap != null)
            animationOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Glide.with(context).load(bitmap).into(imageView);
                    animationIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    imageView.startAnimation(animationIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        else
            animationOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Glide.with(context).load(R.drawable.album_cover).into(imageView);
                    animationIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    imageView.startAnimation(animationIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        imageView.startAnimation(animationOut);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        if (position == -1)
            position = sharedPreferences.getInt("nowPlayingPosition", 0);
        bindService(intent, this, 0);
        playPauseButton();
        nextButton();
        previousButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = sharedPreferences.edit();
        editor.putInt("nowPlayingPosition", position);
        editor.commit();
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getMusicService();
//        check if the playlist of the MusicService is null, if so this means that the service is null
//        so we start the app from its launcher activity (MainActivity) to prevent null exception
//        of MusicService.songsArrayList.
        if(MusicService.songArrayList == null){
            Intent intent = new Intent(MusicPlayerActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        songList = MusicService.songArrayList;
        playListAdapter = new PlayListAdapter(getApplicationContext(), songList);
        listView.setAdapter(playListAdapter);
        String title = getResources().getString(R.string.play_list);
        dragViewTitle.setText(title);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            MusicPlayerActivity.position = position;
            musicService.stop();
            musicService.createMediaPlayer(MusicPlayerActivity.position);
            metaDataRetriever(Uri.parse(songList.get(MusicPlayerActivity.position).path));
            seekBar.setMax(musicService.getDuration() / 1000);
            MusicPlayerActivity.this.runOnUiThread(runnable);
            floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause__white24);
            musicService.onComplete();
            musicService.start();
        });
        metaDataRetriever(Uri.parse(songList.get(position).path));
        if(!musicService.isPlaying()){
            musicService.stop();
            musicService.createMediaPlayer(position);
            musicService.start();
        }
        seekBar.setMax(musicService.getDuration() / 1000);
        musicService.onComplete();
        musicService.showNotification(R.drawable.ic_baseline_pause__white24);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    durationPlayed.setText(durationConversion(currentPosition));
                }
                handler.postDelayed(this, 200);
            }
        };
        MusicPlayerActivity.this.runOnUiThread(runnable);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService.stop();
        musicService = null;
        handler.removeCallbacks(runnable);
    }

    public static class PlayListAdapter extends ArrayAdapter<Song>{
        Context context;
        List<Song> songList;
        ViewHolder viewHolder;

        public PlayListAdapter(@NonNull Context context, @NonNull List<Song> songList) {
            super(context, 0, songList);
            this.context = context;
            this.songList = songList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.play_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textView = convertView.findViewById(R.id.text);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.textView.setText(songList.get(position).songName);
            return convertView;
        }

        @Override
        public int getCount() {
            return songList.size();
        }

        @Override
        public Song getItem(int position) {
            return songList.get(position);
        }

        static class ViewHolder{
            TextView textView;
        }
    }

    public static class MusicService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

        public IBinder myBinder = new MyBinder();
        public static MediaPlayer mediaPlayer;
        MediaSessionCompat mediaSessionCompat;
        public static ArrayList<Song> songArrayList = new ArrayList<>();

        @Override
        public void onCreate() {
            super.onCreate();
            songArrayList = (ArrayList<Song>) MusicPlayerActivity.songList;
            mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "notification");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stop();
            release();
            mediaPlayer = null;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return myBinder;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (mediaPlayer == null) {
                createMediaPlayer(MusicPlayerActivity.position);
                start();
            }
            String actionName = intent.getStringExtra("actionName");
            if(actionName != null){
                editor = sharedPreferences.edit();
                switch (actionName){
                    case"play":
                        MusicPlayerActivity.floatingActionButton.performClick();
                        break;
                    case"next":
                        MusicPlayerActivity.next.performClick();
                        editor.putInt("nowPlayingPosition", position);
                        editor.commit();
                        break;
                    case"previous":
                        MusicPlayerActivity.previous.performClick();
                        editor.putInt("nowPlayingPosition", position);
                        editor.commit();
                        break;
                    case"dismiss":
                        stop();
                        stopSelf();
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MusicService.this);
                        localBroadcastManager.sendBroadcast(new Intent("finishActivity"));
                        break;
                }
            }
//            Use this return statement to make sure that the service won't start/restart by itself after
//            closing it from music player notification to avoid null exception of MusicService.songArrayList
//            when starting the app again from task manager after closing.
            return START_NOT_STICKY;
        }

        void showNotification(int playPauseButton){
            Intent openActivity = new Intent(this, MusicPlayerActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openActivity, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent previousIntent = new Intent(this, NotificationReceiver.class).setAction(Application.previous);
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent playIntent = new Intent(this, NotificationReceiver.class).setAction(Application.play);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(Application.next);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent dismiss = new Intent(this, NotificationReceiver.class).setAction(Application.dismiss);
            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);

            byte[] bytes = Song.getAudioAlbumArt(songArrayList.get(position).path);
            Bitmap bitmap;
            if(bytes != null)
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            else
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gradient_brown);
            Notification notification = new NotificationCompat.Builder(this, Application.media_player_channel_id).setSmallIcon(playPauseButton)
                    .setLargeIcon(bitmap).setContentTitle(songArrayList.get(position).songName).setContentText(songArrayList.get(position).artistName)
                    .setContentIntent(pendingIntent).setDeleteIntent(dismissPendingIntent)
                    .addAction(R.drawable.ic_baseline_skip_previous_white_24, "previous", previousPendingIntent)
                    .addAction(playPauseButton, "play", playPendingIntent)
                    .addAction(R.drawable.ic_baseline_skip_next_white_24, "next", nextPendingIntent)
                    .addAction(R.drawable.ic_baseline_close_24, "dismiss", dismissPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_HIGH).setOnlyAlertOnce(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
            startForeground(1, notification);
        }

        public int getCurrentPosition() {
            return mediaPlayer.getCurrentPosition();
        }

        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            next.performClick();
        }

        void start(){
            mediaPlayer.start();
        }

        boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }

        int getDuration(){
            return mediaPlayer.getDuration();
        }

        void stop(){
            mediaPlayer.stop();
        }

        void release(){
            mediaPlayer.release();
        }

        void seekTo(int position){
            mediaPlayer.seekTo(position);
        }

        void createMediaPlayer(int position){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(songArrayList.get(position).path));
        }

        void onComplete(){
            mediaPlayer.setOnCompletionListener(this);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            /* we use try catch block to avoid IllegalStateException when service stops and the system starts it
             because of audio focus change (example: when start a call after stops)*/
            try {
                if(focusChange <= 0)
                    mediaPlayer.pause();
                else
                    mediaPlayer.start();
            }catch (IllegalStateException ignored){ }
        }

        public class MyBinder extends Binder{
            MusicService getMusicService(){
                return MusicService.this;
            }
        }
    }
}