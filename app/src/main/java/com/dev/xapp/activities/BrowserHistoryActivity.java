package com.dev.xapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.dev.xapp.R;
import com.dev.xapp.database.history.HistoryContents;
import com.dev.xapp.database.history.HistoryDatabase;
import com.dev.xapp.database.history.Wrapper;
import com.dev.xapp.fragments.BrowserFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BrowserHistoryActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    ArrayList<String> headerList = new ArrayList<>();
    private final HashMap<String, List<HistoryContents>> childHashMap = new HashMap<>();
    
    @Override
    protected void onStart() {
        super.onStart();
        HistoryDatabase historyDatabase = Room.databaseBuilder(getApplicationContext(), HistoryDatabase.class, "History").allowMainThreadQueries().build();
        List<Wrapper> wrapperList = historyDatabase.getHistoryDAO().getAllHistories();
        for(Wrapper wrapper: wrapperList){
            long headerId = wrapper.historyHeader.headerId;
            List<HistoryContents> historyContentsList = historyDatabase.getHistoryDAO().getHistoryContentsForHeader(headerId);
            if(historyContentsList.isEmpty()){
                //delete empty headers which has no HistoryContents
                historyDatabase.getHistoryDAO().deleteHeader(headerId);
            }
            else{
                headerList.add(wrapper.historyHeader.date);
                childHashMap.put(wrapper.historyHeader.date, historyContentsList);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLanguage();
        setContentView(R.layout.activity_browser_history);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle(R.string.history_browser);
        expandableListView = findViewById(R.id.expandable_list_view);
        expandableListView.setAdapter(new ExpandableLstViewAdapter(getApplicationContext(), headerList, childHashMap));
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String string = Objects.requireNonNull(childHashMap.get(headerList.get(groupPosition))).get(childPosition).url;
            BrowserFragment.webView.loadUrl(string);
            finish();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLanguage();
    }

    public void language(String langCode){
        Resources res = getResources();
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        config.setLayoutDirection(locale);
        res.updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void checkLanguage(){
        String langCode = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("language","en" );
        if(langCode != null)
            if(langCode.equals("ar"))
                language(langCode);
            else
                language("en");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ExpandableLstViewAdapter extends BaseExpandableListAdapter{
        Context context;
        List<String> headersList;
        HashMap<String, List<HistoryContents>> childHashMap;

        public ExpandableLstViewAdapter(Context context, List<String> headersList, HashMap<String,List<HistoryContents>> childHashMap){
            this.context = context;
            this.headersList = headersList;
            this.childHashMap = childHashMap;
        }

        @Override
        public int getGroupCount() {
            return this.headersList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return Objects.requireNonNull(this.childHashMap.get(this.headersList.get(groupPosition))).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.headersList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return Objects.requireNonNull(childHashMap.get(headersList.get(groupPosition))).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String headers = (String) getGroup(groupPosition);
            if(convertView == null)
                convertView = LayoutInflater.from(this.context).inflate(R.layout.expandable_list_header, null);
            TextView textView = convertView.findViewById(R.id.expandable_list_header);
            textView.setText(headers);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(this.context).inflate(R.layout.expandable_list_items, null);
            TextView titleTextView = convertView.findViewById(R.id.historyTitleTextView);
            TextView urlTextView = convertView.findViewById(R.id.historyUrlTextView);
            HistoryContents history = (HistoryContents) getChild(groupPosition, childPosition);
            titleTextView.setText(history.title);
            urlTextView.setText(history.url);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}