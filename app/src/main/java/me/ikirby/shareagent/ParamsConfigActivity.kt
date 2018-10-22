package me.ikirby.shareagent

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_config_params.*
import kotlinx.android.synthetic.main.dialog_add.*

class ParamsConfigActivity : Activity() {
    private lateinit var preferences: SharedPreferences
    private val list = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_params)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.config_params)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val currentValue = preferences.getString("remove_params", "")
        if (currentValue != null && currentValue.isNotBlank()) {
            list.addAll(currentValue.split(","))
        }

        val adapter = UrlListAdapter(list, layoutInflater)
        listview_urls.adapter = adapter

        listview_urls.setOnItemLongClickListener { _, _, position, _ ->
            list.removeAt(position)
            adapter.notifyDataSetChanged()
            true
        }

        button_add.setOnClickListener {
            AlertDialog.Builder(this)
                    .setView(R.layout.dialog_add)
                    .setTitle(R.string.add)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        val text = (dialog as AlertDialog).edittext_add.text.toString()
                        if (!text.isBlank()) {
                            list.add(text)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        preferences.edit().putString("remove_params", list.joinToString(",")).apply()
        super.onBackPressed()
    }

    private inner class UrlListAdapter(private val list: MutableList<String>,
                                       private val inflater: LayoutInflater) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.layout_text_item, parent, false)
            }
            (view as TextView).text = list[position]
            return view
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return list.size
        }
    }
}
