package com.dev.xapp.activities;

import static java.lang.System.exit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.dev.everyEntertainment.R;
import com.dev.xapp.fragments.BrowserFragment;
import com.dev.xapp.fragments.GalleryFragment;
import com.dev.xapp.fragments.MusicFragment;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.StorageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static BottomNavigationView bottomNavigationView;
    private final int REQUEST_CODE_ASK_PERMISSIONS = 1;
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

    public void loadFragment(Fragment fragment) {
        fragment.onAttach(getApplicationContext());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    protected void checkPermissions() {
        String[] permissions = new String[] {android.Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission: permissions) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED)
                missingPermissions.add(permission);
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissionsMissing = missingPermissions.toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissionsMissing, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[permissions.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, permissions, grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (int index = permissions.length - 1; index >= 0; --index) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    // exit the app if one permission is not granted
                    Toast.makeText(this, "All permissions should be granted", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            // all permissions were granted
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
                this.finishAffinity(); //use this to finish all activities that has the same affinity in a single task list
//                this.finishAndRemoveTask();   //to remove task of root activity (MainActivity)
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