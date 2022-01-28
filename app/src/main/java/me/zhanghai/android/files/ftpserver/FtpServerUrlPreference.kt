/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.View
import android.view.View.OnCreateContextMenuListener
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.clipboardManager
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.copyText
import me.zhanghai.android.files.util.getLocalAddress
import me.zhanghai.android.files.util.valueCompat
import java.net.InetAddress

class FtpServerUrlPreference : Preference {
    private val observer = Observer<Any> { updateSummary() }
    private val connectivityReceiver = ConnectivityReceiver()

    private val contextMenuListener = ContextMenuListener()
    private var hasUrl = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        isPersistent = false
        updateSummary()
    }

    override fun onAttached() {
        super.onAttached()

        Settings.FTP_SERVER_ANONYMOUS_LOGIN.observeForever(observer)
        Settings.FTP_SERVER_USERNAME.observeForever(observer)
        Settings.FTP_SERVER_PORT.observeForever(observer)
        context.registerReceiver(
            connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onDetached() {
        super.onDetached()

        Settings.FTP_SERVER_ANONYMOUS_LOGIN.removeObserver(observer)
        Settings.FTP_SERVER_USERNAME.removeObserver(observer)
        Settings.FTP_SERVER_PORT.removeObserver(observer)
        context.unregisterReceiver(connectivityReceiver)
    }

    private fun updateSummary() {
        val localAddress = InetAddress::class.getLocalAddress()
        val summary: String
        if (localAddress != null) {
            val username = if (!Settings.FTP_SERVER_ANONYMOUS_LOGIN.valueCompat) {
                Settings.FTP_SERVER_USERNAME.valueCompat
            } else {
                null
            }
            val host = localAddress.hostAddress
            val port = Settings.FTP_SERVER_PORT.valueCompat
            summary = "ftp://${if (username != null) "$username@" else ""}$host:$port/"
            hasUrl = true
        } else {
            summary = context.getString(R.string.ftp_server_url_summary_no_local_inet_address)
            hasUrl = false
        }
        setSummary(summary)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.setOnCreateContextMenuListener(contextMenuListener)
    }

    private inner class ConnectivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (val action = intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION -> updateSummary()
                else -> throw IllegalArgumentException(action)
            }
        }
    }

    private inner class ContextMenuListener : OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?
        ) {
            if (!hasUrl) {
                return
            }
            val url = summary!!
            menu
                .setHeaderTitle(url)
                .apply {
                    add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.ftp_server_url_menu_copy_url)
                        .setOnMenuItemClickListener {
                            clipboardManager.copyText(url, context)
                            true
                        }
                }
                .apply {
                    if (!Settings.FTP_SERVER_ANONYMOUS_LOGIN.valueCompat) {
                        val password = Settings.FTP_SERVER_PASSWORD.valueCompat
                        if (password.isNotEmpty()) {
                            add(
                                Menu.NONE, Menu.NONE, Menu.NONE,
                                R.string.ftp_server_url_menu_copy_password
                            )
                                .setOnMenuItemClickListener {
                                    clipboardManager.copyText(password, context)
                                    true
                                }
                        }
                    }
                }
        }
    }
}
