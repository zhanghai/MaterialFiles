/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.glide.GlideApp;
import me.zhanghai.android.files.glide.IgnoreErrorDrawableImageViewTarget;
import me.zhanghai.android.files.settings.SettingsLiveDatas;
import me.zhanghai.android.files.ui.AnimatedSortedListAdapter;
import me.zhanghai.android.files.ui.CheckableFrameLayout;
import me.zhanghai.android.files.util.StringCompat;
import me.zhanghai.android.files.util.ViewUtils;

public class FileListAdapter extends AnimatedSortedListAdapter<FileItem, FileListAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private static final Object PAYLOAD_SELECTED_CHANGED = new Object();

    @NonNull
    private Comparator<FileItem> mComparator;
    @NonNull
    private final SortedList.Callback<FileItem> mCallback =
            new SortedListAdapterCallback<FileItem>(this) {
                @Override
                public int compare(FileItem file1, FileItem file2) {
                    return mComparator.compare(file1, file2);
                }
                @Override
                public boolean areItemsTheSame(FileItem oldItem, FileItem newItem) {
                    return Objects.equals(oldItem, newItem);
                }
                @Override
                public boolean areContentsTheSame(FileItem oldItem, FileItem newItem) {
                    return FileItem.contentEquals(oldItem, newItem);
                }
            };

    @NonNull
    private final Set<FileItem> mSelectedFiles = new HashSet<>();
    @NonNull
    private final Map<FileItem, Integer> mFilePositionMap = new HashMap<>();

    @NonNull
    private FilePasteMode mPasteMode;

    @NonNull
    private Fragment mFragment;
    @NonNull
    private Listener mListener;

    public FileListAdapter(@NonNull Fragment fragment, @NonNull Listener listener) {
        init(FileItem.class, mCallback);

        mFragment = fragment;
        mListener = listener;
    }

    public void setComparator(@NonNull Comparator<FileItem> comparator) {
        mComparator = comparator;
        refresh();
        rebuildFilePositionMap();
    }

    public void replaceSelectedFiles(@NonNull Set<FileItem> files) {
        Set<FileItem> changedFiles = new HashSet<>();
        for (Iterator<FileItem> iterator = mSelectedFiles.iterator(); iterator.hasNext(); ) {
            FileItem file = iterator.next();
            if (!files.contains(file)) {
                iterator.remove();
                changedFiles.add(file);
            }
        }
        for (FileItem file : files) {
            if (!mSelectedFiles.contains(file)) {
                mSelectedFiles.add(file);
                changedFiles.add(file);
            }
        }
        for (FileItem file : changedFiles) {
            Integer position = mFilePositionMap.get(file);
            if (position != null) {
                notifyItemChanged(position, PAYLOAD_SELECTED_CHANGED);
            }
        }
    }

    @Override
    public void clear() {
        super.clear();

        rebuildFilePositionMap();
    }

    @Override
    public void replace(@NonNull List<FileItem> list) {
        super.replace(list);

        rebuildFilePositionMap();
    }

    private void rebuildFilePositionMap() {
        mFilePositionMap.clear();
        for (int i = 0, count = getItemCount(); i < count; ++i) {
            FileItem file = getItem(i);
            mFilePositionMap.put(file, i);
        }
    }

    public void setPasteMode(@NonNull FilePasteMode pasteMode) {
        mPasteMode = pasteMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(ViewUtils.inflate(R.layout.file_item, parent));
        holder.itemLayout.setBackground(AppCompatResources.getDrawable(
                holder.itemLayout.getContext(), R.drawable.checkable_item_background));
        holder.menu = new PopupMenu(holder.menuButton.getContext(), holder.menuButton);
        holder.menu.inflate(R.menu.file_item);
        holder.menuButton.setOnClickListener(view -> holder.menu.show());
        holder.menuButton.setOnTouchListener(holder.menu.getDragToOpenListener());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        FileItem file = getItem(position);
        holder.itemLayout.setChecked(mSelectedFiles.contains(file));
        if (!payloads.isEmpty()) {
            return;
        }
        bindViewHolderAnimation(holder);
        holder.itemView.setOnClickListener(view -> {
            if (mSelectedFiles.isEmpty() || mPasteMode != FilePasteMode.NONE) {
                mListener.openFile(file);
            } else {
                mListener.selectFile(file, !mSelectedFiles.contains(file));
            }
        });
        holder.itemLayout.setOnLongClickListener(view -> {
            if (mSelectedFiles.isEmpty() || mPasteMode != FilePasteMode.NONE) {
                mListener.selectFile(file, !mSelectedFiles.contains(file));
            } else {
                mListener.openFile(file);
            }
            return true;
        });
        String mimeType = file.getMimeType();
        Drawable icon = AppCompatResources.getDrawable(holder.iconImage.getContext(),
                MimeTypes.getIconRes(mimeType));
        BasicFileAttributes attributes = file.getAttributes();
        if (MimeTypes.supportsThumbnail(mimeType)) {
            GlideApp.with(mFragment)
                    .load(file.getPath())
                    .signature(new ObjectKey(attributes.lastModifiedTime()))
                    .placeholder(icon)
                    .into(new IgnoreErrorDrawableImageViewTarget(holder.iconImage));
        } else {
            GlideApp.with(mFragment)
                    .clear(holder.iconImage);
            holder.iconImage.setImageDrawable(icon);
        }
        holder.iconImage.setOnClickListener(view -> mListener.selectFile(file,
                !mSelectedFiles.contains(file)));
        Integer badgeIconRes;
        if (file.getAttributesNoFollowLinks().isSymbolicLink()) {
            badgeIconRes = file.isSymbolicLinkBroken() ? R.drawable.error_badge_icon_18dp
                    : R.drawable.symbolic_link_badge_icon_18dp;
        } else {
            badgeIconRes = null;
        }
        boolean hasBadge = badgeIconRes != null;
        ViewUtils.setVisibleOrGone(holder.badgeImage, hasBadge);
        if (hasBadge) {
            holder.badgeImage.setImageResource(badgeIconRes);
        }
        holder.nameText.setText(FileUtils.getName(file));
        String description;
        if (file.getAttributes().isDirectory()) {
            description = null;
        } else {
            Context context = holder.descriptionText.getContext();
            String descriptionSeparator = context.getString(
                    R.string.file_item_description_separator);
            String lastModificationTime = FormatUtils.formatShortTime(
                    attributes.lastModifiedTime().toInstant(), context);
            String size = FormatUtils.formatHumanReadableSize(attributes.size(), context);
            description = StringCompat.join(descriptionSeparator, lastModificationTime, size);
        }
        holder.descriptionText.setText(description);
        holder.menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_open_as:
                    mListener.showOpenFileAsDialog(file);
                    return true;
                case R.id.action_cut:
                    mListener.cutFile(file);
                    return true;
                case R.id.action_copy:
                    mListener.copyFile(file);
                    return true;
                case R.id.action_delete:
                    mListener.confirmDeleteFile(file);
                    return true;
                case R.id.action_rename:
                    mListener.showRenameFileDialog(file);
                    return true;
                case R.id.action_send:
                    mListener.sendFile(file);
                    return true;
                case R.id.action_copy_path:
                    mListener.copyPath(file);
                    return true;
                case R.id.action_properties:
                    mListener.showPropertiesDialog(file);
                    return true;
                default:
                    return false;
            }
        });
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        FileItem file = getItem(position);
        String name = FileUtils.getName(file);
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        return name.substring(0, 1).toUpperCase();
    }

    @Override
    protected boolean isAnimationEnabled() {
        return SettingsLiveDatas.FILE_LIST_ANIMATION.getValue();
    }

    public interface Listener {
        void selectFile(@NonNull FileItem file, boolean selected);
        void openFile(@NonNull FileItem file);
        void showOpenFileAsDialog(@NonNull FileItem file);
        void cutFile(@NonNull FileItem file);
        void copyFile(@NonNull FileItem file);
        void confirmDeleteFile(@NonNull FileItem file);
        void showRenameFileDialog(@NonNull FileItem file);
        void sendFile(@NonNull FileItem file);
        void copyPath(@NonNull FileItem file);
        void showPropertiesDialog(@NonNull FileItem file);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item)
        public CheckableFrameLayout itemLayout;
        @BindView(R.id.icon)
        public ImageView iconImage;
        @BindView(R.id.badge)
        public ImageView badgeImage;
        @BindView(R.id.name)
        public TextView nameText;
        @BindView(R.id.description)
        public TextView descriptionText;
        @BindView(R.id.menu)
        public ImageButton menuButton;

        @NonNull
        public PopupMenu menu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
