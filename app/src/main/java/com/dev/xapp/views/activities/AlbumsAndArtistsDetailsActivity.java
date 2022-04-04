package com.dev.xapp.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dev.everyEntertainment.R;
import com.dev.everyEntertainment.databinding.ActivityAlbumsAndArtistsDetailsBinding;
import com.dev.xapp.models.Song;
import com.dev.xapp.views.fragments.MusicFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumsAndArtistsDetailsActivity extends AppCompatActivity {
    ListViewAdapter listViewAdapter;
    ArrayList<Song> songList = new ArrayList<>();
    ListView listView;
    LinearLayout linearLayout;
    ActivityAlbumsAndArtistsDetailsBinding binding;
    int totalDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlbumsAndArtistsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(getIntent().getStringExtra("name"));
        toolbar.setOnClickListener((v -> listView.smoothScrollToPosition(0)));

        ImageView imageView = binding.albumCover;
        TextView totalDuration = binding.totalDuration;
        TextView tracksCount = binding.albumArtistMusicCount;

        songList.clear();
        linearLayout = binding.linearLayout;
        switch (Objects.requireNonNull(getIntent().getStringExtra("idType"))){
            case "album tabId":
                for(int x = 0; x < MusicFragment.AllSongsFragment.songsList.size(); x++)
                    if(Objects.equals(getIntent().getStringExtra("tabId"), MusicFragment.AllSongsFragment.songsList.get(x).albumId)) {
                        songList.add(MusicFragment.AllSongsFragment.songsList.get(x));
                        this.totalDuration += MusicFragment.AllSongsFragment.songsList.get(x).songDuration;
                    }
                break;
            case "artist tabId":
                for(int x = 0; x < MusicFragment.AllSongsFragment.songsList.size(); x++)
                    if(Objects.equals(getIntent().getStringExtra("tabId"), MusicFragment.AllSongsFragment.songsList.get(x).artistId)) {
                        songList.add(MusicFragment.AllSongsFragment.songsList.get(x));
                        this.totalDuration += MusicFragment.AllSongsFragment.songsList.get(x).songDuration;
                    }
                break;
        }

        try {
            final byte[] bytes = Song.getAudioAlbumArt(songList.get(0).path);
            if(bytes != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Palette.from(bitmap).generate(palette -> {
                    Palette.Swatch swatch = Objects.requireNonNull(palette).getDominantSwatch();
                    if (swatch != null) {
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, swatch.getRgb()});
                        linearLayout.setBackground(gradientDrawable);
                        Glide.with(getApplicationContext()).load(bytes).into(imageView);
                        totalDuration.setText(Song.durationConversion(this.totalDuration));
                        tracksCount.setText(getIntent().getStringExtra("tracksCount"));
                    }
                });
            }
            else{
                final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.album_cover);
                Palette.from(bitmap).generate(palette -> {
                    Palette.Swatch swatch = Objects.requireNonNull(palette).getDominantSwatch();
                    if (swatch != null) {
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, swatch.getRgb()});
                        linearLayout.setBackground(gradientDrawable);
                        Glide.with(getApplicationContext()).load(bitmap).into(imageView);
                        totalDuration.setText(Song.durationConversion(this.totalDuration));
                        tracksCount.setText(getIntent().getStringExtra("tracksCount"));
                    }
                });
            }
        }
        catch (IndexOutOfBoundsException ignored){
            Toast.makeText(getApplicationContext(), R.string.no_contents, Toast.LENGTH_LONG).show();
        }

        listViewAdapter = new ListViewAdapter(getApplicationContext(), songList);
        listView = binding.listView;
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AlbumsAndArtistsDetailsActivity.this, MusicPlayerActivity.class);
            intent.putExtra("serviceState", true);
            intent.putParcelableArrayListExtra("songsList", songList);
            intent.putExtra("position", position);
            startActivity(intent);
        });
    }

    static class ListViewAdapter extends ArrayAdapter<Song> {
        List<Song> songList;
        Context context;
        public ListViewAdapter(@NonNull Context context, @NonNull List<Song> songList) {
            super(context, 0, songList);
            this.context = context;
            this.songList = songList;
        }

        @Override
        public int getCount() {
            return songList.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.albums_and_artists_songs_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.songName = convertView.findViewById(R.id.song_name);
                viewHolder.artistName = convertView.findViewById(R.id.artist_name);
                viewHolder.songDuration = convertView.findViewById(R.id.song_duration);
                viewHolder.imageView = convertView.findViewById(R.id.song_image);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.songName.setText(songList.get(position).songName);
            viewHolder.songDuration.setText(Song.durationConversion(songList.get(position).songDuration));
            viewHolder.artistName.setText(songList.get(position).artistName);
            byte[] bytes = Song.getAudioAlbumArt(songList.get(position).path);
            if(bytes != null)
                Glide.with(this.context).load(bytes).into(viewHolder.imageView);
            else
                viewHolder.imageView.setImageResource(R.drawable.album_cover);
            return convertView;
        }

        static class ViewHolder {
            TextView songName, artistName, songDuration;
            ImageView imageView;
        }
    }
}