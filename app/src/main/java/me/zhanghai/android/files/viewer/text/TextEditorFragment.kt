/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import java8.nio.file.Path
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.TextEditorFragmentBinding
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.PreviewType
import me.zhanghai.android.files.file.PreviewableFileDetector
import me.zhanghai.android.files.ui.ThemedFastScroller
import me.zhanghai.android.files.util.ActionState
import me.zhanghai.android.files.util.DataState
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.addOnBackPressedCallback
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.fadeInUnsafe
import me.zhanghai.android.files.util.fadeOutUnsafe
import me.zhanghai.android.files.util.isReady
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.viewModels
import java.nio.charset.Charset

class TextEditorFragment : Fragment(), ConfirmReloadDialogFragment.Listener,
    ConfirmCloseDialogFragment.Listener, ConfirmLargeFileDialogFragment.Listener {
    private val args by args<Args>()
    private lateinit var argsFile: Path

    private lateinit var binding: TextEditorFragmentBinding

    private lateinit var menuBinding: MenuBinding

    private val viewModel by viewModels { { TextEditorViewModel(argsFile) } }

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var isSettingText = false

    private var isPreviewMode = false

    private var previewWebViewInitialized = false

    private var previewRenderer: PreviewRenderer? = null

    private var currentText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        lifecycleScope.launchWhenStarted {
            onBackPressedCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    ConfirmCloseDialogFragment.show(this@TextEditorFragment)
                }
            }
            launch {
                viewModel.isTextChanged.collect {
                    onBackPressedCallback.isEnabled = viewModel.isTextChanged.value
                }
            }
            addOnBackPressedCallback(onBackPressedCallback)

            launch { viewModel.encoding.collect { onEncodingChanged(it) } }
            launch { viewModel.textState.collect { onTextStateChanged(it) } }
            launch { viewModel.isTextChanged.collect { onIsTextChangedChanged(it) } }
            launch { viewModel.writeFileState.collect { onWriteFileStateChanged(it) } }
            launch { viewModel.largeFileSize.collect { onLargeFileSizeChanged(it) } }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        TextEditorFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argsFile = args.intent.extraPath
        if (argsFile == null) {
            finish()
            return
        }
        this.argsFile = argsFile

        val activity = requireActivity() as AppCompatActivity
        activity.lifecycleScope.launchWhenCreated {
            activity.setSupportActionBar(binding.toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initPreviewWebView()

        binding.lineNumberView.editText = binding.textEdit
        binding.scrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                binding.lineNumberView.setScrollOffset(scrollY)
            }
        )

        ThemedFastScroller.create(binding.scrollView)
        binding.textEdit.isSaveEnabled = false
        val textEditSavedState = viewModel.removeEditTextSavedState()
        if (textEditSavedState != null) {
            binding.textEdit.onRestoreInstanceState(textEditSavedState)
        }
        binding.textEdit.doAfterTextChanged {
            if (isSettingText) {
                return@doAfterTextChanged
            }
            if (viewModel.textState.value !is DataState.Success) {
                return@doAfterTextChanged
            }
            viewModel.isTextChanged.value = true
        }

        if (args.startInPreview) {
            showPreviewMode()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        viewModel.setEditTextSavedState(binding.textEdit.onSaveInstanceState())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menuBinding = MenuBinding.inflate(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        updatePreviewToggleMenuItem()
        updateSaveMenuItem()
        updateEncodingMenuItems()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_preview_edit_toggle -> {
                togglePreviewMode()
                true
            }
            R.id.action_save -> {
                save()
                true
            }
            R.id.action_reload -> {
                onReload()
                true
            }
            Menu.FIRST -> {
                viewModel.encoding.value = Charset.forName(item.titleCondensed!!.toString())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    fun onSupportNavigateUp(): Boolean {
        if (onBackPressedCallback.isEnabled) {
            onBackPressedCallback.handleOnBackPressed()
            return true
        }
        return false
    }

    override fun finish() {
        requireActivity().finish()
    }

    private fun initPreviewWebView() {
        val webView = binding.previewWebView
        val webViewClient = PreviewWebViewClient(
            onPageLoaded = {
                previewRenderer?.onReady()
                renderCurrentContent()
            }
        )
        webView.webViewClient = webViewClient
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.loadUrl("file:///android_asset/preview/preview.html")
        previewRenderer = PreviewRenderer(webView)
        previewWebViewInitialized = true
    }

    private fun showPreviewMode() {
        isPreviewMode = true
        binding.scrollView.visibility = View.GONE
        binding.webViewContainer.visibility = View.VISIBLE
        binding.lineNumberView.visibility = View.GONE
        renderCurrentContent()
        requireActivity().invalidateOptionsMenu()
    }

    private fun showEditMode() {
        isPreviewMode = false
        binding.webViewContainer.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
        binding.lineNumberView.visibility = View.VISIBLE
        requireActivity().invalidateOptionsMenu()
    }

    private fun togglePreviewMode() {
        if (isPreviewMode) {
            showEditMode()
        } else {
            showPreviewMode()
        }
    }

    private fun renderCurrentContent() {
        val text = currentText ?: return
        val renderer = previewRenderer ?: return
        val path = argsFile
        val previewType = PreviewableFileDetector.getPreviewType(path)
        val language = PreviewableFileDetector.getHighlightLanguage(path)
        val theme = getCurrentTheme()
        renderer.render(previewType, text, language, theme)
    }

    private fun getCurrentTheme(): String {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> "dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "light"
            else -> {
                val uiMode = resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
                if (uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
            }
        }
    }

    private fun updatePreviewToggleMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        menuBinding.previewToggleItem.isVisible = true
        menuBinding.previewToggleItem.title = if (isPreviewMode) {
            getString(R.string.file_viewer_editor_action_edit)
        } else {
            getString(R.string.file_viewer_editor_action_preview)
        }
    }

    private fun onEncodingChanged(encoding: Charset) {
        updateEncodingMenuItems()
    }

    private fun updateEncodingMenuItems() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val charsetName = viewModel.encoding.value.name()
        val charsetItem = menuBinding.encodingSubMenu.children
            .find { it.titleCondensed == charsetName }!!
        charsetItem.isChecked = true
    }

    private fun onTextStateChanged(state: DataState<String>) {
        updateTitle()
        when (state) {
            is DataState.Loading -> {
                binding.progress.fadeInUnsafe()
                binding.errorText.fadeOutUnsafe()
                binding.textEdit.fadeOutUnsafe()
            }
            is DataState.Success -> {
                binding.progress.fadeOutUnsafe()
                binding.errorText.fadeOutUnsafe()
                if (!viewModel.isTextChanged.value) {
                    setText(state.data)
                }
                currentText = state.data
                if (isPreviewMode) {
                    renderCurrentContent()
                } else {
                    binding.textEdit.fadeInUnsafe()
                }
            }
            is DataState.Error -> {
                state.throwable.printStackTrace()
                binding.progress.fadeOutUnsafe()
                binding.errorText.fadeInUnsafe()
                binding.errorText.text = state.throwable.toString()
                binding.textEdit.fadeOutUnsafe()
            }
        }
    }

    private fun setText(text: String?) {
        isSettingText = true
        binding.textEdit.setText(text)
        isSettingText = false
        viewModel.isTextChanged.value = false
        binding.lineNumberView.post { binding.lineNumberView.invalidate() }
    }

    private fun onIsTextChangedChanged(changed: Boolean) {
        updateTitle()
    }

    private fun updateTitle() {
        val fileName = viewModel.file.value.fileName.toString()
        val changed = viewModel.isTextChanged.value
        requireActivity().title = getString(
            if (changed) {
                R.string.text_editor_title_changed_format
            } else {
                R.string.text_editor_title_format
            }, fileName
        )
    }

    private fun onReload() {
        if (viewModel.isTextChanged.value) {
            ConfirmReloadDialogFragment.show(this)
        } else {
            reload()
        }
    }

    override fun reload() {
        viewModel.isTextChanged.value = false
        viewModel.reload()
    }

    override fun loadLargeFile() {
        viewModel.loadLargeFile()
    }

    private fun onLargeFileSizeChanged(size: Long?) {
        if (size != null && size > 0) {
            ConfirmLargeFileDialogFragment.show(this, formatFileSize(size))
        }
    }

    private fun formatFileSize(bytes: Long): String {
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb >= 1.0) {
            String.format("%.1f MB", mb)
        } else {
            val kb = bytes.toDouble() / 1024
            String.format("%.0f KB", kb)
        }
    }

    private fun save() {
        val text = binding.textEdit.text.toString()
        viewModel.writeFile(argsFile, text, requireContext())
    }

    private fun onWriteFileStateChanged(state: ActionState<Pair<Path, String>, Unit>) {
        when (state) {
            is ActionState.Ready, is ActionState.Running -> updateSaveMenuItem()
            is ActionState.Success -> {
                showToast(R.string.text_editor_save_success)
                viewModel.finishWritingFile()
                viewModel.isTextChanged.value = false
            }
            is ActionState.Error -> viewModel.finishWritingFile()
        }
    }

    private fun updateSaveMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        menuBinding.saveItem.isEnabled = viewModel.writeFileState.value.isReady
    }

    @Parcelize
    class Args(val intent: Intent, val startInPreview: Boolean = false) : ParcelableArgs

    private class MenuBinding private constructor(
        val menu: Menu,
        val previewToggleItem: MenuItem,
        val saveItem: MenuItem,
        val encodingSubMenu: SubMenu
    ) {
        companion object {
            fun inflate(menu: Menu, inflater: MenuInflater): MenuBinding {
                inflater.inflate(R.menu.text_editor, menu)
                val encodingSubMenu = menu.findItem(R.id.action_encoding).subMenu!!
                for ((charsetName, charset) in Charset.availableCharsets()) {
                    encodingSubMenu.add(Menu.NONE, Menu.FIRST, Menu.NONE, charset.displayName())
                        .titleCondensed = charsetName
                }
                encodingSubMenu.setGroupCheckable(Menu.NONE, true, true)
                return MenuBinding(
                    menu,
                    menu.findItem(R.id.action_preview_edit_toggle),
                    menu.findItem(R.id.action_save),
                    encodingSubMenu
                )
            }
        }
    }
}
