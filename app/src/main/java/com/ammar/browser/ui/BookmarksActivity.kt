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
import com.ammar.browser.bookmarks.BookmarkEntity
import com.ammar.browser.bookmarks.BookmarkRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lists saved bookmarks. Tapping an item finishes the activity with the
 * selected URL in the result intent so [MainActivity] can navigate to it.
 * Each row also has a delete button that removes that single bookmark.
 */
class BookmarksActivity : AppCompatActivity() {

    companion object {
        const val RESULT_URL = "result_url"
    }

    private lateinit var repository: BookmarkRepository
    private lateinit var recycler: RecyclerView
    private lateinit var emptyLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        repository = BookmarkRepository(this)
        recycler = findViewById(R.id.bookmarks_recycler)
        emptyLabel = findViewById(R.id.empty_label)
        recycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch { loadBookmarks() }
    }

    private suspend fun loadBookmarks() {
        val items = repository.getAll()
        runOnUiThread {
            if (items.isEmpty()) {
                emptyLabel.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                recycler.adapter = null
            } else {
                emptyLabel.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                recycler.adapter = BookmarkAdapter(
                    items,
                    onClick = { url ->
                        setResult(Activity.RESULT_OK, Intent().putExtra(RESULT_URL, url))
                        finish()
                    },
                    onDelete = { id ->
                        lifecycleScope.launch {
                            repository.delete(id)
                            loadBookmarks()
                        }
                    }
                )
            }
        }
    }

    private class BookmarkAdapter(
        private val items: List<BookmarkEntity>,
        private val onClick: (String) -> Unit,
        private val onDelete: (Long) -> Unit
    ) : RecyclerView.Adapter<BookmarkAdapter.VH>() {

        private val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.bookmark_title)
            val url: TextView = view.findViewById(R.id.bookmark_url)
            val delete: ImageButton = view.findViewById(R.id.btn_delete_bookmark)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bookmark, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.title.text = item.title.ifEmpty { item.url }
            holder.url.text = "${dateFormat.format(Date(item.lastUpdated))} · ${item.url}"
            holder.itemView.setOnClickListener { onClick(item.url) }
            holder.delete.setOnClickListener { onDelete(item.id) }
        }

        override fun getItemCount() = items.size
    }
}
