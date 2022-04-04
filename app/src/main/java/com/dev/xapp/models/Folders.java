package com.dev.xapp.models;

import android.content.Context;

import com.dev.everyEntertainment.R;
import com.dev.xapp.views.activities.MoveOrCopyActivity;
import com.dev.xapp.views.fragments.storageFragments.MemoryFragment;
import com.dev.xapp.views.fragments.storageFragments.SDCardFragment;

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
    public static String sort = "";

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

    public static void memoryRenameFileOrFolder(File file, String newName,List<Folders> foldersList, boolean showHidden) {
        String path = file.getAbsolutePath();
        final File newFileName = new File(path.replace(file.getName(), newName));
        if (file.renameTo(newFileName)) {
            foldersList.clear();
            File[] files = MemoryFragment.currentFolder.listFiles();
            if(files != null)
                if(showHidden)
                    for (File childFile: files)
                        if (childFile.isDirectory())
                            foldersList.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                        else
                            foldersList.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
                else
                    for (File childFile: files)
                        if(!childFile.getName().startsWith(".")){
                            if (childFile.isDirectory())
                                foldersList.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                            else
                                foldersList.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
                        }
        }
    }

    public static void SDCardRenameFileOrFolder(File file, String newName,List<Folders> foldersList , boolean showHidden) {
        String path = file.getAbsolutePath();
        final File newFileName = new File(path.replace(file.getName(), newName));
        if (file.renameTo(newFileName)) {
            foldersList.clear();
            File[] files = MemoryFragment.currentFolder.listFiles();
            if (files != null)
                if (showHidden)
                    for (File childFile : files)
                        if (childFile.isDirectory())
                            foldersList.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                        else
                            foldersList.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
                else
                    for (File childFile : files)
                        if (!childFile.getName().startsWith(".")) {
                            if (childFile.isDirectory())
                                foldersList.add(new Folders(R.drawable.ic_folders, childFile, getFolderSize(childFile)));
                            else
                                foldersList.add(new Folders(R.drawable.ic_file, childFile, getFolderSize(childFile)));
                        }
        }
    }

    public static void memorySort(String sort, boolean isReverse, List<Folders> foldersList){
        if(sort != null)
            switch (sort){
                case "small to large":
                    Folders.memorySortFromSmallToLarge(foldersList, isReverse);
                    break;
                case "old to new":
                    Folders.memorySortFromOldToNew(foldersList, isReverse);
                    break;
                default:
                    Folders.memorySortFromAToZ(foldersList, isReverse);
            }
    }

    public static void SDCardSort(String sort, boolean isReverse, List<Folders> foldersList){
        if(sort != null)
            switch (sort){
                case "small to large":
                    Folders.SDCardSortFromSmallToLarge(foldersList, isReverse);
                    break;
                case "old to new":
                    Folders.SDCardSortFromOldToNew(foldersList, isReverse);
                    break;
                default:
                    Folders.SDCardSortFromAToZ(foldersList, isReverse);
            }
    }

    public static void moveOrCopySort(String sort, boolean isReverse, List<Folders> foldersList){
        if(sort != null)
            switch (sort){
                case "small to large":
                    Folders.moveOrCopySortFromSmallToLarge(foldersList, isReverse);
                    break;
                case "old to new":
                    Folders.moveOrCopySortFromOldToNew(foldersList, isReverse);
                    break;
                default:
                    Folders.moveOrCopySortFromAToZ(foldersList, isReverse);
            }
    }

    public static void memorySortFromAToZ(List<Folders> foldersList, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
        }
        else {
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
            Collections.reverse(foldersList);
        }
    }

    public static void SDCardSortFromAToZ(List<Folders> foldersList, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
        }
        else {
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
            Collections.reverse(foldersList);
        }
    }

    public static void moveOrCopySortFromAToZ(List<Folders> foldersList, boolean isReverse) {
        if (!isReverse){
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
        }
        else {
            Collections.sort(foldersList, (folders, f2) -> folders.getFile().getName().toLowerCase().compareTo(f2.getFile().getName().toLowerCase()));
            Collections.reverse(foldersList);
        }
    }


    public static void memorySortFromOldToNew(List<Folders> foldersList, boolean isReverse) {
        if(!isReverse)
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.getFile().lastModified(), f2.getFile().lastModified()));
        else{
            File[] files = new File[foldersList.size()];
            for (int x = 0; x < files.length; x++)
                files[x] = foldersList.get(x).getFile();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            foldersList.clear();
            for (File file : files)
                if (file.isDirectory())
                    foldersList.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                else
                    foldersList.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
        }
    }

    public static void moveOrCopySortFromOldToNew(List<Folders> foldersList, boolean isReverse) {
        if(!isReverse)
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.getFile().lastModified(), f2.getFile().lastModified()));
        else{
            File[] files = new File[foldersList.size()];
            for (int x = 0; x < files.length; x++)
                files[x] = foldersList.get(x).getFile();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            foldersList.clear();
            for (File file : files)
                if (file.isDirectory())
                    foldersList.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                else
                    foldersList.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
        }
    }

    public static void SDCardSortFromOldToNew(List<Folders> foldersList, boolean isReverse) {
        if(!isReverse)
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.getFile().lastModified(), f2.getFile().lastModified()));
        else{
            File[] files = new File[foldersList.size()];
            for (int x = 0; x < files.length; x++)
                files[x] = foldersList.get(x).getFile();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            foldersList.clear();
            for (File file : files)
                if (file.isDirectory())
                    foldersList.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                else
                    foldersList.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
        }
    }

    public static void memorySortFromSmallToLarge(List<Folders> foldersList, boolean isReverse) {
        if (!isReverse)
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
        else{
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
            Collections.reverse(foldersList);
        }
    }

    public static void moveOrCopySortFromSmallToLarge(List<Folders> foldersList, boolean isReverse) {
        if (!isReverse)
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
        else{
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
            Collections.reverse(foldersList);
        }
    }

    public static void SDCardSortFromSmallToLarge(List<Folders> foldersList, boolean isReverse) {
        if (!isReverse)
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
        else{
            Collections.sort(foldersList, (folders, f2) -> Long.compare(folders.size, f2.size));
            Collections.reverse(foldersList);
        }
    }

    public static void memoryNewFolders(int position, List<Folders> foldersList, String sort, boolean isReverse, boolean showHidden) {
        File newFile = foldersList.get(position).getFile();
        try {
            if (newFile.isDirectory()) {
                File[] newFolders = newFile.listFiles();
                if(newFolders != null) {
                    foldersList.clear();
                    if (showHidden)
                        for (File newFolder : newFolders) {
                            if (newFolder.isDirectory())
                                foldersList.add(new Folders(R.drawable.ic_folders, newFolder, getFolderSize(newFolder)));
                            else
                                foldersList.add(new Folders(R.drawable.ic_file, newFolder, getFolderSize(newFolder)));
                        }
                    else
                        for (File newFolder : newFolders) {
                            if (!newFolder.getName().startsWith("."))
                                if (newFolder.isDirectory())
                                    foldersList.add(new Folders(R.drawable.ic_folders, newFolder, getFolderSize(newFolder)));
                                else
                                    foldersList.add(new Folders(R.drawable.ic_file, newFolder, getFolderSize(newFolder)));
                        }
                }
            }
        }
        finally {
            memorySort(sort, isReverse, foldersList);
            MemoryFragment.currentFolder = newFile;
        }
    }

    public static void SDCardNewFolders(int position, List<Folders> foldersList, String sort, boolean isReverse, boolean showHidden) {
        Folders newFolder = foldersList.get(position);
        File newFile = newFolder.getFile();
        try {
            if (newFile.isDirectory()) {
                File[] newFolders = newFile.listFiles();
                if (newFolders != null) {
                    foldersList.clear();
                    if (showHidden)
                        for (File folder : newFolders) {
                            if (folder.isDirectory())
                                foldersList.add(new Folders(R.drawable.ic_folders, folder, getFolderSize(folder)));
                            else
                                foldersList.add(new Folders(R.drawable.ic_file, folder, getFolderSize(folder)));
                        }
                    else
                        for (File folder : newFolders)
                            if (!folder.getName().startsWith("."))
                                if (folder.isDirectory())
                                    foldersList.add(new Folders(R.drawable.ic_folders, folder, getFolderSize(folder)));
                                else
                                    foldersList.add(new Folders(R.drawable.ic_file, folder, getFolderSize(folder)));
                }
            }
        } finally {
            SDCardSort(sort, isReverse, foldersList);
            SDCardFragment.currentFolder = newFile;
        }
    }

    public static void moveOrCopyNewFolders(int position, List<Folders> foldersList, String sort, boolean isReverse) {
        Folders newFolder = foldersList.get(position);
        File newFile = newFolder.getFile();
        try {
            if (newFile.isDirectory()) {
                File[] newFolders = newFile.listFiles();
                if(newFolders != null){
                    foldersList.clear();
                    for (File folder : newFolders) {
                        if (folder.isDirectory()) {
                            foldersList.add(new Folders(R.drawable.ic_folders, folder, getFolderSize(folder)));
                        } else {
                            foldersList.add(new Folders(R.drawable.ic_file, folder, getFolderSize(folder)));
                        }
                        MoveOrCopyActivity.destinationDir = folder.getPath();
                    }
                }
                MoveOrCopyActivity.currentFolder = newFile;
            }
        }
        finally {
            moveOrCopySort(sort, isReverse, foldersList);
            SDCardFragment.currentFolder = newFile;
        }
    }

    public static void memoryBackToFolder(int position, List<String> namesList, List<Folders> foldersList,
                                    String sort, boolean isReverse, boolean showHidden) {
        if (position != namesList.size() - 1) {
            File folder = new File(getFolderPath(namesList, position));
            foldersList.clear();
            File[] files = folder.listFiles();
            if(files != null)
                if(showHidden)
                    for (File file : files)
                        if (file.isDirectory())
                            foldersList.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                        else
                            foldersList.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
                else
                    for (File file : files)
                        if (!file.getName().startsWith(".")){
                            if (file.isDirectory())
                                foldersList.add(new Folders(R.drawable.ic_folders, file, getFolderSize(file)));
                            else
                                foldersList.add(new Folders(R.drawable.ic_file, file, getFolderSize(file)));
                        }
            MemoryFragment.currentFolder = folder;
            namesList.subList(position + 1, namesList.size()).clear();
            memorySort(sort, isReverse, foldersList);
        }
    }

    public static void SDCardBackToFolder(int position, List<String> namesList, List<Folders> foldersList,
                                          String sort, boolean isReverse, boolean showHidden) {
        if (position != namesList.size() - 1) {
            File folder = new File(getFolderPath(namesList, position));
            foldersList.clear();
            File[] files = folder.listFiles();
            if(files != null)
                if(showHidden)
                    for (File value : files)
                        if (value.isDirectory())
                            foldersList.add(new Folders(R.drawable.ic_folders, value,
                                    getFolderSize(value)));
                        else
                            foldersList.add(new Folders(R.drawable.ic_file, value,
                                    getFolderSize(value)));
                else
                    for (File value : files)
                        if(!value.getName().startsWith(".")){
                            if (value.isDirectory())
                                foldersList.add(new Folders(R.drawable.ic_folders, value,
                                        getFolderSize(value)));
                            else
                                foldersList.add(new Folders(R.drawable.ic_file, value,
                                        getFolderSize(value)));
                        }
            SDCardFragment.currentFolder = folder;
            namesList.subList(position + 1, namesList.size()).clear();
            SDCardSort(sort, isReverse, foldersList);
        }
    }

    public static void moveOrCopyBackToFolder(int position, List<String> namesList, List<Folders> foldersList) {
        if (position != namesList.size() - 1) {
            File folder = new File(getFolderPath(namesList, position));
            foldersList.clear();
            File[] files = folder.listFiles();
            if(files != null)
                for (File value: files)
                    if (value.isDirectory())
                        foldersList.add(new Folders(R.drawable.ic_folders, value,
                                getFolderSize(value)));
                    else
                        foldersList.add(new Folders(R.drawable.ic_file, value,
                                getFolderSize(value)));
            MoveOrCopyActivity.currentFolder = folder;
            namesList.subList(position + 1, namesList.size()).clear();
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
            if(subFoldersFiles != null)
                return subFoldersFiles.length + " " + context.getResources().getString(R.string.folders_files_quantity);
        }
        return "";
    }
}