/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AnyRes;
import android.support.annotation.AttrRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StyleRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.compat.BooleanSupplier;
import me.zhanghai.android.materialfilemanager.ui.ClickableMovementMethod;
import me.zhanghai.android.materialfilemanager.ui.LinkArrowKeyMovementMethod;

public class ViewUtils {

    private ViewUtils() {}

    public static void fadeOut(@NonNull View view, int duration, boolean gone,
                               @Nullable Runnable nextRunnable) {
        if (view.getVisibility() != View.VISIBLE || view.getAlpha() == 0) {
            // Cancel any starting animation.
            view.animate()
                    .alpha(0)
                    .setDuration(0)
                    .start();
            view.setVisibility(gone ? View.GONE : View.INVISIBLE);
            if (nextRunnable != null) {
                nextRunnable.run();
            }
            return;
        }
        view.animate()
                .alpha(0)
                .setDuration(duration)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    private boolean mCanceled = false;
                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {
                        mCanceled = true;
                    }
                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        if (!mCanceled) {
                            view.setVisibility(gone ? View.GONE : View.INVISIBLE);
                            if (nextRunnable != null) {
                                nextRunnable.run();
                            }
                        }
                    }
                })
                .start();
    }

    public static void fadeOut(@NonNull View view, int duration, boolean gone) {
        fadeOut(view, duration, gone, null);
    }

    public static void fadeOut(@NonNull View view, boolean gone) {
        fadeOut(view, getShortAnimTime(view), gone);
    }

    public static void fadeOut(@NonNull View view) {
        fadeOut(view, true);
    }

    public static void fadeIn(@NonNull View view, int duration) {
        if (view.getVisibility() == View.VISIBLE && view.getAlpha() == 1) {
            // Cancel any starting animation.
            view.animate()
                    .alpha(1)
                    .setDuration(0)
                    .start();
            return;
        }
        view.setAlpha(isVisible(view) ? view.getAlpha() : 0);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1)
                .setDuration(duration)
                .setInterpolator(new FastOutSlowInInterpolator())
                // NOTE: We need to remove any previously set listener or Android will reuse it.
                .setListener(null)
                .start();
    }

    public static void fadeIn(@NonNull View view) {
        fadeIn(view, getShortAnimTime(view));
    }

    public static void fadeToVisibility(@NonNull View view, boolean visible, boolean gone) {
        if (visible) {
            fadeIn(view);
        } else {
            fadeOut(view, gone);
        }
    }

    public static void fadeToVisibility(@NonNull View view, boolean visible) {
        fadeToVisibility(view, visible, true);
    }

    public static void crossfade(@NonNull View fromView, @NonNull View toView, int duration,
                                 boolean gone) {
        fadeOut(fromView, duration, gone);
        fadeIn(toView, duration);
    }

    public static void crossfade(@NonNull View fromView, @NonNull View toView, boolean gone) {
        crossfade(fromView, toView, getShortAnimTime(fromView), gone);
    }

    public static void crossfade(@NonNull View fromView, @NonNull View toView) {
        crossfade(fromView, toView, false);
    }

    public static void fadeOutThenFadeIn(@NonNull View fromView, @NonNull View toView, int duration,
                                         boolean gone) {
        fadeOut(fromView, duration, gone, () -> fadeIn(toView, duration));
    }

    public static void fadeOutThenFadeIn(@NonNull View fromView, @NonNull View toView,
                                         boolean gone) {
        fadeOutThenFadeIn(fromView, toView, getShortAnimTime(fromView), gone);
    }

    public static void fadeOutThenFadeIn(@NonNull View fromView, @NonNull View toView) {
        fadeOutThenFadeIn(fromView, toView, false);
    }

    @Dimension
    public static float dpToPx(@Dimension(unit = Dimension.DP) float dp, @NonNull Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    @Px
    public static int dpToPxOffset(@Dimension(unit = Dimension.DP) float dp,
                                   @NonNull Context context) {
        return (int) dpToPx(dp, context);
    }

    @Px
    public static int dpToPxSize(@Dimension(unit = Dimension.DP) float dp,
                                 @NonNull Context context) {
        float value = dpToPx(dp, context);
        int size = (int) (value >= 0 ? value + 0.5f : value - 0.5f);
        if (size != 0) {
            return size;
        } else if (value == 0) {
            return 0;
        } else if (value > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    public static boolean getBooleanFromAttrRes(@AttrRes int attrRes, boolean defaultValue,
                                                @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getBoolean(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    public static int getColorFromAttrRes(@AttrRes int attrRes, int defaultValue,
                                          @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getColor(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    @Nullable
    public static ColorStateList getColorStateListFromAttrRes(@AttrRes int attrRes,
                                                              @NonNull Context context) {
        // TODO: Switch to TintTypedArray when they added this overload.
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            // 0 is an invalid identifier according to the docs of {@link Resources}.
            int resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return AppCompatResources.getColorStateList(context, resId);
            }
            return null;
        } finally {
            a.recycle();
        }
    }

    @Dimension
    public static float getDimensionFromAttrRes(@AttrRes int attrRes, float defaultValue,
                                                @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getDimension(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    @Px
    public static int getDimensionPixelOffsetFromAttrRes(@AttrRes int attrRes, int defaultValue,
                                                         @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getDimensionPixelOffset(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    @Px
    public static int getDimensionPixelSizeFromAttrRes(@AttrRes int attrRes, int defaultValue,
                                                       @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getDimensionPixelSize(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    @Nullable
    public static Drawable getDrawableFromAttrRes(@AttrRes int attrRes, @NonNull Context context) {
        // TODO: Switch to TintTypedArray when they added this overload.
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            // 0 is an invalid identifier according to the docs of {@link Resources}.
            int resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return AppCompatResources.getDrawable(context, resId);
            }
            return null;
        } finally {
            a.recycle();
        }
    }

    public static float getFloatFromAttrRes(@AttrRes int attrRes, float defaultValue,
                                            @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getFloat(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    @AnyRes
    public static int getResIdFromAttrRes(@AttrRes int attrRes, int defaultValue,
                                          @NonNull Context context) {
        // TODO: Switch to TintTypedArray when they added this overload.
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getResourceId(0, defaultValue);
        } finally {
            a.recycle();
        }
    }

    public static int getShortAnimTime(@NonNull Resources resources) {
        return resources.getInteger(android.R.integer.config_shortAnimTime);
    }

    public static int getShortAnimTime(@NonNull View view) {
        return getShortAnimTime(view.getResources());
    }

    public static int getShortAnimTime(@NonNull Context context) {
        return getShortAnimTime(context.getResources());
    }

    public static int getMediumAnimTime(@NonNull Resources resources) {
        return resources.getInteger(android.R.integer.config_mediumAnimTime);
    }

    public static int getMediumAnimTime(@NonNull View view) {
        return getMediumAnimTime(view.getResources());
    }

    public static int getMediumAnimTime(@NonNull Context context) {
        return getMediumAnimTime(context.getResources());
    }

    public static int getLongAnimTime(@NonNull Resources resources) {
        return resources.getInteger(android.R.integer.config_longAnimTime);
    }

    public static int getLongAnimTime(@NonNull View view) {
        return getLongAnimTime(view.getResources());
    }

    public static int getLongAnimTime(@NonNull Context context) {
        return getLongAnimTime(context.getResources());
    }

    public static int getDisplayWidth(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeight(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getMarginStart(@NonNull View view) {
        return MarginLayoutParamsCompat.getMarginStart(
                (ViewGroup.MarginLayoutParams) view.getLayoutParams());
    }

    public static int getMarginEnd(@NonNull View view) {
        return MarginLayoutParamsCompat.getMarginEnd(
                (ViewGroup.MarginLayoutParams) view.getLayoutParams());
    }

    public static int getMarginLeft(@NonNull View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
    }

    public static int getMarginRight(@NonNull View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
    }

    public static int getMarginTop(@NonNull View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
    }

    public static int getMarginBottom(@NonNull View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin;
    }

    public static int getWidthExcludingPadding(@NonNull View view) {
        return Math.max(0, view.getWidth() - view.getPaddingLeft() - view.getPaddingRight());
    }

    public static int getHeightExcludingPadding(@NonNull View view) {
        return Math.max(0, view.getHeight() - view.getPaddingTop() - view.getPaddingBottom());
    }

    private static boolean hasSwDp(@Dimension(unit = Dimension.DP) int dp, @NonNull Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= dp;
    }

    public static boolean hasSw600Dp(@NonNull Context context) {
        return hasSwDp(600, context);
    }

    private static boolean hasWDp(@Dimension(unit = Dimension.DP) int dp, @NonNull Context context) {
        return context.getResources().getConfiguration().screenWidthDp >= dp;
    }

    public static boolean hasW600Dp(@NonNull Context context) {
        return hasWDp(600, context);
    }

    public static boolean hasW960Dp(@NonNull Context context) {
        return hasWDp(960, context);
    }

    public static void hideTextInputLayoutErrorOnTextChange(
            @NonNull EditText editText, @NonNull TextInputLayout textInputLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(@NonNull CharSequence s, int start, int count,
                                          int after) {}
            @Override
            public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                textInputLayout.setError(null);
            }
        });
    }

    @NonNull
    public static View inflate(int resource, @NonNull Context context) {
        return inflate(resource, null, false, context);
    }

    @NonNull
    public static View inflate(int resource, @NonNull ViewGroup parent) {
        return inflate(resource, parent, false, parent.getContext());
    }

    @NonNull
    public static View inflateWithTheme(int resource, @NonNull ViewGroup parent,
                                        @StyleRes int themeRes) {
        return inflate(resource, parent, false, new ContextThemeWrapper(parent.getContext(),
                themeRes));
    }

    @NonNull
    public static View inflateInto(int resource, @NonNull ViewGroup parent) {
        return inflate(resource, parent, true, parent.getContext());
    }

    @NonNull
    public static View inflateIntoWithTheme(int resource, @NonNull ViewGroup parent,
                                            @StyleRes int themeRes) {
        return inflate(resource, parent, true, new ContextThemeWrapper(parent.getContext(),
                themeRes));
    }

    @NonNull
    private static View inflate(int resource, @Nullable ViewGroup parent, boolean attachToRoot,
                                @NonNull Context context) {
        return LayoutInflater.from(context).inflate(resource, parent, attachToRoot);
    }

    public static boolean isLightTheme(@NonNull Context context) {
        return getBooleanFromAttrRes(R.attr.isLightTheme, false, context);
    }

    public static boolean isInPortait(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isInLandscape(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isVisible(@NonNull View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public static void postOnDrawerClosed(@NonNull DrawerLayout drawerLayout,
                                          @NonNull Runnable runnable) {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                drawerLayout.removeDrawerListener(this);
                runnable.run();
            }
        });
    }

    public static void postOnPreDraw(@NonNull View view, @NonNull Runnable runnable) {
        view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListenerRunnableWrapper(
                view, runnable));
    }

    public static void removeOnPreDraw(@NonNull View view, @NonNull Runnable runnable) {
        view.getViewTreeObserver().removeOnPreDrawListener(new OnPreDrawListenerRunnableWrapper(
                view, runnable));
    }

    private static class OnPreDrawListenerRunnableWrapper
            implements ViewTreeObserver.OnPreDrawListener {

        @NonNull
        private final View mView;
        @NonNull
        private final Runnable mRunnable;

        public OnPreDrawListenerRunnableWrapper(@NonNull View view, @NonNull Runnable runnable) {
            mView = view;
            mRunnable = runnable;
        }

        @Override
        public boolean onPreDraw() {
            mView.getViewTreeObserver().removeOnPreDrawListener(this);
            mRunnable.run();
            return true;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            OnPreDrawListenerRunnableWrapper that = (OnPreDrawListenerRunnableWrapper) object;
            return ObjectsCompat.equals(mRunnable, that.mRunnable);
        }

        @Override
        public int hashCode() {
            return mRunnable.hashCode();
        }
    }

    public static void postOnPreDraw(@NonNull View view, @NonNull BooleanSupplier runnable) {
        view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListenerBooleanSupplierWrapper(
                view, runnable));
    }

    public static void removeOnPreDraw(@NonNull View view, @NonNull BooleanSupplier runnable) {
        view.getViewTreeObserver().removeOnPreDrawListener(
                new OnPreDrawListenerBooleanSupplierWrapper(view, runnable));
    }

    private static class OnPreDrawListenerBooleanSupplierWrapper
            implements ViewTreeObserver.OnPreDrawListener {

        @NonNull
        private final View mView;
        @NonNull
        private final BooleanSupplier mRunnable;

        public OnPreDrawListenerBooleanSupplierWrapper(@NonNull View view,
                                                       @NonNull BooleanSupplier runnable) {
            mView = view;
            mRunnable = runnable;
        }

        @Override
        public boolean onPreDraw() {
            mView.getViewTreeObserver().removeOnPreDrawListener(this);
            return mRunnable.getAsBoolean();
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            OnPreDrawListenerRunnableWrapper that = (OnPreDrawListenerRunnableWrapper) object;
            return ObjectsCompat.equals(mRunnable, that.mRunnable);
        }

        @Override
        public int hashCode() {
            return mRunnable.hashCode();
        }
    }

    @Dimension(unit = Dimension.DP)
    public static float pxToDp(@Dimension float px, @NonNull Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }

    @Dimension(unit = Dimension.DP)
    public static int pxToDpInt(@Dimension float px, @NonNull Context context) {
        return Math.round(pxToDp(px, context));
    }

    public static void replaceChild(@NonNull ViewGroup viewGroup, @NonNull View oldChild,
                                    @NonNull View newChild) {
        int index = viewGroup.indexOfChild(oldChild);
        viewGroup.removeViewAt(index);
        viewGroup.addView(newChild, index);
    }

    public static void setBackgroundPreservingPadding(@NonNull View view,
                                                      @Nullable Drawable background) {
        int savedPaddingStart = ViewCompat.getPaddingStart(view);
        int savedPaddingEnd = ViewCompat.getPaddingEnd(view);
        int savedPaddingTop = view.getPaddingTop();
        int savedPaddingBottom = view.getPaddingBottom();
        view.setBackground(background);
        ViewCompat.setPaddingRelative(view, savedPaddingStart, savedPaddingTop, savedPaddingEnd,
                savedPaddingBottom);
    }

    public static void setMarginStart(@NonNull View view, int marginStart) {
        MarginLayoutParamsCompat.setMarginStart(
                (ViewGroup.MarginLayoutParams) view.getLayoutParams(), marginStart);
    }

    public static void setMarginEnd(@NonNull View view, int marginEnd) {
        MarginLayoutParamsCompat.setMarginEnd((ViewGroup.MarginLayoutParams) view.getLayoutParams(),
                marginEnd);
    }

    public static void setMarginLeft(@NonNull View view, int marginLeft) {
        ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin = marginLeft;
    }

    public static void setMarginRight(@NonNull View view, int marginRight) {
        ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin = marginRight;
    }

    public static void setMarginTop(@NonNull View view, int marginTop) {
        ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin = marginTop;
    }

    public static void setMarginBottom(@NonNull View view, int marginBottom) {
        ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin = marginBottom;
    }

    public static void setWidth(@NonNull View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.width == width) {
            return;
        }
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    public static void setHeight(@NonNull View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.height == height) {
            return;
        }
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    public static void setSize(@NonNull View view, int size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.width == size && layoutParams.height == size) {
            return;
        }
        layoutParams.width = size;
        layoutParams.height = size;
        view.setLayoutParams(layoutParams);
    }

    public static void setWeight(@NonNull View view, float weight) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.weight = weight;
        view.setLayoutParams(layoutParams);
    }

    public static void setPaddingStart(@NonNull View view, int paddingStart) {
        ViewCompat.setPaddingRelative(view, paddingStart, view.getPaddingTop(),
                ViewCompat.getPaddingEnd(view), view.getPaddingBottom());
    }

    public static void setPaddingEnd(@NonNull View view, int paddingEnd) {
        ViewCompat.setPaddingRelative(view, ViewCompat.getPaddingStart(view), view.getPaddingTop(),
                paddingEnd, view.getPaddingBottom());
    }

    public static void setPaddingLeft(@NonNull View view, int paddingLeft) {
        view.setPadding(paddingLeft, view.getPaddingTop(), view.getPaddingRight(),
                view.getPaddingBottom());
    }

    public static void setPaddingRight(@NonNull View view, int paddingRight) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), paddingRight,
                view.getPaddingBottom());
    }

    public static void setPaddingTop(@NonNull View view, int paddingTop) {
        view.setPadding(view.getPaddingLeft(), paddingTop, view.getPaddingRight(),
                view.getPaddingBottom());
    }

    public static void setPaddingBottom(@NonNull View view, int paddingBottom) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(),
                paddingBottom);
    }

    public static void setLayoutFullscreen(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    public static void setLayoutFullscreen(@NonNull Activity activity) {
        setLayoutFullscreen(activity.getWindow().getDecorView());
    }

    public static void setTextViewBold(@NonNull TextView textView, boolean bold) {

        Typeface typeface = textView.getTypeface();
        if (typeface.isBold() == bold) {
            return;
        }

        int style = textView.getTypeface().getStyle();
        if (bold) {
            style |= Typeface.BOLD;
        } else {
            style &= ~Typeface.BOLD;
        }
        // Workaround insane behavior in TextView#setTypeface(Typeface, int).
        if (style > 0) {
            textView.setTypeface(typeface, style);
        } else {
            textView.setTypeface(Typeface.create(typeface, style), style);
        }
    }

    public static void setTextViewItalic(@NonNull TextView textView, boolean italic) {

        Typeface typeface = textView.getTypeface();
        if (typeface.isItalic() == italic) {
            return;
        }

        int style = textView.getTypeface().getStyle();
        if (italic) {
            style |= Typeface.ITALIC;
        } else {
            style &= ~Typeface.ITALIC;
        }
        // Workaround insane behavior in TextView#setTypeface(Typeface, int).
        if (style > 0) {
            textView.setTypeface(typeface, style);
        } else {
            textView.setTypeface(Typeface.create(typeface, style), style);
        }
    }

    public static void setTextViewLinkClickable(TextView textView) {
        boolean wasClickable = textView.isClickable();
        boolean wasLongClickable = textView.isLongClickable();
        textView.setMovementMethod(ClickableMovementMethod.getInstance());
        // Reset for TextView.fixFocusableAndClickableSettings(). We don't want View.onTouchEvent()
        // to consume touch events.
        textView.setClickable(wasClickable);
        textView.setLongClickable(wasLongClickable);
    }

    public static void setTextViewLinkClickableAndTextSelectable(TextView textView) {
        textView.setTextIsSelectable(true);
        textView.setMovementMethod(LinkArrowKeyMovementMethod.getInstance());
    }

    public static void setVisibleOrGone(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void setVisibleOrInvisible(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
