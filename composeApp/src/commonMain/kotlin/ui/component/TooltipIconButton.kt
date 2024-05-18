/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TooltipIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TooltipBox(
        positionProvider = rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = contentDescription) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            Icon(imageVector = imageVector, contentDescription = contentDescription)
        }
    }
}
