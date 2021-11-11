package com.dev.xapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.everyEntertainment.R;
import com.dev.xapp.BrowserTab;
import com.dev.xapp.fragments.BrowserFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dev.xapp.fragments.BrowserFragment.tabsButton;
import static com.dev.xapp.fragments.BrowserFragment.webView;

public class BrowserTabsActivity extends AppCompatActivity{
    public static int currentTabPosition = 0;
    RecyclerView recyclerView;
    public static BrowserTabAdapter browserTabAdapter;
    public static List<BrowserTab> browserTabList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Objects.equals(intent.getAction(), "finishActivity")){
                finish();
            }
        }
    };
//    public static TabDatabase tabDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_tabs);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("finishActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

//        tabDatabase = Room.databaseBuilder(getApplicationContext(), TabDatabase.class, "Tabs").allowMainThreadQueries().build();

        recyclerView = findViewById(R.id.browseTabsRecyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        browserTabAdapter = new BrowserTabAdapter(this,browserTabList);
        recyclerView.setAdapter(browserTabAdapter);
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            webView.buildDrawingCache();
            Bitmap bitmap = webView.getDrawingCache(true);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            canvas.drawBitmap(bitmap, 0, 0, paint);
            webView.draw(canvas);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,0, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            browserTabList.add(new BrowserTab(currentTabPosition, webView.getTitle(), webView.getUrl(), bytes));
            browserTabAdapter.notifyDataSetChanged();
            BrowserFragment.tabsButton.setText(String.valueOf(browserTabList.size()));
        });
    }

    public static class BrowserTabAdapter extends RecyclerView.Adapter<BrowserTabAdapter.MyViewHolder>{
        Context context;
        static List<BrowserTab> browserTabList;

        public BrowserTabAdapter(Context context, List<BrowserTab> browserTabList){
            this.context = context;
            BrowserTabAdapter.browserTabList = browserTabList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_tab_card_view_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BrowserTabAdapter.MyViewHolder holder, int position) {
            BrowserTab browserTab = browserTabList.get(position);
            holder.textView.setText(browserTab.title);
            byte[] bytes = browserTab.bytes;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.imageView.setImageBitmap(bitmap);
        }

        @Override
        public int getItemCount() {
            return browserTabList.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView imageView;
            TextView textView;
            FloatingActionButton floatingActionButton;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                floatingActionButton = itemView.findViewById(R.id.close_tab);
                imageView = itemView.findViewById(R.id.cardImage);
                textView = itemView.findViewById(R.id.cardTitle);
                imageView.setOnClickListener(this);
                floatingActionButton.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.close_tab:
                        if (browserTabList.size() > 1) {
                            browserTabList.remove(getAdapterPosition());
                            browserTabAdapter.notifyDataSetChanged();
                            tabsButton.setText(String.valueOf(browserTabList.size()));
                            currentTabPosition = 0;
                        }
                        break;

                    case R.id.cardImage:
                        currentTabPosition = getAdapterPosition();
                        webView.loadUrl(browserTabList.get(getAdapterPosition()).url);
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(v.getContext());
                        localBroadcastManager.sendBroadcast(new Intent("finishActivity"));
                        break;
                }
            }
        }
    }
}