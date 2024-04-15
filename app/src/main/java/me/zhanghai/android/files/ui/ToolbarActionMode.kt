/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */
package me.zhanghai.android.files.ui

import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar

abstract class ToolbarActionMode(
    private val bar: ViewGroup,
    private val toolbar: Toolbar
) {
    @MenuRes
    private var menuRes = 0

    private var callback: Callback? = null

    init {
        toolbar.setNavigationOnClickListener { callback?.onToolbarNavigationIconClicked(this) }
        toolbar.setOnMenuItemClickListener {
            callback?.onToolbarActionModeMenuItemClicked(this, it) ?: false
        }
    }

    val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    var navigationIcon: Drawable?
        get() = toolbar.navigationIcon
        set(value) {
            toolbar.navigationIcon = value
        }

    var navigationContentDescription: CharSequence?
        get() = toolbar.navigationContentDescription
        set(value) {
            toolbar.navigationContentDescription = value
        }

    fun setNavigationIcon(@DrawableRes iconRes: Int, @StringRes contentDescriptionRes: Int) {
        toolbar.setNavigationIcon(iconRes)
        toolbar.setNavigationContentDescription(contentDescriptionRes)
    }

    var title: CharSequence?
        get() = toolbar.title
        set(value) {
            toolbar.title = value
        }

    fun setTitle(@StringRes titleRes: Int) {
        toolbar.setTitle(titleRes)
    }

    var subtitle: CharSequence?
        get() = toolbar.subtitle
        set(value) {
            toolbar.subtitle = value
        }

    fun setSubtitle(@StringRes subtitleRes: Int) {
        toolbar.setSubtitle(subtitleRes)
    }

    val menu: Menu
        get() = toolbar.menu

    fun setMenuResource(@MenuRes menuRes: Int) {
        if (this.menuRes == menuRes) {
            return
        }
        this.menuRes = menuRes
        toolbar.menu.clear()
        if (menuRes != 0) {
            toolbar.inflateMenu(menuRes)
        }
    }

    val isActive: Boolean
        get() = callback != null

    fun start(callback: Callback, animate: Boolean = true) {
        this.callback = callback
        onBackPressedCallback.isEnabled = true
        show(bar, animate)
        callback.onToolbarActionModeStarted(this)
    }

    protected abstract fun show(bar: ViewGroup, animate: Boolean)

    fun finish(animate: Boolean = true) {
        val callback = callback ?: return
        this.callback = null
        onBackPressedCallback.isEnabled = false
        toolbar.menu.close()
        hide(bar, animate)
        callback.onToolbarActionModeFinished(this)
    }

    protected abstract fun hide(bar: ViewGroup, animate: Boolean)

    interface Callback {
        fun onToolbarActionModeStarted(toolbarActionMode: ToolbarActionMode) {}

        fun onToolbarNavigationIconClicked(toolbarActionMode: ToolbarActionMode) {
            toolbarActionMode.finish()
        }

        fun onToolbarActionModeMenuItemClicked(
            toolbarActionMode: ToolbarActionMode,
            item: MenuItem
        ): Boolean

        fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode)
    }
}
