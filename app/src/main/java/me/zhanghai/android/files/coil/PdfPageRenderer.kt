/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.drawable.toDrawable
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.DecodeUtils
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Parameters
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Size
import kotlin.math.roundToInt

fun ImageRequest.Builder.pdfBackgroundColor(@ColorInt backgroundColor: Int): ImageRequest.Builder {
    return setParameter(PdfPageFetcher.PDF_BACKGROUND_COLOR_KEY, backgroundColor)
}

fun ImageRequest.Builder.pdfPageIndex(@IntRange(from = 0) pageIndex: Int): ImageRequest.Builder {
    require(pageIndex >= 0) { "pageIndex must be >= 0." }
    return setParameter(PdfPageFetcher.PDF_PAGE_INDEX_KEY, pageIndex)
}

@ColorInt
fun Parameters.pdfBackgroundColor(): Int? = value(PdfPageFetcher.PDF_BACKGROUND_COLOR_KEY) as Int?

@IntRange(from = 0)
fun Parameters.pdfPageIndex(): Int? = value(PdfPageFetcher.PDF_PAGE_INDEX_KEY) as Int?

abstract class PdfPageFetcher<T : Any>(private val context: Context) : Fetcher<T> {
    protected abstract fun openParcelFileDescriptor(data: T): ParcelFileDescriptor

    override suspend fun fetch(
        pool: BitmapPool,
        data: T,
        size: Size,
        options: Options
    ): FetchResult {
        val pfd = openParcelFileDescriptor(data)
        PdfRenderer(pfd).use { renderer ->
            val pageIndex = options.parameters.pdfPageIndex() ?: 0
            renderer.openPage(pageIndex).use { page ->
                val srcWidth = page.width
                val srcHeight = page.height
                val dstSize = when (size) {
                    is PixelSize -> {
                        if (srcWidth > 0 && srcHeight > 0) {
                            val rawScale = DecodeUtils.computeSizeMultiplier(
                                srcWidth = srcWidth,
                                srcHeight = srcHeight,
                                dstWidth = size.width,
                                dstHeight = size.height,
                                scale = options.scale
                            )
                            val scale = if (options.allowInexactSize) {
                                rawScale.coerceAtMost(1.0)
                            } else {
                                rawScale
                            }
                            val width = (scale * srcWidth).roundToInt()
                            val height = (scale * srcHeight).roundToInt()
                            PixelSize(width, height)
                        } else {
                            OriginalSize
                        }
                    }
                    is OriginalSize -> OriginalSize
                }
                val bitmap = if (dstSize is PixelSize) {
                    pool.getDirty(dstSize.width, dstSize.height, Bitmap.Config.ARGB_8888)
                } else {
                    pool.getDirty(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
                }
                val backgroundColor = options.parameters.pdfBackgroundColor() ?: Color.WHITE
                bitmap.eraseColor(backgroundColor)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val isSampled = dstSize is PixelSize
                    && (dstSize.width < srcWidth || dstSize.height < srcHeight)
                return DrawableResult(
                    drawable = bitmap.toDrawable(context.resources),
                    isSampled = isSampled,
                    dataSource = DataSource.DISK
                )
            }
        }
    }

    companion object {
        const val PDF_BACKGROUND_COLOR_KEY = "coil#pdf_background_color"
        const val PDF_PAGE_INDEX_KEY = "coil#pdf_page_index"
    }
}
