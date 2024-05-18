/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.filelist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import materialfiles.composeapp.generated.resources.Res
import materialfiles.composeapp.generated.resources.more_options
import materialfiles.composeapp.generated.resources.open_navigation_drawer
import me.zhanghai.kotlin.files.ui.component.Breadcrumb
import me.zhanghai.kotlin.files.ui.component.BreadcrumbDivider
import me.zhanghai.kotlin.files.ui.component.BreadcrumbRow
import me.zhanghai.kotlin.files.ui.component.TooltipIconButton
import me.zhanghai.kotlin.files.ui.component.TopAppBarContainer
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
fun FileListTopBar() {
    TopAppBarContainer(
        modifier = Modifier.fillMaxWidth(),
        expandedHeight = 112.dp,
        windowInsets =
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    ) {
        Column {
            var query by remember { mutableStateOf("") }
            var active by remember { mutableStateOf(false) }
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                placeholder = {
                    Text("Search 8 folders, 25 files")
                },
                leadingIcon = {
                    TooltipIconButton(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = stringResource(Res.string.open_navigation_drawer),
                        onClick = {}
                    )
                },
                trailingIcon = {
                    TooltipIconButton(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(Res.string.more_options),
                        onClick = {}
                    )
                },
                windowInsets = WindowInsets(0.dp)
            ) {}
            BreadcrumbRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 60.dp)
            ) {
                Breadcrumb(
                    compact = true,
                    text = { Text("内部共享存储空间") },
                    selected = false,
                    onClick = {}
                )
                BreadcrumbDivider()
                Breadcrumb(
                    compact = true,
                    text = { Text("Download") },
                    selected = false,
                    onClick = {}
                )
                BreadcrumbDivider()
                Breadcrumb(
                    compact = true,
                    text = { Text("Anime") },
                    selected = true,
                    onClick = {}
                )
            }
        }
    }
}
