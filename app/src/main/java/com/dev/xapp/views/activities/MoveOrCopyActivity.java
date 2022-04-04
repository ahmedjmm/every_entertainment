package com.dev.xapp.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.dev.everyEntertainment.R;
import com.dev.xapp.views.Adapters.MoveOrCopyListViewAdapter;
import com.dev.xapp.views.Adapters.MoveOrCopyRecyclerViewAdapter;
import com.dev.xapp.models.Folders;
import com.dev.xapp.views.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.views.fragments.storageFragments.SDCardFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoveOrCopyActivity extends AppCompatActivity {
    ListView listView;
    Toolbar toolbar;
    public static List<Folders> foldersList = new ArrayList<>();
    public static MoveOrCopyListViewAdapter moveOrCopyListViewAdapter;
    public static List<String> recyclerList = new ArrayList<>();
    private MoveOrCopyRecyclerViewAdapter moveOrCopyRecyclerViewAdapter;
    Button button;
    ProgressDialog progressDialog;
    File sourceFile, destinationFile, rootDirectory;
    public static File currentFolder;
    File[] rootFolders;
    public static String destinationDir = "";
    public static int currentListItemPosition;
    public static String rootPath;
    ArrayList<String> sourceDirs, names;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AsyncTask<ArrayList<String>, Integer, Void> syncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_or_copy);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.move_or_copy_toolbar_title);

        recyclerList.clear();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        moveOrCopyRecyclerViewAdapter = new MoveOrCopyRecyclerViewAdapter(recyclerList, this);
        recyclerView.setAdapter(moveOrCopyRecyclerViewAdapter);

        moveOrCopyListViewAdapter = setupAdapter();
        String sort = sharedPreferences.getString("move_or_copy_sort", "a to z");
        boolean isReverse = sharedPreferences.getBoolean("move_or_copy_sort_isReverse", false);
        new Thread(() ->{
            Folders.moveOrCopySort(sort, isReverse, foldersList);
            runOnUiThread(() -> moveOrCopyListViewAdapter.notifyDataSetChanged());
        }).start();

        listView = findViewById(R.id.list_view);
        listView.setAdapter(moveOrCopyListViewAdapter);
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            currentListItemPosition = position;
            Folders folder = foldersList.get(position);
            String fileName = folder.getFile().getName();
            recyclerList.add(fileName);
            new Thread(() ->{
                Folders.moveOrCopyNewFolders(position, foldersList, sort, isReverse);
                runOnUiThread(() -> {
                    moveOrCopyListViewAdapter.notifyDataSetChanged();
                    moveOrCopyRecyclerViewAdapter.notifyDataSetChanged();
                });
            }).start();
        });

        final String operation = getIntent().getStringExtra("operation");
        button = findViewById(R.id.button);
        button.setText(operation);
        button.setOnClickListener(view -> {
            sourceDirs = getIntent().getStringArrayListExtra("paths");
            names = getIntent().getStringArrayListExtra("names");
            syncTask = new SyncTask().execute(sourceDirs);
        });
    }

    public void progressDialog() {
        progressDialog = new ProgressDialog(MoveOrCopyActivity.this);
        progressDialog.setMax(sourceDirs.size());
        if (Objects.equals(getIntent().getStringExtra("operation"), "copy here"))
            progressDialog.setTitle(R.string.progress_dialog_title_copy);
        else
            progressDialog.setTitle(R.string.progress_dialog_title_move);
        final ProgressDialog.OnCancelListener onCancelListener = dialogInterface -> {
            syncTask.cancel(true);
            if(MemoryFragment.actionMode != null) {
                MemoryFragment.actionMode.finish();
                MemoryFragment.actionMode = null;
            }
            if(SDCardFragment.actionMode != null) {
                SDCardFragment.actionMode.finish();
                SDCardFragment.actionMode = null;
            }
            finish();
        };
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(R.string.cancel_alert_dialog),
                (dialogInterface, i) -> progressDialog.cancel());
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    public MoveOrCopyListViewAdapter setupAdapter() {
        if (Objects.equals(getIntent().getStringExtra("destination"), "internal")) {
            rootDirectory = Environment.getExternalStorageDirectory();
            rootFolders = rootDirectory.listFiles();
            rootPath = rootDirectory.getPath();
            recyclerList.add(rootPath);
            for(File file: rootFolders) {
                if (file.isDirectory())
                    foldersList.add(new Folders(R.drawable.ic_folders, file, Folders.getFolderSize(file)));
            }
        }
        else {
            rootDirectory = new File("/storage");
            rootFolders = rootDirectory.listFiles();
            if(rootFolders != null)
                for (int x = 0; x < rootFolders.length; x++)
                    if (!rootFolders[x].getName().equals("emulated") && !rootFolders[x].getName().equals("self")) {
                        rootPath = rootFolders[x].getPath();
                        recyclerList.add(rootPath);
                        if (rootFolders[x].isDirectory()) {
                            rootDirectory = rootFolders[x];
                            rootFolders = rootDirectory.listFiles();
                            if (rootFolders != null) {
                                for (File file: rootFolders)
                                    if(file.isDirectory())
                                        foldersList.add(new Folders(R.drawable.ic_folders, file, Folders.getFolderSize(file)));
                            }
                        }
                        break;
                    }
        }
        currentFolder = rootDirectory;
        return  new MoveOrCopyListViewAdapter(getApplicationContext(), foldersList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.storage_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.settings);
        boolean isReverse = sharedPreferences.getBoolean("move_or_copy_sort_isReverse", false);
        menu.findItem(R.id.reverse_sort).setChecked(isReverse);
        String sort = sharedPreferences.getString("move_or_copy_sort", "a to z");
        if(sort != null){
            switch (sort){
                case "a to z":
                    menu.findItem(R.id.sort_a_to_z).setChecked(true);
                    break;
                case "small to large":
                    menu.findItem(R.id.sort_small_to_large).setChecked(true);
                    break;
                case "old to new":
                    menu.findItem(R.id.sort_old_to_new).setChecked(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final boolean[] isReverse = {sharedPreferences.getBoolean("move_or_copy_sort_isReverse", false)};
        switch (item.getItemId()) {
            case R.id.create_folder:
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
                View view = View.inflate(this, R.layout.alert_dialog_rename, null);
                final EditText editText = view.findViewById(R.id.edit_text);
                editText.setHint(R.string.folder_name);
                alertDialog.setView(view);
                alertDialog.setTitle(R.string.create_folder);
                alertDialog.setPositiveButton(R.string.ok_alert_dialog, (dialogInterface, i) -> {
                    String folderName = editText.getText().toString();
                    File file = new File(currentFolder + File.separator + folderName);
                    if (file.mkdir()){
                        long size = Folders.getFolderSize(file);
                        moveOrCopyListViewAdapter.add(new Folders(R.drawable.ic_folders, file, size));
                    }
                }).setNegativeButton(R.string.cancel_alert_dialog, (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
                break;
            case R.id.sort_a_to_z:
                boolean finalIsReverse = isReverse[0];
                new Thread(() -> {
                    Folders.moveOrCopySortFromAToZ(foldersList, finalIsReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("move_or_copy_sort", "a to z");
                    editor.commit();
                    runOnUiThread(() -> {
                        moveOrCopyListViewAdapter.notifyDataSetChanged();
                        item.setChecked(true);
                    });
                }).start();
                break;
            case R.id.sort_small_to_large:
                finalIsReverse = isReverse[0];
                new Thread(()->{
                    Folders.moveOrCopySortFromSmallToLarge(foldersList, finalIsReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("move_or_copy_sort", "small to large");
                    editor.commit();
                    runOnUiThread(() ->{
                        moveOrCopyListViewAdapter.notifyDataSetChanged();
                        item.setChecked(true);
                    });
                }).start();
                break;
            case R.id.sort_old_to_new:
                finalIsReverse = isReverse[0];
                new Thread(()->{
                    Folders.moveOrCopySortFromOldToNew(foldersList, finalIsReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("move_or_copy_sort", "old to new");
                    editor.commit();
                    runOnUiThread(()->{
                        moveOrCopyListViewAdapter.notifyDataSetChanged();
                        item.setChecked(true);
                    });
                }).start();
                break;
            case R.id.reverse_sort:
                new Thread(()->{
                    editor = sharedPreferences.edit();
                    if(isReverse[0]) {
                        editor.putBoolean("move_or_copy_sort_isReverse", false);
                        editor.commit();
                        isReverse[0] = false;
                        runOnUiThread(()-> item.setChecked(false));
                    }
                    else {
                        editor.putBoolean("move_or_copy_sort_isReverse", true);
                        editor.commit();
                        isReverse[0] = true;
                        runOnUiThread(()-> item.setChecked(true));
                    }
                    String sort = sharedPreferences.getString("move_or_copy_sort", "a to z");
                    if (sort != null)
                        Folders.moveOrCopySort(sort, isReverse[0], foldersList);
                    runOnUiThread(()-> item.setChecked(true));
                }).start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(syncTask != null)
            syncTask.cancel(true);
        if(progressDialog != null) {
            progressDialog.cancel();
            progressDialog.dismiss();
        }
    }

    class SyncTask extends AsyncTask<ArrayList<String>, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog();
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(ArrayList<String>... arrayLists) {
            if(Objects.requireNonNull(getIntent().getStringExtra("operation")).equals("copy here"))
                for (int x = 1; x <= arrayLists[0].size(); x++) {
                    if (isCancelled())
                        break;
                    else {
                        sourceFile = new File(arrayLists[0].get(x - 1));
                        destinationFile = new File(currentFolder + File.separator + names.get(x - 1));
                        try {
                            Folders.copyFileOrDirectory(sourceFile, destinationFile);
                            progressDialog.setProgress(x);
                        } catch (IOException ignored) {

                        }
                    }
                }
            else
                for (int x = 1; x <= arrayLists[0].size(); x++) {
                    if (isCancelled())
                        break;
                    else {
                        sourceFile = new File(arrayLists[0].get(x - 1));
                        destinationFile = new File(currentFolder + File.separator + names.get(x - 1));
                        try {
                            Folders.moveFileOrDirectory(sourceFile, destinationFile);
                            progressDialog.setProgress(x);
                        } catch (IOException ignored) {

                        }
                    }
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Folders.foldersPath.clear();
            Folders.foldersName.clear();
            if(MemoryFragment.actionMode != null) {
                MemoryFragment.actionMode.finish();
                MemoryFragment.actionMode = null;
            }
            if(SDCardFragment.actionMode != null) {
                SDCardFragment.actionMode.finish();
                SDCardFragment.actionMode = null;
            }
//            MemoryFragment.memoryListViewAdapter.notifyDataSetChanged();
//            SDCardFragment.sdCardListViewAdapter.notifyDataSetChanged();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destinationFile.getParentFile())));
            Intent intent = new Intent(MoveOrCopyActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}