/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.hierynomus.smbj.auth.AuthenticationContext
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditSmbServerFragmentBinding
import me.zhanghai.android.files.provider.smb.client.Authority
import me.zhanghai.android.files.ui.UnfilteredArrayAdapter
import me.zhanghai.android.files.util.ActionState
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getTextArray
import me.zhanghai.android.files.util.hideTextInputLayoutErrorOnTextChange
import me.zhanghai.android.files.util.isReady
import me.zhanghai.android.files.util.setResult
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.takeIfNotEmpty
import me.zhanghai.android.files.util.viewModels
import java.net.URI

class EditSmbServerFragment : Fragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { EditSmbServerViewModel() } }

    private lateinit var binding: EditSmbServerFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            launch { viewModel.connectState.collect { onConnectStateChanged(it) } }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EditSmbServerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.lifecycleScope.launchWhenCreated {
            activity.setSupportActionBar(binding.toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity.setTitle(
                if (args.server != null) {
                    R.string.storage_edit_smb_server_title_edit
                } else {
                    R.string.storage_edit_smb_server_title_add
                }
            )
        }

        binding.hostEdit.hideTextInputLayoutErrorOnTextChange(binding.hostLayout)
        binding.hostEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.portEdit.hideTextInputLayoutErrorOnTextChange(binding.portLayout)
        binding.portEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.pathEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.authenticationTypeEdit.setAdapter(
            UnfilteredArrayAdapter(
                binding.authenticationTypeEdit.context, R.layout.dropdown_item,
                objects = getTextArray(R.array.storage_edit_smb_server_authentication_type_entries)
            )
        )
        authenticationType = AuthenticationType.PASSWORD
        binding.authenticationTypeEdit.doAfterTextChanged {
            onAuthenticationTypeChanged(authenticationType)
            updateNamePlaceholder()
        }
        binding.usernameEdit.hideTextInputLayoutErrorOnTextChange(binding.usernameLayout)
        binding.usernameEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.domainEdit.doAfterTextChanged { updateNamePlaceholder() }
        binding.saveOrConnectAndAddButton.setText(
            if (args.server != null) {
                R.string.save
            } else {
                R.string.storage_edit_smb_server_connect_and_add
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
            if (args.server != null) R.string.remove else R.string.storage_edit_smb_server_add
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
                    binding.portEdit.setText(authority.port.toString())
                }
                when {
                    AuthenticationContext.guest().let {
                        authority.username == it.username && authority.domain == it.domain
                                && server.password == it.password.concatToString()
                    } -> authenticationType = AuthenticationType.GUEST
                    AuthenticationContext.anonymous().let {
                        authority.username == it.username && authority.domain == it.domain
                                && server.password == it.password.concatToString()
                    } -> authenticationType = AuthenticationType.ANONYMOUS
                    else -> {
                        authenticationType = AuthenticationType.PASSWORD
                        binding.usernameEdit.setText(authority.username)
                        binding.domainEdit.setText(authority.domain)
                        binding.passwordEdit.setText(server.password)
                    }
                }
                binding.pathEdit.setText(server.relativePath)
                binding.nameEdit.setText(server.customName)
            } else {
                val host = args.host
                if (host != null) {
                    binding.hostEdit.setText(host)
                }
            }
        }
    }

    private fun updateNamePlaceholder() {
        val host = binding.hostEdit.text.toString().takeIfNotEmpty()
        val port = binding.portEdit.text.toString().takeIfNotEmpty()?.toIntOrNull()
            ?: Authority.DEFAULT_PORT
        val path = binding.pathEdit.text.toString().trim()
        val username: String
        val domain: String?
        when (authenticationType) {
            AuthenticationType.PASSWORD -> {
                username = binding.usernameEdit.text.toString()
                domain = binding.domainEdit.text.toString().takeIfNotEmpty()
            }
            AuthenticationType.GUEST -> {
                AuthenticationContext.guest().let {
                    username = it.username
                    domain = it.domain
                }
            }
            AuthenticationType.ANONYMOUS -> {
                AuthenticationContext.anonymous().let {
                    username = it.username
                    domain = it.domain
                }
            }
        }
        binding.nameLayout.placeholderText = if (host != null) {
            val authority = Authority(host, port, username, domain)
            if (path.isNotEmpty()) "$authority/$path" else authority.toString()
        } else {
            getString(R.string.storage_edit_smb_server_name_placeholder)
        }
    }

    private var authenticationType: AuthenticationType
        get() {
            val adapter = binding.authenticationTypeEdit.adapter
            val items = List(adapter.count) { adapter.getItem(it) as CharSequence }
            val selectedItem = binding.authenticationTypeEdit.text
            val selectedIndex = items.indexOfFirst { TextUtils.equals(it, selectedItem) }
            return AuthenticationType.entries[selectedIndex]
        }
        set(value) {
            val adapter = binding.authenticationTypeEdit.adapter
            val item = adapter.getItem(value.ordinal) as CharSequence
            binding.authenticationTypeEdit.setText(item, false)
            onAuthenticationTypeChanged(value)
        }

    private fun onAuthenticationTypeChanged(authenticationType: AuthenticationType) {
        binding.passwordAuthenticationLayout.isVisible =
            authenticationType == AuthenticationType.PASSWORD
    }

    private fun saveOrAdd() {
        val server = getServerOrSetError() ?: return
        Storages.addOrReplace(server)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun connectAndAdd() {
        if (!viewModel.connectState.value.isReady) {
            return
        }
        val server = getServerOrSetError() ?: return
        viewModel.connect(server)
    }

    private fun onConnectStateChanged(state: ActionState<SmbServer, Unit>) {
        when (state) {
            is ActionState.Ready, is ActionState.Running -> {
                val isConnecting = state is ActionState.Running
                binding.progress.fadeToVisibilityUnsafe(isConnecting)
                binding.scrollView.fadeToVisibilityUnsafe(!isConnecting)
                binding.saveOrConnectAndAddButton.isEnabled = !isConnecting
                binding.removeOrAddButton.isEnabled = !isConnecting
            }
            is ActionState.Success -> {
                Storages.addOrReplace(state.argument)
                setResult(Activity.RESULT_OK)
                finish()
            }
            is ActionState.Error -> {
                val throwable = state.throwable
                throwable.printStackTrace()
                showToast(throwable.toString())
                viewModel.finishConnecting()
            }
        }
    }

    private fun remove() {
        Storages.remove(args.server!!)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun getServerOrSetError(): SmbServer? {
        var errorEdit: TextInputEditText? = null
        val host = binding.hostEdit.text.toString().takeIfNotEmpty()
            ?.let { URI::class.canonicalizeHost(it) }
        if (host == null) {
            binding.hostLayout.error = getString(R.string.storage_edit_smb_server_host_error_empty)
            if (errorEdit == null) {
                errorEdit = binding.hostEdit
            }
        } else if (!URI::class.isValidHost(host)) {
            binding.hostLayout.error =
                getString(R.string.storage_edit_smb_server_host_error_invalid)
            if (errorEdit == null) {
                errorEdit = binding.hostEdit
            }
        }
        val port = binding.portEdit.text.toString().takeIfNotEmpty()
            .let { if (it != null) it.toIntOrNull() else Authority.DEFAULT_PORT }
        if (port == null) {
            binding.portLayout.error =
                getString(R.string.storage_edit_smb_server_port_error_invalid)
            if (errorEdit == null) {
                errorEdit = binding.portEdit
            }
        }
        val path = binding.pathEdit.text.toString().trim()
        val name = binding.nameEdit.text.toString().takeIfNotEmpty()
        val username: String?
        val domain: String?
        val password: String
        when (authenticationType) {
            AuthenticationType.PASSWORD -> {
                username = binding.usernameEdit.text.toString().takeIfNotEmpty()
                if (username == null) {
                    binding.usernameLayout.error =
                        getString(R.string.storage_edit_smb_server_username_error_empty)
                    if (errorEdit == null) {
                        errorEdit = binding.usernameEdit
                    }
                }
                domain = binding.domainEdit.text.toString().takeIfNotEmpty()
                password = binding.passwordEdit.text.toString()
            }
            AuthenticationType.GUEST -> {
                AuthenticationContext.guest().let {
                    username = it.username
                    domain = it.domain
                    password = it.password.concatToString()
                }
            }
            AuthenticationType.ANONYMOUS -> {
                AuthenticationContext.anonymous().let {
                    username = it.username
                    domain = it.domain
                    password = it.password.concatToString()
                }
            }
        }
        if (errorEdit != null) {
            errorEdit.requestFocus()
            return null
        }
        val authority = Authority(host!!, port!!, username!!, domain)
        return SmbServer(args.server?.id, name, authority, password, path)
    }

    @Parcelize
    class Args(
        val server: SmbServer? = null,
        val host: String? = null
    ) : ParcelableArgs

    private enum class AuthenticationType {
        PASSWORD,
        GUEST,
        ANONYMOUS
    }
}
