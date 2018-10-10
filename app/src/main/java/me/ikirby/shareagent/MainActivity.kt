package me.ikirby.shareagent

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isPermissionGranted()) {
            requestPermission()
        }

        if (intent.action == Intent.ACTION_APPLICATION_PREFERENCES) {
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        switch_save.isChecked = isSaveActionEnabled()
        switch_forward.isChecked = isForwardActionEnabled()

        switch_save.setOnCheckedChangeListener { _, checked -> setSaveActionEnabled(checked) }
        switch_forward.setOnCheckedChangeListener { _, checked -> setForwardActionEnabled(checked) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.hide_icon -> showHideIcon()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted() =
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() = requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
    )

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionExplanationDialog()
            }
        }
    }

    private fun showPermissionExplanationDialog() =
            AlertDialog.Builder(this)
                    .setTitle(R.string.permission_explanation_dialog)
                    .setMessage(R.string.permission_explanation)
                    .setPositiveButton(android.R.string.ok) { dialogInterface, _ -> dialogInterface.cancel() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
                    .setCancelable(false)
                    .setOnDismissListener { requestPermission() }
                    .create()
                    .show()

    private fun isSaveActionEnabled() = packageManager.getComponentEnabledSetting(
            ComponentName(packageName, "me.ikirby.shareagent.SaveActivity")
    ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    private fun isForwardActionEnabled() = packageManager.getComponentEnabledSetting(
            ComponentName(packageName, "me.ikirby.shareagent.ForwardActivity")
    ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    private fun isLauncherIconHidden() = packageManager.getComponentEnabledSetting(
            ComponentName(packageName, "me.ikirby.shareagent.MainLauncherActivity")
    ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    private fun setSaveActionEnabled(enabled: Boolean) {
        setComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.SaveActivity"), enabled)
    }

    private fun setForwardActionEnabled(enabled: Boolean) {
        setComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.ForwardActivity"), enabled)
    }

    private fun setComponentEnabled(component: ComponentName, enabled: Boolean) {
        val state = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        packageManager.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
    }

    private fun showHideIcon() {
        val component = ComponentName(packageName, "me.ikirby.shareagent.MainLauncherActivity")
        if (isLauncherIconHidden()) {
            setComponentEnabled(component, true)
            Toast.makeText(this, R.string.icon_shown, Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.hide_icon)
                    .setMessage(R.string.hide_icon_message)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        setComponentEnabled(component, false)
                        Toast.makeText(this, R.string.icon_hidden, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialogInterface, _ -> dialogInterface.cancel() }
                    .create()
                    .show()
        }
    }
}
