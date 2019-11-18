/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.AppActivity;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.IntentUtils;

public class OpenFileActivity extends AppActivity {

    @NonNull
    private static final String ACTION_OPEN_FILE =
            "me.zhanghai.android.files.intent.action.OPEN_FILE";

    @NonNull
    public static Intent newIntent(@NonNull Path path, @NonNull String mimeType,
                                   @NonNull Context context) {
        Intent intent = new Intent(ACTION_OPEN_FILE)
                .setPackage(context.getPackageName())
                .setType(mimeType);
        IntentPathUtils.putExtraPath(intent, path);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Path path = IntentPathUtils.getExtraPath(intent);
        String mimeType = intent.getType();
        if (path != null && mimeType != null) {
            openFile(path, mimeType);
        }

        finish();
    }

    private void openFile(@NonNull Path path, @NonNull String mimeType) {
        if (LinuxFileSystemProvider.isLinuxPath(path)
                || DocumentFileSystemProvider.isDocumentPath(path)) {
            Uri uri = FileProvider.getUriForPath(path);
            Intent intent = IntentUtils.makeView(uri, mimeType)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            IntentPathUtils.putExtraPath(intent, path);
            AppUtils.startActivity(intent, this);
        } else {
            FileJobService.open(path, mimeType, false, this);
        }
    }
}
