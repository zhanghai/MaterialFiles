/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.AttributeSet;

import java.net.InetAddress;
import java.util.Objects;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.NetworkUtils;

public class FtpServerUrlPreference extends Preference {

    @NonNull
    private final Observer<Object> mObserver = _1 -> updateSummary();
    @NonNull
    private final ConnectivityReceiver mConnectivityReceiver = new ConnectivityReceiver();

    public FtpServerUrlPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public FtpServerUrlPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FtpServerUrlPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public FtpServerUrlPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setPersistent(false);
        updateSummary();
    }

    @Override
    public void onAttached() {
        super.onAttached();

        Settings.FTP_SERVER_ANONYMOUS_LOGIN.observeForever(mObserver);
        Settings.FTP_SERVER_USERNAME.observeForever(mObserver);
        Settings.FTP_SERVER_PORT.observeForever(mObserver);
        getContext().registerReceiver(mConnectivityReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onDetached() {
        super.onDetached();

        Settings.FTP_SERVER_ANONYMOUS_LOGIN.removeObserver(mObserver);
        Settings.FTP_SERVER_USERNAME.removeObserver(mObserver);
        Settings.FTP_SERVER_PORT.removeObserver(mObserver);
        getContext().unregisterReceiver(mConnectivityReceiver);
    }

    private void updateSummary() {
        String summary;
        InetAddress inetAddress = NetworkUtils.getLocalInetAddress(getContext());
        if (inetAddress != null) {
            String username = !Settings.FTP_SERVER_ANONYMOUS_LOGIN.getValue() ?
                    Settings.FTP_SERVER_USERNAME.getValue() : null;
            String host = inetAddress.getHostAddress();
            int port = Settings.FTP_SERVER_PORT.getValue();
            summary = "ftp://" + (username != null ? username + "@" : "") + host + ":" + port + "/";
        } else {
            summary = getContext().getString(R.string.ftp_server_url_summary_no_local_inet_address);
        }
        setSummary(summary);
    }

    private class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            if (!Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }
            updateSummary();
        }
    }
}
