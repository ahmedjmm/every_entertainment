package com.dev.xapp.views.Adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.everyEntertainment.R;
import com.dev.xapp.models.Folders;
import com.dev.xapp.views.activities.MoveOrCopyActivity;
import com.dev.xapp.views.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.views.fragments.storageFragments.SDCardFragment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoveOrCopyRecyclerViewAdapter extends RecyclerView.Adapter<PathRecyclerViewAdapter.ViewHolder> {
    private final List<String> folderNames;
    Context context;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public MoveOrCopyRecyclerViewAdapter(List<String> folderNames, Context context){
        this.folderNames = folderNames;
        this.context = context;
    }

    @NonNull
    @Override
    public PathRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_adapter, parent, false);
        return new PathRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PathRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.imageView.setImageResource(R.drawable.ic_arrow);
//        if(MemoryFragment.isActionMode || SDCardFragment.isActionMode)
//            holder.textView.setEnabled(false);
//        else
//            holder.textView.setEnabled(true);
        holder.textView.setEnabled(!MemoryFragment.isActionMode && !SDCardFragment.isActionMode);
        holder.textView.setText(folderNames.get(position));
        holder.textView.setOnClickListener(v -> {
            executorService.execute(()-> Folders.moveOrCopyBackToFolder(position, folderNames,
                    MoveOrCopyActivity.foldersList));
            handler.post(this::notifyDataSetChanged);
        });
    }

    @Override
    public int getItemCount() {
        return folderNames.size();
    }
}
