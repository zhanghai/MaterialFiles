/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.Observer;
import androidx.preference.SwitchPreferenceCompat;
import me.zhanghai.android.files.R;

public class FtpServerStatePreference extends SwitchPreferenceCompat {

    @NonNull
    private final Observer<FtpServerService.State> mObserver = this::onStateChanged;

    public FtpServerStatePreference(@NonNull Context context) {
        super(context);

        init();
    }

    public FtpServerStatePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FtpServerStatePreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                    @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public FtpServerStatePreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                    @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setPersistent(false);
    }

    @Override
    public void onAttached() {
        super.onAttached();

        FtpServerService.getStateLiveData().observeForever(mObserver);
    }

    @Override
    public void onDetached() {
        super.onDetached();

        FtpServerService.getStateLiveData().removeObserver(mObserver);
    }

    private void onStateChanged(@NonNull FtpServerService.State state) {
        int summaryRes;
        switch (state) {
            case STARTING:
                summaryRes = R.string.ftp_server_state_summary_starting;
                break;
            case RUNNING:
                summaryRes = R.string.ftp_server_state_summary_running;
                break;
            case STOPPING:
                summaryRes = R.string.ftp_server_state_summary_stopping;
                break;
            case STOPPED:
                summaryRes = R.string.ftp_server_state_summary_stopped;
                break;
            default:
                throw new AssertionError(state);
        }
        setSummary(getContext().getString(summaryRes));
        setChecked(state == FtpServerService.State.STARTING
                || state == FtpServerService.State.RUNNING);
        setEnabled(!(state == FtpServerService.State.STARTING
                || state == FtpServerService.State.STOPPING));
    }

    @Override
    protected void onClick() {
        switch (FtpServerService.getStateLiveData().getValue()) {
            case STARTING:
            case STOPPING:
                break;
            case RUNNING:
                FtpServerService.stop();
                break;
            case STOPPED:
                FtpServerService.start(getContext());
                break;
            default:
                throw new AssertionError();
        }
    }
}
