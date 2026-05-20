package com.ammar.browser.ui

import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.search.SearchSettings

/**
 * Generates the local HTML for the Nabd Browser new tab page.
 *
 * The page is intentionally small and local-only: a centered search/address
 * bar with inline controls and no network assets.
 *
 * The public surface ([URL], [isNewTabUrl], [generateHtml]) is unchanged so
 * the rest of the app (MainActivity, BookmarkRepository, NavigationHelper,
 * etc.) continues to work without modification.
 */
object NewTabPage {

    const val URL = "ammar://newtab"

    fun isNewTabUrl(url: String?): Boolean =
        url.isNullOrBlank() || url == URL || url == "about:blank" || url.startsWith("data:")

    @Suppress("UNUSED_PARAMETER")
    fun generateHtml(adBlocker: AdBlocker): String {
        val searchEngine = SearchSettings.currentEngine
        val searchTemplateJs = escapeJsString(searchEngine.urlTemplate)

        return """<!DOCTYPE html><html lang="ar" dir="ltr"><head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,viewport-fit=cover">
<meta name="color-scheme" content="dark">
<title>Nabd Browser</title>
<style>
*{margin:0;padding:0;box-sizing:border-box;-webkit-tap-highlight-color:transparent}
:root{
  --bg:#0B1020;
  --bar:#101827;
  --bar-hi:#162033;
  --border:rgba(0,229,255,0.34);
  --text:#E6EDF6;
  --muted:#8DA1B8;
  --cyan:#00E5FF;
  --teal:#00C2B8;
  --green:#39FF88;
}
html,body{width:100%;min-height:100%;background:var(--bg);color:var(--text)}
body{
  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;
  -webkit-font-smoothing:antialiased;
  padding:24px;
  min-height:100vh;
  min-height:100dvh;
  display:grid;
  place-items:center;
  overflow:hidden;
  overscroll-behavior:contain;
}
.search-shell{
  width:min(100%,620px);
  min-height:64px;
  display:flex;
  align-items:center;
  gap:8px;
  padding:8px;
  border:1px solid var(--border);
  border-radius:999px;
  background:var(--bar);
  box-shadow:0 14px 40px rgba(0,0,0,0.24),0 0 24px rgba(0,194,184,0.08);
}
.search-shell:focus-within{
  border-color:var(--cyan);
  box-shadow:0 14px 40px rgba(0,0,0,0.28),0 0 0 3px rgba(0,229,255,0.16);
}
.search-shell input{
  min-width:0;
  flex:1;
  height:48px;
  border:0;
  outline:0;
  background:transparent;
  color:var(--text);
  font-size:16px;
  line-height:1.2;
  text-align:right;
}
.search-shell input::placeholder{color:var(--muted);opacity:1}
.icon-btn{
  width:48px;
  height:48px;
  flex:0 0 48px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  border:1px solid rgba(0,229,255,0.16);
  border-radius:50%;
  background:var(--bar-hi);
  color:var(--cyan);
  font-size:26px;
  font-weight:500;
  outline:0;
}
.icon-btn:active{background:#0F2630;color:var(--green)}
.icon-btn svg{width:22px;height:22px;display:block;stroke:currentColor}
.submit-btn{color:var(--green);border-color:rgba(57,255,136,0.24)}
@media (max-width:380px){
  body{padding:16px}
  .search-shell{gap:6px;padding:7px}
  .icon-btn{width:44px;height:44px;flex-basis:44px}
  .search-shell input{height:44px;font-size:15px}
}
</style></head><body>
<form class="search-shell" onsubmit="go(event)" autocomplete="off">
  <button class="icon-btn add-btn" type="button" aria-label="إضافة" onclick="openBookmarks()">+</button>
  <input type="text" id="q"
         placeholder="ابحث أو اكتب رابطاً..."
         autocapitalize="off" autocorrect="off" spellcheck="false" inputmode="url" dir="auto">
  <button class="icon-btn mic-btn" type="button" aria-label="ميكروفون">
    <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M12 3a3 3 0 0 0-3 3v6a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3Z"/>
      <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
      <path d="M12 19v3"/>
    </svg>
  </button>
  <button class="icon-btn submit-btn" type="submit" aria-label="انتقال">
    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M5 12h14"/>
      <path d="m13 6 6 6-6 6"/>
    </svg>
  </button>
</form>

<script>
(function(){
  var SEARCH='$searchTemplateJs';
  function go(e){
    e.preventDefault();
    var q=document.getElementById('q').value.trim();
    if(!q)return;
    if(q.startsWith('http://')||q.startsWith('https://')){location.href=q;return;}
    if(q.indexOf('.')!==-1&&q.indexOf(' ')===-1){location.href='https://'+q;return;}
    location.href=SEARCH+encodeURIComponent(q);
  }
  function openBookmarks(){
    location.href='ammar://action/bookmarks';
  }
  window.go=go;
  window.openBookmarks=openBookmarks;
})();
</script>
</body></html>"""
    }

    /** Escape for embedding inside a JavaScript single-quoted string literal. */
    private fun escapeJsString(s: String): String =
        s.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("</", "<\\/")
}
