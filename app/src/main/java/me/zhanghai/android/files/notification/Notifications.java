/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.notification;

import androidx.core.app.NotificationManagerCompat;
import me.zhanghai.android.files.R;

public interface Notifications {

    interface Channels {

        interface FILE_JOB {
            String ID = "file_job";
            int NAME_RES = R.string.notification_channel_file_job_name;
            int DESCRIPTION_RES = R.string.notification_channel_file_job_description;
            int IMPORTANCE = NotificationManagerCompat.IMPORTANCE_LOW;
        }

        interface FILE_JOB_DIALOG {
            String ID = "file_job_dialog";
            int NAME_RES = R.string.notification_channel_file_job_dialog_name;
            int DESCRIPTION_RES = R.string.notification_channel_file_job_dialog_description;
            int IMPORTANCE = NotificationManagerCompat.IMPORTANCE_HIGH;
        }
    }

    interface Ids {}
}
