/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.image

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.DefaultOnImageEventListener
import java8.nio.file.Path
import me.zhanghai.android.files.databinding.ImageViewerItemBinding
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.fileProviderUri
import me.zhanghai.android.files.glide.GlideApp
import me.zhanghai.android.files.glide.ImageInfo
import me.zhanghai.android.files.ui.ViewPagerAdapter
import me.zhanghai.android.files.util.fadeInUnsafe
import me.zhanghai.android.files.util.fadeOutUnsafe
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.shortAnimTime
import kotlin.math.max

class ImageViewerAdapter(private val listener: (View) -> Unit) : ViewPagerAdapter() {
    private val paths = mutableListOf<Path>()

    fun replace(paths: List<Path>) {
        this.paths.clear()
        this.paths.addAll(paths)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = paths.size

    public override fun onCreateView(container: ViewGroup, position: Int): View {
        val binding = ImageViewerItemBinding.inflate(
            container.context.layoutInflater, container, false
        )
        val path = paths[position]
        binding.root.tag = binding to path
        binding.image.setOnPhotoTapListener { view, _, _ -> listener(view) }
        binding.largeImage.setOnClickListener(listener)
        container.addView(binding.root)
        loadImage(binding, path)
        return binding.root
    }

    public override fun onDestroyView(container: ViewGroup, position: Int, view: View) {
        @Suppress("UNCHECKED_CAST")
        val tag = view.tag as Pair<ImageViewerItemBinding, Path>
        val (binding) = tag
        GlideApp.with(binding.image).clear(binding.image)
        container.removeView(view)
    }

    public override fun getViewPosition(view: View): Int {
        @Suppress("UNCHECKED_CAST")
        val tag = view.tag as Pair<ImageViewerItemBinding, Path>
        val (_, path) = tag
        val index = paths.indexOf(path)
        return if (index != -1) index else POSITION_NONE
    }

    private fun loadImage(binding: ImageViewerItemBinding, path: Path) {
        binding.progress.fadeInUnsafe()
        GlideApp
            .with(binding.progress)
            .asImageInfo()
            .load(path)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .addListener(object : RequestListener<ImageInfo> {
                override fun onResourceReady(
                    resource: ImageInfo,
                    model: Any,
                    target: Target<ImageInfo>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onLoadFailed(
                    e: GlideException?, model: Any,
                    target: Target<ImageInfo>,
                    isFirstResource: Boolean
                ): Boolean {
                    showError(binding, e)
                    return false
                }
            })
            .into(object : CustomTarget<ImageInfo>() {
                override fun onResourceReady(
                    imageInfo: ImageInfo,
                    transition: Transition<in ImageInfo>?
                ) {
                    loadImageWithInfo(binding, path, imageInfo)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun loadImageWithInfo(
        binding: ImageViewerItemBinding,
        path: Path,
        imageInfo: ImageInfo
    ) {
        if (!shouldUseLargeImageView(imageInfo)) {
            // Otherwise SizeReadyCallback.onSizeReady() is never called.
            binding.image.isVisible = true
            GlideApp.with(binding.image)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontTransform()
                .placeholder(android.R.color.transparent)
                .transition(
                    DrawableTransitionOptions.withCrossFade(binding.image.context.shortAnimTime)
                )
                .addListener(object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        drawable: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progress.fadeOutUnsafe()
                        binding.image.isVisible = true
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        showError(binding, e)
                        return false
                    }
                })
                .into(binding.image)
        } else {
            binding.largeImage.setDoubleTapZoomDuration(300)
            binding.largeImage.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
            // Otherwise OnImageEventListener.onReady() is never called.
            binding.largeImage.isVisible = true
            binding.largeImage.alpha = 0f
            binding.largeImage.setOnImageEventListener(object : DefaultOnImageEventListener() {
                override fun onReady() {
                    val viewWidth = (binding.largeImage.width - binding.largeImage.paddingLeft
                        - binding.largeImage.paddingRight)
                    val viewHeight = (binding.largeImage.height - binding.largeImage.paddingTop
                        - binding.largeImage.paddingBottom)
                    val orientation = binding.largeImage.appliedOrientation
                    val rotated90Or270 = (orientation == SubsamplingScaleImageView.ORIENTATION_90
                        || orientation == SubsamplingScaleImageView.ORIENTATION_270)
                    val imageWidth = if (rotated90Or270) {
                        binding.largeImage.sHeight
                    } else {
                        binding.largeImage.sWidth
                    }
                    val imageHeight = if (rotated90Or270) {
                        binding.largeImage.sWidth
                    } else {
                        binding.largeImage.sHeight
                    }
                    val cropScale = max(
                        viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight
                    )
                    binding.largeImage.setDoubleTapZoomScale(cropScale)
                    binding.progress.fadeOutUnsafe()
                    binding.largeImage.fadeInUnsafe()
                }

                override fun onImageLoadError(e: Exception) {
                    e.printStackTrace()
                    showError(binding, e)
                }
            })
            binding.largeImage.setImageRestoringSavedState(ImageSource.uri(path.fileProviderUri))
        }
    }

    private fun shouldUseLargeImageView(imageInfo: ImageInfo): Boolean {
        // See BitmapFactory.cpp encodedFormatToString()
        if (imageInfo.mimeType == MimeType.IMAGE_GIF) {
            return false
        }
        if (imageInfo.width <= 0 || imageInfo.height <= 0) {
            return false
        }
        if (imageInfo.width > 2048 || imageInfo.height > 2048) {
            val ratio = imageInfo.width.toFloat() / imageInfo.height
            if (ratio < 0.5 || ratio > 2) {
                return true
            }
        }
        return false
    }

    private fun showError(binding: ImageViewerItemBinding, e: Exception?) {
        val e = e ?: GlideException(null)
        binding.errorText.text = e.toString()
        binding.progress.fadeOutUnsafe()
        binding.errorText.fadeInUnsafe()
    }
}
