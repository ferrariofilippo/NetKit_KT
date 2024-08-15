/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.model.data.ACE

class WildcardItemAdapter :
    ListAdapter<ACE, WildcardItemAdapter.WildcardItemViewHolder>(WildcardItemComparator()) {
    override fun onBindViewHolder(
        holder: WildcardItemAdapter.WildcardItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WildcardItemAdapter.WildcardItemViewHolder {
        return WildcardItemViewHolder.create(parent)
    }

    class WildcardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val _wildcardMaskText = itemView.findViewById<TextView>(R.id.wildcardMaskValue)
        private val _supportIPText = itemView.findViewById<TextView>(R.id.supportIPValue)

        fun bind(item: ACE) {
            _wildcardMaskText.text = item.wildcardMask
            _supportIPText.text = item.supportIP
        }

        companion object {
            fun create(parent: ViewGroup): WildcardItemViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.wildcard_item, parent, false)

                return WildcardItemViewHolder(view)
            }
        }
    }

    class WildcardItemComparator : DiffUtil.ItemCallback<ACE>() {
        override fun areItemsTheSame(oldItem: ACE, newItem: ACE): Boolean {
            return oldItem.wildcardMask == newItem.wildcardMask && oldItem.supportIP == newItem.supportIP
        }

        override fun areContentsTheSame(oldItem: ACE, newItem: ACE): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }
}