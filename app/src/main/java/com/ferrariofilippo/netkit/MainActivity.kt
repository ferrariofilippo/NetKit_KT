/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    // Overrides
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

        val aboutButton = findViewById<Button>(R.id.about_button)
        aboutButton.setOnClickListener {
            findNavController(R.id.viewContainer).navigate(R.id.aboutFragment)
            setPageTitle(R.id.aboutFragment)
            aboutButton.visibility = View.GONE
        }

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController(R.id.viewContainer)
                navController.popBackStack()
                setPageTitle(
                    navController.currentBackStackEntry?.destination?.id ?: R.id.subnetFragment
                )
                aboutButton.visibility =
                    if (navController.currentBackStackEntry?.destination?.id == R.id.aboutFragment) View.GONE
                    else View.VISIBLE
            }
        })
    }

    // UI
    private fun onMenuItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.viewContainer)
        val destination = when (item.itemId) {
            R.id.subnet_bottom_menu_button -> R.id.subnetFragment
            R.id.tools_bottom_menu_button -> R.id.toolsFragment
            R.id.wildcards_bottom_menu_button -> R.id.wildcardFragment
            else -> null
        }

        val aboutButton = findViewById<Button>(R.id.about_button)
        if (destination != null &&
            (navController.currentDestination == null || navController.currentDestination!!.id != destination)
        ) {
            navController.clearBackStack(destination)
            navController.navigate(destination)
            setPageTitle(destination)
            aboutButton.visibility = View.VISIBLE

            return true
        }

        return false
    }

    private fun setPageTitle(fragmentId: Int) {
        findViewById<TextView>(R.id.pageTitleTextView).text = getString(when (fragmentId) {
            R.id.subnetFragment -> R.string.subnets
            R.id.toolsFragment -> R.string.tools
            R.id.wildcardFragment -> R.string.wildcards
            R.id.aboutFragment -> R.string.about
            else -> R.string.subnets
        })
    }
}
