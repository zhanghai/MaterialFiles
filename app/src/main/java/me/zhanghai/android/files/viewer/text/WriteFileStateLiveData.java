/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.content.Context;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.util.StateData;
import me.zhanghai.android.files.util.StateLiveData;

public class WriteFileStateLiveData extends StateLiveData {

    public void write(@NonNull Path path, @NonNull byte[] content, @NonNull Context context) {
        checkReady();
        setValue(StateData.ofLoading());
        FileJobService.write(path, content, success -> setValue(success ? StateData.ofSuccess()
                : StateData.ofError(null)), context);
    }
}
