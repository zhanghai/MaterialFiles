package me.zhanghai.android.files.util;

import android.os.Bundle;

interface IRemoteCallback {
    void sendResult(in Bundle result);
}
