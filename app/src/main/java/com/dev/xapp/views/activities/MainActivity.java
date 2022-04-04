package com.dev.xapp.views.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.dev.everyEntertainment.R;
import com.dev.xapp.views.fragments.BrowserFragment;
import com.dev.xapp.views.fragments.GalleryFragment;
import com.dev.xapp.views.fragments.MusicFragment;
import com.dev.xapp.views.fragments.storageFragments.StorageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static BottomNavigationView bottomNavigationView;

    CoordinatorLayout coordinatorLayout;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    ArrayList<String> deniedPermissions = new ArrayList<>();
    Fragment fragment;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLanguage();
        setContentView(R.layout.activity_main);

        //check count of storage devices to avoid split screen errors in SDCardListViewAdapter if no sd card inserted.
        if(ContextCompat.getExternalFilesDirs(this, null).length == 1)
            Toast.makeText(this,R.string.no_sd_card, Toast.LENGTH_LONG).show();

        checkPermissions();

        coordinatorLayout = findViewById(R.id.coordinator);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_storage:
                    fragment = new StorageFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_gallery:
                    fragment = new GalleryFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_music:
                    fragment = new MusicFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_browser:
                    fragment = new BrowserFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        });
    }

    public void requestPermissionsForAPILessThanR() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{permission},
                        PERMISSIONS_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void requestPermissionsForAPIRAndHigher() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
            startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
        }
    }

    public void loadFragment(Fragment fragment) {
        fragment.onAttach(getApplicationContext());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (Environment.isExternalStorageManager())
                loadFragment(new StorageFragment());
            else{
                new AlertDialog.Builder(this).setTitle(R.string.alert_permissions_title).
                        setMessage(R.string.alert_permissions_message).setCancelable(false).
                        setPositiveButton(R.string.ok_alert_dialog, (dialog, which) -> requestPermissionsForAPIRAndHigher())
                        .setNegativeButton(R.string.cancel_alert_dialog, (dialog, which) -> {
                    String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
                }).show();
            }
        }
        else{
            String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for(String permission: permissions){
                if(ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                        != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this, new String[]{permission},
                            PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if (Environment.isExternalStorageManager())
                loadFragment(new StorageFragment());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int index = 0; index < permissions.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[index]);
                }
            }
            if(!deniedPermissions.isEmpty()){
                Snackbar.make(coordinatorLayout, R.string.manage_external_storage_permission_needed,
                        Snackbar.LENGTH_LONG).setAnchorView(bottomNavigationView)
                        .setAction(R.string.action_settings, v -> {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                requestPermissionsForAPIRAndHigher();
                            else
                                requestPermissionsForAPILessThanR();
                        }).show();
            }
            loadFragment(new StorageFragment());
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                int count = getSupportFragmentManager().getBackStackEntryCount();
                for(int x = 0; x <= count; x++){
                    getSupportFragmentManager().popBackStack();
                }
                super.onBackPressed();
                finishAndRemoveTask();   // remove the app from recent apps task
            }
        }
        else {
            Toast.makeText(getBaseContext(), R.string.back_pressed, Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLanguage();
    }

    public BottomNavigationView getBottomNavigation(){
        return bottomNavigationView;
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
}