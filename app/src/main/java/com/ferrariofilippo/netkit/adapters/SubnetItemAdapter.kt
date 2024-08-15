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
import com.ferrariofilippo.netkit.model.data.NetworkInfo

class SubnetItemAdapter :
    ListAdapter<NetworkInfo, SubnetItemAdapter.SubnetItemViewHolder>(SubnetItemComparator()) {
    override fun onBindViewHolder(holder: SubnetItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubnetItemViewHolder {
        return SubnetItemViewHolder.create(parent)
    }

    class SubnetItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val networkTextView = itemView.findViewById<TextView>(R.id.networkAddressValue)
        private val broadcastTextView = itemView.findViewById<TextView>(R.id.broadcastAddressValue)
        private val subnetMaskTextView = itemView.findViewById<TextView>(R.id.subnetMaskValue)
        private val hostsCountTextView = itemView.findViewById<TextView>(R.id.hostCountValue)

        fun bind(item: NetworkInfo) {
            networkTextView.text = item.networkAddress
            broadcastTextView.text = item.broadcastAddress
            subnetMaskTextView.text = item.subnetMask
            hostsCountTextView.text = item.hostCount.toString()
        }

        companion object {
            fun create(parent: ViewGroup): SubnetItemViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.subnet_item, parent, false)

                return SubnetItemViewHolder(view)
            }
        }
    }

    class SubnetItemComparator : DiffUtil.ItemCallback<NetworkInfo>() {
        override fun areItemsTheSame(oldItem: NetworkInfo, newItem: NetworkInfo): Boolean {
            return oldItem.networkAddress == newItem.networkAddress &&
                    oldItem.subnetMask == newItem.subnetMask
        }

        override fun areContentsTheSame(oldItem: NetworkInfo, newItem: NetworkInfo): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }
}
