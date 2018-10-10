package me.ikirby.shareagent

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.settings)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        switch_hide_icon.isChecked = !packageManager.isComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.MainLauncherActivity"))
        switch_hide_icon.setOnCheckedChangeListener { _, isChecked -> showLauncherIcon(!isChecked) }

        switch_remove_url_params.isChecked = preferences.getBoolean("remove_url_params_enabled", false)
        switch_remove_url_params.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("remove_url_params_enabled", isChecked).apply()
        }

        button_config_url.setOnClickListener {
            startActivity(Intent(this, UrlConfigActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLauncherIcon(show: Boolean) {
        val component = ComponentName(packageName, "me.ikirby.shareagent.MainLauncherActivity")
        packageManager.setComponentEnabled(component, show)
    }
}
