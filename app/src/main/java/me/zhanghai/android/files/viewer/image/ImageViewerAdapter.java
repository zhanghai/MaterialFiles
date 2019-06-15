/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.image;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.glide.GlideApp;
import me.zhanghai.android.files.glide.ImageInfo;
import me.zhanghai.android.files.ui.SaveStateSubsamplingScaleImageView;
import me.zhanghai.android.files.ui.ViewStatePagerAdapter;
import me.zhanghai.android.files.util.ViewUtils;

public class ImageViewerAdapter extends ViewStatePagerAdapter {

    @NonNull
    private final List<Path> mPaths;
    @NonNull
    private final View.OnClickListener mListener;

    public ImageViewerAdapter(@NonNull List<Path> paths, @NonNull View.OnClickListener listener) {
        mPaths = paths;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull ViewGroup container, int position) {
        View view = ViewUtils.inflate(R.layout.image_viewer_item, container);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        holder.image.setOnPhotoTapListener((view2, x, y) -> mListener.onClick(view2));
        holder.largeImage.setOnClickListener(mListener);
        Path path = mPaths.get(position);
        container.addView(view);
        loadImage(holder, path);
        return view;
    }

    private static void loadImage(@NonNull ViewHolder holder, @NonNull Path path) {
        ViewUtils.fadeIn(holder.progress);
        GlideApp
                .with(holder.progress)
                .asImageInfo()
                .load(path)
                .addListener(new RequestListener<ImageInfo>() {
                    @Override
                    public boolean onResourceReady(@NonNull ImageInfo resource,
                                                   @NonNull Object model,
                                                   @NonNull Target<ImageInfo> target,
                                                   @NonNull DataSource dataSource,
                                                   boolean isFirstResource) {
                        return false;
                    }
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @NonNull Object model,
                                                @NonNull Target<ImageInfo> target,
                                                boolean isFirstResource) {
                        showError(holder, e);
                        return false;
                    }
                })
                .into(new CustomTarget<ImageInfo>() {
                    @Override
                    public void onResourceReady(
                            @NonNull ImageInfo imageInfo,
                            @Nullable Transition<? super ImageInfo> transition) {
                        loadImageWithInfo(holder, path, imageInfo);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private static void loadImageWithInfo(@NonNull ViewHolder holder, @NonNull Path path,
                                          @NonNull ImageInfo imageInfo) {
        if (!shouldUseLargeImageView(imageInfo)) {
            // Otherwise SizeReadyCallback.onSizeReady() is never called.
            ViewUtils.setVisibleOrGone(holder.image, true);
            GlideApp.with(holder.image)
                    .load(path)
                    .dontTransform()
                    .placeholder(android.R.color.transparent)
                    .transition(DrawableTransitionOptions.withCrossFade(ViewUtils.getShortAnimTime(
                            holder.image)))
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(@NonNull Drawable drawable,
                                                       @NonNull Object model,
                                                       @NonNull Target<Drawable> target,
                                                       @NonNull DataSource dataSource,
                                                       boolean isFirstResource) {
                            ViewUtils.fadeOut(holder.progress);
                            ViewUtils.setVisibleOrGone(holder.image, true);
                            return false;
                        }
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e,
                                                    @NonNull Object model,
                                                    @NonNull Target<Drawable> target,
                                                    boolean isFirstResource) {
                            showError(holder, e);
                            return false;
                        }
                    })
                    .into(holder.image);
        } else {
            holder.largeImage.setDoubleTapZoomDuration(300);
            holder.largeImage.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
            // Otherwise OnImageEventListener.onReady() is never called.
            ViewUtils.setVisibleOrGone(holder.largeImage, true);
            holder.largeImage.setAlpha(0);
            holder.largeImage.setOnImageEventListener(
                    new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                        @Override
                        public void onReady() {
                            int viewWidth = holder.largeImage.getWidth()
                                    - holder.largeImage.getPaddingLeft()
                                    - holder.largeImage.getPaddingRight();
                            int viewHeight = holder.largeImage.getHeight()
                                    - holder.largeImage.getPaddingTop()
                                    - holder.largeImage.getPaddingBottom();
                            int orientation = holder.largeImage.getAppliedOrientation();
                            boolean rotated90Or270 =
                                    orientation == SubsamplingScaleImageView.ORIENTATION_90
                                            || orientation
                                            == SubsamplingScaleImageView.ORIENTATION_270;
                            int imageWidth = rotated90Or270 ? holder.largeImage.getSHeight()
                                    : holder.largeImage.getSWidth();
                            int imageHeight = rotated90Or270 ? holder.largeImage.getSWidth()
                                    : holder.largeImage.getSHeight();
                            float cropScale = Math.max((float) viewWidth / imageWidth,
                                    (float) viewHeight / imageHeight);
                            holder.largeImage.setDoubleTapZoomScale(cropScale);
                            ViewUtils.crossfade(holder.progress, holder.largeImage);
                        }
                        @Override
                        public void onImageLoadError(Exception e) {
                            e.printStackTrace();
                            showError(holder, e);
                        }
                    });
            holder.largeImage.setImageRestoringSavedState(ImageSource.uri(
                    FileProvider.getUriForPath(path)));
        }
    }

    private static boolean shouldUseLargeImageView(@NonNull ImageInfo imageInfo) {
        // See BitmapFactory.cpp encodedFormatToString()
        if (Objects.equals(imageInfo.mimeType, "image/gif")) {
            return false;
        }
        if (imageInfo.width <= 0 || imageInfo.height <= 0) {
            return false;
        }
        if (imageInfo.width > 2048 || imageInfo.height > 2048) {
            float ratio = (float) imageInfo.width / imageInfo.height;
            if (ratio < 0.5 || ratio > 2) {
                return true;
            }
        }
        return false;
    }

    private static void showError(@NonNull ViewHolder holder, @Nullable Exception e) {
        if (e == null) {
            e = new GlideException(null);
        }
        holder.errorText.setText(e.getLocalizedMessage());
        ViewUtils.crossfade(holder.progress, holder.errorText);
    }

    @Override
    public void onDestroyView(@NonNull ViewGroup container, int position, @NonNull View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        GlideApp.with(holder.image).clear(holder.image);
        container.removeView(view);
    }

    static class ViewHolder {

        @BindView(R.id.image)
        public PhotoView image;
        @BindView(R.id.large_image)
        public SaveStateSubsamplingScaleImageView largeImage;
        @BindView(R.id.error)
        public TextView errorText;
        @BindView(R.id.progress)
        public ProgressBar progress;

        public ViewHolder(@NonNull View view) {
            ButterKnife.bind(this, view);
        }
    }
}
