package me.zhanghai.kotlin.files

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import materialfiles.composeapp.generated.resources.Res
import materialfiles.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = stringResource(Res.string.app_name)) {
        App()
    }
}
