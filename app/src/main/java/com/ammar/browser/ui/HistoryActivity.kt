package com.ammar.browser.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ammar.browser.R
import com.ammar.browser.history.HistoryEntity
import com.ammar.browser.history.HistoryRepository
import com.ammar.browser.utils.applySystemBarPaddingToContent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    companion object {
        const val RESULT_URL = "result_url"
    }

    private lateinit var repository: HistoryRepository
    private lateinit var recycler: RecyclerView
    private lateinit var emptyLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        applySystemBarPaddingToContent()

        repository = HistoryRepository(this)
        recycler = findViewById(R.id.history_recycler)
        emptyLabel = findViewById(R.id.empty_label)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btn_clear_history).setOnClickListener {
            lifecycleScope.launch {
                repository.clearAll()
                loadHistory()
            }
        }

        lifecycleScope.launch { loadHistory() }
    }

    private suspend fun loadHistory() {
        val items = repository.getAll()
        runOnUiThread {
            if (items.isEmpty()) {
                emptyLabel.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            } else {
                emptyLabel.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                recycler.adapter = HistoryAdapter(items) { url ->
                    setResult(Activity.RESULT_OK, Intent().putExtra(RESULT_URL, url))
                    finish()
                }
            }
        }
    }

    private class HistoryAdapter(
        private val items: List<HistoryEntity>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.VH>() {

        private val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.history_title)
            val url: TextView = view.findViewById(R.id.history_url)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.title.text = item.title.ifEmpty { item.url }
            holder.url.text = "${dateFormat.format(Date(item.lastVisited))} · ${item.url}"
            holder.itemView.setOnClickListener { onClick(item.url) }
        }

        override fun getItemCount() = items.size
    }
}
