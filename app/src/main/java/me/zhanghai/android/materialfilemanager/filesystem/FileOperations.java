/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileOperations {

    public static void delete(File file) throws FileSystemException {
        Queue<File> queue = new LinkedList<>();
        buildDeleteQueue(file, queue);
        while (!queue.isEmpty()) {
            deleteSingle(queue.poll());
        }
    }

    private static void deleteSingle(File file) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            JavaFile.delete(file.makeJavaFile());
        } else {
            // TODO
            throw new UnsupportedOperationException();
        }
    }

    private static void buildDeleteQueue(File file, Queue<File> fileList)
            throws FileSystemException {
        file.loadInformation();
        if (file.isDirectory()) {
            List<File> children = file.getFileList();
            for (File child : children) {
                buildDeleteQueue(child, fileList);
            }
        }
        fileList.offer(file);
    }

    public static void rename(File file, String name) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            JavaFile.rename(file.makeJavaFile(), name);
        } else {
            // TODO
            throw new UnsupportedOperationException();
        }
    }

    public static void createFile(File file) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            JavaFile.createFile(file.makeJavaFile());
        }
    }

    public static void createDirectory(File file) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            JavaFile.createDirectory(file.makeJavaFile());
        }
    }
}
