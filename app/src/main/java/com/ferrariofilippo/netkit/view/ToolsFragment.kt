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
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ferrariofilippo.netkit.adapters.ToolsViewPagerAdapter
import com.ferrariofilippo.netkit.databinding.FragmentToolsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class ToolsFragment : Fragment() {
    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
            }

        setupTabs()

        return binding.root
    }

    // UI
    private fun setupTabs() {
        binding.toolsViewPager.adapter = ToolsViewPagerAdapter(requireActivity())
        binding.toolsTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.toolsViewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })

        binding.toolsViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.toolsTabLayout.getTabAt(position)?.select()
            }
        })
    }
}
