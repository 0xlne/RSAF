/*
 * SPDX-FileCopyrightText: 2023-2024 Andrew Gunnerson
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.chiller3.rsaf

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.chiller3.rsaf.databinding.SettingsActivityBinding

abstract class PreferenceBaseActivity : AppCompatActivity() {
    protected abstract val actionBarTitle: CharSequence?

    protected abstract val showUpButton: Boolean

    protected abstract fun createFragment(): PreferenceBaseFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transaction = supportFragmentManager.beginTransaction()

        // https://issuetracker.google.com/issues/181805603
        val bioFragment = supportFragmentManager
            .findFragmentByTag("androidx.biometric.BiometricFragment")
        if (bioFragment != null) {
            transaction.remove(bioFragment)
        }

        val fragment: PreferenceBaseFragment

        if (savedInstanceState == null) {
            fragment = createFragment()
            transaction.replace(R.id.settings, fragment)
        } else {
            fragment = supportFragmentManager.findFragmentById(R.id.settings)
                    as PreferenceBaseFragment
        }

        transaction.commit()

        supportFragmentManager.setFragmentResultListener(fragment.requestTag, this) { _, result ->
            setResult(RESULT_OK, Intent().apply { putExtras(result) })
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(showUpButton)

        actionBarTitle?.let {
            setTitle(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
