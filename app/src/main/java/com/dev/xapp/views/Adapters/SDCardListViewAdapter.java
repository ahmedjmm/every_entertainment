package com.dev.xapp.views.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import com.dev.everyEntertainment.R;
import com.dev.xapp.models.Folders;
import com.dev.xapp.views.activities.MoveOrCopyActivity;
import com.dev.xapp.views.fragments.storageFragments.SDCardFragment;
import com.dev.xapp.views.fragments.storageFragments.StorageFragment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SDCardListViewAdapter extends ArrayAdapter<Folders> implements PopupMenu.OnMenuItemClickListener, Filterable {
    public Context context;
    public static List<Folders> list;
    List<Folders> cancelSearchList;
    public static SparseBooleanArray checkStates;
    ViewHolder viewHolder;
    private int selectedPosition;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public SDCardListViewAdapter(Context context, List<Folders> list) {
        super(context, 0, list);
        this.context = context;
        SDCardListViewAdapter.list = list;
        cancelSearchList = list;
        checkStates = new SparseBooleanArray(cancelSearchList.size());
    }

    public static void selectAll() {
        for (int x = 0; x < list.size(); x++) {
            checkStates.put(x, true);
            list.get(x).setSelected(true);
        }
        SDCardFragment.sdCardListViewAdapter.notifyDataSetChanged();
    }

    public static void dismiss() {
        for (int x = 0; x < list.size(); x++) {
            checkStates.delete(x);
            list.get(x).setSelected(false);
        }
        SDCardFragment.sdCardListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cancelSearchList.size();
    }

//    @Override
//    public Folders getItem(int position) {
//        return cancelSearchList.get(position);
//    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.sdcard_list_view_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.folderIcon = view.findViewById(R.id.image_view);
            viewHolder.fileNameTextView = view.findViewById(R.id.name_text_view);
            viewHolder.subFoldersNumberTextView = view.findViewById(R.id.number_of_files);
            viewHolder.fileSizeTextView = view.findViewById(R.id.size);
            viewHolder.folderDateTextView = view.findViewById(R.id.date);
            viewHolder.arrowDownMenu = view.findViewById(R.id.arrow_down);
            viewHolder.checkBox = view.findViewById(R.id.checkBox);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }

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
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            final ActionMode actionMode = SDCardFragment.actionMode;
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    checkStates.put((Integer) buttonView.getTag(), isChecked);
                    SDCardFragment.foldersList.get(position).setSelected(true);
                }
                else {
                    checkStates.delete((Integer) buttonView.getTag());
                    SDCardFragment.foldersList.get(position).setSelected(false);
                }
                SDCardFragment.sdCardListViewAdapter.notifyDataSetChanged();
                actionMode.setTitle(checkStates.size());
            }
        });
        if (SDCardFragment.isActionMode) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.arrowDownMenu.setVisibility(View.GONE);
        } else {
            viewHolder.checkBox.setChecked(false);
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.arrowDownMenu.setVisibility(View.VISIBLE);
        }
        return view;
    }

    // Popup menu for each item in list
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = sharedPreferences.getString("card_sort", "a to z");
        boolean isReverse = sharedPreferences.getBoolean("card_sort_isReverse", false);
        boolean showHidden = sharedPreferences.getBoolean("hidden", false);
        int id = item.getItemId();
        final AlertDialog.Builder alertDialog;
        View view;
        switch (id) {
            case R.id.move:
                alertDialog = new AlertDialog.Builder(getContext());
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
//                        case 1:
//                            moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
//                            moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
//                            moveOrCopyIntent.putExtra("destination", "external");
//                            break;
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
                            } catch (IOException ignored) { }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            SDCardFragment.sdCardListViewAdapter.clear();
                            File[] files = SDCardFragment.currentFolder.listFiles();
                            for(File file: files)
                                if(file.isDirectory()){
                                    long size = Folders.getFolderSize(file);
                                    SDCardFragment.sdCardListViewAdapter.add(new Folders(R.drawable.ic_folders, file, size));
                                }
                                else{
                                    long size = Folders.getFolderSize(file);
                                    SDCardFragment.sdCardListViewAdapter.add(new Folders(R.drawable.ic_file, file, size));
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
                    executorService.execute(()->{
                        Folders.SDCardRenameFileOrFolder(list.get(selectedPosition).getFile(), string,
                                list, showHidden);
                        Folders.SDCardSort(sort, isReverse, list);
                        handler.post(this::notifyDataSetChanged);
                    });
                }).setNegativeButton(R.string.cancel_alert_dialog, (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
                break;
            case R.id.hide:
                if(list.get(selectedPosition).getFile().getName().startsWith("."))
                    Toast.makeText(getContext(), R.string.file_already_hidden, Toast.LENGTH_LONG).show();
                else {
                    executorService.execute(()->{
                        Folders.SDCardRenameFileOrFolder(list.get(selectedPosition).getFile(),
                                "." + list.get(selectedPosition).getFile().getName(),
                                list, showHidden);
                        Folders.SDCardSort(sort, isReverse, list);
                        handler.post(this::notifyDataSetChanged);
                    });
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
                    if(files1 != null)
                        for (File file : files1) {
                            if (file.isDirectory())
                                contentFolders++;
                            else
                                contentFiles++;
                        }
                    String string = getContext().getResources().getString(R.string.folders) + " " + contentFolders + ", " + getContext().getResources().getString(R.string.files) + " " + contentFiles;
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

    public void showPopUpMenu(View v, int position) {
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
                File[] files = SDCardFragment.currentFolder.listFiles();
                for (File file : files)
                    if (file.isDirectory()){
                        long size = Folders.getFolderSize(file);
                        cancelSearchList.add(new Folders(R.drawable.ic_folders, file, size));
                    }
                    else{
                        long size = Folders.getFolderSize(file);
                        cancelSearchList.add(new Folders(R.drawable.ic_file, file, size));
                    }
                list = cancelSearchList;
                results.count = cancelSearchList.size();
                results.values = cancelSearchList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cancelSearchList = (List<Folders>) results.values;
            SDCardFragment.foldersList = list;
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
