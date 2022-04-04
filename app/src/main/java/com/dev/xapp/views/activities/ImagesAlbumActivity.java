package com.dev.xapp.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import static com.dev.xapp.views.fragments.GalleryFragment.GalleryPagerAdapter.ImagesFragment.imagesList;

public class ImagesAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images);
        int albumPosition = getIntent().getIntExtra("albumPosition", 0);
        String albumName = getIntent().getStringExtra("albumName");
        TextView textView = findViewById(R.id.toolbar_title);
        textView.setText(albumName);
        GridView gridView = findViewById(R.id.grid_view);
        GridViewAdapter gridViewAdapter = new GridViewAdapter(imagesList.get(albumPosition).imagesVideosPath,
                getApplicationContext(), albumPosition);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
            intent.putExtra("imagePosition", position);
            intent.putExtra("albumPosition", albumPosition);
            startActivity(intent);
        });
    }

    static class GridViewAdapter extends ArrayAdapter<ImageVideo> {
        Context context;
        List<String> imagePaths;
        int albumPosition;

        public GridViewAdapter(List<String> imagePaths, Context context, int albumPosition){
            super(context, R.layout.images_item);
            this.imagePaths = imagePaths;
            this.context = context;
            this.albumPosition = albumPosition;
        }

        @Override
        public int getCount() {
            return imagePaths.size();
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
                viewHolder.textViewItemsCount.setVisibility(View.INVISIBLE);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder) convertView.getTag();
            File file = new File(imagePaths.get(position));
            viewHolder.textViewTitle.setText(file.getName());
            Glide.with(context).load(imagePaths.get(position)).placeholder(new ColorDrawable(Color.BLACK)).
                    into(viewHolder.imageView);
            return convertView;
        }

        static class ViewHolder{
            ImageView imageView;
            TextView textViewTitle, textViewItemsCount;
        }
    }
}