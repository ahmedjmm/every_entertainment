package com.dev.xapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dev.xapp.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static com.dev.xapp.fragments.GalleryFragment.GalleryPagerAdapter.ImagesFragment.imagesList;

public class ImageViewerActivity extends AppCompatActivity {
    List<String> pathsList = new ArrayList<>();
    int imagePosition;
    int albumPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        TextView textView = findViewById(R.id.toolbar_title);
        ViewPager viewPager = findViewById(R.id.view_pager);
        View decorView = getWindow().getDecorView();
        decorView.setClickable(true);
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onContextClick(MotionEvent e) {
                boolean visible = (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                if (visible)
                    hideSystemUI();
                else
                    showSystemUI();
                return true;
            }
        });
        decorView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        String imagePath = getIntent().getStringExtra("imagePath");
        if(imagePath != null){
            pathsList.add(imagePath);
            File file = new File(imagePath);
            textView.setText(file.getName());
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this, pathsList);
            viewPager.setAdapter(imagePagerAdapter);
            viewPager.setCurrentItem(0);
        }
        else {
            imagePosition = getIntent().getIntExtra("imagePosition", 0);
            albumPosition = getIntent().getIntExtra("albumPosition", 0);
            pathsList = imagesList.get(albumPosition).imagesVideosPath;
            File file = new File(pathsList.get(imagePosition));
            textView.setText(file.getName());
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this, pathsList);
            viewPager.setAdapter(imagePagerAdapter);
            viewPager.setCurrentItem(imagePosition);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                File file = new File(pathsList.get(position));
                textView.setText(file.getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            hideSystemUI();
        else
            showSystemUI();
    }

    static class ImagePagerAdapter extends PagerAdapter{
        private final List<String> pathsList;
        private final LayoutInflater inflater;
        private final Context context;

        public ImagePagerAdapter(Context context, List<String> pathsList) {
            this.context = context;
            this.pathsList = pathsList;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.fragment_image_viewer, container, false);
            final PhotoView photoView = view.findViewById(R.id.photo_view);
            Glide.with(context).load(pathsList.get(position)).into(photoView);
            container.addView(view, 0);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public int getCount() {
            return pathsList.size();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}