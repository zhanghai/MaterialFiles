/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditSftpServerFragmentBinding
import me.zhanghai.android.files.provider.sftp.client.Authority
import me.zhanghai.android.files.provider.sftp.client.PasswordAuthentication
import me.zhanghai.android.files.provider.sftp.client.PublicKeyAuthentication
import me.zhanghai.android.files.ui.UnfilteredArrayAdapter
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getTextArray
import me.zhanghai.android.files.util.hideTextInputLayoutErrorOnTextChange
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.takeIfNotEmpty
import me.zhanghai.android.files.util.viewModels

class EditSftpServerFragment : Fragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { EditSftpServerViewModel() } }

    private lateinit var binding: EditSftpServerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EditSftpServerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.setTitle(
            if (args.server != null) {
                R.string.storage_edit_sftp_server_title_edit
            } else {
                R.string.storage_edit_sftp_server_title_add
            }
        )

        binding.hostEdit.hideTextInputLayoutErrorOnTextChange(binding.hostLayout)
        binding.hostEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.portEdit.hideTextInputLayoutErrorOnTextChange(binding.portLayout)
        binding.portEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.authenticationTypeEdit.setAdapter(
            UnfilteredArrayAdapter(
                binding.authenticationTypeEdit.context, R.layout.dropdown_item,
                objects = getTextArray(R.array.storage_edit_sftp_server_authentication_type_entries)
            )
        )
        authenticationType = AuthenticationType.PASSWORD
        binding.authenticationTypeEdit.doAfterTextChanged {
            onAuthenticationTypeChanged(authenticationType)
        }
        binding.usernameEdit.hideTextInputLayoutErrorOnTextChange(binding.usernameLayout)
        binding.privateKeyEdit.hideTextInputLayoutErrorOnTextChange(binding.privateKeyLayout)
        binding.saveOrConnectAndAddButton.setText(
            if (args.server != null) {
                R.string.save
            } else {
                R.string.storage_edit_sftp_server_connect_and_add
            }
        )
        binding.saveOrConnectAndAddButton.setOnClickListener {
            if (args.server != null) {
                saveOrAdd()
            } else {
                connectAndAdd()
            }
        }
        binding.cancelButton.setOnClickListener { finish() }
        binding.removeOrAddButton.setText(
            if (args.server != null) R.string.remove else R.string.storage_edit_sftp_server_add
        )
        binding.removeOrAddButton.setOnClickListener {
            if (args.server != null) {
                remove()
            } else {
                saveOrAdd()
            }
        }

        if (savedInstanceState == null) {
            val server = args.server
            if (server != null) {
                val authority = server.authority
                binding.hostEdit.setText(authority.host)
                if (authority.port != Authority.DEFAULT_PORT) {
                    binding.portEdit.setText(authority.port)
                }
                val authentication = server.authentication
                binding.usernameEdit.setText(authentication.username)
                when (authentication) {
                    is PasswordAuthentication -> {
                        authenticationType = AuthenticationType.PASSWORD
                        binding.passwordEdit.setText(authentication.password)
                    }
                    is PublicKeyAuthentication -> {
                        authenticationType = AuthenticationType.PUBLIC_KEY
                        binding.privateKeyEdit.setText(authentication.privateKey)
                    }
                }
            }
        }

        viewModel.connectStatefulLiveData.observe(viewLifecycleOwner) {
            onConnectStatefulChanged(it)
        }
    }

    private fun updateNamePlaceholder() {
        val host = binding.hostEdit.text.toString().takeIfNotEmpty()
        val port = binding.portEdit.text.toString().takeIfNotEmpty()
            .let { if (it != null) it.toIntOrNull() else Authority.DEFAULT_PORT }
        binding.nameLayout.placeholderText = if (host != null && port != null) {
            Authority(host, port).toString()
        } else if (host != null) {
            host
        } else {
            getString(R.string.storage_edit_sftp_server_name_placeholder)
        }
    }

    private var authenticationType: AuthenticationType
        get() {
            val adapter = binding.authenticationTypeEdit.adapter
            val items = List(adapter.count) { adapter.getItem(it) as CharSequence }
            val selectedItem = binding.authenticationTypeEdit.text
            val selectedIndex = items.indexOfFirst { TextUtils.equals(it, selectedItem) }
            return AuthenticationType.values()[selectedIndex]
        }
        set(value) {
            val adapter = binding.authenticationTypeEdit.adapter
            val item = adapter.getItem(value.ordinal) as CharSequence
            binding.authenticationTypeEdit.setText(item, false)
            onAuthenticationTypeChanged(value)
        }

    private fun onAuthenticationTypeChanged(authenticationType: AuthenticationType) {
        binding.passwordLayout.isVisible = authenticationType == AuthenticationType.PASSWORD
        binding.privateKeyLayout.isVisible = authenticationType == AuthenticationType.PUBLIC_KEY
    }

    private fun saveOrAdd() {
        val server = getServerOrSetError() ?: return
        Storages.addOrReplace(server)
        finish()
    }

    private fun connectAndAdd() {
        if (!viewModel.connectStatefulLiveData.isReady) {
            return
        }
        val server = getServerOrSetError() ?: return
        viewModel.connectStatefulLiveData.connect(server)
    }

    private fun onConnectStatefulChanged(connectStateful: Stateful<SftpServer>) {
        val liveData = viewModel.connectStatefulLiveData
        when (connectStateful) {
            is Loading -> {}
            is Failure -> {
                connectStateful.throwable.printStackTrace()
                showToast(connectStateful.throwable.toString())
                liveData.reset()
            }
            is Success -> {
                Storages.addOrReplace(connectStateful.value)
                finish()
                return
            }
        }
        val isConnecting = !liveData.isReady
        binding.progress.fadeToVisibilityUnsafe(isConnecting)
        binding.scrollView.fadeToVisibilityUnsafe(!isConnecting)
        binding.saveOrConnectAndAddButton.isEnabled = !isConnecting
        binding.removeOrAddButton.isEnabled = !isConnecting
    }

    private fun remove() {
        Storages.remove(args.server!!)
        finish()
    }

    private fun getServerOrSetError(): SftpServer? {
        var errorEdit: TextInputEditText? = null
        val host = binding.hostEdit.text.toString().takeIfNotEmpty()
        if (host == null) {
            binding.hostLayout.error =
                getString(R.string.storage_edit_sftp_server_host_error_empty)
            if (errorEdit == null) {
                errorEdit = binding.hostEdit
            }
        }
        val port = binding.portEdit.text.toString().takeIfNotEmpty()
            .let { if (it != null) it.toIntOrNull() else Authority.DEFAULT_PORT }
        if (port == null) {
            binding.portLayout.error = getString(R.string.storage_edit_sftp_server_port_error_invalid)
            if (errorEdit == null) {
                errorEdit = binding.portEdit
            }
        }
        val name = binding.nameEdit.text.toString().takeIfNotEmpty()
        val username = binding.usernameEdit.text.toString().takeIfNotEmpty()
        if (username == null) {
            binding.usernameLayout.error =
                getString(R.string.storage_edit_sftp_server_username_error_empty)
            if (errorEdit == null) {
                errorEdit = binding.usernameEdit
            }
        }
        val authentication = when (authenticationType) {
            AuthenticationType.PASSWORD -> {
                val password = binding.passwordEdit.text.toString()
                if (errorEdit == null) PasswordAuthentication(username!!, password) else null
            }
            AuthenticationType.PUBLIC_KEY -> {
                val privateKey = binding.privateKeyEdit.text.toString().takeIfNotEmpty()
                if (privateKey == null) {
                    binding.privateKeyLayout.error =
                        getString(R.string.storage_edit_sftp_server_private_key_error_empty)
                    if (errorEdit == null) {
                        errorEdit = binding.privateKeyEdit
                    }
                } else if (!PublicKeyAuthentication.validatePrivateKey(privateKey)) {
                    binding.privateKeyLayout.error =
                        getString(R.string.storage_edit_sftp_server_private_key_error_invalid)
                    if (errorEdit == null) {
                        errorEdit = binding.privateKeyEdit
                    }
                }
                if (errorEdit == null) PublicKeyAuthentication(username!!, privateKey!!) else null
            }
        }
        if (errorEdit != null) {
            errorEdit.requestFocus()
            return null
        }
        val authority = Authority(host!!, port!!)
        return SftpServer(args.server?.id, name, authority, authentication!!)
    }

    @Parcelize
    class Args(val server: SftpServer? = null) : ParcelableArgs

    private enum class AuthenticationType {
        PASSWORD,
        PUBLIC_KEY
    }
}
