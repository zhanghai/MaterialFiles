/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcel
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.requireViewByIdCompat
import me.zhanghai.android.files.databinding.FileJobActionDialogViewBinding
import me.zhanghai.android.files.provider.common.PosixFileStore
import me.zhanghai.android.files.util.ActionState
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getArgs
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.isReady
import me.zhanghai.android.files.util.isRunning
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.readParcelable
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.viewModels

class FileJobActionDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FileJobActionViewModel() } }

    private lateinit var binding: FileJobActionDialogViewBinding

    private var isListenerNotified = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(binding.allCheck.isChecked))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(args.title)
            .setMessage(args.message)
            .apply {
                binding = FileJobActionDialogViewBinding.inflate(context.layoutInflater)
                val hasReadOnlyFileStore = args.readOnlyFileStore != null
                binding.remountButton.isVisible = hasReadOnlyFileStore
                if (hasReadOnlyFileStore) {
                    updateRemountButton()
                    binding.remountButton.setOnClickListener { remount() }
                }
                binding.allSpace.isVisible = !hasReadOnlyFileStore && args.showAll
                binding.allCheck.isVisible = args.showAll
                if (savedInstanceState != null) {
                    binding.allCheck.isChecked = savedInstanceState.getState<State>().isAllChecked
                }

                if (hasReadOnlyFileStore) {
                    lifecycleScope.launchWhenStarted {
                        launch { viewModel.remountState.collect { onRemountStateChanged(it) } }
                    }
                }
            }
            .setPositiveButton(args.positiveButtonText, ::onDialogButtonClick)
            .setNegativeButton(args.negativeButtonText, ::onDialogButtonClick)
            .setNeutralButton(args.neutralButtonText, ::onDialogButtonClick)
            .create()
            .apply { setCanceledOnTouchOutside(false) }

    private fun remount() {
        if (!viewModel.remountState.value.isReady || !args.readOnlyFileStore!!.isReadOnly) {
            return
        }
        viewModel.remount(args.readOnlyFileStore!!)
    }

    private fun onRemountStateChanged(state: ActionState<PosixFileStore, Unit>) {
        when (state) {
            is ActionState.Ready, is ActionState.Running -> updateRemountButton()
            is ActionState.Success -> viewModel.finishRemounting()
            is ActionState.Error -> {
                val throwable = state.throwable
                throwable.printStackTrace()
                showToast(throwable.toString())
                viewModel.finishRemounting()
            }
        }
    }

    private fun updateRemountButton() {
        val textRes = when {
            viewModel.remountState.value.isRunning -> R.string.file_job_remount_loading_format
            args.readOnlyFileStore!!.isReadOnly -> R.string.file_job_remount_format
            else -> R.string.file_job_remount_success_format
        }
        binding.remountButton.text = getString(textRes, args.readOnlyFileStore!!.name())
    }

    private fun onDialogButtonClick(dialog: DialogInterface, which: Int) {
        val action = when (which) {
            DialogInterface.BUTTON_POSITIVE -> FileJobAction.POSITIVE
            DialogInterface.BUTTON_NEGATIVE -> FileJobAction.NEGATIVE
            DialogInterface.BUTTON_NEUTRAL -> FileJobAction.NEUTRAL
            else -> throw AssertionError(which)
        }
        notifyListenerOnce(action, args.showAll && binding.allCheck.isChecked)
        finish()
    }

    override fun onStart() {
        super.onStart()

        if (binding.root.parent == null) {
            val dialog = requireDialog() as AlertDialog
            val scrollView = dialog.requireViewByIdCompat<NestedScrollView>(R.id.scrollView)
            val linearLayout = scrollView.getChildAt(0) as LinearLayout
            linearLayout.addView(binding.root)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        notifyListenerOnce(FileJobAction.CANCELED, false)
        finish()
    }

    fun onFinish() {
        notifyListenerOnce(FileJobAction.CANCELED, false)
    }

    private fun notifyListenerOnce(action: FileJobAction, isAll: Boolean) {
        if (isListenerNotified) {
            return
        }
        args.listener(action, isAll)
        isListenerNotified = true
    }

    @Parcelize
    class Args(
        val title: CharSequence,
        val message: CharSequence,
        val readOnlyFileStore: @WriteWith<ParcelableParceler> PosixFileStore?,
        val showAll: Boolean,
        val positiveButtonText: CharSequence?,
        val negativeButtonText: CharSequence?,
        val neutralButtonText: CharSequence?,
        val listener: @WriteWith<ListenerParceler>() (FileJobAction, Boolean) -> Unit
    ) : ParcelableArgs {
        object ListenerParceler : Parceler<(FileJobAction, Boolean) -> Unit> {
            override fun create(parcel: Parcel): (FileJobAction, Boolean) -> Unit =
                parcel.readParcelable<RemoteCallback>()!!.let {
                    { action, isAll ->
                        it.sendResult(Bundle().putArgs(ListenerArgs(action, isAll)))
                    }
                }

            override fun ((FileJobAction, Boolean) -> Unit).write(parcel: Parcel, flags: Int) {
                parcel.writeParcelable(RemoteCallback {
                    val args = it.getArgs<ListenerArgs>()
                    this(args.action, args.isAll)
                }, flags)
            }

            @Parcelize
            private class ListenerArgs(
                val action: FileJobAction,
                val isAll: Boolean
            ) : ParcelableArgs
        }
    }

    @Parcelize
    private class State(val isAllChecked: Boolean) : ParcelableState
}
