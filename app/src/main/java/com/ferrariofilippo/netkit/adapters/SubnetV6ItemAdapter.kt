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

class SubnetV6ItemAdapter :
    ListAdapter<String, SubnetV6ItemAdapter.SubnetV6ItemViewHolder>(SubnetV6ItemComparator()) {
    override fun onBindViewHolder(holder: SubnetV6ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubnetV6ItemViewHolder {
        return SubnetV6ItemViewHolder.create(parent)
    }

    class SubnetV6ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val networkTextView = itemView.findViewById<TextView>(R.id.networkAddressV6Value)

        fun bind(address: String) {
            networkTextView.text = address
        }

        companion object {
            fun create(parent: ViewGroup): SubnetV6ItemViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.subnet_v6_item, parent, false)

                return SubnetV6ItemViewHolder(view)
            }
        }
    }

    class SubnetV6ItemComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }
}
