package com.b_lam.resplash.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.NavUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivitySettingsBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.setupActionBar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity(R.layout.activity_settings) {

    override val viewModel: SettingsViewModel by viewModel()

    override val binding: ActivitySettingsBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            setTitle(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }

    override fun onBackPressed() {
        if (viewModel.shouldRestartMainActivity ||
            intent.getBooleanExtra(EXTRA_SHOULD_RESTART, false)) {
            NavUtils.navigateUpFromSameTask(this)
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        const val EXTRA_SHOULD_RESTART = "extra_should_restart"
    }

    class SettingsFragment :
        PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val sharedViewModel: SettingsViewModel by sharedViewModel()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val clearCachePreference = findPreference<Preference>("clear_cache")
            with(sharedViewModel) {
                glideCacheSize.observe(viewLifecycleOwner) { cacheSize ->
                    clearCachePreference?.summary = getString(R.string.cache_size, cacheSize)
                }
                clearCachePreference?.setOnPreferenceClickListener {
                    launchClearCache()
                    true
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == "language" || key == "layout" ||
                key == "load_quality" || key == "long_press_download") {
                sharedViewModel.shouldRestartMainActivity = true
            }

            if (key == "language") {
                activity?.finish()
                activity?.overrideActivityTransition( OVERRIDE_TRANSITION_OPEN, 0, R.anim.bottom_sheet_slide_in)
                activity?.intent?.apply {
                    putExtra(EXTRA_SHOULD_RESTART, true)
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }?.let { startActivity(it) }
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}