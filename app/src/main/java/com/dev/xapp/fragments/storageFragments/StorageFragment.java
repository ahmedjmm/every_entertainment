package com.dev.xapp.fragments.storageFragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.dev.xapp.R;
import com.google.android.material.tabs.TabLayout;

public class StorageFragment extends Fragment {
    ViewPager viewPager;
    public static TabLayout tabLayout;
    public static Toolbar toolbar;
    StoragePagerAdapter storagePagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storagePagerAdapter = new StoragePagerAdapter(this.getChildFragmentManager(), getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(storagePagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(MemoryFragment.actionMode != null) {
                    MemoryFragment.actionMode.finish();
                    MemoryFragment.actionMode = null;
                }
                if(SDCardFragment.actionMode != null) {
                    SDCardFragment.actionMode.finish();
                    SDCardFragment.actionMode = null;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        toolbar = view.findViewById(R.id.tool_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class StoragePagerAdapter extends FragmentPagerAdapter {
        Context context;

        public StoragePagerAdapter(@NonNull FragmentManager fm, Context context) {
            super(fm);
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
                    return context.getResources().getString(R.string.internal_storage);
                case 1:
                    return context.getResources().getString(R.string.sd_card);
            }
            return null;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MemoryFragment();
                case 1:
                    return new SDCardFragment();
            }
            return null;
        }
    }
}
