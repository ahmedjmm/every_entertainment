package com.dev.xapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dev.xapp.BrowserTab;
import com.dev.xapp.activities.BrowserTabsActivity;
import com.dev.xapp.R;

import com.dev.xapp.activities.BrowserHistoryActivity;
import com.dev.xapp.activities.SettingsActivity;
import com.dev.xapp.database.history.HistoryContents;
import com.dev.xapp.database.history.HistoryDatabase;
import com.dev.xapp.database.history.HistoryHeader;
import com.dev.xapp.database.history.Wrapper;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.SDCardFragment;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.dev.xapp.activities.BrowserTabsActivity.browserTabList;
import static com.dev.xapp.activities.BrowserTabsActivity.currentTabPosition;

public class BrowserFragment extends Fragment {
    public static WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton homeImageButton, backImageButton;
    public static Button tabsButton;
    ProgressBar progressBar;
    EditText urlEditText;
    public static String urlString, currentDate, webTitle;
    HistoryDatabase historyDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLanguage();
        if(MemoryFragment.actionMode != null) {
            MemoryFragment.actionMode.finish();
            MemoryFragment.actionMode = null;
        }
        if(SDCardFragment.actionMode != null) {
            SDCardFragment.actionMode.finish();
            SDCardFragment.actionMode = null;
        }
        setHasOptionsMenu(true);
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){
            Toast.makeText(getContext(), R.string.check_internet,
                    Toast.LENGTH_LONG).show();
        }
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentBrowserView = inflater.inflate(R.layout.fragment_browser, container, false);
        Toolbar toolbar = fragmentBrowserView.findViewById(R.id.tool_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        webView = fragmentBrowserView.findViewById(R.id.webView);
        swipeRefreshLayout = fragmentBrowserView.findViewById(R.id.swipe);
        webAction();
        progressBar = fragmentBrowserView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        urlEditText = fragmentBrowserView.findViewById(R.id.url_editText);
        urlEditText.setOnEditorActionListener((v, actionId, event) -> {
            urlString = urlEditText.getText().toString();
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.getAction()
                    == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                if (event == null || !event.isShiftPressed()) {
                    swipeRefreshLayout.setRefreshing(true);
                    InputMethodManager inputManager = (InputMethodManager) requireContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus())
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    urlEditText.clearFocus();
                    if(Patterns.WEB_URL.matcher(urlString).matches())
                        webView.loadUrl("https://" + urlString);
                    else
                        webView.loadUrl("https://www.google.com/search?q=" + urlString);
                    return true;
                }
            return false;
        });

        homeImageButton = fragmentBrowserView.findViewById(R.id.home_browser);
        homeImageButton.setOnClickListener(v -> {
            swipeRefreshLayout.setRefreshing(true);
            webView.loadUrl("https://www.google.com/");
        });

        backImageButton = fragmentBrowserView.findViewById(R.id.back_browser);
        backImageButton.setOnClickListener(v -> {
            swipeRefreshLayout.setRefreshing(true);
            webView.goBack();
            swipeRefreshLayout.setRefreshing(false);
        });

        tabsButton = fragmentBrowserView.findViewById(R.id.tabs_browser);
        tabsButton.setText(String.valueOf(browserTabList.size()));
        tabsButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), BrowserTabsActivity.class)));

        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());
        return fragmentBrowserView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.browser_options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.history_browser:
                startActivity(new Intent(getActivity(), BrowserHistoryActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
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
        res.updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void checkLanguage(){
        String langCode = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("language","en" );
        if(langCode != null)
            if(langCode.equals("ar"))
                language(langCode);
            else
                language("en");
    }

    public void webAction(){
        swipeRefreshLayout.setRefreshing(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.loadUrl("https://www.google.com/");
        webView.setDrawingCacheEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if(newProgress < 100 && progressBar.getVisibility() == ProgressBar.GONE){
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                if(newProgress == 100){
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                webTitle = title;
                urlEditText.setText(title);
            }
        });
    }

    public void addWebToHistory(String webTitle, String url, Context context){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = simpleDateFormat.format(new Date());
        historyDatabase = Room.databaseBuilder(context, HistoryDatabase.class, "History")
                .allowMainThreadQueries().build();
        Wrapper wrapper;
        try {
            String lastDateRecord = historyDatabase.getHistoryDAO().getLastHistoryRecord().date;
            long lastDateRecordId = historyDatabase.getHistoryDAO().getLastHistoryRecord().headerId;
            if(lastDateRecord.equals(currentDate)){
                HistoryContents historyContents = new HistoryContents(lastDateRecordId, webTitle, url);
                wrapper = new Wrapper(historyDatabase.getHistoryDAO().getLastHistoryRecord(), historyContents);
            }
            else {
                HistoryHeader historyHeader = new HistoryHeader(currentDate);
                long id = historyDatabase.getHistoryDAO().addHistoryHeader(historyHeader);
                HistoryContents historyContents = new HistoryContents(id, webTitle, url);
                wrapper = new Wrapper(historyHeader, historyContents);
            }
            historyDatabase.getHistoryDAO().addHistoryContents(wrapper.historyContents);
        }
        catch (Exception e) {
            HistoryHeader historyHeader = new HistoryHeader(currentDate);
            long headerId = historyDatabase.getHistoryDAO().addHistoryHeader(historyHeader);
            HistoryContents historyContents = new HistoryContents(headerId, webTitle, url);
            wrapper = new Wrapper(historyHeader, historyContents);
            historyDatabase.getHistoryDAO().addHistoryContents(wrapper.historyContents);
        }
    }

    private class WebViewClient extends android.webkit.WebViewClient {
        private String urlFinished = "";

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(getContext() != null)
                addWebToHistory(view.getTitle(), view.getUrl(), getContext());
            urlEditText.setText(webTitle);
            swipeRefreshLayout.setRefreshing(false);
            if(!urlFinished.equals(url)){
                webView.buildDrawingCache();
                Bitmap bitmap = webView.getDrawingCache(true);
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                canvas.drawBitmap(bitmap, 0, 0, paint);
                webView.draw(canvas);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,0, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                if(BrowserTabsActivity.browserTabList.size() == 0) {
                    BrowserTabsActivity.browserTabList.add(new BrowserTab(currentTabPosition,
                            view.getTitle(), view.getUrl(), bytes));
                }
                else {
                    BrowserTabsActivity.browserTabList.set(currentTabPosition, new BrowserTab(view.getTitle(),
                            view.getUrl(), bytes));
                }
            }
            urlFinished = url;
            tabsButton.setText(String.valueOf(browserTabList.size()));
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            Toast.makeText(getContext(), errorResponse.getReasonPhrase(), Toast.LENGTH_LONG).show();
        }
    }
}
