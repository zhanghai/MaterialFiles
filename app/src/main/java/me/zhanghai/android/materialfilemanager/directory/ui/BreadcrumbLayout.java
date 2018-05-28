/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.directory.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class BreadcrumbLayout extends HorizontalScrollView {

    @BindDimen(R.dimen.tab_layout_height)
    int mTabLayoutHeight;

    private ColorStateList mItemColor;

    private LinearLayout mItemsLayout;

    private OnItemSelectedListener mOnItemSelectedListener;

    private List<String> mItems = new ArrayList<>();
    private int mSelectedIndex;

    private final Runnable mScrollToSelectedItemRunnable = () -> scrollToSelectedItem(false);

    public BreadcrumbLayout(Context context) {
        super(context);

        init();
    }

    public BreadcrumbLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public BreadcrumbLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public BreadcrumbLayout(Context context, AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setHorizontalScrollBarEnabled(false);
        Context context = getContext();
        mItemColor = new ColorStateList(new int[][] {
                { android.R.attr.state_activated },
                {}
        }, new int[] {
                ViewUtils.getColorFromAttrRes(android.R.attr.textColorPrimary, 0, context),
                ViewUtils.getColorFromAttrRes(android.R.attr.textColorSecondary, 0, context)
        });
        mItemsLayout = new LinearLayout(getContext());
        mItemsLayout.setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(),
                getPaddingBottom());
        setPaddingRelative(0, 0, 0, 0);
        mItemsLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(mItemsLayout, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            int height = mTabLayoutHeight;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public List<String> getItems() {
        return Collections.unmodifiableList(mItems);
    }

    public void setItems(List<String> items) {
        if (mItems.equals(items)) {
            return;
        }
        boolean isPrefix = items.size() <= mItems.size() && Functional.every(items, (item, index) ->
                TextUtils.equals(item, mItems.get(index)));
        if (!isPrefix) {
            mItems.clear();
            mItems.addAll(items);
        }
        mSelectedIndex = items.size() - 1;
        if (isPrefix) {
            onSelectedIndexChanged();
        } else {
            onItemsChanged();
        }
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (mSelectedIndex == index) {
            return;
        }
        mSelectedIndex = index;
        onSelectedIndexChanged();
    }

    public void trimItems() {
        if (mSelectedIndex == mItems.size() - 1) {
            return;
        }
        mItems.subList(mSelectedIndex + 1, mItems.size()).clear();
    }

    private void onItemsChanged() {
        inflateItemViews();
        bindItemViews();
        scrollToSelectedItem(false);
    }

    private void onSelectedIndexChanged() {
        bindItemViews();
        scrollToSelectedItem(true);
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(mSelectedIndex);
        }
    }

    private void scrollToSelectedItem(boolean smooth) {
        if (!isLaidOut()) {
            ViewUtils.removeOnPreDraw(this, mScrollToSelectedItemRunnable);
            ViewUtils.postOnPreDraw(this, mScrollToSelectedItemRunnable);
            return;
        }
        ViewUtils.removeOnPreDraw(this, mScrollToSelectedItemRunnable);
        smooth &= getVisibility() == VISIBLE;
        View selectedItemView = mItemsLayout.getChildAt(mSelectedIndex);
        int itemsPaddingStart = mItemsLayout.getPaddingStart();
        int scrollX = getLayoutDirection() == LAYOUT_DIRECTION_LTR ?
                selectedItemView.getLeft() - itemsPaddingStart
                : selectedItemView.getRight() - getWidth() + itemsPaddingStart;
        if (smooth) {
            smoothScrollTo(scrollX, 0);
        } else {
            scrollTo(scrollX, 0);
        }
    }

    private void inflateItemViews() {
        for (int i = mItemsLayout.getChildCount(), size = mItems.size(); i >= size; --i) {
            mItemsLayout.removeViewAt(i);
        }
        for (int i = mItemsLayout.getChildCount(), size = mItems.size(); i < size; ++i) {
            View itemView = ViewUtils.inflate(R.layout.breadcrumb_item, mItemsLayout);
            ViewHolder holder = new ViewHolder(itemView);
            holder.text.setTextColor(mItemColor);
            holder.arrowImage.setImageTintList(mItemColor);
            itemView.setTag(holder);
            mItemsLayout.addView(itemView);
        }
    }

    private void bindItemViews() {
        for (int i = 0, size = mItems.size(), last = size - 1; i < size; ++i) {
            String item = mItems.get(i);
            ViewHolder holder = (ViewHolder) mItemsLayout.getChildAt(i).getTag();
            holder.itemView.setActivated(i == mSelectedIndex);
            int index = i;
            holder.itemView.setOnClickListener(view -> {
                if (mSelectedIndex == index) {
                    scrollToSelectedItem(true);
                    return;
                }
                setSelectedIndex(index);
            });
            holder.text.setText(item);
            ViewUtils.setVisibleOrGone(holder.arrowImage, i != last);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int index);
    }

    static class ViewHolder {

        public View itemView;

        @BindView(R.id.text)
        public TextView text;
        @BindView(R.id.arrow)
        public ImageView arrowImage;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
