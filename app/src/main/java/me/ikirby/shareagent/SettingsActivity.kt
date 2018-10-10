package me.ikirby.shareagent

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.settings)

        switch_hide_icon.isChecked = packageManager.isComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.MainLauncherActivity"))
        switch_hide_icon.setOnCheckedChangeListener { _, isChecked -> showLauncherIcon(isChecked) }
    }

    private fun showLauncherIcon(show: Boolean) {
        val component = ComponentName(packageName, "me.ikirby.shareagent.MainLauncherActivity")
        packageManager.setComponentEnabled(component, show)
    }
}
