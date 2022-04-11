package com.dev.xapp.views.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.everyEntertainment.R;
import com.dev.xapp.models.Folders;
import com.dev.xapp.views.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.views.fragments.storageFragments.SDCardFragment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PathRecyclerViewAdapter extends RecyclerView.Adapter<PathRecyclerViewAdapter.ViewHolder> {
    private final List<String> folderNames;
    Context context;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public PathRecyclerViewAdapter(List<String> folderNames, Context context){
        this.folderNames = folderNames;
        this.context = context;
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
            executorService.execute(()->{
                boolean showHidden = sharedPreferences.getBoolean("hidden", false);
                if (folderNames.get(0).equals(SDCardFragment.path)) {
                    String sort = sharedPreferences.getString("card_sort", "a to z");
                    boolean isReverse = sharedPreferences.getBoolean("card_sort_isReverse", false);
                    Folders.SDCardBackToFolder(position, folderNames,
                            SDCardFragment.foldersList, sort, isReverse, showHidden);
                    handler.post(()-> {
                        SDCardFragment.filesListViewAdapter.notifyDataSetChanged();
                        this.notifyDataSetChanged();
                    });
                }
                else {
                    String sort = sharedPreferences.getString("memory_sort", "a to z");
                    boolean isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
                    Folders.memoryBackToFolder(position, folderNames,
                            MemoryFragment.foldersList, sort, isReverse, showHidden);
                    handler.post(()-> {
                        MemoryFragment.filesListViewAdapter.notifyDataSetChanged();
                        this.notifyDataSetChanged();
                    });
                }
            });
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
