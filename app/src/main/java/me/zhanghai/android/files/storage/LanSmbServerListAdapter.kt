/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.files.databinding.LanSmbServerItemBinding
import me.zhanghai.android.files.ui.SimpleAdapter
import me.zhanghai.android.files.util.layoutInflater

class LanSmbServerListAdapter(
    val listener: (LanSmbServer) -> Unit
) : SimpleAdapter<LanSmbServer, LanSmbServerListAdapter.ViewHolder>() {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LanSmbServerItemBinding.inflate(parent.context.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val server = getItem(position)
        val binding = holder.binding
        binding.itemLayout.setOnClickListener { listener(server) }
        binding.hostText.text = server.host
        binding.addressText.text = server.address.hostAddress
    }

    class ViewHolder(val binding: LanSmbServerItemBinding) : RecyclerView.ViewHolder(binding.root)
}
