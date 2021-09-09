package com.dev.xapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.xapp.Folders;
import com.dev.xapp.activities.MoveOrCopyActivity;
import com.dev.xapp.R;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.SDCardFragment;

import java.util.List;

public class MoveOrCopyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<String> folderNames;

    public MoveOrCopyRecyclerViewAdapter(List<String> folderNames){
        this.folderNames = folderNames;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_adapter, parent, false);
        return new RecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.imageView.setImageResource(R.drawable.ic_arrow);
//        if(MemoryFragment.isActionMode || SDCardFragment.isActionMode)
//            holder.textView.setEnabled(false);
//        else
//            holder.textView.setEnabled(true);
        holder.textView.setEnabled(!MemoryFragment.isActionMode && !SDCardFragment.isActionMode);
        holder.textView.setText(folderNames.get(position));
        holder.textView.setOnClickListener(v -> Folders.backToFolder(position,
                MoveOrCopyActivity.recyclerList, MoveOrCopyActivity.moveOrCopyListViewAdapter,
                MoveOrCopyRecyclerViewAdapter.this));
    }

    @Override
    public int getItemCount() {
        return folderNames.size();
    }
}
