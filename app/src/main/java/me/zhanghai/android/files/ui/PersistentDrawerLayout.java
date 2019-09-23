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

public class PersistentDrawerLayout extends ViewGroup {

    private static final float DRAWER_ELEVATION_DP = 2;

    private float mDrawerElevation;

    @NonNull
    private final ViewDragHelper mLeftDragger;
    @NonNull
    private final ViewDragHelper mRightDragger;

    @Nullable
    private WindowInsets mLastInsets;

    public PersistentDrawerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PersistentDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersistentDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PersistentDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mDrawerElevation = ViewUtils.dpToPx(DRAWER_ELEVATION_DP, context);

        if (getFitsSystemWindows()) {
            ViewUtils.setLayoutFullscreen(this);
        }

        ViewDragCallback leftCallback = new ViewDragCallback(Gravity.LEFT);
        mLeftDragger = ViewDragHelper.create(this, leftCallback);
        ViewDragCallback rightCallback = new ViewDragCallback(Gravity.RIGHT);
        mRightDragger = ViewDragHelper.create(this, rightCallback);
    }

    @NonNull
    @Override
    public WindowInsets dispatchApplyWindowInsets(@NonNull WindowInsets insets) {
        if (!getFitsSystemWindows()) {
            return insets;
        }
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);

            if (isDrawerView(child)) {
                if (isLeftDrawerView(child)) {
                    child.dispatchApplyWindowInsets(insets.replaceSystemWindowInsets(
                            insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), 0,
                            insets.getSystemWindowInsetBottom()));
                } else {
                    child.dispatchApplyWindowInsets(insets.replaceSystemWindowInsets(0,
                            insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(),
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

            if (isDrawerView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childRange = childLayoutParams.leftMargin + child.getMeasuredWidth()
                        + childLayoutParams.rightMargin;
                int childConsumedInset = (int) (childRange * childLayoutParams.offset);
                if (isLeftDrawerView(child)) {
                    contentInsets = contentInsets.replaceSystemWindowInsets(
                            Math.max(0, contentInsets.getSystemWindowInsetLeft()
                                    - childConsumedInset),
                            contentInsets.getSystemWindowInsetTop(),
                            contentInsets.getSystemWindowInsetRight(),
                            contentInsets.getSystemWindowInsetBottom());
                } else {
                    contentInsets = contentInsets.replaceSystemWindowInsets(
                            contentInsets.getSystemWindowInsetLeft(),
                            contentInsets.getSystemWindowInsetTop(),
                            Math.max(0, contentInsets.getSystemWindowInsetRight()
                                    - childConsumedInset),
                            contentInsets.getSystemWindowInsetBottom());
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
        boolean leftSettling = mLeftDragger.continueSettling(true);
        boolean rightSettling = mRightDragger.continueSettling(true);
        if (leftSettling || rightSettling) {
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
                        "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }

        setMeasuredDimension(widthSize, heightSize);

        boolean hasLeftDrawer = false;
        boolean hasRightDrawer = false;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            boolean isDrawer = isDrawerView(child);
            if (isDrawer || isFillView(child)) {

                if (isDrawer) {

                    boolean isLeftDrawer = isLeftDrawerView(child);
                    if ((isLeftDrawer && hasLeftDrawer) || (!isLeftDrawer && hasRightDrawer)) {
                        throw new IllegalStateException("Child " + child + " at index " + i
                                + " is a second " + (isLeftDrawer ? "left" : "right") + " drawer");
                    }
                    if (isLeftDrawer) {
                        hasLeftDrawer = true;
                    } else {
                        hasRightDrawer = true;
                    }

                    child.setElevation(mDrawerElevation);
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

            if (isDrawerView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childRange = childLayoutParams.leftMargin + child.getMeasuredWidth()
                        + childLayoutParams.rightMargin;
                contentWidth -= (int) (childRange * childLayoutParams.offset);
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

            if (isDrawerView(child)) {

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();

                int childLeft = computeDrawerViewLeft(child);

                int childVerticalGravity = childLayoutParams.gravity
                        & Gravity.VERTICAL_GRAVITY_MASK;
                int height = bottom - top;
                switch (childVerticalGravity) {
                    //noinspection DefaultNotLastCaseInSwitch
                    default:
                    case Gravity.TOP:
                        child.layout(childLeft, childLayoutParams.topMargin, childLeft + childWidth,
                                childLayoutParams.topMargin + childHeight);
                        break;
                    case Gravity.BOTTOM: {
                        int childBottom = height - childLayoutParams.bottomMargin;
                        child.layout(childLeft, childBottom - childHeight, childLeft + childWidth,
                                childBottom);
                        break;
                    }
                    case Gravity.CENTER_VERTICAL: {
                        int childTop = (height - childHeight) / 2 + childLayoutParams.topMargin
                                - childLayoutParams.bottomMargin;
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

    private int computeDrawerViewLeft(@NonNull View drawerView) {
        LayoutParams childLayoutParams = (LayoutParams) drawerView.getLayoutParams();
        int childRange = childLayoutParams.leftMargin + drawerView.getMeasuredWidth()
                + childLayoutParams.rightMargin;
        if (isLeftDrawerView(drawerView)) {
            return -childRange + (int) (childRange * childLayoutParams.offset)
                    + childLayoutParams.leftMargin;
        } else {
            return getMeasuredWidth() - (int) (childRange * childLayoutParams.offset)
                    + childLayoutParams.leftMargin;
        }
    }

    private void layoutContentViews() {

        int contentLeft = 0;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (isDrawerView(child)) {
                if (isLeftDrawerView(child)) {
                    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                    contentLeft = child.getRight() + childLayoutParams.rightMargin;
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
                int childLeft = contentLeft + childLayoutParams.leftMargin;
                child.layout(childLeft, childLayoutParams.topMargin,
                        childLeft + child.getMeasuredWidth(),
                        childLayoutParams.topMargin + child.getMeasuredHeight());
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
                : layoutParams instanceof ViewGroup.MarginLayoutParams ?
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

    public float getDrawerElevation() {
        return mDrawerElevation;
    }

    public void setDrawerElevation(float elevation) {

        if (mDrawerElevation == elevation) {
            return;
        }

        mDrawerElevation = elevation;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            if (isDrawerView(child)) {
                child.setElevation(mDrawerElevation);
            }
        }
    }

    public boolean isDrawerOpen(@NonNull View drawerView) {

        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a drawer");
        }

        LayoutParams childLayoutParams = (LayoutParams) drawerView.getLayoutParams();
        return childLayoutParams.open;
    }

    public boolean isDrawerOpen(int gravity) {
        View drawerView = findDrawerView(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity " + gravity);
        }
        return isDrawerOpen(drawerView);
    }

    public void openDrawer(@NonNull View drawerView, boolean animate) {

        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a drawer");
        }

        LayoutParams childLayoutParams = (LayoutParams) drawerView.getLayoutParams();
        if (childLayoutParams.open && childLayoutParams.offset == 1) {
            return;
        }

        childLayoutParams.open = true;
        if (!isLaidOut()) {
            childLayoutParams.offset = 1;
        } else if (animate) {
            if (isLeftDrawerView(drawerView)) {
                mLeftDragger.smoothSlideViewTo(drawerView, 0, drawerView.getTop());
            } else {
                mRightDragger.smoothSlideViewTo(drawerView,
                        getWidth() - drawerView.getWidth() - childLayoutParams.rightMargin,
                        drawerView.getTop());
            }
        } else {
            moveDrawerToOffset(drawerView, 1);
            updateContentViewsWindowInsets();
            measureContentViews();
            layoutContentViews();
        }
        invalidate();
    }

    public void openDrawer(@NonNull View drawerView) {
        openDrawer(drawerView, true);
    }

    public void openDrawer(int gravity, boolean animate) {
        View drawerView = findDrawerView(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity " + gravity);
        }
        openDrawer(drawerView, animate);
    }

    public void openDrawer(int gravity) {
        openDrawer(gravity, true);
    }

    public void closeDrawer(@NonNull View drawerView, boolean animate) {

        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a drawer");
        }

        LayoutParams childLayoutParams = (LayoutParams) drawerView.getLayoutParams();
        if (!childLayoutParams.open && childLayoutParams.offset == 0) {
            return;
        }

        childLayoutParams.open = false;
        if (!isLaidOut()) {
            childLayoutParams.offset = 0;
        } else if (animate) {
            if (isLeftDrawerView(drawerView)) {
                mLeftDragger.smoothSlideViewTo(drawerView,
                        -drawerView.getWidth() - childLayoutParams.rightMargin,
                        drawerView.getTop());
            } else {
                mRightDragger.smoothSlideViewTo(drawerView, getWidth(), drawerView.getTop());
            }
        } else {
            childLayoutParams.offset = 0;
            moveDrawerToOffset(drawerView, 0);
            updateContentViewsWindowInsets();
            measureContentViews();
            layoutContentViews();
        }
        invalidate();
    }

    public void closeDrawer(@NonNull View drawerView) {
        closeDrawer(drawerView, true);
    }

    public void closeDrawer(int gravity, boolean animate) {
        View drawerView = findDrawerView(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity " + gravity);
        }
        closeDrawer(drawerView, animate);
    }

    public void closeDrawer(int gravity) {
        closeDrawer(gravity, true);
    }

    private void moveDrawerToOffset(@NonNull View drawerView, float offset) {
        LayoutParams childLayoutParams = (LayoutParams) drawerView.getLayoutParams();
        if (childLayoutParams.offset == offset) {
            return;
        }
        childLayoutParams.offset = offset;
        int oldChildLeft = drawerView.getLeft();
        int newChildLeft = computeDrawerViewLeft(drawerView);
        drawerView.offsetLeftAndRight(newChildLeft - oldChildLeft);
        drawerView.setVisibility(offset > 0 ? VISIBLE : INVISIBLE);
    }

    public void toggleDrawer(@NonNull View drawerView) {
        if (isDrawerOpen(drawerView)) {
            closeDrawer(drawerView);
        } else {
            openDrawer(drawerView);
        }
    }

    public void toggleDrawer(int gravity) {
        View drawerView = findDrawerView(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity " + gravity);
        }
        toggleDrawer(drawerView);
    }

    @Nullable
    private View findDrawerView(int gravity) {
        int horizontalGravity = Gravity.getAbsoluteGravity(gravity, getLayoutDirection())
                & Gravity.HORIZONTAL_GRAVITY_MASK;
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);

            int childHorizontalGravity = getChildAbsoluteHorizontalGravity(child);
            if (childHorizontalGravity == horizontalGravity) {
                return child;
            }
        }
        return null;
    }

    private boolean isDrawerView(@NonNull View child) {
        int horizontalGravity = getChildAbsoluteHorizontalGravity(child);
        return horizontalGravity == Gravity.LEFT || horizontalGravity == Gravity.RIGHT;
    }

    private boolean isLeftDrawerView(@NonNull View drawerView) {
        int horizontalGravity = getChildAbsoluteHorizontalGravity(drawerView);
        return horizontalGravity == Gravity.LEFT;
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

    private int getChildAbsoluteHorizontalGravity(@NonNull View child) {
        return Gravity.getAbsoluteGravity(getChildGravity(child), getLayoutDirection())
                & Gravity.HORIZONTAL_GRAVITY_MASK;
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
            int childRange = getViewHorizontalDragRange(changedView);
            LayoutParams childLayoutParams = (LayoutParams) changedView.getLayoutParams();
            if (isLeftDrawerView(changedView)) {
                childLayoutParams.offset = (float) (left - childLayoutParams.leftMargin
                        + childRange) / childRange;
            } else {
                int width = getWidth();
                childLayoutParams.offset = (float) (childLayoutParams.leftMargin + width - left)
                        / childRange;
            }
            changedView.setVisibility(childLayoutParams.offset > 0 ? VISIBLE : INVISIBLE);
            updateContentViewsWindowInsets();
            measureContentViews();
            layoutContentViews();
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            closeOtherDrawer();
        }

        private void closeOtherDrawer() {
            int otherGravity = mGravity == Gravity.LEFT ? Gravity.RIGHT : Gravity.LEFT;
            View otherDrawer = findDrawerView(otherGravity);
            if (otherDrawer != null) {
                closeDrawer(otherDrawer);
            }
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            if (!isDrawerView(child)) {
                return 0;
            }
            LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
            return childLayoutParams.leftMargin + child.getWidth() + childLayoutParams.rightMargin;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (isLeftDrawerView(child)) {
                return MathUtils.clamp(left, -getViewHorizontalDragRange(child), 0);
            } else {
                int width = getWidth();
                return MathUtils.clamp(left, width - getViewHorizontalDragRange(child), width);
            }
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return child.getTop();
        }
    }

    public static class LayoutParams extends MarginLayoutParams {

        private static final int[] ATTRS = { android.R.attr.layout_gravity };

        public int gravity = Gravity.NO_GRAVITY;
        public float offset;
        public boolean open;

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
