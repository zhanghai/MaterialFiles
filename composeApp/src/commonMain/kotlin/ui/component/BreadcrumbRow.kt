/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import me.zhanghai.kotlin.files.ui.token.Durations

@Composable
fun BreadcrumbRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier =
            modifier
                .selectableGroup()
                .horizontalScroll(scrollState)
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun Breadcrumb(
    compact: Boolean,
    text: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .defaultMinSize(minHeight = if (compact) 40.dp else 48.dp)
                .clip(CircleShape)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(horizontal = if (compact) 12.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val targetColor =
            if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        val color by
            animateColorAsState(targetValue = targetColor, animationSpec = tween(Durations.Short2))
        val textStyle =
            if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
        CompositionLocalProvider(
            LocalContentColor provides color,
            LocalTextStyle provides textStyle,
            content = text
        )
    }
}

@Composable
fun BreadcrumbDivider() {
    Box(
        modifier = Modifier.requiredWidth(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.requiredWidth(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
