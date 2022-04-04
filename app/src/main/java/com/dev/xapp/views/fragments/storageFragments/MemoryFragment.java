package com.dev.xapp.views.fragments.storageFragments;

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
import android.os.Handler;
import android.os.Looper;
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

import com.dev.everyEntertainment.R;
import com.dev.xapp.views.Adapters.MemoryListViewAdapter;
import com.dev.xapp.views.Adapters.RecyclerViewAdapter;
import com.dev.xapp.models.Folders;
import com.dev.xapp.views.activities.SettingsActivity;
import com.dev.xapp.models.Song;
import com.dev.xapp.views.activities.ImageViewerActivity;
import com.dev.xapp.views.activities.MoveOrCopyActivity;
import com.dev.xapp.views.activities.MusicPlayerActivity;
import com.dev.xapp.views.activities.VideoPlayerActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dev.xapp.models.Folders.sort;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MemoryFragment extends Fragment {
    public ListView listView;
    public static List<Folders> foldersList = new ArrayList<>();
    public static MemoryListViewAdapter memoryListViewAdapter;
    public static File rootDirectory;
    public static List<String> recyclerList = new ArrayList<>();
    public static RecyclerView recyclerView;
    public static RecyclerViewAdapter recyclerViewAdapter;
    public static boolean isActionMode = false, isReverse, showHidden;
    public static ActionMode actionMode = null;
    public static File currentFolder;
    SearchView searchView;

    SwipeRefreshLayout swipeRefreshLayout;
    public static AbsListView.MultiChoiceModeListener multiChoiceModeListener;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onResume() {
        super.onResume();
        memoryListViewAdapter.notifyDataSetChanged();
    }

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
        } catch (NullPointerException ignored) { }

        currentFolder = rootDirectory;
        try {
            File[] files = rootDirectory.listFiles();
            if(files != null)
                if(showHidden) {
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
                    for (File file: files) {
                        if(!file.getName().startsWith(".")){
                            long size = Folders.getFolderSize(file);
                            if (file.isDirectory()) {
                                foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                            }
                            else {
                                foldersList.add(new Folders(R.drawable.ic_file, file, size));
                            }
                        }
                    }
                }
        }
        catch (NullPointerException nullPointerException){
            File[] files = rootDirectory.listFiles();
            for (File file: Objects.requireNonNull(files)) {
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
        sort = sharedPreferences.getString("memory_sort", "a to z");
        isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
        if(sort != null)
            Folders.memorySort(sort, isReverse, foldersList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory, container, false);

        requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(rootDirectory)));
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
        recyclerViewAdapter = new RecyclerViewAdapter(recyclerList, getContext());
        recyclerView.setAdapter(recyclerViewAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            try {
                memoryListViewAdapter.clear();
                executorService.execute(()->{
                    File[] files = currentFolder.listFiles();
                    if(files != null) {
                        if(showHidden) {
                            for (File file: files) {
                                requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                if (file.isDirectory()) {
                                    long size = Folders.getFolderSize(file);
                                    foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                                }
                                else {
                                    long size = Folders.getFolderSize(file);
                                    foldersList.add(new Folders(R.drawable.ic_file, file, size));
                                }
                            }
                        }
                        else
                            for (File file: files) {
                                if (!file.getName().startsWith(".")){
                                    requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                    if (file.isDirectory()) {
                                        long size = Folders.getFolderSize(file);
                                        foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                                    }
                                    else {
                                        long size = Folders.getFolderSize(file);
                                        foldersList.add(new Folders(R.drawable.ic_file, file, size));
                                    }
                                }
                            }
                        sort = sharedPreferences.getString("memory_sort", "a to z");
                        isReverse = sharedPreferences.getBoolean("memory_sort_isReverse", false);
                        Folders.memorySort(sort, isReverse, foldersList);
                    }
                    handler.post(()-> memoryListViewAdapter.notifyDataSetChanged());
                });
            }
            catch (NullPointerException ignored){ }
            swipeRefreshLayout.setRefreshing(false);
        });

        listView = view.findViewById(R.id.list_view);
        listView.setAdapter(memoryListViewAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            searchView.clearFocus();
            final Folders folder = foldersList.get(position);
            if(folder.getFile().isDirectory()){
                executorService.execute(()->{
                    requireContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(folder.getFile())));
                    String fileName = folder.getFile().getName();
                    recyclerList.add(fileName);
                    Folders.memoryNewFolders(position, foldersList, sort,
                            isReverse, showHidden);
                    handler.post(()->{
                        memoryListViewAdapter.notifyDataSetChanged();
                        recyclerViewAdapter.notifyDataSetChanged();
                    });
                });
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
                            String[] audioProjection = {MediaStore.Audio.Albums.ALBUM,
                                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
                                    MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                                    MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST_ID};
                            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    audioProjection, MediaStore.Audio.Media.DATA + " = ?",
                                    new String[] {path}, null, null);
                            if (cursor != null && cursor.moveToFirst())
                                do {
                                    songArrayList.add(new Song(cursor.getString(3),
                                            cursor.getString(1), cursor.getString(4),
                                            cursor.getInt(2), cursor.getString(5),
                                            cursor.getString(audioProjection.length-1)
                                    , cursor.getString(0)));
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
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.list_item_menu, menu);
                menu.removeItem(R.id.rename);
                menu.removeItem(R.id.create_folder);
                menu.removeItem(R.id.share);
                menu.removeItem(R.id.info);
                menu.removeItem(R.id.hide);
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
                actionMode = null;
            }
        };
        listView.setMultiChoiceModeListener(multiChoiceModeListener);
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
        final boolean[] isReverse = {sharedPreferences.getBoolean("memory_sort_isReverse", false)};
        final boolean finalIsReverse = isReverse[0];
        switch (item.getItemId()) {
            case R.id.create_folder:
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
                View view = View.inflate(getContext(), R.layout.alert_dialog_rename, null);
                final EditText editText = view.findViewById(R.id.edit_text);
                editText.setHint(R.string.folder_name);
                alertDialog.setView(view);
                alertDialog.setTitle(R.string.create_folder);
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
                        foldersList.add(new Folders(R.drawable.ic_folders, file, size));
                        Toast.makeText(getContext(), R.string.folder_created, Toast.LENGTH_LONG).show();
                        executorService.execute(()->{
                            sort = sharedPreferences.getString("memory_sort", "a to z");
                            Folders.memorySort(sort, finalIsReverse, foldersList);
                            handler.post(()-> memoryListViewAdapter.notifyDataSetChanged());
                        });
                    }
                }).setNegativeButton(R.string.cancel_alert_dialog, (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
                break;
            case R.id.sort_a_to_z:
                executorService.execute(()->{
                    Folders.memorySortFromAToZ(foldersList, finalIsReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("memory_sort", "a to z");
                    editor.commit();
                });
                item.setChecked(true);
                memoryListViewAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_small_to_large:
                executorService.execute(()->{
                    Folders.memorySortFromSmallToLarge(foldersList, finalIsReverse);
                    editor = sharedPreferences.edit();
                    editor.putString("memory_sort", "small to large");
                    editor.commit();
                });
                item.setChecked(true);
                memoryListViewAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_old_to_new:
                executorService.execute(()->{
                    Folders.memorySortFromOldToNew(foldersList, isReverse[0]);
                    editor = sharedPreferences.edit();
                    editor.putString("memory_sort", "old to new");
                    editor.commit();
                });
                item.setChecked(true);
                memoryListViewAdapter.notifyDataSetChanged();
                break;
            case R.id.reverse_sort:
                editor = sharedPreferences.edit();
                executorService.execute(()->{
                    if(isReverse[0]) {
                        editor.putBoolean("memory_sort_isReverse", false);
                        isReverse[0] = false;
                        handler.post(()-> item.setChecked(false));
                    }
                    else {
                        editor.putBoolean("memory_sort_isReverse", true);
                        isReverse[0] = true;
                        handler.post(()-> item.setChecked(true));
                    }
                    editor.commit();
                    String sort = sharedPreferences.getString("memory_sort", "a to z");
                    if (sort != null) {
                        Folders.memorySort(sort, isReverse[0], foldersList);
                    }
                });
                memoryListViewAdapter.notifyDataSetChanged();
                break;
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.storage_menu, menu);
    }
}