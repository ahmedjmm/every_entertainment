package com.dev.xapp.fragments.storageFragments;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dev.xapp.Adapters.MemoryListViewAdapter;
import com.dev.xapp.Adapters.RecyclerViewAdapter;
import com.dev.xapp.Folders;
import com.dev.xapp.activities.SettingsActivity;
import com.dev.xapp.Song;
import com.dev.xapp.activities.ImageViewerActivity;
import com.dev.xapp.activities.MainActivity;
import com.dev.xapp.activities.MoveOrCopyActivity;
import com.dev.xapp.R;
import com.dev.xapp.activities.MusicPlayerActivity;
import com.dev.xapp.activities.VideoPlayerActivity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dev.xapp.fragments.storageFragments.StorageFragment.toolbar;

public class MemoryFragment extends Fragment {
    public ListView listView;
    public static List<Folders> foldersList = new ArrayList<>();
    public static MemoryListViewAdapter memoryListViewAdapter;
    public static File rootDirectory;
    public static List<String> recyclerList = new ArrayList<>();
    public static RecyclerView recyclerView;
    public static RecyclerViewAdapter recyclerViewAdapter;
    public static boolean isActionMode = false;
    boolean showHidden;
    public static ActionMode actionMode = null;
    public static File currentFolder;
    SearchView searchView;

    SwipeRefreshLayout swipeRefreshLayout;
    public static AbsListView.MultiChoiceModeListener multiChoiceModeListener;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        showHidden = sharedPreferences.getBoolean("hidden", false);
        rootDirectory = Environment.getExternalStorageDirectory();
        try {
            if (recyclerList != null) {
                recyclerList.clear();
                recyclerViewAdapter.notifyDataSetChanged();
            }
            if (memoryListViewAdapter != null) {
                memoryListViewAdapter.clear();
                memoryListViewAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException ignored) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory, container, false);
        requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(rootDirectory)));
        String sort = sharedPreferences.getString("memory_sort", "a to z");
        boolean isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                memoryListViewAdapter.getFilter().filter(newText);
                return true;
            }
        });
        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerList.add(rootDirectory.getAbsolutePath());
        recyclerViewAdapter = new RecyclerViewAdapter(recyclerList);
        recyclerView.setAdapter(recyclerViewAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            try {
                File[] files = currentFolder.listFiles();
                if(files != null) {
                    memoryListViewAdapter.clear();
                    if(showHidden) {
                        for (File file: files) {
                            requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                            if (file.isDirectory()) {
                                long size = Folders.getFolderSize(file);
                                memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, size));
                            }
                            else {
                                long size = Folders.getFolderSize(file);
                                memoryListViewAdapter.add(new Folders(R.drawable.ic_file, file, size));
                            }
                        }
                    }
                    else
                        for (File file: files) {
                            if (file.getName().startsWith("."))
                                continue;
                            else {
                                requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                if (file.isDirectory()) {
                                    long size = Folders.getFolderSize(file);
                                    memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, size));
                                }
                                else {
                                    long size = Folders.getFolderSize(file);
                                    memoryListViewAdapter.add(new Folders(R.drawable.ic_file, file, size));
                                }
                            }
                        }
                }
            }
            catch (NullPointerException ignored){ }
            String sort1 = sharedPreferences.getString("memory_sort", "a to z");
            boolean isReverse1 = sharedPreferences.getBoolean("memory_sort_isReverse", false);
            Folders.sort(sort1, isReverse1, foldersList, memoryListViewAdapter);
            swipeRefreshLayout.setRefreshing(false);
        });
        currentFolder = rootDirectory;
        try {
            File[] files = rootDirectory.listFiles();
            if(showHidden) {
                assert files != null;
                for (File file: files) {
                    long size = Folders.getFolderSize(file);
                    if (file.isDirectory()) {
                        foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                    } else {
                        foldersList.add(new Folders(R.drawable.ic_file, file, size));
                    }
                }
            }
            else {
//                assert files != null;
                for (File file: files) {
                    if(file.getName().startsWith("."))
                        continue;
                    else {
                        long size = Folders.getFolderSize(file);
                        if (file.isDirectory()) {
                            foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                        } else {
                            foldersList.add(new Folders(R.drawable.ic_file, file, size));
                        }
                    }
                }
            }
        }
        catch (NullPointerException nullPointerException){
            File[] files = rootDirectory.listFiles();
//            assert files != null;
            for (File file: files) {
                long size = Folders.getFolderSize(file);
                if (file.isDirectory()){
                    foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                }
                else{
                    foldersList.add(new Folders(R.drawable.ic_file, file, size));
                }
            }
        }
        memoryListViewAdapter = new MemoryListViewAdapter(getContext(), foldersList);
        if(sort != null)
            Folders.sort(sort, isReverse, foldersList, memoryListViewAdapter);
        listView = view.findViewById(R.id.list_view);
        listView.setAdapter(memoryListViewAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            searchView.clearFocus();
            final Folders folder = foldersList.get(position);
            if(folder.getFile().isDirectory()){
                requireContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(folder.getFile())));
                String fileName = folder.getFile().getName();
                recyclerList.add(fileName);
                Folders.newFolders(position, foldersList, memoryListViewAdapter,
                        recyclerViewAdapter, sort, isReverse, showHidden);
            }
            else {
                try {
                    Uri uri = FileProvider.getUriForFile(requireContext(),
                            requireContext().getPackageName() + ".provider", folder.getFile());
                    String mime = requireContext().getContentResolver().getType(uri);
                    String fileType = Objects.requireNonNull(mime).substring(0, mime.indexOf("/"));
                    Intent intent;
                    String path;
                    ContentResolver contentResolver;
                    switch (fileType) {
                        case "audio":
                            path = folder.getFile().getAbsolutePath();
                            ArrayList<Song> songArrayList = new ArrayList<>();
                            contentResolver = requireActivity().getContentResolver();
                            String[] audioProjection = {MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE,
                                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                    MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST_ID};
                            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioProjection,
                                    MediaStore.Audio.Media.DATA + " = ?", new String[] {path}, null,
                                    null);
                            if (cursor != null && cursor.moveToFirst())
                                do {
                                    songArrayList.add(new Song(cursor.getString(3), cursor.getString(1),
                                            cursor.getString(4), cursor.getInt(2),
                                            cursor.getString(5),
                                            cursor.getString(audioProjection.length-1)));
                                } while (cursor.moveToNext());
                                Objects.requireNonNull(cursor).close();
                            intent = new Intent(getContext(), MusicPlayerActivity.class);
                            intent.putExtra("serviceState", true);
                            intent.putParcelableArrayListExtra("songsList", songArrayList);
                            intent.putExtra("position", 0);
                            startActivity(intent);
                            break;
                        case "video":
                            intent = new Intent(getContext(), VideoPlayerActivity.class);
                            intent.putExtra("videoPath", folder.getFile().getAbsolutePath());
                            startActivity(intent);
                            break;
                        case "image":
                            path = folder.getFile().getAbsolutePath();
                            intent = new Intent(getContext(), ImageViewerActivity.class);
                            intent.putExtra("imagePath", path);
                            startActivity(intent);
                            break;
                        case "application":
                            intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, mime);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, folder.getFile().getName()));
                            break;
                    }
                }
                catch (ActivityNotFoundException activityNotFoundException) {
                    Toast.makeText(getContext(), R.string.open_file_error, Toast.LENGTH_LONG).show();
                }
            }
        });

        multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.list_item_menu, menu);
                menu.removeItem(R.id.rename);
                menu.removeItem(R.id.create_folder);
                menu.removeItem(R.id.share);
                menu.removeItem(R.id.info);
                menu.removeItem(R.id.hide);
                toolbar.setVisibility(View.GONE);
                isActionMode = true;
                actionMode = mode;
                recyclerViewAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                int id = item.getItemId();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
                alertDialog.setTitle(R.string.please_select_your_destination);
                switch (id) {
                    case R.id.copy:
                        alertDialog.setItems(R.array.destination_array, (dialog, which) -> {
                            ArrayList<String> foldersName = Folders.getCheckedFoldersNames(foldersList);
                            ArrayList<String> foldersPath = Folders.getCheckedFoldersPaths(foldersList);
                            Intent moveOrCopyIntent = new Intent(getActivity(), MoveOrCopyActivity.class);
                            switch (which) {
                                    case 0:
                                        moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
                                        moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
                                        moveOrCopyIntent.putExtra("destination", "internal");
                                        break;
//                                case 1:
//                                    moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
//                                    moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
//                                    moveOrCopyIntent.putExtra("destination", "external");
//                                    break;
                            }
                            moveOrCopyIntent.putExtra("operation", "copy here");
                            startActivity(moveOrCopyIntent);
                        });
                        alertDialog.show();
                        break;
                    case R.id.move:
                        alertDialog.setItems(R.array.destination_array, (dialog, which) -> {
                            ArrayList<String> foldersName = Folders.getCheckedFoldersNames(foldersList);
                            ArrayList<String> foldersPath = Folders.getCheckedFoldersPaths(foldersList);
                            Intent moveOrCopyIntent = new Intent(getActivity(), MoveOrCopyActivity.class);
                            switch (which) {
                                case 0:
                                    moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
                                    moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
                                    moveOrCopyIntent.putExtra("destination", "internal");
                                    break;
//                                case 1:
//                                    moveOrCopyIntent.putStringArrayListExtra("names", foldersName);
//                                    moveOrCopyIntent.putStringArrayListExtra("paths", foldersPath);
//                                    moveOrCopyIntent.putExtra("destination", "external");
//                                    break;
                            }
                            moveOrCopyIntent.putExtra("operation", "move here");
                            startActivity(moveOrCopyIntent);
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
                                    int max = 0;
                                    for(Folders folders: foldersList) {
                                        if (folders.isSelected())
                                            max++;
                                    }
                                    progressDialog = new ProgressDialog(getContext());
                                    progressDialog.setMax(max);
                                    progressDialog.setTitle(R.string.progress_dialog_title_delete);
                                    final ProgressDialog.OnCancelListener onCancelListener = dialogInterface -> {
                                        this.cancel(true);
                                    };
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(R.string.cancel_alert_dialog), (dialogInterface, i) -> progressDialog.cancel());
                                    progressDialog.setOnCancelListener(onCancelListener);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.show();
                                }

                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        Folders.deleteFileOrDirectory(foldersList);
                                    } catch (IOException e) { }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void unused) {
                                    super.onPostExecute(unused);
                                    memoryListViewAdapter.notifyDataSetChanged();
                                    Folders.foldersPath.clear();
                                    Folders.foldersName.clear();
                                    progressDialog.dismiss();
                                    requireActivity().getSupportFragmentManager().beginTransaction().
                                            replace(R.id.frame_container, new StorageFragment()).commit();
                                }
                            }.execute();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.select_all:
                        MemoryListViewAdapter.selectAll();
                        break;
                    case R.id.dismiss:
                        MemoryListViewAdapter.dismiss();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isActionMode = false;
                MemoryListViewAdapter.checkStates.clear();
                for (Folders folder: foldersList)
                    folder.setSelected(false);
                recyclerViewAdapter.notifyDataSetChanged();
                mode.finish();
                actionMode = null;
                toolbar.setVisibility(View.VISIBLE);
            }
        };
        listView.setMultiChoiceModeListener(multiChoiceModeListener);
        AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
            int mLastFirstVisibleItem = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {   }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getId() == listView.getId()) {
                    final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        MainActivity.bottomNavigationView.setVisibility(View.GONE);
                    }
                    else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        MainActivity.bottomNavigationView.setVisibility(View.VISIBLE);
                    }
                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }
        };
        listView.setOnScrollListener(scrollListener);
        swipeRefreshLayout.setRefreshing(false);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
        menu.findItem(R.id.reverse_sort).setChecked(isReverse);
        String sort = sharedPreferences.getString("memory_sort", "a to z");
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
        switch (item.getItemId()) {
            case R.id.create_folder:
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
                View view = View.inflate(getContext(), R.layout.alert_dialog_rename, null);
                final EditText editText = view.findViewById(R.id.edit_text);
                editText.setHint(R.string.folder_name);
                alertDialog.setView(view);
                alertDialog.setTitle(R.string.create_folder);
                boolean finalIsReverse = isReverse;
                alertDialog.setPositiveButton(R.string.ok_alert_dialog, (dialogInterface, i) -> {
                    String folderName = editText.getText().toString();
                    for(Folders folder: foldersList){
                        if(folder.getFile().getName().equals(folderName)) {
                            Toast.makeText(getContext(), R.string.cannot_rename, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    File file = new File(currentFolder + File.separator + folderName);
                    if (file.mkdir()){
                        long size = Folders.getFolderSize(file);
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, size));
                        Toast.makeText(getContext(), R.string.folder_created, Toast.LENGTH_LONG).show();
                        String sort1 = sharedPreferences.getString("memory_sort", "a to z");
                        Folders.sort(sort1, finalIsReverse, foldersList, memoryListViewAdapter);
                    }
                }).setNegativeButton(R.string.cancel_alert_dialog, (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
                break;
            case R.id.sort_a_to_z:
                Folders.sortFromAToZ(foldersList, memoryListViewAdapter, isReverse);
                editor = sharedPreferences.edit();
                editor.putString("memory_sort", "a to z");
                editor.commit();
                item.setChecked(true);
                break;
            case R.id.sort_small_to_large:
                Folders.sortFromSmallToLarge(foldersList, memoryListViewAdapter, isReverse);
                editor = sharedPreferences.edit();
                editor.putString("memory_sort", "small to large");
                editor.commit();
                item.setChecked(true);
                break;
            case R.id.sort_old_to_new:
                Folders.sortFromOldToNew(foldersList, memoryListViewAdapter, isReverse);
                editor = sharedPreferences.edit();
                editor.putString("memory_sort", "old to new");
                editor.commit();
                item.setChecked(true);
                break;
            case R.id.reverse_sort:
                editor = sharedPreferences.edit();
                if(isReverse) {
                    item.setChecked(false);
                    editor.putBoolean("memory_sort_isReverse", false);
                    editor.commit();
                    isReverse = false;
                }
                else {
                    item.setChecked(true);
                    editor.putBoolean("memory_sort_isReverse", true);
                    editor.commit();
                    isReverse = true;
                }
                String sort = sharedPreferences.getString("memory_sort", "a to z");
                if (sort != null) {
                    Folders.sort(sort, isReverse, foldersList, memoryListViewAdapter);
                }
                memoryListViewAdapter.notifyDataSetChanged();
                break;
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.storage_menu, menu);
    }
}