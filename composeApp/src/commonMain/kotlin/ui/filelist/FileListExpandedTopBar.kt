/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.filelist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import materialfiles.composeapp.generated.resources.Res
import materialfiles.composeapp.generated.resources.back
import materialfiles.composeapp.generated.resources.forward
import materialfiles.composeapp.generated.resources.more_options
import materialfiles.composeapp.generated.resources.search
import me.zhanghai.kotlin.files.ui.component.Breadcrumb
import me.zhanghai.kotlin.files.ui.component.BreadcrumbDivider
import me.zhanghai.kotlin.files.ui.component.BreadcrumbRow
import me.zhanghai.kotlin.files.ui.component.TooltipIconButton
import me.zhanghai.kotlin.files.ui.component.horizontalScrollIndicators
import me.zhanghai.kotlin.files.ui.token.Elevations
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
fun FileListExpandedTopBar() {
    TopAppBar(
        title = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape,
                    tonalElevation = Elevations.Level3
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val scrollState = rememberScrollState()
                        BreadcrumbRow(
                            modifier =
                                Modifier.weight(1f)
                                    .horizontalScrollIndicators(
                                        scrollableState = scrollState,
                                        drawStartIndicator = false
                                    ),
                            contentPadding = PaddingValues(start = 8.dp),
                            scrollState = scrollState
                        ) {
                            Breadcrumb(
                                compact = false,
                                text = { Text("Home") },
                                selected = false,
                                onClick = {}
                            )
                            BreadcrumbDivider()
                            Breadcrumb(
                                compact = false,
                                text = { Text("Downloads") },
                                selected = false,
                                onClick = {}
                            )
                            BreadcrumbDivider()
                            Breadcrumb(
                                compact = false,
                                text = { Text("temp") },
                                selected = true,
                                onClick = {}
                            )
                        }
                        TooltipIconButton(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(Res.string.search),
                            onClick = {}
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TooltipIconButton(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    onClick = {}
                )
                TooltipIconButton(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = stringResource(Res.string.forward),
                    onClick = {}
                )
            }
        },
        actions = {
            TooltipIconButton(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(Res.string.more_options),
                onClick = {}
            )
        },
        windowInsets =
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )
}
