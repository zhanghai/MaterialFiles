/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.foregroundCompat
import me.zhanghai.android.files.compat.obtainStyledAttributesCompat
import me.zhanghai.android.files.compat.setTextAppearanceCompat
import me.zhanghai.android.files.compat.use
import me.zhanghai.android.files.databinding.NavigationDividerItemBinding
import me.zhanghai.android.files.databinding.NavigationItemBinding
import me.zhanghai.android.files.ui.AutoMirrorDrawable
import me.zhanghai.android.files.ui.SimpleAdapter
import me.zhanghai.android.files.util.getColorStateListByAttr
import me.zhanghai.android.files.util.layoutInflater

class NavigationListAdapter(
    private val listener: NavigationItem.Listener,
    context: Context
) : SimpleAdapter<NavigationItem?, RecyclerView.ViewHolder>() {
    @SuppressLint("PrivateResource", "RestrictedApi")
    private val viewAttributes = context.obtainStyledAttributesCompat(
        null, com.google.android.material.R.styleable.NavigationView,
        com.google.android.material.R.attr.navigationViewStyle,
        com.google.android.material.R.style.Widget_MaterialComponents_NavigationView
    ).use { a ->
        val itemShapeAppearance = a.getResourceId(
            com.google.android.material.R.styleable.NavigationView_itemShapeAppearance, 0
        )
        val itemShapeAppearanceOverlay = a.getResourceId(
            com.google.android.material.R.styleable.NavigationView_itemShapeAppearanceOverlay, 0
        )
        val itemShapeFillColor = a.getColorStateList(
            com.google.android.material.R.styleable.NavigationView_itemShapeFillColor
        )
        val itemShapeInsetStart = a.getDimensionPixelSize(
            com.google.android.material.R.styleable.NavigationView_itemShapeInsetStart, 0
        )
        val itemShapeInsetEnd = a.getDimensionPixelSize(
            com.google.android.material.R.styleable.NavigationView_itemShapeInsetEnd, 0
        )
        val itemShapeInsetTop = a.getDimensionPixelSize(
            com.google.android.material.R.styleable.NavigationView_itemShapeInsetTop, 0
        )
        val itemShapeInsetBottom = a.getDimensionPixelSize(
            com.google.android.material.R.styleable.NavigationView_itemShapeInsetBottom, 0
        )
        val itemBackground = createItemShapeDrawable(
            itemShapeAppearance, itemShapeAppearanceOverlay, itemShapeFillColor,
            itemShapeInsetStart, itemShapeInsetEnd, itemShapeInsetTop, itemShapeInsetBottom, context
        )
        val controlHighlightColor = context.getColorStateListByAttr(
            com.google.android.material.R.attr.colorControlHighlight
        )
        val itemForegroundMaskFillColor = ColorStateList.valueOf(Color.WHITE)
        val itemForegroundMask = createItemShapeDrawable(
            itemShapeAppearance, itemShapeAppearanceOverlay, itemForegroundMaskFillColor,
            itemShapeInsetStart, itemShapeInsetEnd, itemShapeInsetTop, itemShapeInsetBottom, context
        )
        val itemForeground = RippleDrawable(controlHighlightColor, null, itemForegroundMask)
        context.obtainStyledAttributesCompat(
            null, R.styleable.NavigationViewExtra,
            com.google.android.material.R.attr.navigationViewStyle, 0
        ).use { a2 ->
            ViewAttributes(
                a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.NavigationView_itemHorizontalPadding, 0
                ),
                a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.NavigationView_itemVerticalPadding, 0
                ),
                itemBackground,
                itemForeground,
                a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.NavigationView_itemIconSize, 0
                ),
                a.getColorStateList(
                    com.google.android.material.R.styleable.NavigationView_itemIconTint
                ),
                a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.NavigationView_itemIconPadding, 0
                ),
                a.getResourceId(
                    com.google.android.material.R.styleable.NavigationView_itemTextAppearance,
                    ResourcesCompat.ID_NULL
                ),
                a.getColorStateList(
                    com.google.android.material.R.styleable.NavigationView_itemTextColor
                ),
                a2.getResourceId(
                    R.styleable.NavigationViewExtra_itemSubtitleTextAppearance,
                    ResourcesCompat.ID_NULL
                ),
                a2.getColorStateList(R.styleable.NavigationViewExtra_itemSubtitleTextColor),
                a2.getDimension(R.styleable.NavigationViewExtra_itemSubtitleTextSize, 0f),
                a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.NavigationView_dividerInsetStart, 0
                ),
                a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.NavigationView_dividerInsetEnd, 0
                ),
                a2.getDimensionPixelSize(R.styleable.NavigationViewExtra_dividerVerticalPadding, 0)
            )
        }
    }

    // @see com.google.android.material.navigation.NavigationView#createDefaultItemBackground
    private fun createItemShapeDrawable(
        @StyleRes shapeAppearance: Int,
        @StyleRes shapeAppearanceOverlay: Int,
        fillColor: ColorStateList?,
        @Px insetStart: Int,
        @Px insetEnd: Int,
        @Px insetTop: Int,
        @Px insetBottom: Int,
        context: Context
    ): Drawable {
        val shapeAppearanceModel =
            ShapeAppearanceModel.builder(context, shapeAppearance, shapeAppearanceOverlay).build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
            .apply { this.fillColor = fillColor }
        return AutoMirrorDrawable(
            InsetDrawable(materialShapeDrawable, insetStart, insetTop, insetEnd, insetBottom)
        )
    }

    fun notifyCheckedChanged() {
        notifyItemRangeChanged(0, itemCount, PAYLOAD_CHECKED_CHANGED)
    }

    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long =
        getItem(position)?.id ?: list.subList(0, position).count { it == null }.toLong()

    override fun getItemViewType(position: Int): Int {
        val viewType = if (getItem(position) != null) ViewType.ITEM else ViewType.DIVIDER
        return viewType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (ViewType.values()[viewType]) {
            ViewType.ITEM ->
                ItemHolder(
                    NavigationItemBinding.inflate(parent.context.layoutInflater, parent, false)
                ).apply {
                    binding.itemLayout.updatePaddingRelative(
                        viewAttributes.itemHorizontalPadding,
                        viewAttributes.itemVerticalPadding,
                        viewAttributes.itemHorizontalPadding,
                        viewAttributes.itemVerticalPadding
                    )
                    binding.itemLayout.background =
                        viewAttributes.itemBackground?.constantState?.newDrawable()
                    binding.itemLayout.foregroundCompat =
                        viewAttributes.itemForeground?.constantState?.newDrawable()
                    binding.iconImage.updateLayoutParams {
                        width = viewAttributes.itemIconSize
                        height = viewAttributes.itemIconSize
                    }
                    binding.iconImage.imageTintList = viewAttributes.itemIconTint
                    binding.textLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        marginStart = viewAttributes.itemIconPadding
                    }
                    if (viewAttributes.itemTextAppearance != ResourcesCompat.ID_NULL) {
                        binding.titleText.setTextAppearanceCompat(viewAttributes.itemTextAppearance)
                    }
                    binding.titleText.setTextColor(viewAttributes.itemTextColor)
                    if (viewAttributes.itemSubtitleTextAppearance != ResourcesCompat.ID_NULL) {
                        binding.subtitleText.setTextAppearanceCompat(
                            viewAttributes.itemSubtitleTextAppearance
                        )
                    }
                    binding.subtitleText.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX, viewAttributes.itemSubtitleTextSize
                    )
                    binding.subtitleText.setTextColor(viewAttributes.itemSubtitleTextColor)
                }
            ViewType.DIVIDER ->
                DividerHolder(
                    NavigationDividerItemBinding.inflate(
                        parent.context.layoutInflater, parent, false
                    )
                ).apply {
                    binding.root.updatePaddingRelative(
                        viewAttributes.dividerInsetStart,
                        viewAttributes.dividerVerticalPadding,
                        viewAttributes.dividerInsetEnd,
                        viewAttributes.dividerVerticalPadding
                    )
                }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        when (ViewType.values()[getItemViewType(position)]) {
            ViewType.ITEM -> {
                val item = getItem(position)!!
                val binding = (holder as ItemHolder).binding
                binding.itemLayout.isChecked = item.isChecked(listener)
                if (payloads.isNotEmpty()) {
                    return
                }
                binding.itemLayout.setOnClickListener { item.onClick(listener) }
                binding.itemLayout.setOnLongClickListener { item.onLongClick(listener) }
                binding.iconImage.setImageDrawable(item.getIcon(binding.iconImage.context))
                binding.titleText.text = item.getTitle(binding.titleText.context)
                binding.subtitleText.text = item.getSubtitle(binding.subtitleText.context)
            }
            ViewType.DIVIDER -> {}
        }
    }

    companion object {
        private val PAYLOAD_CHECKED_CHANGED = Any()
    }

    private class ViewAttributes(
        @Px val itemHorizontalPadding: Int,
        @Px val itemVerticalPadding: Int,
        val itemBackground: Drawable?,
        val itemForeground: Drawable?,
        @Px val itemIconSize: Int,
        val itemIconTint: ColorStateList?,
        @Px val itemIconPadding: Int,
        @StyleRes val itemTextAppearance: Int,
        val itemTextColor: ColorStateList?,
        @StyleRes val itemSubtitleTextAppearance: Int,
        val itemSubtitleTextColor: ColorStateList?,
        @Px val itemSubtitleTextSize: Float,
        @Px val dividerInsetStart: Int,
        @Px val dividerInsetEnd: Int,
        @Px val dividerVerticalPadding: Int
    )

    private enum class ViewType {
        ITEM,
        DIVIDER
    }

    private class ItemHolder(val binding: NavigationItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    private class DividerHolder(
        val binding: NavigationDividerItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
