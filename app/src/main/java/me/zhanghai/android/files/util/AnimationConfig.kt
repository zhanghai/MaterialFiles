/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util
 
 import me.zhanghai.android.files.settings.Settings

/**
 * Centralized configuration for animations throughout the app.
 * Set [ANIMATIONS_ENABLED] to false to disable all transition animations,
 * useful for e-paper devices where animations cause visual artifacts.
 */
object AnimationConfig {
    /**
     * Enable or disable all animations in the app.
     * Set to false for e-paper device compatibility.
     */
    val ANIMATIONS_ENABLED: Boolean
        get() = Settings.FILE_LIST_ANIMATION.valueCompat

    /**
     * Get animation duration adjusted based on [ANIMATIONS_ENABLED] flag.
     * @param durationMs The requested animation duration in milliseconds
     * @return 0ms if animations are disabled, otherwise the requested duration
     */
    fun getAnimDuration(durationMs: Int): Int =
        if (ANIMATIONS_ENABLED) durationMs else 0

    /**
     * Get animation duration with zero if animations are disabled.
     * Convenience function for cases where duration is already retrieved.
     * @param durationMs The requested animation duration in milliseconds
     * @return 0ms if animations are disabled, otherwise the requested duration
     */
    fun getAnimDuration(durationMs: Long): Long =
        if (ANIMATIONS_ENABLED) durationMs else 0L

    /**
     * Check if animations should be applied.
     * @return true if animations are enabled, false otherwise
     */
    fun shouldAnimate(): Boolean = ANIMATIONS_ENABLED
}
