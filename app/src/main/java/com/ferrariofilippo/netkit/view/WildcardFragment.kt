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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.adapters.WildcardItemAdapter
import com.ferrariofilippo.netkit.databinding.FragmentWildcardBinding
import com.ferrariofilippo.netkit.model.enums.NetworkClass
import com.ferrariofilippo.netkit.model.enums.WildcardMethods
import com.ferrariofilippo.netkit.viewmodel.WildcardViewModel

class WildcardFragment : Fragment() {
    private lateinit var viewModel: WildcardViewModel

    private var _binding: FragmentWildcardBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[WildcardViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWildcardBinding
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
    private fun setupUI() {
        setupMethodPicker()
        setupClassPicker()

        binding.resetWildcardsButton.setOnClickListener {
            viewModel.reset()
        }
        binding.computeWildcardsButton.setOnClickListener {
            viewModel.compute()
        }

        viewModel.validateLowerBound = ::manageLowerBoundError
        viewModel.validateUpperBound = ::manageUpperBoundError
        viewModel.validateNetworkString = ::manageNetworkAddressError

        setupRecycler()
    }

    private fun setupMethodPicker() {
        val methodPicker = binding.wildcardMethodPicker.editText as AutoCompleteTextView
        methodPicker.setAdapter(
            ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                viewModel.wildcardMethods
            )
        )
        methodPicker.setOnItemClickListener { _, _, position, _ ->
            viewModel.setWildcardMethod(WildcardMethods.entries[position])
        }
        methodPicker.setText(viewModel.wildcardMethods[0], false)
    }

    private fun setupClassPicker() {
        val classPicker = binding.wildcardClassPicker.editText as AutoCompleteTextView
        classPicker.setAdapter(
            ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                viewModel.networkClasses
            )
        )
        classPicker.setOnItemClickListener { _, _, position, _ ->
            viewModel.setNetworkClass(NetworkClass.entries[position])
        }
        classPicker.setText(NetworkClass.entries[0].toString(), false)
    }

    private fun setupRecycler() {
        val adapter = WildcardItemAdapter()

        viewModel.setAdapter(adapter)
        binding.wildcardsRecyclerView.adapter = adapter
        binding.wildcardsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    // Validation
    private fun manageLowerBoundError(error: Boolean) {
        binding.wildcardLowerBoundInput.error =
            if (error) getString(R.string.invalid_lower_bound)
            else null
    }

    private fun manageUpperBoundError(error: Boolean) {
        binding.wildcardUpperBoundInput.error =
            if (error) getString(R.string.invalid_upper_bound)
            else null
    }

    private fun manageNetworkAddressError(error: Boolean) {
        binding.wildcardNetworkAddressInput.error =
            if (error) getString(R.string.invalid_network_address)
            else null
    }
}
