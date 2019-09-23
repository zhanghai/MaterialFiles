/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class BreadcrumbLayout extends HorizontalScrollView {

    @BindDimen(R.dimen.tab_layout_height)
    int mTabLayoutHeight;

    @NonNull
    private ColorStateList mItemColor;

    @NonNull
    private Context mPopupContext;

    @NonNull
    private LinearLayout mItemsLayout;

    @Nullable
    private Listener mListener;

    @NonNull
    private BreadcrumbData mData;

    private boolean mIsLayoutDirty = true;
    private boolean mScrollToSelectedItem;

    private boolean mIsFirstScroll = true;

    public BreadcrumbLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public BreadcrumbLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public BreadcrumbLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public BreadcrumbLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr, int defStyleRes) {
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
        int popupTheme = ViewUtils.getResIdFromAttrRes(R.attr.actionBarPopupTheme, 0, context);
        mPopupContext = popupTheme != 0 ? new ContextThemeWrapper(context, popupTheme) : context;
        mItemsLayout = new LinearLayout(context);
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mIsLayoutDirty = false;
        if (mScrollToSelectedItem) {
            scrollToSelectedItem();
            mScrollToSelectedItem = false;
        }
    }

    public void setListener(@NonNull Listener listener) {
        mListener = listener;
    }

    public void setData(@NonNull BreadcrumbData data) {
        if (Objects.equals(mData, data)) {
            return;
        }
        mData = data;
        inflateItemViews();
        bindItemViews();
        scrollToSelectedItem();
    }

    private void scrollToSelectedItem() {
        if (mIsLayoutDirty) {
            mScrollToSelectedItem = true;
            return;
        }
        View selectedItemView = mItemsLayout.getChildAt(mData.selectedIndex);
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
        // HACK: Remove/add views at the front so that ripple remains correct, as we are potentially
        // collapsing/expanding breadcrumbs at the front.
        for (int i = mData.paths.size(), count = mItemsLayout.getChildCount(); i < count; ++i) {
            mItemsLayout.removeViewAt(0);
        }
        for (int i = mItemsLayout.getChildCount(), size = mData.paths.size(); i < size; ++i) {
            View itemView = ViewUtils.inflate(R.layout.breadcrumb_item, mItemsLayout);
            ViewHolder holder = new ViewHolder(itemView);
            holder.menu = new PopupMenu(mPopupContext, holder.itemView);
            holder.menu.inflate(R.menu.file_list_breadcrumb);
            holder.itemView.setOnLongClickListener(view -> {
                holder.menu.show();
                return true;
            });
            holder.text.setTextColor(mItemColor);
            holder.arrowImage.setImageTintList(mItemColor);
            itemView.setTag(holder);
            mItemsLayout.addView(itemView, 0);
        }
    }

    private void bindItemViews() {
        for (int i = 0, size = mData.paths.size(), last = size - 1; i < size; ++i) {
            ViewHolder holder = (ViewHolder) mItemsLayout.getChildAt(i).getTag();
            holder.itemView.setActivated(i == mData.selectedIndex);
            int index = i;
            Path path = mData.paths.get(i);
            holder.itemView.setOnClickListener(view -> {
                if (mData.selectedIndex == index) {
                    scrollToSelectedItem();
                    return;
                }
                if (mListener != null) {
                    mListener.navigateTo(path);
                }
            });
            String name = mData.names.get(i).apply(holder.text.getContext());
            holder.text.setText(name);
            ViewUtils.setVisibleOrGone(holder.arrowImage, i != last);
            holder.menu.setOnMenuItemClickListener(menuItem -> {
                if (mListener == null) {
                    return false;
                }
                switch (menuItem.getItemId()) {
                    case R.id.action_copy_path: {
                        mListener.copyPath(path);
                        return true;
                    }
                    case R.id.action_open_in_new_task:
                        mListener.openInNewTask(path);
                        return true;
                    default:
                        return false;
                }
            });
        }
    }

    public interface Listener {
        void navigateTo(@NonNull Path path);
        void copyPath(@NonNull Path path);
        void openInNewTask(@NonNull Path path);
    }

    static class ViewHolder {

        @NonNull
        public View itemView;

        @BindView(R.id.text)
        public TextView text;
        @BindView(R.id.arrow)
        public ImageView arrowImage;

        @NonNull
        public PopupMenu menu;

        public ViewHolder(@NonNull View itemView) {
            this.itemView = itemView;

            ButterKnife.bind(this, itemView);
        }
    }
}
