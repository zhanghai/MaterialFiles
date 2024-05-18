/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.content

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import me.zhanghai.kotlin.files.ui.token.Palette

@Composable expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme

fun lightColorScheme(): ColorScheme =
    ColorScheme(
        primary = Palette.Primary40,
        onPrimary = Palette.Primary100,
        primaryContainer = Palette.Primary90,
        onPrimaryContainer = Palette.Primary10,
        inversePrimary = Palette.Primary80,
        secondary = Palette.Secondary40,
        onSecondary = Palette.Secondary100,
        secondaryContainer = Palette.Secondary90,
        onSecondaryContainer = Palette.Secondary10,
        tertiary = Palette.Tertiary40,
        onTertiary = Palette.Tertiary100,
        tertiaryContainer = Palette.Tertiary90,
        onTertiaryContainer = Palette.Tertiary10,
        background = Palette.Neutral99,
        onBackground = Palette.Neutral10,
        surface = Palette.Neutral99,
        onSurface = Palette.Neutral10,
        surfaceVariant = Palette.NeutralVariant90,
        onSurfaceVariant = Palette.NeutralVariant30,
        surfaceTint = Palette.Primary40,
        inverseSurface = Palette.Neutral20,
        inverseOnSurface = Palette.Neutral95,
        error = Palette.Error40,
        onError = Palette.Error100,
        errorContainer = Palette.Error90,
        onErrorContainer = Palette.Error10,
        outline = Palette.NeutralVariant50,
        outlineVariant = Palette.NeutralVariant80,
        scrim = Palette.Neutral0,
        surfaceBright = Palette.Neutral98,
        surfaceContainer = Palette.Neutral94,
        surfaceContainerHigh = Palette.Neutral92,
        surfaceContainerHighest = Palette.Neutral90,
        surfaceContainerLow = Palette.Neutral96,
        surfaceContainerLowest = Palette.Neutral100,
        surfaceDim = Palette.Neutral87
    )

fun darkColorScheme(): ColorScheme =
    ColorScheme(
        primary = Palette.Primary80,
        onPrimary = Palette.Primary20,
        primaryContainer = Palette.Primary30,
        onPrimaryContainer = Palette.Primary90,
        inversePrimary = Palette.Primary40,
        secondary = Palette.Secondary80,
        onSecondary = Palette.Secondary20,
        secondaryContainer = Palette.Secondary30,
        onSecondaryContainer = Palette.Secondary90,
        tertiary = Palette.Tertiary80,
        onTertiary = Palette.Tertiary20,
        tertiaryContainer = Palette.Tertiary30,
        onTertiaryContainer = Palette.Tertiary90,
        background = Palette.Neutral10,
        onBackground = Palette.Neutral90,
        surface = Palette.Neutral10,
        onSurface = Palette.Neutral90,
        onSurfaceVariant = Palette.NeutralVariant80,
        surfaceVariant = Palette.NeutralVariant30,
        surfaceTint = Palette.Primary80,
        inverseSurface = Palette.Neutral90,
        inverseOnSurface = Palette.Neutral20,
        error = Palette.Error80,
        onError = Palette.Error20,
        errorContainer = Palette.Error30,
        onErrorContainer = Palette.Error90,
        outline = Palette.NeutralVariant60,
        outlineVariant = Palette.NeutralVariant30,
        scrim = Palette.Neutral0,
        surfaceBright = Palette.Neutral24,
        surfaceContainer = Palette.Neutral12,
        surfaceContainerHigh = Palette.Neutral17,
        surfaceContainerHighest = Palette.Neutral22,
        surfaceContainerLow = Palette.Neutral10,
        surfaceContainerLowest = Palette.Neutral4,
        surfaceDim = Palette.Neutral6
    )
