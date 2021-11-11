package com.dev.xapp;

import android.content.Context;

import com.dev.everyEntertainment.R;
import com.dev.xapp.Adapters.MemoryListViewAdapter;
import com.dev.xapp.Adapters.MoveOrCopyListViewAdapter;
import com.dev.xapp.Adapters.MoveOrCopyRecyclerViewAdapter;
import com.dev.xapp.Adapters.RecyclerViewAdapter;
import com.dev.xapp.Adapters.SDCardListViewAdapter;
import com.dev.xapp.activities.MoveOrCopyActivity;
import com.dev.xapp.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.fragments.storageFragments.SDCardFragment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Folders extends FileUtils {
    public static ArrayList<String> foldersPath, foldersName;
    private final File file;
    private final int folderIcon;
    public long size;
    private boolean isSelected;
    public static String sort;

    public Folders(int imageView, File file, long size) {
        this.folderIcon = imageView;
        this.file = file;
        this.isSelected = false;
        this.size = size;
        foldersPath = new ArrayList<>();
        foldersName = new ArrayList<>();
    }

    public static long getFolderSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getFolderPath(List<String> pathList, int position) {
        StringBuilder path = new StringBuilder();
        for (int x = 0; x <= position; x++) {
            path.append("/");
            path.append(pathList.get(x));
        }
        return path.toString();
    }

    public static void deleteFileOrDirectory(List<Folders> foldersList) throws IOException {
        for (int x = foldersList.size() - 1; x >= 0; x--)
            if (foldersList.get(x).isSelected())
                if (foldersList.get(x).getFile().isDirectory())
                    forceDelete(foldersList.get(x).getFile());
                else
                    foldersList.get(x).getFile().delete();
    }

    public static void moveFileOrDirectory(File sourceFile, File destinationFile) throws IOException {
        if (sourceFile.isDirectory())
            moveDirectory(sourceFile, destinationFile);
        else
            moveFile(sourceFile, destinationFile);
    }

    public static void copyFileOrDirectory(File sourceFile, File destinationFile) throws IOException {
        if (sourceFile.isDirectory())
            copyDirectory(sourceFile, destinationFile);
        else
            copyFile(sourceFile, destinationFile);
    }

    public static void renameFileOrFolder(File file, String newName, MemoryListViewAdapter memoryListViewAdapter, boolean showHidden) {
        String path = file.getAbsolutePath();
        final File newFileName = new File(path.replace(file.getName(), newName));
        if (file.renameTo(newFileName)) {
            memoryListViewAdapter.clear();
            File[] files = MemoryFragment.currentFolder.listFiles();
            if(showHidden)
                for (File childFile: files)
                    if (childFile.isDirectory())
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                    else
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
            else
                for (File childFile: files)
                    if(childFile.getName().startsWith("."))
                        continue;
                    else
                    if (childFile.isDirectory())
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                    else
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
        }
    }

    public static void renameFileOrFolder(File file, String newName, SDCardListViewAdapter sdCardListViewAdapter, boolean showHidden) {
        String path = file.getAbsolutePath();
        final File newFileName = new File(path.replace(file.getName(), newName));
        if (file.renameTo(newFileName)) {
            sdCardListViewAdapter.clear();
            File[] files = MemoryFragment.currentFolder.listFiles();
            if(showHidden)
                for (File childFile : files)
                    if (childFile.isDirectory())
                        sdCardListViewAdapter.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                    else
                        sdCardListViewAdapter.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
            else
                for (File childFile: files)
                    if(childFile.getName().startsWith("."))
                        continue;
                    else
                        if (childFile.isDirectory())
                            sdCardListViewAdapter.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                        else
                            sdCardListViewAdapter.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
        }
    }

    public static void sort(String sort, boolean isReverse, List<Folders> foldersList, MemoryListViewAdapter memoryListViewAdapter){
        if(sort != null)
            switch (sort){
                case "a to z":
                    Folders.sortFromAToZ(foldersList, memoryListViewAdapter, isReverse);
                    break;
                case "small to large":
                    Folders.sortFromSmallToLarge(foldersList, memoryListViewAdapter, isReverse);
                    break;
                case "old to new":
                    Folders.sortFromOldToNew(foldersList, memoryListViewAdapter, isReverse);
                    break;
            }
    }

    public static void sort(String sort, boolean isReverse, List<Folders> foldersList, SDCardListViewAdapter sdCardListViewAdapter){
        if(sort != null)
            switch (sort){
                case "a to z":
                    Folders.sortFromAToZ(foldersList, sdCardListViewAdapter, isReverse);
                    break;
                case "small to large":
                    Folders.sortFromSmallToLarge(foldersList, sdCardListViewAdapter, isReverse);
                    break;
                case "old to new":
                    Folders.sortFromOldToNew(foldersList, sdCardListViewAdapter, isReverse);
                    break;
            }
    }

    public static void sort(String sort, boolean isReverse, List<Folders> foldersList, MoveOrCopyListViewAdapter moveOrCopyListViewAdapter){
        if(sort != null)
            switch (sort){
                case "a to z":
                    Folders.sortFromAToZ(foldersList, moveOrCopyListViewAdapter, isReverse);
                    break;
                case "small to large":
                    Folders.sortFromSmallToLarge(foldersList, moveOrCopyListViewAdapter, isReverse);
                    break;
                case "old to new":
                    Folders.sortFromOldToNew(foldersList, moveOrCopyListViewAdapter, isReverse);
                    break;
            }
    }

    public static void sortFromAToZ(List<Folders> foldersList, MemoryListViewAdapter memoryListViewAdapter, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
        }
        else {
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
            Collections.reverse(foldersList);
        }
        memoryListViewAdapter.notifyDataSetChanged();
    }

    public static void sortFromAToZ(List<Folders> foldersList, SDCardListViewAdapter sdCardListViewAdapter, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
        }
        else {
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
            Collections.reverse(foldersList);
        }
        sdCardListViewAdapter.notifyDataSetChanged();
    }

    public static void sortFromAToZ(List<Folders> foldersList, MoveOrCopyListViewAdapter moveOrCopyListViewAdapter, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
        }
        else {
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
            Collections.reverse(foldersList);
        }
        moveOrCopyListViewAdapter.notifyDataSetChanged();
    }


    public static void sortFromOldToNew(List<Folders> foldersList, MemoryListViewAdapter memoryListViewAdapter, boolean isReverse) {
        if(!isReverse){
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.getFile().lastModified(), f2.getFile().lastModified()));
            memoryListViewAdapter.notifyDataSetChanged();
        }
        else{
            File[] files = new File[memoryListViewAdapter.getCount()];
            for (int x = 0; x < files.length; x++)
                files[x] = foldersList.get(x).getFile();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            memoryListViewAdapter.clear();
            for (File file : files)
                if (file.isDirectory())
                    memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                else
                    memoryListViewAdapter.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
        }
    }

    public static void sortFromOldToNew(List<Folders> foldersList, MoveOrCopyListViewAdapter moveOrCopyListViewAdapter, boolean isReverse) {
        if(!isReverse){
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.getFile().lastModified(), f2.getFile().lastModified()));
            moveOrCopyListViewAdapter.notifyDataSetChanged();
        }
        else{
            File[] files = new File[moveOrCopyListViewAdapter.getCount()];
            for (int x = 0; x < files.length; x++)
                files[x] = foldersList.get(x).getFile();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            moveOrCopyListViewAdapter.clear();
            for (File file : files)
                if (file.isDirectory())
                    moveOrCopyListViewAdapter.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                else
                    moveOrCopyListViewAdapter.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
        }
    }

    public static void sortFromOldToNew(List<Folders> foldersList, SDCardListViewAdapter sdCardListViewAdapter, boolean isReverse) {
        if(!isReverse){
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.getFile().lastModified(), f2.getFile().lastModified()));
            sdCardListViewAdapter.notifyDataSetChanged();
        }
        else{
            File[] files = new File[sdCardListViewAdapter.getCount()];
            for (int x = 0; x < files.length; x++)
                files[x] = foldersList.get(x).getFile();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            sdCardListViewAdapter.clear();
            for (File file : files)
                if (file.isDirectory())
                    sdCardListViewAdapter.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                else
                    sdCardListViewAdapter.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
        }
    }

    public static void sortFromSmallToLarge(List<Folders> foldersList, MemoryListViewAdapter memoryListViewAdapter, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
        }
        else{
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
            Collections.reverse(foldersList);
        }
        memoryListViewAdapter.notifyDataSetChanged();
    }

    public static void sortFromSmallToLarge(List<Folders> foldersList, MoveOrCopyListViewAdapter moveOrCopyListViewAdapter, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
        }
        else{
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
            Collections.reverse(foldersList);
        }
        moveOrCopyListViewAdapter.notifyDataSetChanged();
    }

    public static void sortFromSmallToLarge(List<Folders> foldersList, SDCardListViewAdapter sdCardListViewAdapter, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
        }
        else{
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
            Collections.reverse(foldersList);
        }
        sdCardListViewAdapter.notifyDataSetChanged();
    }

    public static void newFolders(int position, List<Folders> list, MemoryListViewAdapter memoryListViewAdapter,
                                  RecyclerViewAdapter recyclerViewAdapter, String sort, boolean isReverse, boolean showHidden) {
        File newFile = list.get(position).getFile();
        try {
            if (newFile.isDirectory()) {
                File[] newFolders = newFile.listFiles();
                list.clear();
                if (showHidden)
                    for (File newFolder : newFolders) {
                        if (newFolder.isDirectory())
                            list.add(new Folders(R.drawable.ic_folders, newFolder, getFolderSize(newFolder)));
                        else
                            list.add(new Folders(R.drawable.ic_file, newFolder, getFolderSize(newFolder)));
                    }
                else
                    for (File newFolder : newFolders) {
                        if(newFolder.getName().startsWith("."))
                            continue;
                        else
                        if (newFolder.isDirectory())
                            list.add(new Folders(R.drawable.ic_folders, newFolder, getFolderSize(newFolder)));
                        else
                            list.add(new Folders(R.drawable.ic_file, newFolder, getFolderSize(newFolder)));
                    }
            }
        }
        finally {
            sort(sort, isReverse, list, memoryListViewAdapter);
            MemoryFragment.currentFolder = newFile;
            memoryListViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public static void newFolders(int position, List<Folders> list, SDCardListViewAdapter sdCardListViewAdapter,
                                  RecyclerViewAdapter recyclerViewAdapter, String sort, boolean isReverse, boolean showHidden) {
        Folders newFolder = list.get(position);
        File newFile = newFolder.getFile();
        try {
            if (newFile.isDirectory()) {
                File[] newFolders = newFile.listFiles();
                list.clear();
                if(showHidden)
                    for (File folder : newFolders) {
                        if (folder.isDirectory())
                            list.add(new Folders(R.drawable.ic_folders, folder, getFolderSize(folder)));
                        else
                            list.add(new Folders(R.drawable.ic_file, folder, getFolderSize(folder)));
                    }
                else
                    for (File folder : newFolders)
                        if(folder.getName().startsWith("."))
                            continue;
                        else
                        if (folder.isDirectory())
                            list.add(new Folders(R.drawable.ic_folders, folder, getFolderSize(folder)));
                        else
                            list.add(new Folders(R.drawable.ic_file, folder, getFolderSize(folder)));
            }
        } finally {
            sort(sort, isReverse, list, sdCardListViewAdapter);
            SDCardFragment.currentFolder = newFile;
            sdCardListViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public static void newFolders(int position, List<Folders> list, MoveOrCopyListViewAdapter moveOrCopyListViewAdapter,
                                  MoveOrCopyRecyclerViewAdapter moveOrCopyRecyclerViewAdapter, String sort, boolean isReverse) {
        Folders newFolder = list.get(position);
        File newFile = newFolder.getFile();
        try {
            if (newFile.isDirectory()) {
                File[] newFolders = newFile.listFiles();
                list.clear();
                for (File folder : newFolders) {
                    if (folder.isDirectory()) {
                        list.add(new Folders(R.drawable.ic_folders, folder, getFolderSize(folder)));
                    } else {
                        list.add(new Folders(R.drawable.ic_file, folder, getFolderSize(folder)));
                    }
                    MoveOrCopyActivity.destinationDir = folder.getPath();
                }
                MoveOrCopyActivity.currentFolder = newFile;
                moveOrCopyListViewAdapter.notifyDataSetChanged();
                moveOrCopyRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
        finally {
            sort(sort, isReverse, list, moveOrCopyListViewAdapter);
            SDCardFragment.currentFolder = newFile;
            moveOrCopyListViewAdapter.notifyDataSetChanged();
            moveOrCopyRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public static void backToFolder(int position, List<String> namesList, MemoryListViewAdapter memoryListViewAdapter,
                                    RecyclerViewAdapter recyclerViewAdapter, String sort, boolean isReverse, boolean showHidden) {
        if (position != namesList.size() - 1) {
            File folder = new File(getFolderPath(namesList, position));
            memoryListViewAdapter.clear();
            File[] files = folder.listFiles();
            if(showHidden)
                for (File file : files)
                    if (file.isDirectory())
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                    else
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
            else
                for (File file : files)
                    if (file.getName().startsWith("."))
                        continue;
                    else
                    if (file.isDirectory())
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                    else
                        memoryListViewAdapter.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
            memoryListViewAdapter.notifyDataSetChanged();
            MemoryFragment.currentFolder = folder;
            namesList.subList(position + 1, namesList.size()).clear();
            recyclerViewAdapter.notifyDataSetChanged();
            sort(sort, isReverse, MemoryListViewAdapter.list, memoryListViewAdapter);
        }
    }

    public static void backToFolder(int position, List<String> namesList, SDCardListViewAdapter sdCardListViewAdapter,
                                    RecyclerViewAdapter recyclerViewAdapter, String sort, boolean isReverse, boolean showHidden) {
        if (position != namesList.size() - 1) {
            File folder = new File(getFolderPath(namesList, position));
            sdCardListViewAdapter.clear();
            File[] files = folder.listFiles();
            if(showHidden)
                for (File value : files)
                    if (value.isDirectory())
                        sdCardListViewAdapter.add(new Folders(R.drawable.ic_folders, value,
                                getFolderSize(value)));
                    else
                        sdCardListViewAdapter.add(new Folders(R.drawable.ic_file, value,
                                getFolderSize(value)));
            else
                for (File value : files)
                    if(value.getName().startsWith("."))
                        continue;
                    else
                    if (value.isDirectory())
                        sdCardListViewAdapter.add(new Folders(R.drawable.ic_folders, value,
                                getFolderSize(value)));
                    else
                        sdCardListViewAdapter.add(new Folders(R.drawable.ic_file, value,
                                getFolderSize(value)));
            sdCardListViewAdapter.notifyDataSetChanged();
            SDCardFragment.currentFolder = folder;
            namesList.subList(position + 1, namesList.size()).clear();
            recyclerViewAdapter.notifyDataSetChanged();
            sort(sort, isReverse, SDCardListViewAdapter.list, sdCardListViewAdapter);
        }
    }

    public static void backToFolder(int position, List<String> namesList, MoveOrCopyListViewAdapter moveOrCopyListViewAdapter, MoveOrCopyRecyclerViewAdapter moveOrCopyRecyclerViewAdapter) {
        if (position != namesList.size() - 1) {
            File folder = new File(getFolderPath(namesList, position));
            moveOrCopyListViewAdapter.clear();
            File[] files = folder.listFiles();
            for (File value: files)
                if (value.isDirectory())
                    moveOrCopyListViewAdapter.add(new Folders(R.drawable.ic_folders, value,
                            getFolderSize(value)));
                else
                    moveOrCopyListViewAdapter.add(new Folders(R.drawable.ic_file, value,
                            getFolderSize(value)));
            MoveOrCopyActivity.currentFolder = folder;
            moveOrCopyListViewAdapter.notifyDataSetChanged();

            namesList.subList(position + 1, namesList.size()).clear();
            moveOrCopyRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public static String getFolderDateModified(File file) {
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static ArrayList<String> getCheckedFoldersNames(List<Folders> foldersList) {
        for (Folders folders : foldersList)
            if (folders.isSelected())
                foldersName.add(folders.getFile().getName());
        return foldersName;
    }

    public static ArrayList<String> getCheckedFoldersPaths(List<Folders> foldersList) {
        for (Folders folders : foldersList)
            if (folders.isSelected())
                foldersPath.add(folders.getFile().getAbsolutePath());
        return foldersPath;
    }

    public File getFile() {
        return file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getFolderIcon() {
        return folderIcon;
    }

    public String getSubFoldersQuantity(Context context, File file) {
        if (file.isDirectory()) {
            File[] subFoldersFiles = file.listFiles();
            return subFoldersFiles.length + " " + context.getResources().getString(R.string.folders_files_quantity);
        } else
            return "";
    }
}