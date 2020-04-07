/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.graphics.Color
import android.view.ViewGroup
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import me.zhanghai.android.fastscroll.PopupStyles
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

object ThemedFastScroller {
    fun create(view: ViewGroup): FastScroller
        = FastScrollerBuilder(view)
            .apply {
                if (Settings.MATERIAL_DESIGN_2.valueCompat) {
                    useMd2Style()
                } else {
                    setPopupStyle { popupText ->
                        PopupStyles.DEFAULT.accept(popupText)
                        popupText.setTextColor(Color.WHITE)
                    }
                }
            }
            .build()
}
