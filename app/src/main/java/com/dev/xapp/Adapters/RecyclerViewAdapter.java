package com.dev.xapp.Adapters;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.everyEntertainment.R;
import com.dev.xapp.Folders;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.SDCardFragment;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<String> folderNames;

    public RecyclerViewAdapter(List<String> folderNames){
        this.folderNames = folderNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.imageView.setImageResource(R.drawable.ic_arrow);

        holder.textView.setEnabled(!MemoryFragment.isActionMode && !SDCardFragment.isActionMode);

        holder.textView.setText(folderNames.get(position));
        holder.textView.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            boolean showHidden = sharedPreferences.getBoolean("hidden", false);
            if (folderNames.get(0).equals(SDCardFragment.path)) {
                String sort = sharedPreferences.getString("card_sort", "a to z");
                boolean isReverse = sharedPreferences.getBoolean("card_sort_isReverse", false);
                Folders.backToFolder(position, SDCardFragment.recyclerList,
                        SDCardFragment.sdCardListViewAdapter, RecyclerViewAdapter.this,
                        sort, isReverse, showHidden);
            }
            else {
                String sort = sharedPreferences.getString("memory_sort", "a to z");
                boolean isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
                Folders.backToFolder(position, MemoryFragment.recyclerList,
                        MemoryFragment.memoryListViewAdapter, RecyclerViewAdapter.this,
                        sort, isReverse, showHidden);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderNames.size();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
