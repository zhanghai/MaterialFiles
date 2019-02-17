/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.filelist.FileListAdapter;
import me.zhanghai.android.files.glide.GlideApp;
import me.zhanghai.android.files.glide.IgnoreErrorDrawableImageViewTarget;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.util.BundleBuilder;
import me.zhanghai.android.files.util.RemoteCallback;
import me.zhanghai.android.files.util.StringCompat;
import me.zhanghai.android.files.util.ViewUtils;

public class FileJobConflictDialogFragment extends DialogFragment {

    private static final String KEY_PREFIX = FileJobConflictDialogFragment.class.getName() + '.';

    private static final String EXTRA_SOURCE_FILE = KEY_PREFIX + "SOURCE_FILE";
    private static final String EXTRA_TARGET_FILE = KEY_PREFIX + "TARGET_FILE";
    private static final String EXTRA_COPY = KEY_PREFIX + "COPY";
    private static final String EXTRA_LISTENER = KEY_PREFIX + "LISTENER";

    private static final String STATE_ALL_CHECKED = KEY_PREFIX + "ALL_CHECKED";

    @NonNull
    private FileItem mSourceFile;
    @NonNull
    private FileItem mTargetFile;
    private boolean mCopy;
    @Nullable
    private RemoteCallback mListener;

    @BindView(R.id.target_icon)
    ImageView mTargetIconImage;
    @BindView(R.id.target_badge)
    ImageView mTargetBadgeImage;
    @BindView(R.id.target_description)
    TextView mTargetDescriptionText;
    @BindView(R.id.source_icon)
    ImageView mSourceIconImage;
    @BindView(R.id.source_badge)
    ImageView mSourceBadgeImage;
    @BindView(R.id.source_description)
    TextView mSourceDescriptionText;
    @BindView(R.id.all)
    CheckBox mAllCheck;

    public static void putArguments(@NonNull Intent intent, @NonNull FileItem sourceFile,
                                    @NonNull FileItem targetFile, boolean copy,
                                    @NonNull Listener listener) {
        intent
                .putExtra(EXTRA_SOURCE_FILE, sourceFile)
                .putExtra(EXTRA_TARGET_FILE, targetFile)
                .putExtra(EXTRA_COPY, copy)
                .putExtra(EXTRA_LISTENER, new RemoteCallback(new ListenerAdapter(listener)));
    }

    @NonNull
    public static FileJobConflictDialogFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        FileJobConflictDialogFragment fragment = new FileJobConflictDialogFragment();
        fragment.setArguments(intent.getExtras());
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public FileJobConflictDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mSourceFile = arguments.getParcelable(EXTRA_SOURCE_FILE);
        mTargetFile = arguments.getParcelable(EXTRA_TARGET_FILE);
        mCopy = arguments.getBoolean(EXTRA_COPY);
        mListener = arguments.getParcelable(EXTRA_LISTENER);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ALL_CHECKED, mAllCheck.isChecked());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        int theme = getTheme();
        boolean sourceIsDirectory = mSourceFile.getAttributesNoFollowLinks().isDirectory();
        boolean targetIsDirectory = mTargetFile.getAttributesNoFollowLinks().isDirectory();
        int titleRes;
        int messageRes;
        if (sourceIsDirectory && targetIsDirectory) {
            titleRes = R.string.file_job_merge_title_format;
            messageRes = mCopy ? R.string.file_job_merge_copy_message_format
                    : R.string.file_job_merge_move_message_format;
        } else {
            titleRes = R.string.file_job_replace_title_format;
            messageRes = R.string.file_job_replace_message_format;
        }
        String title = context.getString(titleRes, mTargetFile.getPath().getFileName());
        String message = context.getString(messageRes,
                mTargetFile.getPath().getParent().getFileName());
        View view = ViewUtils.inflateWithTheme(R.layout.file_job_conflict_dialog_view, context,
                theme);
        ButterKnife.bind(this, view);
        bindFileItem(mTargetFile, mTargetIconImage, mTargetBadgeImage, mTargetDescriptionText);
        bindFileItem(mSourceFile, mSourceIconImage, mSourceBadgeImage, mSourceDescriptionText);
        if (savedInstanceState != null) {
            mAllCheck.setChecked(savedInstanceState.getBoolean(STATE_ALL_CHECKED));
        }

        AlertDialog dialog = new AlertDialog.Builder(context, theme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.file_job_action_replace, this::onDialogButtonClick)
                .setNegativeButton(R.string.file_job_action_skip, this::onDialogButtonClick)
                .setNeutralButton(android.R.string.cancel, this::onDialogButtonClick)
                .setView(view)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * @see me.zhanghai.android.files.filelist.FileListAdapter#onBindViewHolder(
     *      FileListAdapter.ViewHolder, int, List)
     */
    private void bindFileItem(@NonNull FileItem file, @NonNull ImageView iconImage,
                              @NonNull ImageView badgeImage, @NonNull TextView descriptionText) {
        String mimeType = file.getMimeType();
        Drawable icon = AppCompatResources.getDrawable(iconImage.getContext(), MimeTypes.getIconRes(
                mimeType));
        BasicFileAttributes attributes = file.getAttributes();
        Path path = file.getPath();
        if (LinuxFileSystemProvider.isLinuxPath(path) && MimeTypes.supportsThumbnail(mimeType)) {
            GlideApp.with(this)
                    .load(path.toFile())
                    .signature(new ObjectKey(attributes.lastModifiedTime()))
                    .placeholder(icon)
                    .into(new IgnoreErrorDrawableImageViewTarget(iconImage));
        } else {
            GlideApp.with(this)
                    .clear(iconImage);
            iconImage.setImageDrawable(icon);
        }
        Integer badgeIconRes;
        if (file.getAttributesNoFollowLinks().isSymbolicLink()) {
            badgeIconRes = file.isSymbolicLinkBroken() ? R.drawable.error_badge_icon_18dp
                    : R.drawable.symbolic_link_badge_icon_18dp;
        } else {
            badgeIconRes = null;
        }
        boolean hasBadge = badgeIconRes != null;
        ViewUtils.setVisibleOrGone(badgeImage, hasBadge);
        if (hasBadge) {
            badgeImage.setImageResource(badgeIconRes);
        }
        Context descriptionContext = descriptionText.getContext();
        String descriptionSeparator = descriptionContext.getString(
                R.string.file_item_description_separator);
        String lastModificationTime = FormatUtils.formatShortTime(
                attributes.lastModifiedTime().toInstant(), descriptionContext);
        String size = FormatUtils.formatHumanReadableSize(attributes.size(), descriptionContext);
        String description = StringCompat.join(descriptionSeparator, lastModificationTime, size);
        descriptionText.setText(description);
    }

    private void onDialogButtonClick(@NonNull DialogInterface dialog, int which) {
        Action action;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                action = Action.REPLACE_OR_MERGE;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                action = Action.SKIP;
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                action = Action.CANCEL;
                break;
            default:
                throw new IllegalArgumentException(Integer.toString(which));
        }
        sendResult(action, mAllCheck.isChecked());
        requireActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        requireDialog().findViewById(R.id.customPanel).setMinimumHeight(0);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        sendResult(Action.CANCELED, false);
        requireActivity().finish();
    }

    public void onFinish() {
        sendResult(Action.CANCELED, false);
    }

    private void sendResult(@NonNull Action action, boolean all) {
        if (mListener != null) {
            ListenerAdapter.sendResult(mListener, action, all);
            mListener = null;
        }
    }

    public enum Action {
        REPLACE_OR_MERGE,
        SKIP,
        CANCEL,
        CANCELED
    }

    public interface Listener {

        void onAction(@NonNull Action action, boolean all);
    }

    private static class ListenerAdapter implements RemoteCallback.Listener {

        private static final String KEY_PREFIX = Listener.class.getName() + '.';

        private static final String RESULT_ACTION = KEY_PREFIX + "ACTION";
        private static final String RESULT_ALL = KEY_PREFIX + "ALL";

        @NonNull
        private final Listener mListener;

        public ListenerAdapter(@NonNull Listener listener) {
            mListener = listener;
        }

        public static void sendResult(@NonNull RemoteCallback listener, @NonNull Action action,
                                      boolean all) {
            listener.sendResult(new BundleBuilder()
                    .putSerializable(RESULT_ACTION, action)
                    .putBoolean(RESULT_ALL, all)
                    .build());
        }

        @Override
        public void onResult(Bundle result) {
            Action action = (Action) result.getSerializable(RESULT_ACTION);
            boolean all = result.getBoolean(RESULT_ALL);
            mListener.onAction(action, all);
        }
    }
}
