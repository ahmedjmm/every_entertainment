package com.dev.xapp.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.dev.xapp.Folders;
import com.dev.xapp.activities.MoveOrCopyActivity;
import com.dev.xapp.R;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.StorageFragment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dev.xapp.fragments.storageFragments.MemoryFragment.memoryListViewAdapter;

public class MemoryListViewAdapter extends ArrayAdapter<Folders> implements PopupMenu.OnMenuItemClickListener, Filterable {
    public static SparseBooleanArray checkStates;
    private final Context context;
    public static List<Folders> list;
    List<Folders> cancelSearchList;
    ViewHolder viewHolder;
    private int selectedPosition;

    public MemoryListViewAdapter(Context context, List<Folders> list) {
        super(context, 0, list);
        this.context = context;
        MemoryListViewAdapter.list = list;
        cancelSearchList = list;
        checkStates = new SparseBooleanArray(cancelSearchList.size());
    }

    public static void selectAll() {
        for (int x = 0; x < list.size(); x++) {
            checkStates.put(x, true);
            list.get(x).setSelected(true);
        }
        memoryListViewAdapter.notifyDataSetChanged();
    }

    public static void dismiss() {
        for (int x = 0; x < list.size(); x++) {
            checkStates.delete(x);
            list.get(x).setSelected(false);
        }
        memoryListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cancelSearchList.size();
    }

    @Override
    public Folders getItem(int position) {
        return cancelSearchList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.memory_list_view_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.folderIcon = convertView.findViewById(R.id.image_view);
            viewHolder.fileNameTextView = convertView.findViewById(R.id.name_text_view);
            viewHolder.subFoldersNumberTextView = convertView.findViewById(R.id.number_of_files);
            viewHolder.fileSizeTextView = convertView.findViewById(R.id.size);
            viewHolder.folderDateTextView = convertView.findViewById(R.id.date);
            viewHolder.arrowDownMenu = convertView.findViewById(R.id.arrow_down);
            viewHolder.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        final Folders folders = cancelSearchList.get(position);
        viewHolder.folderIcon.setImageResource(folders.getFolderIcon());
        viewHolder.fileNameTextView.setText(folders.getFile().getName());
        viewHolder.subFoldersNumberTextView.setText(folders.getSubFoldersQuantity(context, folders.getFile()));
        if (!folders.getFile().isDirectory())
            viewHolder.fileSizeTextView.setText(Folders.readableFileSize(Folders.getFolderSize(folders.getFile())));
        else
            viewHolder.fileSizeTextView.setText("");
        viewHolder.folderDateTextView.setText(Folders.getFolderDateModified(folders.getFile()));
        viewHolder.arrowDownMenu.setImageResource(R.drawable.ic_arrow_down);
        viewHolder.arrowDownMenu.setOnClickListener(v -> showPopUpMenu(v, position));
        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setChecked(checkStates.get(position, false));
        viewHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                checkStates.put((Integer) buttonView.getTag(), isChecked);
                folders.setSelected(isChecked);
            }
            else {
                checkStates.delete((Integer) buttonView.getTag());
                folders.setSelected(false);
            }
            if(MemoryFragment.actionMode != null)
                MemoryFragment.actionMode.setTitle(checkStates.size() + " selected");
        });
        if (MemoryFragment.isActionMode) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.arrowDownMenu.setVisibility(View.GONE);
        }
        else {
            viewHolder.checkBox.setChecked(false);
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.arrowDownMenu.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    // Popup menu for each item in list
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final AlertDialog.Builder alertDialog;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = sharedPreferences.getString("memory_sort", "a to z");
        boolean isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
        boolean showHidden = sharedPreferences.getBoolean("hidden", false);
        View view;
        switch (item.getItemId()) {
            case R.id.move:
                alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle(R.string.please_select_your_destination);
                alertDialog.setItems(R.array.destination_array, (dialog, which) -> {
                    ArrayList<String> foldersName = new ArrayList<>();
                    ArrayList<String> foldersPath = new ArrayList<>();
                    foldersName.add(list.get(selectedPosition).getFile().getName());
                    foldersPath.add(list.get(selectedPosition).getFile().getAbsolutePath());
                    Intent moveOrCopyIntent = new Intent(context, MoveOrCopyActivity.class);
                    switch (which) {
                        case 0:
                            moveOrCopyIntent.putExtra("sourceSize", list.get(selectedPosition).size);
                            moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
                            moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
                            moveOrCopyIntent.putExtra("destination", "internal");
                            break;
//                        case 1:
//                            moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
//                            moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
//                            moveOrCopyIntent.putExtra("destination", "external");
//                            break;
                    }
                    moveOrCopyIntent.putExtra("operation", "move here");
                    context.startActivity(moveOrCopyIntent);
                });
                alertDialog.show();
                break;
            case R.id.copy:
                alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle(R.string.please_select_your_destination);
                alertDialog.setItems(R.array.destination_array, (dialog, which) -> {
                    ArrayList<String> foldersName = new ArrayList<>();
                    ArrayList<String> foldersPath = new ArrayList<>();
                    foldersName.add(list.get(selectedPosition).getFile().getName());
                    foldersPath.add(list.get(selectedPosition).getFile().getAbsolutePath());
                    Intent moveOrCopyIntent = new Intent(context, MoveOrCopyActivity.class);
                    switch (which) {
                        case 0:
                            moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
                            moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
                            moveOrCopyIntent.putExtra("destination", "internal");
                            break;
//                            case 1:
//                                moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
//                                moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
//                                moveOrCopyIntent.putExtra("destination", "external");
//                                break;
                    }
                    moveOrCopyIntent.putExtra("operation", "copy here");
                    context.startActivity(moveOrCopyIntent);
                });
                alertDialog.show();
                break;
            case R.id.delete:
                try {
                    new AsyncTask<Void, Integer, Void>(){
                        ProgressDialog progressDialog;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            progressDialog = new ProgressDialog(getContext());
                            progressDialog.setTitle(R.string.progress_dialog_title_delete);
                            progressDialog.setMax(1);
                            progressDialog.setIndeterminate(true);
                            final ProgressDialog.OnCancelListener onCancelListener = dialogInterface -> {
                                this.cancel(true);
                            };
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(R.string.cancel_alert_dialog),
                                    (dialogInterface, i) -> progressDialog.cancel());
                            progressDialog.setOnCancelListener(onCancelListener);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.show();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                if(list.get(selectedPosition).getFile().isDirectory())
                                    FileUtils.forceDelete(list.get(selectedPosition).getFile());
                                else
                                    list.get(selectedPosition).getFile().delete();
                            } catch (IOException e) { }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            memoryListViewAdapter.clear();
                            File[] files = MemoryFragment.currentFolder.listFiles();
                            for(File file: files)
                                if(file.isDirectory()){
                                    long size = Folders.getFolderSize(file);
                                    memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, size));
                                }
                                else{
                                    long size = Folders.getFolderSize(file);
                                    memoryListViewAdapter.add(new Folders(R.drawable.ic_file, file, size));
                                }
                            Folders.foldersPath.clear();
                            Folders.foldersName.clear();
                            progressDialog.dismiss();
                            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().
                                    replace(R.id.frame_container, new StorageFragment()).commit();
                        }
                    }.execute();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.rename:
                alertDialog = new AlertDialog.Builder(getContext());
                view = View.inflate(getContext(), R.layout.alert_dialog_rename, null);
                View alertDialogCustomTitle = View.inflate(getContext(), R.layout.alert_dialog_custom_title, null);
                alertDialog.setView(view);
                final EditText editText = view.findViewById(R.id.edit_text);
                alertDialog.setCustomTitle(alertDialogCustomTitle);
                editText.setText(list.get(selectedPosition).getFile().getName());
                alertDialog.setPositiveButton(R.string.ok_alert_dialog, (dialogInterface, i) -> {
                    String string = editText.getText().toString();
                    if(string.startsWith(".")) {
                        Toast.makeText(getContext(), R.string.cannot_rename, Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                        return;
                    }
                    for(Folders folder: list) {
                        if (folder.getFile().getName().equals(string)) {
                            Toast.makeText(getContext(), R.string.name_alredy_exist, Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                            return;
                        }
                    }
                    Folders.renameFileOrFolder(list.get(selectedPosition).getFile(), string,
                            memoryListViewAdapter, showHidden);
                    Folders.sort(sort, isReverse, list, this);
                }).setNegativeButton(R.string.cancel_alert_dialog, (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
                break;
            case R.id.hide:
                if(list.get(selectedPosition).getFile().getName().startsWith("."))
                    Toast.makeText(getContext(), R.string.file_already_hidden, Toast.LENGTH_LONG).show();
                else {
                    Folders.renameFileOrFolder(list.get(selectedPosition).getFile(),
                            "." + list.get(selectedPosition).getFile().getName(), memoryListViewAdapter,
                            showHidden);
                    Folders.sort(sort, isReverse, list, this);
                }
                break;
            case R.id.share:
                File file1 = new File(list.get(selectedPosition).getFile().getAbsolutePath());
                Uri fileUri = FileProvider.getUriForFile(getContext(), "com.dev.xapp.provider", file1);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("*/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                context.startActivity(Intent.createChooser(sharingIntent, "Share file using:"));
                break;
            case R.id.info:
                alertDialog = new AlertDialog.Builder(getContext());
                view = View.inflate(getContext(), R.layout.alert_dialog_info, null);
                alertDialog.setView(view);
                alertDialog.setTitle(list.get(selectedPosition).getFile().getName());
                TextView typeTextView = view.findViewById(R.id.type);
                if(list.get(selectedPosition).getFile().isDirectory())
                    typeTextView.setText(R.string.folder);
                else
                    typeTextView.setText(R.string.file);
                TextView sizeTextView = view.findViewById(R.id.size);
                if(list.get(selectedPosition).getFile().isDirectory())
                    sizeTextView.setText(Folders.readableFileSize(Folders.getFolderSize(list.get(selectedPosition).getFile())));
                else
                    sizeTextView.setText(Folders.readableFileSize(list.get(selectedPosition).getFile().length()));
                TextView contentsTextView1 = view.findViewById(R.id.contents1);
                TextView contentsTextView = view.findViewById(R.id.contents);
                if(list.get(selectedPosition).getFile().isDirectory()) {
                    int contentFiles = 0;  int contentFolders = 0;
                    File[] files1 = list.get(selectedPosition).getFile().listFiles();
                    for (File file : files1) {
                        if (file.isDirectory())
                            contentFolders++;
                        else
                            contentFiles++;
                    }
                    String string = getContext().getResources().getString(R.string.folders) + " " + contentFolders
                            + ", " + getContext().getResources().getString(R.string.files) + " " + contentFiles;
                    contentsTextView.setText(string);
                }
                else{
                    contentsTextView1.setVisibility(View.GONE);
                    contentsTextView.setVisibility(View.GONE);
                }
                TextView modifiedTextView = view.findViewById(R.id.modified);
                modifiedTextView.setText(Folders.getFolderDateModified(list.get(selectedPosition).getFile()));
                alertDialog.setNeutralButton(R.string.ok, (dialog, which) -> dialog.dismiss());
                TextView pathTextView = view.findViewById(R.id.path);
                pathTextView.setText(list.get(selectedPosition).getFile().getAbsolutePath());
                TextView hiddenTextView = view.findViewById(R.id.hidden);
                if(list.get(selectedPosition).getFile().getName().startsWith(".")){
                    hiddenTextView.setText(R.string.hidden_yes);
                }
                else {
                    hiddenTextView = view.findViewById(R.id.hidden);
                    hiddenTextView.setText(R.string.hidden_no);
                }
                alertDialog.show();
                break;
        }
        return true;
    }

    void showPopUpMenu(View v, int position) {
        selectedPosition = position;
        PopupMenu popup = new PopupMenu(this.getContext(), v);
        popup.inflate(R.menu.list_item_menu);
        Menu menu = popup.getMenu();
        menu.removeItem(R.id.select_all);
        menu.removeItem(R.id.dismiss);
        menu.removeItem(R.id.create_folder);
        if (list.get(position).getFile().isDirectory())
            menu.removeItem(R.id.share);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new ListFilter();
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<Folders> filterList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getFile().getName().toUpperCase().contains(constraint.toString().toUpperCase())) {
                        filterList.add(list.get(i));
                    }
                }
                list = filterList;
                results.count = filterList.size();
                results.values = filterList;
            }
            else {
                cancelSearchList.clear();
                File[] files = MemoryFragment.currentFolder.listFiles();
                for(File file: files)
                    if(file.isDirectory()){
                        long size = Folders.getFolderSize(file);
                        cancelSearchList.add(new Folders(R.drawable.ic_folders, file, size));
                    }
                    else{
                        long size = Folders.getFolderSize(file);
                        cancelSearchList.add(new Folders(R.drawable.ic_file, file, size));
                    }
                results.count = cancelSearchList.size();
                results.values = cancelSearchList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cancelSearchList = (List<Folders>) results.values;
            MemoryFragment.foldersList = list;
            notifyDataSetChanged();
        }
    }
    static class ViewHolder {
        ImageView folderIcon;
        ImageView arrowDownMenu;
        CheckBox checkBox;
        TextView fileNameTextView;
        TextView subFoldersNumberTextView;
        TextView fileSizeTextView;
        TextView folderDateTextView;
    }
}