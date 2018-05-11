// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
            if (deleteFile(currentFile)) {
                removedFiles.add(currentFile);
            }
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
