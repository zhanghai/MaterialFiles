/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;

@RequiresApi(Build.VERSION_CODES.N)
public class FtpServerTileService extends TileService {

    @NonNull
    private final Observer<FtpServerService.State> mObserver =
            this::onFtpServerStateChanged;

    @Override
    public void onStartListening() {
        super.onStartListening();

        FtpServerService.getStateLiveData().observeForever(mObserver);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        FtpServerService.getStateLiveData().removeObserver(mObserver);
    }

    private void onFtpServerStateChanged(@NonNull FtpServerService.State state) {
        Tile tile = getQsTile();
        switch (state) {
            case STARTING:
            case RUNNING:
                tile.setState(Tile.STATE_ACTIVE);
                break;
            case STOPPING:
                tile.setState(Tile.STATE_UNAVAILABLE);
                break;
            case STOPPED:
                tile.setState(Tile.STATE_INACTIVE);
                break;
            default:
                throw new AssertionError(state);
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isLocked()) {
            unlockAndRun(this::toggle);
        } else {
            toggle();
        }
    }

    private void toggle() {
        FtpServerService.toggle(this);
    }
}
