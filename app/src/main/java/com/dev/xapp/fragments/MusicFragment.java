package com.dev.xapp.fragments;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.dev.xapp.R;
import com.dev.xapp.Song;
import com.dev.xapp.activities.AlbumsAndArtistsDetailsActivity;
import com.dev.xapp.activities.MusicPlayerActivity;
import com.dev.xapp.activities.SettingsActivity;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.SDCardFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MusicFragment extends Fragment {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewStateAdapter viewStateAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MemoryFragment.actionMode != null) {
            MemoryFragment.actionMode.finish();
            MemoryFragment.actionMode = null;
        }
        if(SDCardFragment.actionMode != null) {
            SDCardFragment.actionMode.finish();
            SDCardFragment.actionMode = null;
        }
        viewStateAdapter = new ViewStateAdapter(this.getChildFragmentManager(), getLifecycle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_music, container, false);

        toolbar = view.findViewById(R.id.tool_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.music_fragment_tab_all_music));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.music_fragment_tab_albums));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.music_fragment_tab_artists));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2 = view.findViewById(R.id.viewPager2);
        viewPager2.setAdapter(viewStateAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
        return view;
    }

    private static class ViewStateAdapter extends FragmentStateAdapter {

        public ViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0:
                    return new AllSongsFragment();
                case 1:
                    return new AlbumsFragment();
                case 2:
                    return new ArtistsFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class AlbumsFragment extends Fragment {
        List<Song> songsList = new ArrayList<>();
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        GridViewAdapter gridViewAdapter;
        Cursor cursor;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            loadAudioData();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return View.inflate(getContext(), R.layout.fragment_music_albums, null);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            gridViewAdapter = new GridViewAdapter(getContext(), songsList);
            final GridView gridView = view.findViewById(R.id.grid_view);
            gridView.setAdapter(gridViewAdapter);
            gridView.setOnItemClickListener((parent, view1, position, id) -> {
                Intent intent = new Intent(getActivity(), AlbumsAndArtistsDetailsActivity.class);
                intent.putExtra("tabId", songsList.get(position).albumId);
                intent.putExtra("idType", "album tabId");
                intent.putExtra("name", songsList.get(position).albumName);
                intent.putExtra("tracksCount", songsList.get(position).tracksCount);
                startActivity(intent);

            });
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.sort_song_a_to_z).setVisible(false);
            menu.findItem(R.id.sort_duration_short_to_long).setVisible(false);
            String sortType = sharedPreferences.getString("album_sort", "album a to z");
            boolean isReverse = sharedPreferences.getBoolean("album_sort_isReverse", false);
            if(sortType != null){
                switch (sortType){
                    case "album a to z":
                        menu.findItem(R.id.sort_album_a_to_z).setChecked(true);
                        break;
                    case "artist a to z":
                        menu.findItem(R.id.sort_artist_a_to_z).setChecked(true);
                        break;
                    case "album count small to large":
                        menu.findItem(R.id.sort_count_small_to_large).setChecked(true);
                        break;
                }
            }
            menu.findItem(R.id.reverse_sort).setChecked(isReverse);
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.music_menu, menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            boolean isReverse = sharedPreferences.getBoolean("album_sort_isReverse", false);
            switch (item.getItemId()){
                case R.id.sort_album_a_to_z:
                    Song.sortAlbum(songsList, "album a to z", isReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("album_sort", "album a to z");
                    editor.commit();
                    item.setChecked(true);
                    gridViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.sort_artist_a_to_z:
                    Song.sortAlbum(songsList, "artist a to z", isReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("album_sort", "artist a to z");
                    editor.commit();
                    item.setChecked(true);
                    gridViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.sort_count_small_to_large:
                    editor = sharedPreferences.edit();
                    editor.putString("album_sort", "album count small to large");
                    editor.commit();
                    Song.sortAlbum(songsList, "album count small to large", isReverse);
                    item.setChecked(true);
                    gridViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.reverse_sort:
                    editor = sharedPreferences.edit();
                    if(isReverse) {
                        item.setChecked(false);
                        editor.putBoolean("album_sort_isReverse", false);
                        editor.commit();
                        isReverse = false;
                    }
                    else {
                        item.setChecked(true);
                        editor.putBoolean("album_sort_isReverse", true);
                        editor.commit();
                        isReverse = true;
                    }
                    String sort = sharedPreferences.getString("album_sort", "album a to z");
                    if (sort != null) {
                        Song.sortAlbum(songsList, sort, isReverse);
                    }
                    gridViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.settings:
                    startActivity(new Intent(requireActivity(), SettingsActivity.class));
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        void loadAudioData() {
            ContentResolver contentResolver = requireActivity().getContentResolver();
            final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Audio.Albums._ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.ARTIST};
            cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst())
                while (cursor.moveToNext())
                    songsList.add(new Song(cursor.getString(1), cursor.getString(4),
                            cursor.getString(3), cursor.getString(0)));
            Objects.requireNonNull(cursor).close();
            String sortType = sharedPreferences.getString("album_sort", "album a to z");
            boolean isReverse = sharedPreferences.getBoolean("album_sort_isReverse", false);
            if (sortType != null)
                Song.sortAlbum(songsList, sortType, isReverse);
        }

        static class GridViewAdapter extends BaseAdapter{
            Context context;
            List<Song> songList;
            HashMap<String, String> hashMap = new HashMap<>();

            public GridViewAdapter(Context context, List<Song> songList){
                this.context = context;
                this.songList = songList;
                Song.setAudioAlbumArt(AllSongsFragment.songsList, hashMap);
            }

            @Override
            public int getCount() {
                return songList.size();
            }

            @Override
            public Object getItem(int position) {
                return songList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if(convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.album_grid_view_item, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.imageView = convertView.findViewById(R.id.album_image);
                    viewHolder.albumName = convertView.findViewById(R.id.album_name);
                    viewHolder.artistName = convertView.findViewById(R.id.artist_name);
                    viewHolder.tracksCount = convertView.findViewById(R.id.tracks_count);
                    convertView.setTag(viewHolder);
                }
                else
                    viewHolder = (ViewHolder) convertView.getTag();

                viewHolder.albumName.setText(songList.get(position).albumName);
                viewHolder.artistName.setText(songList.get(position).artistName);
                viewHolder.tracksCount.setText(songList.get(position).tracksCount);

                try {
                    byte[] bytes = Song.getAudioAlbumArt(hashMap.get(songList.get(position).albumId));
                    if(bytes != null)
                        Glide.with(this.context).asBitmap().load(bytes).into(viewHolder.imageView);
                    else
                        Glide.with(this.context).asBitmap().load(R.drawable.album_cover).into(viewHolder.imageView);
                }
                catch (IllegalArgumentException ignored){}

                return convertView;
            }
        }
        static class ViewHolder{
            ImageView imageView;
            TextView albumName, artistName, tracksCount;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ArtistsFragment extends Fragment {
        public List<Song> songsList = new ArrayList<>();
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        ListViewAdapter listViewAdapter;
        Cursor cursor;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            setHasOptionsMenu(true);
            loadAudioData();
            listViewAdapter = new ListViewAdapter(requireContext(), songsList);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return View.inflate(getContext(), R.layout.fragment_music_all_songs, container);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ListView listView = view.findViewById(R.id.list_view);
            listView.setAdapter(listViewAdapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                Intent intent = new Intent(getActivity(), AlbumsAndArtistsDetailsActivity.class);
                intent.putExtra("tabId", songsList.get(position).artistId);
                intent.putExtra("idType", "artist tabId");
                intent.putExtra("name", songsList.get(position).artistName);
                intent.putExtra("tracksCount", songsList.get(position).tracksCount);
                startActivity(intent);
            });
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.sort_song_a_to_z).setVisible(false);
            menu.findItem(R.id.sort_duration_short_to_long).setVisible(false);
            menu.findItem(R.id.sort_album_a_to_z).setVisible(false);
            boolean isReverse = sharedPreferences.getBoolean("artist_sort_isReverse", false);
            menu.findItem(R.id.reverse_sort).setChecked(isReverse);
            String sortType = sharedPreferences.getString("artist_sort", "artist a to z");
            if(sortType != null){
                switch (sortType){
                    case "artist a to z":
                        menu.findItem(R.id.sort_artist_a_to_z).setChecked(true);
                        break;
                    case "artist count small to large":
                        menu.findItem(R.id.sort_count_small_to_large).setChecked(true);
                        break;
                }
            }
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.music_menu, menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            boolean isReverse = sharedPreferences.getBoolean("artist_sort_isReverse", false);
            switch (item.getItemId()){
                case R.id.sort_artist_a_to_z:
                    Song.sortArtist(songsList, "artist a to z", isReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("artist_sort", "artist a to z");
                    editor.commit();
                    item.setChecked(true);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.sort_count_small_to_large:
                    editor = sharedPreferences.edit();
                    editor.putString("artist_sort", "artist count small to large");
                    editor.commit();
                    Song.sortArtist(songsList, "artist count small to large", isReverse);
                    item.setChecked(true);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.reverse_sort:
                    editor = sharedPreferences.edit();
                    if(isReverse) {
                        item.setChecked(false);
                        editor.putBoolean("artist_sort_isReverse", false);
                        editor.commit();
                        isReverse = false;
                    }
                    else {
                        item.setChecked(true);
                        editor.putBoolean("artist_sort_isReverse", true);
                        editor.commit();
                        isReverse = true;
                    }
                    String sort = sharedPreferences.getString("artist_sort", "artist a to z");
                    if (sort != null) {
                        Song.sortArtist(songsList, sort, isReverse);
                    }
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.settings:
                    startActivity(new Intent(requireActivity(), SettingsActivity.class));
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        void loadAudioData() {
            ContentResolver contentResolver = requireActivity().getContentResolver();
            final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Audio.Artists._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS};
            cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst())
                while (cursor.moveToNext())
                    songsList.add(new Song(cursor.getString(2), cursor.getString(0), cursor.getString(3)));
            Objects.requireNonNull(cursor).close();
            String sortType = sharedPreferences.getString("artist_sort", "artist a to z");
            boolean isReverse = sharedPreferences.getBoolean("artist_sort_isReverse", false);
            if (sortType != null)
                Song.sortArtist(songsList, sortType, isReverse);
        }

        static class ListViewAdapter extends ArrayAdapter<Song>{
            Context context;
            MyViewHolder myViewHolder;
            List<Song> songsList;
            HashMap<String, String> hashMap = new HashMap<>();

            public ListViewAdapter(@NonNull Context context, List<Song> songsList) {
                super(context, 0, songsList);
                this.context = context;
                this.songsList = songsList;
                Song.setAudioArtistArt(AllSongsFragment.songsList, hashMap);
            }

            @Override
            public int getCount() {
                return songsList.size();
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView == null){
                    myViewHolder = new MyViewHolder();
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.songs_item, parent, false);
                    myViewHolder.artistName = convertView.findViewById(R.id.song_name);
                    myViewHolder.tracksCount = convertView.findViewById(R.id.artist_name);
                    myViewHolder.imageView = convertView.findViewById(R.id.song_image);
                    convertView.setTag(myViewHolder);
                }
                else
                    myViewHolder = (MyViewHolder)convertView.getTag();

                myViewHolder.artistName.setText(songsList.get(position).artistName);
                myViewHolder.tracksCount.setText(songsList.get(position).tracksCount);

                try {
                    byte[] bytes = Song.getAudioAlbumArt(hashMap.get(songsList.get(position).artistId));
                    if(bytes != null)
                        Glide.with(this.context).asBitmap().load(bytes).into(myViewHolder.imageView);
                    else
                        Glide.with(this.context).asBitmap().load(R.drawable.album_cover).into(myViewHolder.imageView);
                }
                catch (IllegalArgumentException ignored){}

                return convertView;
            }

            @Nullable
            @Override
            public Song getItem(int position) {
                return songsList.get(position);
            }

            static class MyViewHolder{
                TextView artistName, tracksCount;
                ImageView imageView;
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class AllSongsFragment extends Fragment {
        public static ArrayList<Song> songsList = new ArrayList<>();
        public ListViewAdapter listViewAdapter;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        Cursor cursor;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getContext());
            songsList.clear();
            loadAudioData();
            listViewAdapter = new ListViewAdapter(requireContext(), songsList);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return View.inflate(getContext(), R.layout.fragment_music_all_songs, container);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            final ListView listView = view.findViewById(R.id.list_view);
            listView.setAdapter(listViewAdapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                Intent intent = new Intent(getContext(), MusicPlayerActivity.class);
                intent.putExtra("serviceState", true);
                intent.putParcelableArrayListExtra("songsList", songsList);
                intent.putExtra("position", position);
                startActivity(intent);
            });
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.sort_count_small_to_large).setVisible(false);
            String sortType = sharedPreferences.getString("song_sort", "name a to z");
            boolean isReverse = sharedPreferences.getBoolean("song_sort_isReverse", false);
            if(sortType != null){
                switch (sortType){
                    case "name a to z":
                        menu.findItem(R.id.sort_song_a_to_z).setChecked(true);
                        break;
                    case "album a to z":
                        menu.findItem(R.id.sort_album_a_to_z).setChecked(true);
                    case "artist a to z":
                        menu.findItem(R.id.sort_artist_a_to_z).setChecked(true);
                    case "duration short to long":
                        menu.findItem(R.id.sort_duration_short_to_long).setChecked(true);
                        break;
                }
            }
            menu.findItem(R.id.reverse_sort).setChecked(isReverse);
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.music_menu, menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            boolean isReverse = sharedPreferences.getBoolean("song_sort_isReverse", false);
            switch (item.getItemId()){
                case R.id.sort_song_a_to_z:
                    Song.sortSong(songsList, "name a to z", isReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("song_sort", "name a to z");
                    editor.commit();
                    item.setChecked(true);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.sort_album_a_to_z:
                    editor = sharedPreferences.edit();
                    editor.putString("song_sort", "album a to z");
                    editor.commit();
                    Song.sortSong(songsList, "album a to z", isReverse);
                    item.setChecked(true);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.sort_artist_a_to_z:
                    editor = sharedPreferences.edit();
                    editor.putString("song_sort", "artist a to z");
                    editor.commit();
                    Song.sortSong(songsList, "artist a to z", isReverse);
                    item.setChecked(true);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.sort_duration_short_to_long:
                    editor = sharedPreferences.edit();
                    editor.putString("song_sort", "duration short to long");
                    editor.commit();
                    Song.sortSong(songsList, "duration short to long", isReverse);
                    item.setChecked(true);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.reverse_sort:
                    editor = sharedPreferences.edit();
                    if(isReverse) {
                        item.setChecked(false);
                        editor.putBoolean("song_sort_isReverse", false);
                        editor.commit();
                        isReverse = false;
                    }
                    else {
                        item.setChecked(true);
                        editor.putBoolean("song_sort_isReverse", true);
                        editor.commit();
                        isReverse = true;
                    }
                    String sort = sharedPreferences.getString("song_sort", "name a to z");
                    if (sort != null) {
                        Song.sortSong(songsList, sort, isReverse);
                    }
                    listViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.settings:
                    startActivity(new Intent(requireActivity(), SettingsActivity.class));
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        void loadAudioData() {
            ContentResolver contentResolver = requireActivity().getContentResolver();
            final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST_ID};
            cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst())
                while (cursor.moveToNext()) {
                    songsList.add(new Song(cursor.getString(3), cursor.getString(1), cursor.getString(4),
                            cursor.getInt(2), cursor.getString(5), cursor.getString(projection.length-1)));
                }
            Objects.requireNonNull(cursor).close();
            String sortType = sharedPreferences.getString("song_sort", "name a to z");
            boolean isReverse = sharedPreferences.getBoolean("song_sort_isReverse", false);
            if (sortType != null)
                Song.sortSong(songsList, sortType, isReverse);
        }

        static class ListViewAdapter extends ArrayAdapter<Song>{
            Context context;
            MyViewHolder myViewHolder;
            List<Song> songsList;
            public ListViewAdapter(@NonNull Context context, ArrayList<Song> songsList) {
                super(context, 0, songsList);
                this.context = context;
                this.songsList = songsList;
            }

            @Override
            public int getCount() {
                return songsList.size();
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView == null){
                    myViewHolder = new MyViewHolder();
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.songs_item, parent, false);
                    myViewHolder.songName = convertView.findViewById(R.id.song_name);
                    myViewHolder.artistName = convertView.findViewById(R.id.artist_name);
                    myViewHolder.songDuration = convertView.findViewById(R.id.song_duration);
                    myViewHolder.imageView = convertView.findViewById(R.id.song_image);
                    convertView.setTag(myViewHolder);
                }
                else
                    myViewHolder = (MyViewHolder)convertView.getTag();

                myViewHolder.songName.setText(songsList.get(position).songName);
                myViewHolder.artistName.setText(songsList.get(position).artistName);
                myViewHolder.songDuration.setText(Song.durationConversion(songsList.get(position).songDuration));
                try {
                    byte[] bytes = Song.getAudioAlbumArt(songsList.get(position).path);
                    if(bytes != null)
                        Glide.with(getContext()).asBitmap().load(bytes).into(myViewHolder.imageView);
                    else
                        Glide.with(getContext()).asBitmap().load(R.drawable.album_cover).into(myViewHolder.imageView);
                }
                catch (IllegalArgumentException ignored){}
                return convertView;
            }

            @Nullable
            @Override
            public Song getItem(int position) {
                return songsList.get(position);
            }

            static class MyViewHolder{
                TextView songName, artistName, songDuration;
                ImageView imageView;
            }
        }
    }
}