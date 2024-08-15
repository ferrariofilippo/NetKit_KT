/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<NavigationBarView>(R.id.bottom_navigation_menu).setOnItemSelectedListener {
            onMenuItemSelected(it)
        }

        findViewById<MaterialButton>(R.id.about_button).setOnClickListener {
            findNavController(R.id.viewContainer).navigate(R.id.aboutFragment)
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.viewContainer)
        val destination = when (item.itemId) {
            R.id.subnet_bottom_menu_button -> R.id.subnetFragment
            R.id.tools_bottom_menu_button -> R.id.toolsFragment
            R.id.wildcards_bottom_menu_button -> R.id.wildcardFragment
            else -> null
        }

        if (destination != null &&
            (navController.currentDestination == null || navController.currentDestination!!.id != destination)
        ) {
            navController.clearBackStack(destination)
            navController.navigate(destination)

            return true
        }

        return false
    }
}
