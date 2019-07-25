/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.Menu;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.StringCompat;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.glide.GlideApp;
import me.zhanghai.android.files.glide.IgnoreErrorDrawableImageViewTarget;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.document.DocumentFileAttributes;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.settings.SettingsLiveDatas;
import me.zhanghai.android.files.ui.AnimatedSortedListAdapter;
import me.zhanghai.android.files.ui.CheckableFrameLayout;
import me.zhanghai.android.files.util.ViewUtils;

public class FileListAdapter extends AnimatedSortedListAdapter<FileItem, FileListAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private static final Object PAYLOAD_STATE_CHANGED = new Object();

    private boolean mSearching;
    @NonNull
    private Comparator<FileItem> mComparator;
    @NonNull
    private final SortedList.Callback<FileItem> mCallback =
            new SortedListAdapterCallback<FileItem>(this) {
                @Override
                public int compare(FileItem file1, FileItem file2) {
                    if (mSearching) {
                        return ((SearchFileItem) file1).compareTo((SearchFileItem) file2);
                    } else {
                        return mComparator.compare(file1, file2);
                    }
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

    @Nullable
    private PickOptions mPickOptions;

    @NonNull
    private final LinkedHashSet<FileItem> mSelectedFiles = new LinkedHashSet<>();
    @NonNull
    private final Map<FileItem, Integer> mFilePositionMap = new HashMap<>();

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
        if (!mSearching) {
            refresh();
            rebuildFilePositionMap();
        }
    }

    public void setPickOptions(@Nullable PickOptions pickOptions) {
        mPickOptions = pickOptions;
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_STATE_CHANGED);
    }

    public void replaceSelectedFiles(@NonNull LinkedHashSet<FileItem> files) {
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
                notifyItemChanged(position, PAYLOAD_STATE_CHANGED);
            }
        }
    }

    private void selectFile(@NonNull FileItem file) {
        if (!isFileSelectable(file)) {
            return;
        }
        if (mPickOptions != null && !mPickOptions.allowMultiple) {
            mListener.clearSelectedFiles();
        }
        mListener.selectFile(file, !mSelectedFiles.contains(file));
    }

    public void selectAllFiles() {
        LinkedHashSet<FileItem> files = new LinkedHashSet<>();
        for (int i = 0, count = getItemCount(); i < count; ++i) {
            FileItem file = getItem(i);
            if (isFileSelectable(file)) {
                files.add(file);
            }
        }
        mListener.selectFiles(files, true);
    }

    private boolean isFileSelectable(@NonNull FileItem file) {
        if (mPickOptions != null) {
            return !file.getAttributes().isDirectory() && mPickOptions.mimeTypeMatches(
                    file.getMimeType());
        }
        return true;
    }

    @Override
    public void clear() {
        super.clear();

        rebuildFilePositionMap();
    }

    /**
     * @deprecated Use {@link #replace2(List, boolean)} instead.
     */
    @Override
    public void replace(@NonNull List<FileItem> list, boolean clear) {
        throw new UnsupportedOperationException();
    }

    public void replace2(@NonNull List<FileItem> list, boolean searching) {

        boolean clear = mSearching != searching;
        mSearching = searching;

        super.replace(list, clear);

        rebuildFilePositionMap();
    }

    private void rebuildFilePositionMap() {
        mFilePositionMap.clear();
        for (int i = 0, count = getItemCount(); i < count; ++i) {
            FileItem file = getItem(i);
            mFilePositionMap.put(file, i);
        }
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
        boolean isDirectory = file.getAttributes().isDirectory();
        boolean enabled = isFileSelectable(file) || isDirectory;
        holder.itemLayout.setEnabled(enabled);
        holder.iconLayout.setEnabled(enabled);
        holder.menuButton.setEnabled(enabled);
        Menu menu = holder.menu.getMenu();
        Path path = file.getPath();
        boolean hasPickOptions = mPickOptions != null;
        boolean isReadOnly = path.getFileSystem().isReadOnly();
        menu.findItem(R.id.action_cut).setVisible(!hasPickOptions && !isReadOnly);
        menu.findItem(R.id.action_copy).setVisible(!hasPickOptions);
        boolean checked = mSelectedFiles.contains(file);
        holder.itemLayout.setChecked(checked);
        if (!payloads.isEmpty()) {
            return;
        }
        bindViewHolderAnimation(holder);
        holder.itemLayout.setOnClickListener(view -> {
            if (mSelectedFiles.isEmpty()) {
                mListener.openFile(file);
            } else {
                selectFile(file);
            }
        });
        holder.itemLayout.setOnLongClickListener(view -> {
            if (mSelectedFiles.isEmpty()) {
                selectFile(file);
            } else {
                mListener.openFile(file);
            }
            return true;
        });
        holder.iconLayout.setOnClickListener(view -> selectFile(file));
        String mimeType = file.getMimeType();
        Drawable icon = AppCompatResources.getDrawable(holder.iconImage.getContext(),
                MimeTypes.getIconRes(mimeType));
        BasicFileAttributes attributes = file.getAttributes();
        if (supportsThumbnail(file)) {
            GlideApp.with(mFragment)
                    .load(path)
                    .signature(new ObjectKey(attributes.lastModifiedTime()))
                    .placeholder(icon)
                    .into(new IgnoreErrorDrawableImageViewTarget(holder.iconImage));
        } else {
            GlideApp.with(mFragment)
                    .clear(holder.iconImage);
            holder.iconImage.setImageDrawable(icon);
        }
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
        if (isDirectory) {
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
        boolean isArchivePath = ArchiveFileSystemProvider.isArchivePath(path);
        menu.findItem(R.id.action_copy).setTitle(isArchivePath ? R.string.file_item_action_extract
                : R.string.copy);
        menu.findItem(R.id.action_delete).setVisible(!isReadOnly);
        menu.findItem(R.id.action_rename).setVisible(!isReadOnly);
        boolean isArchiveFile = FileUtils.isArchiveFile(file);
        menu.findItem(R.id.action_extract).setVisible(isArchiveFile);
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
                case R.id.action_extract:
                    mListener.extractFile(file);
                    return true;
                case R.id.action_archive:
                    mListener.showCreateArchiveDialog(file);
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

    private static boolean supportsThumbnail(@NonNull FileItem file) {
        Path path = file.getPath();
        if (LinuxFileSystemProvider.isLinuxPath(path)) {
            return MimeTypes.supportsThumbnail(file.getMimeType());
        } else if (DocumentFileSystemProvider.isDocumentPath(path)) {
            DocumentFileAttributes attributes = (DocumentFileAttributes) file.getAttributes();
            return (attributes.getFlags() & DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL)
                    == DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
        } else {
            // TODO: Allow other providers as well - but might be resource consuming.
            return false;
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        FileItem file = getItem(position);
        String name = FileUtils.getName(file);
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        return name.substring(0, 1).toUpperCase(Locale.getDefault());
    }

    @Override
    protected boolean isAnimationEnabled() {
        return SettingsLiveDatas.FILE_LIST_ANIMATION.getValue();
    }

    public interface Listener {
        void clearSelectedFiles();
        void selectFile(@NonNull FileItem file, boolean selected);
        void selectFiles(@NonNull LinkedHashSet<FileItem> files, boolean selected);
        void openFile(@NonNull FileItem file);
        void showOpenFileAsDialog(@NonNull FileItem file);
        void cutFile(@NonNull FileItem file);
        void copyFile(@NonNull FileItem file);
        void confirmDeleteFile(@NonNull FileItem file);
        void showRenameFileDialog(@NonNull FileItem file);
        void extractFile(@NonNull FileItem file);
        void showCreateArchiveDialog(@NonNull FileItem file);
        void sendFile(@NonNull FileItem file);
        void copyPath(@NonNull FileItem file);
        void showPropertiesDialog(@NonNull FileItem file);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item)
        public CheckableFrameLayout itemLayout;
        @BindView(R.id.icon_layout)
        public ViewGroup iconLayout;
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
