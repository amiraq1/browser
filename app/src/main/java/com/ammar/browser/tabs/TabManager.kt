package com.ammar.browser.tabs

/**
 * Manages browser tabs. Engine-agnostic — does not hold WebView references.
 * The UI layer is responsible for mapping the active tab to an engine session.
 */
class TabManager {

    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null
    private var listener: Listener? = null

    interface Listener {
        fun onTabCreated(tab: Tab)
        fun onTabClosed(tabId: String, newActiveTab: Tab?)
        fun onTabSelected(tab: Tab)
        fun onTabUpdated(tab: Tab)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun createNewTab(initialUrl: String = "", isPrivate: Boolean = false): Tab {
        val tab = Tab(url = initialUrl, isPrivate = isPrivate)
        tabs.add(tab)
        activeTabId = tab.id
        listener?.onTabCreated(tab)
        return tab
    }

    fun closeTab(tabId: String) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return

        tabs.removeAt(index)

        if (tabs.isEmpty()) {
            // Never allow zero tabs — open a blank one
            createNewTab()
            return
        }

        if (activeTabId == tabId) {
            // Select nearest tab
            val newIndex = index.coerceAtMost(tabs.size - 1)
            activeTabId = tabs[newIndex].id
            listener?.onTabClosed(tabId, tabs[newIndex])
        } else {
            listener?.onTabClosed(tabId, getCurrentTab())
        }
    }

    fun selectTab(tabId: String) {
        val tab = tabs.find { it.id == tabId } ?: return
        activeTabId = tab.id
        listener?.onTabSelected(tab)
    }

    fun getCurrentTab(): Tab? = tabs.find { it.id == activeTabId }

    fun getAllTabs(): List<Tab> = tabs.toList()

    fun getTabCount(): Int = tabs.size

    fun updateTabTitle(tabId: String, title: String) {
        updateTab(tabId) { it.copy(title = title) }
    }

    fun updateTabUrl(tabId: String, url: String) {
        updateTab(tabId) { it.copy(url = url) }
    }

    fun updateLoadingState(tabId: String, isLoading: Boolean) {
        updateTab(tabId) { it.copy(isLoading = isLoading) }
    }

    fun updateNavState(tabId: String, canGoBack: Boolean, canGoForward: Boolean) {
        updateTab(tabId) { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    private fun updateTab(tabId: String, transform: (Tab) -> Tab) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return
        tabs[index] = transform(tabs[index])
        listener?.onTabUpdated(tabs[index])
    }
}
