/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;

import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class CrossfadeSubtitleToolbar extends Toolbar {

    private ObjectAnimator mSubtitleAnimator;

    private CharSequence mNextSubtitle;

    public CrossfadeSubtitleToolbar(Context context) {
        super(context);

        init();
    }

    public CrossfadeSubtitleToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CrossfadeSubtitleToolbar(Context context, @Nullable AttributeSet attrs,
                                    int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mSubtitleAnimator = ObjectAnimator.ofFloat(null, View.ALPHA, 1, 0, 1)
                .setDuration(2 * ViewUtils.getShortAnimTime(this));
        mSubtitleAnimator.setInterpolator(new FastOutSlowInInterpolator());
        AnimatorListener listener = new AnimatorListener();
        mSubtitleAnimator.addUpdateListener(listener);
        mSubtitleAnimator.addListener(listener);
    }

    @Override
    public CharSequence getSubtitle() {
        if (mNextSubtitle != null) {
            return mNextSubtitle;
        }
        return super.getSubtitle();
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {

        if (TextUtils.equals(getSubtitle(), subtitle)) {
            return;
        }

        mNextSubtitle = subtitle;
        ensureSubtitleAnimatorTarget();
        if (mSubtitleAnimator.getTarget() == null) {
            // Subtitle text view not available (yet), just delegate to super.
            super.setSubtitle(subtitle);
            return;
        }
        if (!mSubtitleAnimator.isRunning()) {
            mSubtitleAnimator.start();
        }
    }

    private void ensureSubtitleAnimatorTarget() {
        if (mSubtitleAnimator.getTarget() != null) {
            return;
        }
        try {
            Field subtitleTextViewField = Toolbar.class.getDeclaredField("mSubtitleTextView");
            subtitleTextViewField.setAccessible(true);
            mSubtitleAnimator.setTarget(subtitleTextViewField.get(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private class AnimatorListener extends AnimatorListenerAdapter
            implements ValueAnimator.AnimatorUpdateListener {

        private boolean mTextUpdated;

        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            if (animator.getAnimatedFraction() < 0.5) {
                mTextUpdated = false;
            } else {
                ensureTextUpdated();
            }
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            ensureTextUpdated();
            if (mNextSubtitle != null) {
                mTextUpdated = false;
                animator.start();
            }
        }

        private void ensureTextUpdated() {
            if (!mTextUpdated) {
                if (mNextSubtitle != null) {
                    CrossfadeSubtitleToolbar.super.setSubtitle(mNextSubtitle);
                    mNextSubtitle = null;
                }
                mTextUpdated = true;
            }
        }
    }
}
