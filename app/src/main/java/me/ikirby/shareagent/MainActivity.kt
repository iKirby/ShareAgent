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

        switch_save.isChecked = packageManager.isComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.SaveActivity"))
        switch_forward.isChecked = packageManager.isComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.ForwardActivity"))

        switch_save.setOnCheckedChangeListener { _, checked -> setSaveActionEnabled(checked) }
        switch_forward.setOnCheckedChangeListener { _, checked -> setForwardActionEnabled(checked) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun setSaveActionEnabled(enabled: Boolean) {
        packageManager.setComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.SaveActivity"), enabled)
    }

    private fun setForwardActionEnabled(enabled: Boolean) {
        packageManager.setComponentEnabled(ComponentName(packageName, "me.ikirby.shareagent.ForwardActivity"), enabled)
    }
}
