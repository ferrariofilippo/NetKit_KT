/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.model.data.SizeInfo
import com.ferrariofilippo.netkit.util.IPv4Util
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SizeItemAdapter(private val textChangedCallback: () -> Unit) :
    ListAdapter<SizeInfo, SizeItemAdapter.SizeItemViewHolder>(SizeItemComparator()) {
    override fun onBindViewHolder(holder: SizeItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeItemViewHolder {
        return SizeItemViewHolder.create(parent, textChangedCallback)
    }

    class SizeItemViewHolder(itemView: View, textChangedCallback: () -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val _sizeInput = itemView.findViewById<TextInputLayout>(R.id.sizeTextInput)
        private var _info: SizeInfo? = null

        init {
            _sizeInput.setEndIconOnClickListener {
                _sizeInput.editText?.text?.clear()
                syncData()
                textChangedCallback()
            }
            _sizeInput.editText?.setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    textChangedCallback()
                }
            }
            _sizeInput.editText?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun beforeTextChanged(seq: CharSequence?, s: Int, c: Int, a: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    syncData()
                    textChangedCallback()
                }
            })
        }

        fun bind(item: SizeInfo) {
            _sizeInput.editText?.text?.clear()
            _info = item
        }

        private fun syncData() {
            if (_info == null) {
                return
            }

            _info!!.sizeOrPrefix = (_sizeInput.editText as TextInputEditText).text.toString()
            if (_info!!.sizeOrPrefix.isBlank()) {
                _info!!.actualSize = 0u
            } else if (_info!!.sizeOrPrefix.isDigitsOnly()) {
                _info!!.actualSize = IPv4Util.getMinimumWasteSize(_info!!.sizeOrPrefix.toUInt())
            } else {
                val prefix = _info!!.sizeOrPrefix.substring(1)
                if (prefix.isNotBlank() and prefix.isDigitsOnly()) {
                    _info!!.actualSize = IPv4Util.getHostsCountByPrefixLength(prefix.toInt())
                } else {
                    _info!!.actualSize = 0u
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup, textChangedCallback: () -> Unit): SizeItemViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.size_item, parent, false)

                return SizeItemViewHolder(view, textChangedCallback)
            }
        }
    }

    class SizeItemComparator : DiffUtil.ItemCallback<SizeInfo>() {
        override fun areItemsTheSame(oldItem: SizeInfo, newItem: SizeInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SizeInfo, newItem: SizeInfo): Boolean {
            return oldItem.sizeOrPrefix == newItem.sizeOrPrefix
        }
    }
}
