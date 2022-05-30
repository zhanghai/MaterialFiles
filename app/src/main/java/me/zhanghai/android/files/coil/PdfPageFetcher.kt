/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.DecodeUtils
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Options
import coil.request.Parameters
import kotlin.math.roundToInt

fun ImageRequest.Builder.pdfBackgroundColor(@ColorInt backgroundColor: Int): ImageRequest.Builder =
    setParameter(PdfPageFetcher.PDF_BACKGROUND_COLOR_KEY, backgroundColor)

fun ImageRequest.Builder.pdfPageIndex(@IntRange(from = 0) pageIndex: Int): ImageRequest.Builder {
    require(pageIndex >= 0) { "pageIndex must be >= 0." }
    return setParameter(PdfPageFetcher.PDF_PAGE_INDEX_KEY, pageIndex)
}

@ColorInt
fun Parameters.pdfBackgroundColor(): Int? = value(PdfPageFetcher.PDF_BACKGROUND_COLOR_KEY) as Int?

@IntRange(from = 0)
fun Parameters.pdfPageIndex(): Int? = value(PdfPageFetcher.PDF_PAGE_INDEX_KEY) as Int?

class PdfPageFetcher(
    private val options: Options,
    private val openParcelFileDescriptor: () -> ParcelFileDescriptor
) : Fetcher {
    override suspend fun fetch(): FetchResult =
        openParcelFileDescriptor().use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                val pageIndex = options.parameters.pdfPageIndex() ?: 0
                renderer.openPage(pageIndex).use { page ->
                    val srcWidth = page.width
                    check(srcWidth > 0) {
                        "PDF page $pageIndex width $srcWidth isn't greater than 0"
                    }
                    val srcHeight = page.height
                    check(srcWidth > 0) {
                        "PDF page $pageIndex height $srcHeight isn't greater than 0"
                    }
                    val dstWidth = options.size.widthPx(options.scale) { srcWidth }
                    val dstHeight = options.size.heightPx(options.scale) { srcHeight }
                    val rawScale = DecodeUtils.computeSizeMultiplier(
                        srcWidth = srcWidth,
                        srcHeight = srcHeight,
                        dstWidth = dstWidth,
                        dstHeight = dstHeight,
                        scale = options.scale
                    )
                    val scale = if (options.allowInexactSize) {
                        rawScale.coerceAtMost(1.0)
                    } else {
                        rawScale
                    }
                    val width = (scale * srcWidth).roundToInt()
                    val height = (scale * srcHeight).roundToInt()
                    val config = options.config.toSoftware()
                    val bitmap = createBitmap(width, height, config)
                    val backgroundColor = options.parameters.pdfBackgroundColor() ?: Color.WHITE
                    bitmap.eraseColor(backgroundColor)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    DrawableResult(
                        drawable = bitmap.toDrawable(options.context.resources),
                        isSampled = scale < 1.0,
                        dataSource = DataSource.DISK
                    )
                }
            }
        }

    companion object {
        const val PDF_BACKGROUND_COLOR_KEY = "coil#pdf_background_color"
        const val PDF_PAGE_INDEX_KEY = "coil#pdf_page_index"
    }

    abstract class Factory<T : Any> : Fetcher.Factory<T> {
        override fun create(data: T, options: Options, imageLoader: ImageLoader): Fetcher =
            PdfPageFetcher(options) { openParcelFileDescriptor(data) }

        protected abstract fun openParcelFileDescriptor(data: T): ParcelFileDescriptor
    }
}
