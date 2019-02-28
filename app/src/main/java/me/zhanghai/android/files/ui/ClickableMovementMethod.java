/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.BaseMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A movement method that traverses links in the text buffer and fires clicks. Unlike
 * {@link LinkMovementMethod}, this will not consume touch events outside {@link ClickableSpan}s.
 */
public class ClickableMovementMethod extends BaseMovementMethod {

    @Nullable
    private static ClickableMovementMethod sInstance;

    @NonNull
    public static ClickableMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ClickableMovementMethod();
        }
        return sInstance;
    }

    @Override
    public boolean canSelectArbitrarily() {
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull TextView widget, @Nullable Spannable buffer,
                                @NonNull MotionEvent event) {

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
            if (link.length > 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return false;
    }

    @Override
    public void initialize(@NonNull TextView widget, @NonNull Spannable text) {
        Selection.removeSelection(text);
    }
}
