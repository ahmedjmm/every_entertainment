package com.dev.xapp.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dev.everyEntertainment.R;
import com.dev.xapp.models.ImageVideo;

import java.io.File;
import java.util.List;

import static com.dev.xapp.views.fragments.GalleryFragment.GalleryPagerAdapter.VideosFragment.videoList;

public class VideosAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_album);
        int albumPosition = getIntent().getIntExtra("albumPosition", 0);
        String albumName = getIntent().getStringExtra("albumName");
        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle(albumName);
        GridView gridView = findViewById(R.id.grid_view);
        GridViewAdapter gridViewAdapter = new GridViewAdapter(videoList.get(albumPosition).imagesVideosPath,
                getApplicationContext(), albumPosition);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(VideosAlbumActivity.this, VideoPlayerActivity.class);
            intent.putExtra("videoPath", videoList.get(albumPosition).imagesVideosPath.get(position));
            startActivity(intent);
        });

    }

    static class GridViewAdapter extends ArrayAdapter<ImageVideo> {
        Context context;
        List<String> videoPaths;
        int albumPosition;

        public GridViewAdapter(List<String> videoPaths, Context context, int albumPosition){
            super(context, R.layout.images_item);
            this.videoPaths = videoPaths;
            this.context = context;
            this.albumPosition = albumPosition;
        }

        @Override
        public int getCount() {
            return videoPaths.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.images_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = convertView.findViewById(R.id.image_view);
                viewHolder.textViewTitle = convertView.findViewById(R.id.text_view_title);
                viewHolder.textViewItemsCount = convertView.findViewById(R.id.text_view_items_count);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder) convertView.getTag();
            File file = new File(videoPaths.get(position));
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(file));
            int timeInMillisecond = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            retriever.release();
            viewHolder.textViewTitle.setText(file.getName());
            viewHolder.textViewItemsCount.setText(ImageVideo.durationConversion(timeInMillisecond));
            Glide.with(context).load(videoPaths.get(position)).placeholder(new ColorDrawable(Color.BLACK)).
                    into(viewHolder.imageView);
            return convertView;
        }

        static class ViewHolder{
            ImageView imageView;
            TextView textViewTitle, textViewItemsCount;
        }
    }
}