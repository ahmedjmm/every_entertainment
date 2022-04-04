package com.dev.xapp.views.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dev.everyEntertainment.R;
import com.dev.xapp.models.Folders;

import java.util.List;

public class MoveOrCopyListViewAdapter extends ArrayAdapter<Folders> {
    List<Folders> list;
    Context context;
    public MoveOrCopyListViewAdapter(@NonNull Context context, List<Folders> list) {
        super(context, 0, list);
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Folders folders = list.get(position);
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.copy_or_move_list_view_item, parent, false);
        }
        ImageView imageView = listItem.findViewById(R.id.image_view);

        if(folders.getFile().isDirectory())
            imageView.setImageResource(R.drawable.ic_folders);
        else
            imageView.setImageResource(R.drawable.ic_file);

        TextView nameTextView = listItem.findViewById(R.id.name_text_view);
        nameTextView.setText(folders.getFile().getName());

        TextView dateTextView = listItem.findViewById(R.id.date);
        dateTextView.setText(Folders.getFolderDateModified(list.get(position).getFile()));

        TextView fileSizeTextView = listItem.findViewById(R.id.size);
        if (!folders.getFile().isDirectory())
            fileSizeTextView.setText(Folders.readableFileSize(Folders.getFolderSize(folders.getFile())));
        else
            fileSizeTextView.setText("");

        TextView subFoldersTextView = listItem.findViewById(R.id.number_of_files);
        subFoldersTextView.setText(folders.getSubFoldersQuantity(context, folders.getFile()));

        return listItem;
    }
}
