/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.databinding.FragmentToolsIpv6Binding
import com.ferrariofilippo.netkit.viewmodel.ToolsIPv6ViewModel

class ToolsIPv6Fragment : Fragment() {
    private lateinit var viewModel: ToolsIPv6ViewModel

    private var _binding: FragmentToolsIpv6Binding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ToolsIPv6ViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsIpv6Binding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupUI()

        return binding.root
    }

    // UI
    // This method will be used to organize the setup of future IPv6 tools
    private fun setupUI() {
        setupCompressionUI()
    }

    private fun setupCompressionUI() {
        viewModel.validateIPAddressToSqueeze = ::ipCompressionManageIPError

        binding.resetCompressButton.setOnClickListener {
            viewModel.resetSqueeze()
        }
        binding.compressIPv6Button.setOnClickListener {
            if (viewModel.squeezeAddress()) {
                binding.compressedAddressValue.text = viewModel.squeezedAddress
            }
        }
    }

    // Validation
    // IP Compression
    private fun ipCompressionManageIPError(error: Boolean) {
        binding.ipToSqueezeTextInput.error =
            if (error) getString(R.string.invalid_ip)
            else null
    }
}
