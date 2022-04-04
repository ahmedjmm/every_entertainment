package com.dev.xapp.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dev.everyEntertainment.R;
import com.dev.xapp.views.activities.ImagesAlbumActivity;
import com.dev.xapp.models.ImageVideo;
import com.dev.xapp.views.activities.VideosAlbumActivity;
import com.dev.xapp.views.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.views.fragments.storageFragments.SDCardFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GalleryFragment extends Fragment {
    ViewPager viewPager;
    TabLayout tabLayout;
    GalleryPagerAdapter galleryPagerAdapter;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite),
                getResources().getColor(R.color.colorWhite));
        viewPager = view.findViewById(R.id.viewPager);
        galleryPagerAdapter = new GalleryPagerAdapter(this.getChildFragmentManager(), 0, this.getContext());
        viewPager.setAdapter(galleryPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    public static class GalleryPagerAdapter extends FragmentPagerAdapter {
        Context context;

        public GalleryPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getResources().getString(R.string.Images);
                case 1:
                    return context.getResources().getString(R.string.videos);
            }
            return null;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ImagesFragment();
                case 1:
                    return new VideosFragment();
            }
            return null;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        public static class ImagesFragment extends Fragment {
            public static List<ImageVideo> imagesList;
            GridViewAdapter gridViewRecyclerAdapter;
            GridView gridView;

            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                imagesList = getAlbums();
                gridViewRecyclerAdapter = new GridViewAdapter(imagesList, getContext());
            }

            @Nullable
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                return inflater.inflate(R.layout.fragment_images_videos, container, false);
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                gridView = view.findViewById(R.id.grid_view);
                gridView.setAdapter(gridViewRecyclerAdapter);
                gridView.setOnItemClickListener((parent, view1, position, id) -> {
                    Intent intent = new Intent(getActivity(), ImagesAlbumActivity.class);
                    intent.putExtra("albumPosition", position);
                    intent.putExtra("albumName", imagesList.get(position).albumName);
                    startActivity(intent);
                });
            }

            public List<ImageVideo> getAlbums() {
                List<ImageVideo> imagesList = new ArrayList<>();
                boolean boolean_folder = false;
                int int_position = 0;
                Uri uri;
                Cursor cursor;
                int column_index_data;
                int column_index_folder_name;
                String absolutePathOfImage;
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
                final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
                cursor = requireContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
                column_index_data = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                if (cursor.moveToFirst()) {
                    while (cursor.moveToNext()) {
                        absolutePathOfImage = cursor.getString(column_index_data);
                        for (int i = 0; i < imagesList.size(); i++) {
                            if (imagesList.get(i).albumName.equals(cursor.getString(column_index_folder_name))) {
                                boolean_folder = true;
                                int_position = i;
                                break;
                            } else
                                boolean_folder = false;
                        }
                        List<String> al_path = new ArrayList<>();
                        if (boolean_folder) {
                            al_path.addAll(imagesList.get(int_position).imagesVideosPath);
                            al_path.add(absolutePathOfImage);
                            imagesList.get(int_position).imagesVideosPath = al_path;
                        } else {
                            al_path.add(absolutePathOfImage);
                            ImageVideo imageVideo = new ImageVideo(cursor.getString(column_index_folder_name), al_path);
                            imageVideo.albumName = cursor.getString(column_index_folder_name);
                            imagesList.add(imageVideo);
                        }
                    }
                    cursor.close();
                }
                return imagesList;
            }

            static class GridViewAdapter extends BaseAdapter {
                List<ImageVideo> imageList;
                Context context;

                public GridViewAdapter(List<ImageVideo> imageList, Context context) {
                    this.imageList = imageList;
                    this.context = context;
                }

                @Override
                public int getCount() {
                    return imageList.size();
                }

                @Override
                public Object getItem(int position) {
                    return imageList.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    ViewHolder viewHolder;
                    if (convertView == null) {
                        convertView = LayoutInflater.from(context).inflate(R.layout.images_item, parent, false);
                        viewHolder = new ViewHolder();
                        viewHolder.imageView = convertView.findViewById(R.id.image_view);
                        viewHolder.textViewTitle = convertView.findViewById(R.id.text_view_title);
                        viewHolder.textViewItemsCount = convertView.findViewById(R.id.text_view_items_count);
                        convertView.setTag(viewHolder);
                    } else
                        viewHolder = (ViewHolder) convertView.getTag();
                    viewHolder.textViewTitle.setText(imageList.get(position).albumName);
                    viewHolder.textViewItemsCount.setText(String.valueOf(imageList.get(position).imagesVideosPath.size()));
                    Glide.with(context).load(imageList.get(position).imagesVideosPath.get(0))
                            .placeholder(new ColorDrawable(Color.BLACK)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(false).into(viewHolder.imageView);
                    return convertView;
                }

                public static class ViewHolder {
                    ImageView imageView;
                    TextView textViewTitle, textViewItemsCount;
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        public static class VideosFragment extends Fragment {
            public static List<ImageVideo> videoList;
            GridViewAdapter gridViewAdapter;
            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                videoList = getVideosAlbums();
                gridViewAdapter = new GridViewAdapter(videoList, getContext());

            }

            @Nullable
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                return inflater.inflate(R.layout.fragment_images_videos, container, false);
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                GridView gridView = view.findViewById(R.id.grid_view);
                gridView.setAdapter(gridViewAdapter);
                gridView.setOnItemClickListener((parent, view1, position, id) -> {
                    Intent intent = new Intent(getActivity(), VideosAlbumActivity.class);
                    intent.putExtra("albumPosition", position);
                    intent.putExtra("albumName", videoList.get(position).albumName);
                    startActivity(intent);
                });
            }

            public List<ImageVideo> getVideosAlbums() {
                List<ImageVideo> videosList = new ArrayList<>();
                boolean boolean_folder = false;
                int int_position = 0;
                Uri uri;
                Cursor cursor;
                int column_index_data;
                int column_index_folder_name;
                String absolutePathOfVideo;
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME};
                final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
                cursor = requireContext().getContentResolver().query(uri, projection,
                        null, null, orderBy + " DESC");
                column_index_data = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                if (cursor.moveToFirst()) {
                    while (cursor.moveToNext()) {
                        absolutePathOfVideo = cursor.getString(column_index_data);
                        for (int i = 0; i < videosList.size(); i++) {
                            if (videosList.get(i).albumName.equals(cursor.getString(column_index_folder_name))) {
                                boolean_folder = true;
                                int_position = i;
                                break;
                            } else
                                boolean_folder = false;
                        }
                        List<String> al_path = new ArrayList<>();
                        if (boolean_folder) {
                            al_path.addAll(videosList.get(int_position).imagesVideosPath);
                            al_path.add(absolutePathOfVideo);
                            videosList.get(int_position).imagesVideosPath = al_path;
                        } else {
                            al_path.add(absolutePathOfVideo);
                            ImageVideo imageVideo = new ImageVideo(cursor.getString(column_index_folder_name), al_path);
                            imageVideo.albumName = cursor.getString(column_index_folder_name);
                            videosList.add(imageVideo);
                        }
                    }
                    cursor.close();
                }
                return videosList;
            }
        }

        static class GridViewAdapter extends BaseAdapter {
            List<ImageVideo> videoList;
            Context context;

            public GridViewAdapter(List<ImageVideo> videoList, Context context) {
                this.videoList = videoList;
                this.context = context;
            }

            @Override
            public int getCount() {
                return videoList.size();
            }

            @Override
            public Object getItem(int position) {
                return videoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.images_item, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.imageView = convertView.findViewById(R.id.image_view);
                    viewHolder.textViewTitle = convertView.findViewById(R.id.text_view_title);
                    viewHolder.textViewItemsCount = convertView.findViewById(R.id.text_view_items_count);
                    convertView.setTag(viewHolder);
                } else
                    viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.textViewTitle.setText(videoList.get(position).albumName);
                viewHolder.textViewItemsCount.setText(String.valueOf(videoList.get(position).imagesVideosPath.size()));
                Glide.with(context).load(videoList.get(position).imagesVideosPath.get(0))
                        .placeholder(new ColorDrawable(Color.BLACK)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(false).into(viewHolder.imageView);
                return convertView;
            }

            public static class ViewHolder {
                ImageView imageView;
                TextView textViewTitle, textViewItemsCount;
            }
        }
    }
}
