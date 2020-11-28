package me.zhanghai.android.files.filelist

import android.text.TextUtils.TruncateAt

enum class LongFilenameEllipsizeStyle(val value: TruncateAt) {
    MIDDLE(TruncateAt.MIDDLE),
    END(TruncateAt.END),
    MARQUEE(TruncateAt.MARQUEE),
}
