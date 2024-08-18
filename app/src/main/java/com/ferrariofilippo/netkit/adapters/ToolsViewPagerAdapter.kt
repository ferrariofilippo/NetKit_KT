/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ferrariofilippo.netkit.view.ToolsIPv4Fragment
import com.ferrariofilippo.netkit.view.ToolsIPv6Fragment

class ToolsViewPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return if (position == 0) ToolsIPv4Fragment() else ToolsIPv6Fragment()
    }

    override fun getItemCount(): Int {
        return 2
    }
}
