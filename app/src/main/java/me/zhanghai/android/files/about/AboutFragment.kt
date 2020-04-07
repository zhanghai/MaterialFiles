/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.about

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import me.zhanghai.android.files.databinding.AboutFragmentBinding
import me.zhanghai.android.files.ui.LicensesDialogFragment
import me.zhanghai.android.files.util.createViewIntent
import me.zhanghai.android.files.util.startActivitySafe

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        AboutFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        binding.gitHubLayout.setOnClickListener { startActivitySafe(GITHUB_URI.createViewIntent()) }
        binding.licensesLayout.setOnClickListener {
            // @see https://github.com/zhanghai/MaterialFiles/issues/161
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LicensesDialogFragment.show(this)
            } else {
                startActivitySafe(LICENSES_URI.createViewIntent())
            }
        }
        binding.privacyPolicyLayout.setOnClickListener {
            startActivitySafe(PRIVACY_POLICY_URI.createViewIntent())
        }
        binding.authorNameLayout.setOnClickListener {
            startActivitySafe(AUTHOR_RESUME_URI.createViewIntent())
        }
        binding.authorGitHubLayout.setOnClickListener {
            startActivitySafe(AUTHOR_GITHUB_URI.createViewIntent())
        }
        binding.authorGooglePlusLayout.setOnClickListener {
            startActivitySafe(AUTHOR_GOOGLE_PLUS_URI.createViewIntent())
        }
        binding.authorTwitterLayout.setOnClickListener {
            startActivitySafe(AUTHOR_TWITTER_URI.createViewIntent())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                // This recreates MainActivity but we cannot have singleTop as launch mode along
                // with document launch mode.
                //AppCompatActivity activity = (AppCompatActivity) requireActivity();
                //activity.onSupportNavigateUp();
                requireActivity().finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    companion object {
        private val GITHUB_URI = Uri.parse("https://github.com/zhanghai/MaterialFiles")
        private val LICENSES_URI = Uri.parse(
            "https://github.com/zhanghai/MaterialFiles/blob/master/app/src/main/res/raw" +
                "/licenses.xml"
        )
        private val PRIVACY_POLICY_URI =
            Uri.parse("https://github.com/zhanghai/MaterialFiles/blob/master/PRIVACY.md")
        private val AUTHOR_RESUME_URI = Uri.parse("https://resume.zhanghai.me/")
        private val AUTHOR_GITHUB_URI = Uri.parse("https://github.com/zhanghai")
        private val AUTHOR_GOOGLE_PLUS_URI =
            Uri.parse("https://plus.google.com/100015937320889992498")
        private val AUTHOR_TWITTER_URI = Uri.parse("https://twitter.com/zhanghai95")
    }
}
