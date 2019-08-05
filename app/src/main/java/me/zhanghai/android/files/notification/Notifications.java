/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.notification;

import androidx.core.app.NotificationManagerCompat;
import me.zhanghai.android.files.R;

public interface Notifications {

    interface Channels {

        interface BACKGROUND_ACTIVITY_START {
            String ID = "background_activity_start";
            int NAME_RES = R.string.notification_channel_background_activity_start_name;
            int DESCRIPTION_RES = R.string.notification_channel_background_activity_start_description;
            int IMPORTANCE = NotificationManagerCompat.IMPORTANCE_HIGH;
        }

        interface FILE_JOB {
            String ID = "file_job";
            int NAME_RES = R.string.notification_channel_file_job_name;
            int DESCRIPTION_RES = R.string.notification_channel_file_job_description;
            int IMPORTANCE = NotificationManagerCompat.IMPORTANCE_LOW;
        }
    }

    interface Ids {}
}
