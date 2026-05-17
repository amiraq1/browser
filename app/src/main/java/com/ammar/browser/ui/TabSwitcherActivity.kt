package com.ammar.browser.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ammar.browser.R
import com.ammar.browser.tabs.Tab

class TabSwitcherActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TABS = "tabs_json"
        const val EXTRA_ACTIVE_TAB_ID = "active_tab_id"
        const val RESULT_ACTION = "action"
        const val RESULT_TAB_ID = "tab_id"
        const val ACTION_SELECT = "select"
        const val ACTION_CLOSE = "close"
        const val ACTION_NEW = "new"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_switcher)

        val tabsJson = intent.getStringExtra(EXTRA_TABS) ?: "[]"
        val activeTabId = intent.getStringExtra(EXTRA_ACTIVE_TAB_ID) ?: ""

        val tabs = parseTabs(tabsJson)

        val countLabel = findViewById<TextView>(R.id.tab_count_label)
        countLabel.text = getString(R.string.tab_count_format, tabs.size)

        val recycler = findViewById<RecyclerView>(R.id.tabs_recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = TabAdapter(tabs, activeTabId,
            onSelect = { finishWithResult(ACTION_SELECT, it) },
            onClose = { finishWithResult(ACTION_CLOSE, it) }
        )

        findViewById<ImageButton>(R.id.btn_new_tab).setOnClickListener {
            finishWithResult(ACTION_NEW, "")
        }
    }

    private fun finishWithResult(action: String, tabId: String) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(RESULT_ACTION, action)
            putExtra(RESULT_TAB_ID, tabId)
        })
        finish()
    }

    private fun parseTabs(json: String): List<Tab> {
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Tab(
                    id = obj.optString("id", ""),
                    title = obj.optString("title", ""),
                    url = obj.optString("url", "")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private class TabAdapter(
        private val tabs: List<Tab>,
        private val activeTabId: String,
        private val onSelect: (String) -> Unit,
        private val onClose: (String) -> Unit
    ) : RecyclerView.Adapter<TabAdapter.VH>() {

        class VH(val root: ViewGroup) : RecyclerView.ViewHolder(root) {
            val title: TextView = root.findViewById(R.id.tab_title)
            val url: TextView = root.findViewById(R.id.tab_url)
            val closeBtn: ImageButton = root.findViewById(R.id.btn_close_tab)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tab, parent, false) as ViewGroup
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val tab = tabs[position]
            holder.title.text = tab.title.ifEmpty { "New Tab" }
            holder.url.text = tab.url
            holder.root.alpha = if (tab.id == activeTabId) 1.0f else 0.7f
            holder.root.setOnClickListener { onSelect(tab.id) }
            holder.closeBtn.setOnClickListener { onClose(tab.id) }
        }

        override fun getItemCount() = tabs.size
    }
}
