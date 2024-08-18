/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ferrariofilippo.netkit.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
            }

        setupUI()

        return binding.root
    }

    // UIs
    private fun setupUI() {
        binding.privacyButton.setOnClickListener {
            // TODO: Set appropriate link
            val uri = Uri.parse("https://netkit.vercel.app/privacy")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        binding.reportBugButton.setOnClickListener {
            // TODO: Set appropriate link
            val uri = Uri.parse("https://github.com/ferrariofilippo/NetKit_Release/issues/new")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
