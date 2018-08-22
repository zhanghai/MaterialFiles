/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

public interface LocalFileOperationStrategy {

    void createDirectory(LocalFile file) throws FileSystemException;

    void createFile(LocalFile file) throws FileSystemException;

    void delete(LocalFile file) throws FileSystemException;

    void rename(LocalFile file, String newName) throws FileSystemException;
}
