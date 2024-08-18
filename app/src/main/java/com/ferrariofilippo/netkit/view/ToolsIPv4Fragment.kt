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
import com.ferrariofilippo.netkit.databinding.FragmentToolsIpv4Binding
import com.ferrariofilippo.netkit.viewmodel.ToolsIPv4ViewModel

class ToolsIPv4Fragment : Fragment() {
    private lateinit var viewModel: ToolsIPv4ViewModel

    private var _binding: FragmentToolsIpv4Binding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ToolsIPv4ViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsIpv4Binding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupUI()

        return binding.root
    }

    // UI
    private fun setupUI() {
        setupFindSubnetUI()
        setupGetNetworkUI()
    }

    private fun setupFindSubnetUI() {
        viewModel.validateFindSubnetSubnet = ::findSubnetManageSubnetMaskError
        viewModel.validateFindSubnetNumber = ::findSubnetManageSubnetNumberError

        binding.computeFindSubnetButton.setOnClickListener {
            if (viewModel.computeFindSubnet()) {
                binding.findSubnetValue.text = viewModel.findSubnetNetworkAddress
            }
        }
        binding.resetFindSubnetButton.setOnClickListener {
            viewModel.resetFindSubnet()
        }
    }

    private fun setupGetNetworkUI() {
        viewModel.validateGetNetworkHostIP = ::getNetworkManageHostIPError
        viewModel.validateGetNetworkSubnet = ::getNetworkManageSubnetMaskError

        binding.computeGetNetworkButton.setOnClickListener {
            if (viewModel.computeGetNetwork()) {
                binding.getNetworkValue.text = viewModel.getNetworkNetworkAddress
            }
        }
        binding.resetGetNetworkButton.setOnClickListener {
            viewModel.resetGetNetwork()
        }
    }

    // Validation
    // Find Subnet
    private fun findSubnetManageSubnetMaskError(error: Boolean) {
        binding.findSubnetSubnetTextInput.error =
            if (error) getString(R.string.invalid_subnet_mask)
            else null
    }

    private fun findSubnetManageSubnetNumberError(error: Boolean) {
        binding.findSubnetSubnetNumberInput.error =
            if (error) getString(R.string.invalid_subnet_number)
            else null
    }

    // Get Network
    private fun getNetworkManageHostIPError(error: Boolean) {
        binding.getNetworkHostIPInput.error =
            if (error) getString(R.string.invalid_ip)
            else null
    }

    private fun getNetworkManageSubnetMaskError(error: Boolean) {
        binding.getNetworkSubnetMaskInput.error =
            if (error) getString(R.string.invalid_subnet_mask)
            else null
    }
}
