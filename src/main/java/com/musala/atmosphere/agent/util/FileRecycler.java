package com.musala.atmosphere.agent.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class responsible for recycling files and directories.
 * 
 * @author yavor.stankov
 *
 */
public class FileRecycler implements Runnable {
    private List<File> filesToRemove;

    private List<File> removedFiles;

    public FileRecycler() {
        filesToRemove = Collections.synchronizedList(new ArrayList<File>());
        removedFiles = new ArrayList<File>();
    }

    @Override
    public void run() {
        if (!filesToRemove.isEmpty()) {
            System.gc();
            removeFiles();
        }
    }

    /**
     * Adds a file to the list with files to be removed.
     * 
     * @param filePath
     *        - the full path to the file
     */
    public void addFile(String filePath) {
        File file = new File(filePath);
        filesToRemove.add(file);
    }

    /**
     * Adds a {@link List list} with files to the list with files to be removed.
     * 
     * @param filePaths
     *        - {@link List list} with the full file paths
     */
    public void addFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            addFile(filePath);
        }
    }

    private void removeFiles() {
        for (File currentFile : filesToRemove) {
            deleteFile(currentFile);
        }

        clearList();
    }

    private boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();

            for (File currentFile : files) {
                deleteFile(currentFile);
            }
        }

        return file.delete();
    }

    private void clearList() {
        filesToRemove.removeAll(removedFiles);

        removedFiles.clear();
    }
}
