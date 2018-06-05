/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

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

    private boolean mIsLayoutDirty = true;
    private boolean mScrollToSelectedItem;

    private boolean mIsFirstScroll = true;

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

    @Override
    public void requestLayout() {
        mIsLayoutDirty = true;
        super.requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        mIsLayoutDirty = false;
        if (mScrollToSelectedItem) {
            scrollToSelectedItem();
            mScrollToSelectedItem = false;
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public void setItems(List<String> items, int selectedIndex) {
        mItems.clear();
        mItems.addAll(items);
        mSelectedIndex = selectedIndex;
        inflateItemViews();
        bindItemViews();
        scrollToSelectedItem();
    }

    private void scrollToSelectedItem() {
        if (mIsLayoutDirty) {
            mScrollToSelectedItem = true;
            return;
        }
        View selectedItemView = mItemsLayout.getChildAt(mSelectedIndex);
        int itemsPaddingStart = mItemsLayout.getPaddingStart();
        int scrollX = getLayoutDirection() == LAYOUT_DIRECTION_LTR ?
                selectedItemView.getLeft() - itemsPaddingStart
                : selectedItemView.getRight() - getWidth() + itemsPaddingStart;
        if (!mIsFirstScroll && isShown()) {
            smoothScrollTo(scrollX, 0);
        } else {
            scrollTo(scrollX, 0);
        }
        mIsFirstScroll = false;
    }

    private void inflateItemViews() {
        for (int i = mItemsLayout.getChildCount() - 1, size = mItems.size(); i >= size; --i) {
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
                    scrollToSelectedItem();
                    return;
                }
                mSelectedIndex = index;
                bindItemViews();
                scrollToSelectedItem();
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(mSelectedIndex);
                }
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
