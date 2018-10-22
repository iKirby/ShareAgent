package me.ikirby.shareagent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import java.io.File

class ForwardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // workaround for strict mode on Android N and up
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        if (intent.action == Intent.ACTION_SEND) {
            setContentView(R.layout.activity_progress)
            val type = intent.type
            when (type) {
                "text/plain" -> handleText(intent)
                else -> handleOtherFile(intent)
            }
        } else finish()
    }

    override fun onBackPressed() {
    }

    private fun handleText(intent: Intent) {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (text == null) {
            handleOtherFile(intent)
            return
        }
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val content = if (subject != null) "$subject\n$text" else text
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)))
        finish()
    }

    private fun handleOtherFile(intent: Intent) {
        val uri = if (intent.data != null) intent.data else intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (uri != null) {
            setContentView(R.layout.activity_progress)
            val extCacheDir = externalCacheDir
            if (extCacheDir != null) {
                Thread(Runnable {
                    val fileName = File(uri.path).name
                    val nomedia = File(extCacheDir.parent, ".nomedia")
                    if (!nomedia.exists()) {
                        nomedia.createNewFile()
                    }
                    saveFile(fileName, extCacheDir, "", uri)
                }).start()
            } else {
                Toast.makeText(this, R.string.unable_to_access_ext_cache, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, R.string.uri_invalid, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveFile(fileName: String, dir: File, relativePath: String, uri: Uri) {
        writeFile(contentResolver, fileName, dir, relativePath, uri) { absPath: String?, mimeType: String? ->
            run {
                if (absPath != null) {
                    val fileUri = Uri.fromFile(File(absPath))
                    runOnUiThread {
                        val shareIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            type = mimeType ?: "*/*"
                        }
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)))
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
