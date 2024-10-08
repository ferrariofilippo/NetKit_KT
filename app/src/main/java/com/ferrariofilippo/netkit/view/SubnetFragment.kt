/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.adapters.SizeItemAdapter
import com.ferrariofilippo.netkit.adapters.SubnetItemAdapter
import com.ferrariofilippo.netkit.adapters.SubnetV6ItemAdapter
import com.ferrariofilippo.netkit.databinding.FragmentSubnetBinding
import com.ferrariofilippo.netkit.viewmodel.SubnetViewModel

class SubnetFragment : Fragment() {
    companion object {
        private const val IPV4_INDEX = 0
    }

    private lateinit var viewModel: SubnetViewModel

    private var _binding: FragmentSubnetBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SubnetViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubnetBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupUI()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // UI
    private fun setupRecyclers() {
        val sizesAdapter = SizeItemAdapter {
            viewModel.formatList()
        }
        val subnetsAdapter = SubnetItemAdapter()
        val subnetsV6Adapter = SubnetV6ItemAdapter()

        viewModel.setAdapters(sizesAdapter, subnetsAdapter, subnetsV6Adapter)

        binding.sizesRecyclerView.adapter = sizesAdapter
        binding.sizesRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.subnetsRecyclerView.adapter = subnetsAdapter
        binding.subnetsRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.subnetsIPv6RecyclerView.adapter = subnetsV6Adapter
        binding.subnetsIPv6RecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupUI() {
        val ipVersionPicker = binding.ipVersionPicker.editText as AutoCompleteTextView
        ipVersionPicker.setAdapter(
            ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                viewModel.ipVersions
            )
        )
        ipVersionPicker.setOnItemClickListener { _, _, position, _ ->
            viewModel.setIPVersionIndex(position)
        }
        ipVersionPicker.setText(viewModel.ipVersions[0], false)

        setupRecyclers()

        binding.computeSubnetsButton.setOnClickListener {
            if (viewModel.ipVersionIndex.value == IPV4_INDEX) {
                viewModel.computeIPv4()
            } else {
                viewModel.computeIPv6()
            }
        }
        binding.resetSubnetsButton.setOnClickListener {
            viewModel.reset()
        }

        viewModel.validateIPv6IPAddress = ::ipv6ManageIPAddressError
        viewModel.validateIPv6GlobalRoutingPrefix = ::ipv6ManageGlobalRoutingPrefixError
        viewModel.validateIPv6SubnetsCount = ::ipv6ManageSubnetsCountError
    }

    // Validation
    // IPv6
    private fun ipv6ManageIPAddressError(error: Boolean) {
        binding.ipv6AddressInput.error =
            if (error) getString(R.string.invalid_ip)
            else null
    }

    private fun ipv6ManageGlobalRoutingPrefixError(error: Boolean) {
        binding.globalRoutingPrefixInput.error =
            if (error) getString(R.string.invalid_prefix)
            else null
    }

    private fun ipv6ManageSubnetsCountError(error: Boolean) {
        binding.ipv6SubnetsCountInput.error =
            if (error) getString(R.string.invalid_subnet_count)
            else null
    }
}
