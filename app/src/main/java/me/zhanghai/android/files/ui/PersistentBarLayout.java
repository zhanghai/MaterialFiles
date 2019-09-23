/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.math.MathUtils;
import androidx.customview.widget.ViewDragHelper;
import me.zhanghai.android.files.util.ViewUtils;

/**
 * @see PersistentDrawerLayout
 */
public class PersistentBarLayout extends ViewGroup {

    @NonNull
    private final ViewDragHelper mTopDragger;
    @NonNull
    private final ViewDragHelper mBottomDragger;

    @Nullable
    private WindowInsets mLastInsets;

    public PersistentBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public PersistentBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersistentBarLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                               @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PersistentBarLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                               @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (getFitsSystemWindows()) {
            ViewUtils.setLayoutFullscreen(this);
        }

        ViewDragCallback topCallback = new ViewDragCallback(Gravity.TOP);
        mTopDragger = ViewDragHelper.create(this, topCallback);
        ViewDragCallback bottomCallback = new ViewDragCallback(Gravity.BOTTOM);
        mBottomDragger = ViewDragHelper.create(this, bottomCallback);
    }

    @NonNull
    @Override
    public WindowInsets dispatchApplyWindowInsets(@NonNull WindowInsets insets) {
        if (!getFitsSystemWindows()) {
            return insets;
        }
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);

            if (isBarView(child)) {
                if (isTopBarView(child)) {
                    child.dispatchApplyWindowInsets(insets.replaceSystemWindowInsets(
                            insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(), 0));
                } else {
                    child.dispatchApplyWindowInsets(insets.replaceSystemWindowInsets(
                            insets.getSystemWindowInsetLeft(), 0,
                            insets.getSystemWindowInsetRight(),
                            insets.getSystemWindowInsetBottom()));
                }
            } else if (isFillView(child)) {
                child.dispatchApplyWindowInsets(insets);
            }
        }
        mLastInsets = insets;
        updateContentViewsWindowInsets();
        return insets.consumeSystemWindowInsets();
    }

    private void updateContentViewsWindowInsets() {

        if (mLastInsets == null) {
            return;
        }

        WindowInsets contentInsets = mLastInsets;
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);

            if (isBarView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childRange = childLayoutParams.topMargin + child.getMeasuredHeight()
                        + childLayoutParams.bottomMargin;
                int childConsumedInset = (int) (childRange * childLayoutParams.offset);
                if (isTopBarView(child)) {
                    contentInsets = contentInsets.replaceSystemWindowInsets(
                            contentInsets.getSystemWindowInsetLeft(),
                            Math.max(0, contentInsets.getSystemWindowInsetTop()
                                    - childConsumedInset),
                            contentInsets.getSystemWindowInsetRight(),
                            contentInsets.getSystemWindowInsetBottom());
                } else {
                    contentInsets = contentInsets.replaceSystemWindowInsets(
                            contentInsets.getSystemWindowInsetLeft(),
                            contentInsets.getSystemWindowInsetTop(),
                            contentInsets.getSystemWindowInsetRight(),
                            Math.max(0, contentInsets.getSystemWindowInsetBottom()
                                    - childConsumedInset));
                }
            }
        }

        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);

            if (isContentView(child)) {
                child.dispatchApplyWindowInsets(contentInsets);
            }
        }
    }

    @Override
    public void computeScroll() {
        boolean topSettling = mTopDragger.continueSettling(true);
        boolean bottomSettling = mBottomDragger.continueSettling(true);
        if (topSettling || bottomSettling) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthSize = 300;
                }
                if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightSize = 300;
                }
            } else {
                throw new IllegalArgumentException(
                        "BarLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }

        setMeasuredDimension(widthSize, heightSize);

        boolean hasTopBar = false;
        boolean hasBottomBar = false;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            boolean isBar = isBarView(child);
            if (isBar || isFillView(child)) {

                if (isBar) {
                    boolean isTopBar = isTopBarView(child);
                    if ((isTopBar && hasTopBar) || (!isTopBar && hasBottomBar)) {
                        throw new IllegalStateException("Child " + child + " at index " + i
                                + " is a second " + (isTopBar ? "top" : "bottom") + " bar");
                    }
                    if (isTopBar) {
                        hasTopBar = true;
                    } else {
                        hasBottomBar = true;
                    }
                }

                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                        childLayoutParams.leftMargin + childLayoutParams.rightMargin,
                        childLayoutParams.width);
                int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                        childLayoutParams.topMargin + childLayoutParams.bottomMargin,
                        childLayoutParams.height);
                child.measure(childWidthSpec, childHeightSpec);
            } else if (!isContentView(child)) {
                throw new IllegalStateException("Child " + child + " at index " + i
                        + " does not have a valid layout_gravity - must be Gravity.LEFT,"
                        + " Gravity.RIGHT, Gravity.NO_GRAVITY or Gravity.FILL");
            }
        }

        updateContentViewsWindowInsets();
        measureContentViews();
    }

    private void measureContentViews() {

        int contentWidth = getMeasuredWidth();
        int contentHeight = getMeasuredHeight();
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (isBarView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childRange = childLayoutParams.topMargin + child.getMeasuredHeight()
                        + childLayoutParams.bottomMargin;
                contentHeight -= (int) (childRange * childLayoutParams.offset);
            }
        }

        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (isContentView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childWidthSpec = MeasureSpec.makeMeasureSpec(
                        contentWidth - childLayoutParams.leftMargin - childLayoutParams.rightMargin,
                        MeasureSpec.EXACTLY);
                int contentHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight
                                - childLayoutParams.topMargin - childLayoutParams.bottomMargin,
                        MeasureSpec.EXACTLY);
                child.measure(childWidthSpec, contentHeightSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (isBarView(child)) {

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();

                int childTop = computeBarViewTop(child);

                int childHorizontalGravity = Gravity.getAbsoluteGravity(childLayoutParams.gravity,
                        getLayoutDirection()) & Gravity.HORIZONTAL_GRAVITY_MASK;
                int width = right - left;
                switch (childHorizontalGravity) {
                    //noinspection DefaultNotLastCaseInSwitch
                    default:
                    case Gravity.LEFT:
                        child.layout(childLayoutParams.leftMargin, childTop,
                                childLayoutParams.leftMargin + childWidth, childTop + childHeight);
                        break;
                    case Gravity.RIGHT: {
                        int childRight = width - childLayoutParams.rightMargin;
                        child.layout(childRight - childWidth, childTop, childRight,
                                childTop + childHeight);
                        break;
                    }
                    case Gravity.CENTER_HORIZONTAL: {
                        int childLeft = (width - childWidth) / 2 + childLayoutParams.leftMargin
                                - childLayoutParams.rightMargin;
                        child.layout(childLeft, childTop, childLeft + childWidth,
                                childTop + childHeight);
                        break;
                    }
                }

                child.setVisibility(childLayoutParams.offset > 0 ? VISIBLE : INVISIBLE);
            } else if (isFillView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                child.layout(childLayoutParams.leftMargin, childLayoutParams.topMargin,
                        childLayoutParams.leftMargin + child.getMeasuredWidth(),
                        childLayoutParams.topMargin + child.getMeasuredHeight());
            }
        }

        layoutContentViews();
    }

    private int computeBarViewTop(@NonNull View barView) {
        LayoutParams childLayoutParams = (LayoutParams) barView.getLayoutParams();
        int childRange = childLayoutParams.topMargin + barView.getMeasuredHeight()
                + childLayoutParams.bottomMargin;
        if (isTopBarView(barView)) {
            return -childRange + (int) (childRange * childLayoutParams.offset)
                    + childLayoutParams.topMargin;
        } else {
            return getMeasuredHeight() - (int) (childRange * childLayoutParams.offset)
                    + childLayoutParams.bottomMargin;
        }
    }

    private void layoutContentViews() {

        int contentTop = 0;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (isBarView(child)) {
                if (isTopBarView(child)) {
                    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                    contentTop = child.getBottom() + childLayoutParams.bottomMargin;
                }
            }
        }

        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (isContentView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childTop = contentTop + childLayoutParams.topMargin;
                child.layout(childLayoutParams.leftMargin, childTop,
                        childLayoutParams.leftMargin + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());
            }
        }
    }

    @NonNull
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(@Nullable AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @NonNull
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(
            @NonNull ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams)
                : layoutParams instanceof MarginLayoutParams ?
                new LayoutParams((MarginLayoutParams) layoutParams)
                : new LayoutParams(layoutParams);
    }

    @NonNull
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(@NonNull ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams && super.checkLayoutParams(layoutParams);
    }

    public boolean isBarShown(@NonNull View barView) {

        if (!isBarView(barView)) {
            throw new IllegalArgumentException("View " + barView + " is not a bar");
        }

        LayoutParams childLayoutParams = (LayoutParams) barView.getLayoutParams();
        return childLayoutParams.shown;
    }

    public boolean isBarShown(int gravity) {
        View barView = findBarView(gravity);
        if (barView == null) {
            throw new IllegalArgumentException("No bar view found with gravity " + gravity);
        }
        return isBarShown(barView);
    }

    public void showBar(@NonNull View barView, boolean animate) {

        if (!isBarView(barView)) {
            throw new IllegalArgumentException("View " + barView + " is not a bar");
        }

        LayoutParams childLayoutParams = (LayoutParams) barView.getLayoutParams();
        if (childLayoutParams.shown && childLayoutParams.offset == 1) {
            return;
        }

        childLayoutParams.shown = true;
        if (!isLaidOut()) {
            childLayoutParams.offset = 1;
        } else if (animate) {
            if (isTopBarView(barView)) {
                mTopDragger.smoothSlideViewTo(barView, barView.getLeft(), 0);
            } else {
                mBottomDragger.smoothSlideViewTo(barView, barView.getLeft(),
                        getHeight() - barView.getHeight() - childLayoutParams.bottomMargin);
            }
        } else {
            moveBarToOffset(barView, 1);
            updateContentViewsWindowInsets();
            measureContentViews();
            layoutContentViews();
        }
        invalidate();
    }

    public void showBar(@NonNull View barView) {
        showBar(barView, true);
    }

    public void showBar(int gravity, boolean animate) {
        View barView = findBarView(gravity);
        if (barView == null) {
            throw new IllegalArgumentException("No bar view found with gravity " + gravity);
        }
        showBar(barView, animate);
    }

    public void showBar(int gravity) {
        showBar(gravity, true);
    }

    public void hideBar(@NonNull View barView, boolean animate) {

        if (!isBarView(barView)) {
            throw new IllegalArgumentException("View " + barView + " is not a bar");
        }

        LayoutParams childLayoutParams = (LayoutParams) barView.getLayoutParams();
        if (!childLayoutParams.shown && childLayoutParams.offset == 0) {
            return;
        }

        childLayoutParams.shown = false;
        if (!isLaidOut()) {
            childLayoutParams.offset = 0;
        } else if (animate) {
            if (isTopBarView(barView)) {
                mTopDragger.smoothSlideViewTo(barView, barView.getLeft(),
                        -barView.getHeight() - childLayoutParams.bottomMargin);
            } else {
                mBottomDragger.smoothSlideViewTo(barView, barView.getLeft(), getHeight());
            }
        } else {
            childLayoutParams.offset = 0;
            moveBarToOffset(barView, 0);
            updateContentViewsWindowInsets();
            measureContentViews();
            layoutContentViews();
        }
        invalidate();
    }

    public void hideBar(@NonNull View barView) {
        hideBar(barView, true);
    }

    public void hideBar(int gravity, boolean animate) {
        View barView = findBarView(gravity);
        if (barView == null) {
            throw new IllegalArgumentException("No bar view found with gravity " + gravity);
        }
        hideBar(barView, animate);
    }

    public void hideBar(int gravity) {
        hideBar(gravity, true);
    }

    private void moveBarToOffset(@NonNull View barView, float offset) {
        LayoutParams childLayoutParams = (LayoutParams) barView.getLayoutParams();
        if (childLayoutParams.offset == offset) {
            return;
        }
        childLayoutParams.offset = offset;
        int oldChildTop = barView.getTop();
        int newChildTop = computeBarViewTop(barView);
        barView.offsetTopAndBottom(newChildTop - oldChildTop);
        barView.setVisibility(offset > 0 ? VISIBLE : INVISIBLE);
    }

    public void toggleBar(@NonNull View barView) {
        if (isBarShown(barView)) {
            hideBar(barView);
        } else {
            showBar(barView);
        }
    }

    public void toggleBar(int gravity) {
        View barView = findBarView(gravity);
        if (barView == null) {
            throw new IllegalArgumentException("No bar view found with gravity " + gravity);
        }
        toggleBar(barView);
    }

    @Nullable
    private View findBarView(int gravity) {
        int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            int childVerticalGravity = getChildVerticalGravity(child);
            if (childVerticalGravity == verticalGravity) {
                return child;
            }
        }
        return null;
    }

    private boolean isBarView(@NonNull View child) {
        int verticalGravity = getChildVerticalGravity(child);
        return verticalGravity == Gravity.TOP || verticalGravity == Gravity.BOTTOM;
    }

    private boolean isTopBarView(@NonNull View barView) {
        int verticalGravity = getChildVerticalGravity(barView);
        return verticalGravity == Gravity.TOP;
    }

    private boolean isContentView(@NonNull View child) {
        return getChildGravity(child) == Gravity.NO_GRAVITY;
    }

    private boolean isFillView(@NonNull View child) {
        return getChildGravity(child) == Gravity.FILL;
    }

    private int getChildGravity(@NonNull View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity;
    }

    private int getChildVerticalGravity(@NonNull View child) {
        return getChildGravity(child) & Gravity.VERTICAL_GRAVITY_MASK;
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private final int mGravity;

        public ViewDragCallback(int gravity) {
            mGravity = gravity;
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return false;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx,
                                          int dy) {
            int childRange = getViewVerticalDragRange(changedView);
            LayoutParams childLayoutParams = (LayoutParams) changedView.getLayoutParams();
            if (isTopBarView(changedView)) {
                childLayoutParams.offset = (float) (top - childLayoutParams.topMargin
                        + childRange) / childRange;
            } else {
                int height = getHeight();
                childLayoutParams.offset = (float) (childLayoutParams.topMargin + height - top)
                        / childRange;
            }
            changedView.setVisibility(childLayoutParams.offset > 0 ? VISIBLE : INVISIBLE);
            updateContentViewsWindowInsets();
            measureContentViews();
            layoutContentViews();
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            closeOtherBar();
        }

        private void closeOtherBar() {
            int otherGravity = mGravity == Gravity.TOP ? Gravity.BOTTOM : Gravity.TOP;
            View otherBar = findBarView(otherGravity);
            if (otherBar != null) {
                hideBar(otherBar);
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            if (!isBarView(child)) {
                return 0;
            }
            LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
            return childLayoutParams.topMargin + child.getHeight() + childLayoutParams.bottomMargin;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (isTopBarView(child)) {
                return MathUtils.clamp(top, -getViewVerticalDragRange(child), 0);
            } else {
                int height = getHeight();
                return MathUtils.clamp(top, height - getViewVerticalDragRange(child), height);
            }
        }
    }

    public static class LayoutParams extends MarginLayoutParams {

        private static final int[] ATTRS = { android.R.attr.layout_gravity };

        public int gravity = Gravity.NO_GRAVITY;
        public float offset;
        public boolean shown;

        public LayoutParams(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
            gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);

            this.gravity = gravity;
        }

        public LayoutParams(@NonNull LayoutParams source) {
            super(source);

            this.gravity = source.gravity;
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
