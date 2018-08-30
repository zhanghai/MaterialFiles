/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package android.support.v7.widget;

import android.content.Context;
import android.util.AttributeSet;

public class IndeterminateSwitch extends SwitchCompat {

    private boolean mIndeterminate;

    public IndeterminateSwitch(Context context) {
        super(context);
    }

    public IndeterminateSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IndeterminateSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isIndeterminate() {
        return mIndeterminate;
    }

    public void setIndeterminate(boolean indeterminate) {
        if (mIndeterminate == indeterminate) {
            return;
        }
        mIndeterminate = indeterminate;
        if (mIndeterminate) {
            super.setChecked(false);
            setThumbPosition(0.5f);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        mIndeterminate = false;
        super.setChecked(checked);
    }
}
