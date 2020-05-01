/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.apk

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.observe
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java8.nio.file.Path
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.longVersionCodeCompat
import me.zhanghai.android.files.databinding.FilePropertiesApkTabFragmentBinding
import me.zhanghai.android.files.databinding.FilePropertiesApkTabFragmentMd2Binding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.fadeInUnsafe
import me.zhanghai.android.files.util.fadeOutUnsafe
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.getStringArray
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.viewModels

class FilePropertiesApkTabFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FilePropertiesApkTabViewModel(args.path) } }

    private lateinit var binding: Binding

    private var lastIsSuccess = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        Binding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener { refresh() }

        viewModel.apkInfoLiveData.observe(viewLifecycleOwner) { onApkInfoChanged(it) }
    }

    private fun refresh() {
        viewModel.reload()
    }

    private fun onApkInfoChanged(stateful: Stateful<ApkInfo>) {
        when (stateful) {
            is Loading -> {
                binding.progress.fadeToVisibilityUnsafe(!lastIsSuccess)
                if (lastIsSuccess) {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                binding.errorText.fadeOutUnsafe()
                if (!lastIsSuccess) {
                    binding.scrollView.isInvisible = true
                }
            }
            is Failure -> {
                binding.progress.fadeOutUnsafe()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.errorText.fadeInUnsafe()
                binding.errorText.text = stateful.throwable.toString()
                binding.scrollView.fadeOutUnsafe()
                lastIsSuccess = false
            }
            is Success -> {
                binding.progress.fadeOutUnsafe()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.errorText.fadeOutUnsafe()
                binding.scrollView.fadeInUnsafe()
                updateView(stateful.value)
                lastIsSuccess = true
            }
        }
    }

    private fun updateView(apkInfo: ApkInfo) {
        binding.labelText.text = apkInfo.label
        val packageInfo = apkInfo.packageInfo
        binding.packageNameText.text = packageInfo.packageName
        binding.versionText.text = getString(
            R.string.file_properties_apk_version_format, packageInfo.versionName,
            packageInfo.longVersionCodeCompat
        )
        val applicationInfo = packageInfo.applicationInfo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.minSdkVersionText.text = getSdkVersionText(applicationInfo.minSdkVersion)
        } else {
            // PackageParser didn't return minSdkVersion before N, so it's hard to implement a
            // compat version.
            binding.minSdkVersionLayout.isVisible = false
        }
        binding.targetSdkVersionText.text = getSdkVersionText(applicationInfo.targetSdkVersion)
        binding.signatureDigestsText.text = apkInfo.signingCertificateDigests.joinToString("\n")
        binding.pastSignatureDigestsLayout.isVisible =
            apkInfo.pastSigningCertificateDigests.isNotEmpty()
        binding.pastSignatureDigestsText.text =
            apkInfo.pastSigningCertificateDigests.joinToString("\n")
    }

    private fun getSdkVersionText(sdkVersion: Int): String {
        val names = getStringArray(R.array.file_properites_apk_sdk_version_names)
        val codeNames = getStringArray(R.array.file_properites_apk_sdk_version_codenames)
        return getString(
            R.string.file_properites_apk_sdk_version_format,
            names[sdkVersion.coerceIn(names.indices)],
            codeNames[sdkVersion.coerceIn(codeNames.indices)], sdkVersion
        )
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean =
            file.mimeType == MimeType.APK && file.path.isLinuxPath
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs

    private class Binding private constructor(
        val root: View,
        val progress: ProgressBar,
        val errorText: TextView,
        val swipeRefreshLayout: SwipeRefreshLayout,
        val scrollView: NestedScrollView,
        val labelText: TextView,
        val packageNameText: TextView,
        val versionText: TextView,
        val minSdkVersionLayout: ViewGroup,
        val minSdkVersionText: TextView,
        val targetSdkVersionText: TextView,
        val signatureDigestsText: TextView,
        val pastSignatureDigestsLayout: ViewGroup,
        val pastSignatureDigestsText: TextView
    ) {
        companion object {
            fun inflate(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean
            ): Binding =
                if (Settings.MATERIAL_DESIGN_2.valueCompat) {
                    val binding = FilePropertiesApkTabFragmentMd2Binding.inflate(
                        inflater, root, attachToRoot
                    )
                    Binding(
                        binding.root, binding.progress, binding.errorText,
                        binding.swipeRefreshLayout, binding.scrollView, binding.labelText,
                        binding.packageNameText, binding.versionText, binding.minSdkVersionLayout,
                        binding.minSdkVersionText, binding.targetSdkVersionText,
                        binding.signatureDigestsText, binding.pastSignatureDigestsLayout,
                        binding.pastSignatureDigestsText
                    )
                } else {
                    val binding = FilePropertiesApkTabFragmentBinding.inflate(
                        inflater, root, attachToRoot
                    )
                    Binding(
                        binding.root, binding.progress, binding.errorText,
                        binding.swipeRefreshLayout, binding.scrollView, binding.labelText,
                        binding.packageNameText, binding.versionText, binding.minSdkVersionLayout,
                        binding.minSdkVersionText, binding.targetSdkVersionText,
                        binding.signatureDigestsText, binding.pastSignatureDigestsLayout,
                        binding.pastSignatureDigestsText
                    )
                }
        }
    }
}
