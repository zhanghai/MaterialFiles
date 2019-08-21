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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;

import java.net.InetAddress;
import java.util.Objects;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.ClipboardUtils;
import me.zhanghai.android.files.util.NetworkUtils;

public class FtpServerUrlPreference extends Preference {

    @NonNull
    private final Observer<Object> mObserver = _1 -> updateSummary();
    @NonNull
    private final ConnectivityReceiver mConnectivityReceiver = new ConnectivityReceiver();

    @NonNull
    private final ContextMenuListener mContextMenuListener = new ContextMenuListener();

    private boolean mHasUrl;

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
            mHasUrl = true;
        } else {
            summary = getContext().getString(R.string.ftp_server_url_summary_no_local_inet_address);
            mHasUrl = false;
        }
        setSummary(summary);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setOnCreateContextMenuListener(mContextMenuListener);
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

    private class ContextMenuListener implements View.OnCreateContextMenuListener {

        @Override
        public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View view,
                                        @NonNull ContextMenu.ContextMenuInfo menuInfo) {
            if (!mHasUrl) {
                return;
            }
            CharSequence url = getSummary();
            menu.setHeaderTitle(url);
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.ftp_server_url_menu_copy_url)
                    .setOnMenuItemClickListener(item -> {
                        ClipboardUtils.copyText(url, getContext());
                        return true;
                    });
            if (!Settings.FTP_SERVER_ANONYMOUS_LOGIN.getValue()) {
                String password = Settings.FTP_SERVER_PASSWORD.getValue();
                if (!TextUtils.isEmpty(password)) {
                    menu.add(Menu.NONE, Menu.NONE, Menu.NONE,
                            R.string.ftp_server_url_menu_copy_password)
                            .setOnMenuItemClickListener(item -> {
                                ClipboardUtils.copyText(password, getContext());
                                return true;
                            });
                }
            }
        }
    }
}
