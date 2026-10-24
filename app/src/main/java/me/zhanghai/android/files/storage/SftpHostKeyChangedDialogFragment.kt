/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX‑License‑Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.storage

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcel
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getArgs
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.readParcelable

class SftpHostKeyChangedDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private var isListenerNotified = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_sftp_host_key_changed_title)
            .setMessage(
                getMessage(args.authority, args.oldHostKey, args.newHostKey, requireContext())
            )
            .setPositiveButton(R.string.storage_sftp_host_key_changed_trust) { _, _ ->
                notifyListenerOnce(true)
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                notifyListenerOnce(false)
                finish()
            }
            .create()
            .apply { setCanceledOnTouchOutside(false) }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        notifyListenerOnce(false)
        finish()
    }

    fun onFinish() {
        notifyListenerOnce(false)
    }

    private fun notifyListenerOnce(trust: Boolean) {
        if (isListenerNotified) {
            return
        }
        args.listener(trust)
        isListenerNotified = true
    }

    companion object {
        fun getMessage(authority: String, context: Context): String =
            context.getString(R.string.storage_sftp_host_key_changed_message_format, authority)

        private fun getMessage(
            authority: String,
            oldHostKey: String,
            newHostKey: String,
            context: Context
        ): String = context.getString(
            R.string.storage_sftp_host_key_changed_message_with_keys_format,
            authority,
            oldHostKey,
            newHostKey
        )
    }

    @Parcelize
    class Args(
        val authority: String,
        val oldHostKey: String,
        val newHostKey: String,
        val listener: @WriteWith<ListenerParceler>() (Boolean) -> Unit
    ) : ParcelableArgs {
        object ListenerParceler : Parceler<(Boolean) -> Unit> {
            override fun create(parcel: Parcel): (Boolean) -> Unit =
                parcel.readParcelable<RemoteCallback>()!!.let {
                    { trust -> it.sendResult(Bundle().putArgs(ListenerArgs(trust))) }
                }

            override fun ((Boolean) -> Unit).write(parcel: Parcel, flags: Int) {
                parcel.writeParcelable(RemoteCallback {
                    val args = it.getArgs<ListenerArgs>()
                    this(args.trust)
                }, flags)
            }

            @Parcelize
            private class ListenerArgs(val trust: Boolean) : ParcelableArgs
        }
    }
}
