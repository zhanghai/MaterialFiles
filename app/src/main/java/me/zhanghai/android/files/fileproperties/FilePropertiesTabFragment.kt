/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import me.zhanghai.android.files.databinding.FilePropertiesTabFragmentBinding
import me.zhanghai.android.files.databinding.FilePropertiesTabItemBinding
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.showToast

abstract class FilePropertiesTabFragment : Fragment() {
    protected lateinit var binding: FilePropertiesTabFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FilePropertiesTabFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener { refresh() }
    }

    abstract fun refresh()

    protected inline fun <T> bindView(stateful: Stateful<T>, block: ViewBuilder.(T) -> Unit) {
        val value = stateful.value
        val hasValue = value != null
        binding.progress.fadeToVisibilityUnsafe(stateful is Loading && !hasValue)
        binding.swipeRefreshLayout.isRefreshing = stateful is Loading && hasValue
        binding.errorText.fadeToVisibilityUnsafe(stateful is Failure && !hasValue)
        if (stateful is Failure) {
            stateful.throwable.printStackTrace()
            val error = stateful.throwable.toString()
            if (hasValue) {
                showToast(error)
            } else {
                binding.errorText.text = error
            }
        }
        binding.scrollView.fadeToVisibilityUnsafe(hasValue)
        if (value != null) {
            ViewBuilder(binding.linearLayout).apply {
                block(value)
                build()
            }
        }
    }

    protected class ViewBuilder(val linearLayout: LinearLayout) {
        private val scrapViews = mutableMapOf<Class<out ViewBinding>, MutableList<ViewBinding>>()

        init {
            linearLayout.forEach { view ->
                val binding = view.tag as ViewBinding
                scrapViews.getOrPut(binding.javaClass) { mutableListOf() } += binding
            }
            linearLayout.removeAllViews()
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : ViewBinding> getScrapItemBinding(bindingClass: Class<T>): T? =
            scrapViews[bindingClass]?.removeLastOrNull() as T?

        fun addView(binding: ViewBinding) {
            linearLayout.addView(binding.root)
        }

        fun addItemView(
            hint: String,
            text: String,
            onClickListener: ((View) -> Unit)? = null
        ): TextView {
            val itemBinding =
                getScrapItemBinding(FilePropertiesTabItemBinding::class.java)?.also { addView(it) }
                    ?: FilePropertiesTabItemBinding.inflate(
                        linearLayout.context.layoutInflater, linearLayout, true
                    )
                        .also { it.root.tag = it }
            itemBinding.textInputLayout.hint = hint
            itemBinding.textInputLayout.setDropDown(onClickListener != null)
            itemBinding.text.setText(text)
            itemBinding.text.setTextIsSelectable(onClickListener == null)
            itemBinding.text.setOnClickListener(onClickListener?.let { View.OnClickListener(it) })
            return itemBinding.text
        }

        fun addItemView(
            @StringRes hintRes: Int,
            text: String,
            onClickListener: ((View) -> Unit)? = null
        ): TextView = addItemView(linearLayout.context.getString(hintRes), text, onClickListener)

        fun build() {
            scrapViews.clear()
        }
    }
}
