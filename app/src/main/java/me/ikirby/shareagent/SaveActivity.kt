package me.ikirby.shareagent

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.Toast
import java.io.File

class SaveActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type
            when {
                type == "text/plain" -> handleText(intent)
                type?.startsWith("image/") == true -> handleImage(intent)
                else -> handleOtherFile(intent)
            }
        } else finish()
    }

    override fun onBackPressed() {
    }

    private fun handleText(intent: Intent) {
        var text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (text == null) {
            handleOtherFile(intent)
            return
        }
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val removeParamsEnabled = preference.getBoolean("remove_url_params_enabled", false)
        if (removeParamsEnabled && text.isURL()) {
            val paramsToRemove = preference.getString("remove_params", "")
            if (paramsToRemove != null && paramsToRemove.isNotBlank()) {
                text = removeParamsFromURL(text, paramsToRemove.split(","))
            }
        }
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val content = if (subject != null || text.contains(subject)) "$subject\n$text" else text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SaveHelperCopy", content)
        clipboardManager.primaryClip = clip
        Toast.makeText(this, R.string.text_copied, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleIntent(intent: Intent, run: (Uri) -> Unit) {
        val uri = if (intent.data != null) intent.data else intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (uri != null) {
            setContentView(R.layout.activity_progress)
            Thread(Runnable { run.invoke(uri) }).start()
        } else {
            Toast.makeText(this, R.string.uri_invalid, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun handleImage(intent: Intent) {
        handleIntent(intent) { uri ->
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val fileName = File(uri.path).name
            val relativePath = if (fileName.contains("screenshot", true)) "Screenshots/" else ""
            saveFile(fileName, dir, relativePath, uri)
        }
    }

    private fun handleOtherFile(intent: Intent) {
        handleIntent(intent) { uri ->
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "ShareHelper/${File(uri.path).name}"
            saveFile(fileName, dir, "", uri)
        }
    }

    private fun saveFile(fileName: String, dir: File, relativePath: String, uri: Uri) {
        writeFile(contentResolver, fileName, dir, relativePath, uri) { absPath: String?, _ ->
            run {
                if (absPath != null) {
                    val fileUri = Uri.fromFile(File(absPath))
                    runOnUiThread {
                        val intent = Intent().apply {
                            action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
                            data = fileUri
                        }
                        sendBroadcast(intent)
                        Toast.makeText(this, getString(R.string.file_saved, absPath), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, R.string.unable_to_access_source, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
