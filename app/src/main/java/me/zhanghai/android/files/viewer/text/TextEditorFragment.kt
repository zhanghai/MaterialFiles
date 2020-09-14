/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import kotlinx.android.parcel.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.TextEditorFragmentBinding
import me.zhanghai.android.files.ui.ThemedFastScroller
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.SimpleTextWatcher
import me.zhanghai.android.files.util.StateData
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.fadeInUnsafe
import me.zhanghai.android.files.util.fadeOutUnsafe
import me.zhanghai.android.files.util.getExtraPath
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.viewModels

class TextEditorFragment : Fragment(), ConfirmReloadDialogFragment.Listener,
    ConfirmCloseDialogFragment.Listener {
    private val args by args<Args>()
    private lateinit var argsPath: Path

    private lateinit var binding: TextEditorFragmentBinding

    private lateinit var menuBinding: MenuBinding

    private val viewModel by viewModels { { TextEditorViewModel() } }

    private var isSettingText = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        TextEditorFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val argsPath = args.intent.getExtraPath(true)
        if (argsPath == null) {
            // TODO: Show a toast.
            finish()
            return
        }
        this.argsPath = argsPath

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        // TODO: Move reload-prevent here so that we can also handle save-as, etc. Or maybe just get
        //  rid of the mPathLiveData in TextEditorViewModel.
        viewModel.path = argsPath
        ThemedFastScroller.create(binding.scrollView)
        // Manually save and restore state in view model to avoid TransactionTooLargeException.
        binding.textEdit.isSaveEnabled = false
        val textEditSavedState = viewModel.removeEditTextSavedState()
        if (textEditSavedState != null) {
            binding.textEdit.onRestoreInstanceState(textEditSavedState)
        }
        binding.textEdit.addTextChangedListener(object : SimpleTextWatcher {
            override fun afterTextChanged(text: Editable) {
                if (isSettingText) {
                    return
                }
                viewModel.isTextChanged = true
            }
        })

        val viewLifecycleOwner = viewLifecycleOwner
        viewModel.fileContentLiveData.observe(viewLifecycleOwner) { onFileContentChanged(it) }
        viewModel.textChangedLiveData.observe(viewLifecycleOwner) { onTextChangedChanged(it) }
        viewModel.writeFileStateLiveData.observe(viewLifecycleOwner) { onWriteFileStateChanged(it) }

        // TODO: Request storage permission if not granted.
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

        updateSaveMenuItem()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                if (!onFinish()) {
                    finish()
                }
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
            else -> super.onOptionsItemSelected(item)
        }

    fun onBackPressed(): Boolean = onFinish()

    private fun onFinish(): Boolean {
        if (viewModel.isTextChanged) {
            ConfirmCloseDialogFragment.show(this)
            return true
        }
        return false
    }

    override fun finish() {
        requireActivity().finish()
    }

    private fun onFileContentChanged(stateful: Stateful<ByteArray>) {
        updateTitle()
        when (stateful) {
            is Loading -> {
                binding.progress.fadeInUnsafe()
                binding.errorText.fadeOutUnsafe()
                binding.textEdit.fadeOutUnsafe()
            }
            is Failure -> {
                stateful.throwable.printStackTrace()
                binding.progress.fadeOutUnsafe()
                binding.errorText.fadeInUnsafe()
                binding.errorText.text = stateful.throwable.toString()
                binding.textEdit.fadeOutUnsafe()
            }
            is Success -> {
                binding.progress.fadeOutUnsafe()
                binding.errorText.fadeOutUnsafe()
                binding.textEdit.fadeInUnsafe()
                if (!viewModel.isTextChanged) {
                    // TODO: Charset.
                    setText(String(stateful.value))
                }
            }
        }
    }

    private fun setText(text: String?) {
        isSettingText = true
        binding.textEdit.setText(text)
        isSettingText = false
        viewModel.isTextChanged = false
    }

    private fun onTextChangedChanged(changed: Boolean) {
        updateTitle()
    }

    private fun updateTitle() {
        val fileName = viewModel.path.fileName.toString()
        val changed = viewModel.isTextChanged
        requireActivity().title = getString(
            if (changed) {
                R.string.text_editor_title_changed_format
            } else {
                R.string.text_editor_title_format
            }, fileName
        )
    }

    private fun onReload() {
        if (viewModel.isTextChanged) {
            ConfirmReloadDialogFragment.show(this)
        } else {
            reload()
        }
    }

    override fun reload() {
        viewModel.isTextChanged = false
        viewModel.reload()
    }

    private fun save() {
        // TODO: Charset
        val content = binding.textEdit.text.toString().toByteArray()
        viewModel.writeFileStateLiveData.write(argsPath, content, requireContext())
    }

    private fun onWriteFileStateChanged(stateData: StateData) {
        val liveData = viewModel.writeFileStateLiveData
        when (stateData.state) {
            StateData.State.READY, StateData.State.LOADING -> updateSaveMenuItem()
            StateData.State.ERROR -> liveData.reset()
            StateData.State.SUCCESS -> {
                showToast(R.string.text_editor_save_success)
                liveData.reset()
                viewModel.isTextChanged = false
            }
        }
    }

    private fun updateSaveMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val liveData = viewModel.writeFileStateLiveData
        menuBinding.saveItem.isEnabled = liveData.valueCompat.state === StateData.State.READY
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs

    private class MenuBinding private constructor(
        val menu: Menu,
        val saveItem: MenuItem
    ) {
        companion object {
            fun inflate(menu: Menu, inflater: MenuInflater): MenuBinding {
                inflater.inflate(R.menu.text_editor, menu)
                return MenuBinding(menu, menu.findItem(R.id.action_save))
            }
        }
    }
}
