/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

// @see https://developer.android.com/training/animation/screen-slide
public class ViewPagerTransformers {

    private ViewPagerTransformers() {}

    public static ViewPager.PageTransformer DEPTH = new Depth();
    public static ViewPager.PageTransformer ZOOM_OUT = new ZoomOut();

    private static class Depth implements ViewPager.PageTransformer {

        private static final float MIN_SCALE = 0.75f;

        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    private static class ZoomOut implements ViewPager.PageTransformer {

        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
                float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horizontalMargin - verticalMargin / 2);
                } else {
                    view.setTranslationX(-horizontalMargin + verticalMargin / 2);
                }
                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)
                        * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
