/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import me.zhanghai.android.files.util.ViewUtils;

public class CrossfadeSubtitleToolbar extends Toolbar {

    @NonNull
    private ObjectAnimator mSubtitleAnimator;

    @Nullable
    private CharSequence mNextSubtitle;

    public CrossfadeSubtitleToolbar(@NonNull Context context) {
        super(context);

        init();
    }

    public CrossfadeSubtitleToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CrossfadeSubtitleToolbar(@NonNull Context context, @Nullable AttributeSet attrs,
                                    int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mSubtitleAnimator = ObjectAnimator.ofFloat(null, ALPHA, 1, 0, 1)
                .setDuration(2 * ViewUtils.getShortAnimTime(this));
        mSubtitleAnimator.setInterpolator(new FastOutSlowInInterpolator());
        AnimatorListener listener = new AnimatorListener();
        mSubtitleAnimator.addUpdateListener(listener);
        mSubtitleAnimator.addListener(listener);
    }

    @Nullable
    @Override
    public CharSequence getSubtitle() {
        if (mNextSubtitle != null) {
            return mNextSubtitle;
        }
        return super.getSubtitle();
    }

    @Override
    public void setSubtitle(@Nullable CharSequence subtitle) {

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
        TextView subtitleTextView;
        //noinspection TryWithIdenticalCatches
        try {
            Field subtitleTextViewField = Toolbar.class.getDeclaredField("mSubtitleTextView");
            subtitleTextViewField.setAccessible(true);
            subtitleTextView = (TextView) subtitleTextViewField.get(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        if (subtitleTextView == null) {
            return;
        }
        // HACK: Prevent setText() from calling requestLayout() during animation which triggers
        // re-layout of the entire view hierarchy and breaks the ripple of BreadcrumbLayout.
        ViewUtils.setWidth(subtitleTextView, LayoutParams.MATCH_PARENT);
        mSubtitleAnimator.setTarget(subtitleTextView);
    }

    private class AnimatorListener extends AnimatorListenerAdapter
            implements ValueAnimator.AnimatorUpdateListener {

        private boolean mTextUpdated;

        @Override
        public void onAnimationUpdate(@NonNull ValueAnimator animator) {
            if (animator.getAnimatedFraction() < 0.5) {
                mTextUpdated = false;
            } else {
                ensureTextUpdated();
            }
        }

        @Override
        public void onAnimationEnd(@NonNull Animator animator) {
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
