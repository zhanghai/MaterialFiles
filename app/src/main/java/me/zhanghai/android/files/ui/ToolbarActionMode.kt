/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */
package me.zhanghai.android.files.ui

import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
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
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener {
            callback?.onToolbarActionModeItemClicked(this, it) ?: false
        }
    }

    var navigationIcon: Drawable?
        get() = toolbar.navigationIcon
        set(value) {
            toolbar.navigationIcon = value
        }

    fun setNavigationIcon(@DrawableRes iconRes: Int) {
        toolbar.setNavigationIcon(iconRes)
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
        show(bar, animate)
        callback.onToolbarActionModeStarted(this)
    }

    protected abstract fun show(bar: ViewGroup, animate: Boolean)

    fun finish(animate: Boolean = true) {
        val callback = callback ?: return
        this.callback = null
        toolbar.menu.close()
        hide(bar, animate)
        callback.onToolbarActionModeFinished(this)
    }

    protected abstract fun hide(bar: ViewGroup, animate: Boolean)

    interface Callback {
        fun onToolbarActionModeStarted(toolbarActionMode: ToolbarActionMode)

        fun onToolbarActionModeItemClicked(
            toolbarActionMode: ToolbarActionMode,
            item: MenuItem
        ): Boolean

        fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode)
    }
}
