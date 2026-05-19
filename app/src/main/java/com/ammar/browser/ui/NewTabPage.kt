package com.ammar.browser.ui

import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.TrackerCompanyClassifier
import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.search.SearchSettings

/**
 * Generates the local HTML for the **Shield Dashboard** new tab page.
 *
 * The page is intentionally non-traditional: instead of a generic search +
 * shortcuts page, it foregrounds the browser's privacy posture (privacy
 * grade, blocked counts, top tracker company, speed mode, HTTPS-Only).
 *
 * All assets are inline — no network fetches, no external images, no remote
 * fonts. This keeps the new tab page consistent with the Zero Tracking
 * defaults of the rest of the app.
 *
 * The public surface ([URL], [isNewTabUrl], [generateHtml]) is unchanged so
 * the rest of the app (MainActivity, BookmarkRepository, NavigationHelper,
 * etc.) continues to work without modification.
 */
object NewTabPage {

    const val URL = "ammar://newtab"

    fun isNewTabUrl(url: String?): Boolean =
        url.isNullOrBlank() || url == URL || url == "about:blank" || url.startsWith("data:")

    fun generateHtml(adBlocker: AdBlocker): String {
        val mode = SpeedSettings.mode
        val total = adBlocker.stats.totalBlocked
        val ads = adBlocker.stats.blockedAds
        val trackers = adBlocker.stats.blockedTrackers +
            adBlocker.stats.blockedAnalytics +
            adBlocker.stats.blockedSocial
        val recentHosts = adBlocker.blockedLog.getRecent().map { it.host }
        val topCompany = TrackerCompanyClassifier.topCompany(recentHosts)

        // Page-agnostic privacy grade (no current page when the new tab is open).
        val grade: Char = when (mode) {
            SpeedMode.EXTREME -> 'A'
            SpeedMode.BALANCED -> 'B'
            SpeedMode.OFF -> 'D'
        }

        val gradeColor = when (grade) {
            'A' -> "#00e676"  // green
            'B' -> "#4fc3f7"  // cyan
            'C' -> "#ffb74d"  // amber
            else -> "#ef5350" // red
        }

        val zeroTrackingState = when (mode) {
            SpeedMode.EXTREME -> "ON"
            SpeedMode.BALANCED -> "Partial"
            SpeedMode.OFF -> "OFF"
        }

        val zeroTrackingColor = when (mode) {
            SpeedMode.EXTREME -> "#00e676"
            SpeedMode.BALANCED -> "#4fc3f7"
            SpeedMode.OFF -> "#ef5350"
        }

        val adBlockStatus = if (mode == SpeedMode.OFF) "Disabled" else "Enabled"

        val searchEngine = SearchSettings.currentEngine
        // The URL template is safe to embed in a JS single-quoted string
        // (no quotes, no backslashes in the supported templates), but escape
        // anyway in case future engines need it.
        val searchTemplateJs = escapeJsString(searchEngine.urlTemplate)
        val searchEngineName = escapeHtml(searchEngine.displayName)

        // Pre-format counts with thousand separators for readability.
        val totalStr = formatCount(total)
        val adsStr = formatCount(ads)
        val trackersStr = formatCount(trackers)

        return """<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1">
<meta name="color-scheme" content="dark">
<title>Nabd Browser — Shield Dashboard</title>
<style>
*{margin:0;padding:0;box-sizing:border-box;-webkit-tap-highlight-color:transparent}
:root{
  --bg:#0a0e1a;
  --bg-grad-1:#0a0e1a;
  --bg-grad-2:#0d1426;
  --card:#101729;
  --card-hi:#13203a;
  --border:#1a2540;
  --border-hi:#2a3a60;
  --text:#e6edf6;
  --muted:#7a8aa3;
  --accent:#00e676;
  --accent2:#4fc3f7;
  --warn:#ffb74d;
  --danger:#ef5350;
}
html,body{background:var(--bg);color:var(--text);font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;
  -webkit-font-smoothing:antialiased;min-height:100vh}
body{
  background:
    radial-gradient(1200px 600px at 50% -10%, rgba(0,230,118,0.08), transparent 60%),
    radial-gradient(900px 500px at 100% 110%, rgba(79,195,247,0.06), transparent 60%),
    linear-gradient(180deg,var(--bg-grad-1),var(--bg-grad-2));
  padding:24px 16px 32px;
  display:flex;flex-direction:column;align-items:center;
}
.wrap{width:100%;max-width:540px;display:flex;flex-direction:column;gap:18px}

/* ===== Header ===== */
.brand{
  display:flex;flex-direction:column;align-items:center;gap:4px;margin-top:6px;
}
.brand .name{
  font-size:1.5em;font-weight:700;letter-spacing:0.5px;
  background:linear-gradient(90deg,var(--accent),var(--accent2));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.brand .sub{
  font-size:0.78em;color:var(--muted);letter-spacing:2px;text-transform:uppercase;
}

/* ===== Grade card ===== */
.grade-card{
  display:flex;align-items:center;gap:14px;
  padding:16px;border:1px solid var(--border);border-radius:18px;
  background:linear-gradient(135deg,rgba(255,255,255,0.02),rgba(255,255,255,0.005));
  position:relative;overflow:hidden;
}
.grade-card::after{
  content:"";position:absolute;inset:0;
  background:radial-gradient(180px 80px at 0% 50%, ${gradeColorAlpha(gradeColor)}, transparent 70%);
  pointer-events:none;
}
.grade{
  font-size:2.6em;font-weight:800;line-height:1;
  width:64px;height:64px;border-radius:50%;
  display:flex;align-items:center;justify-content:center;
  border:2px solid $gradeColor;color:$gradeColor;
  flex-shrink:0;background:rgba(0,0,0,0.25);
  box-shadow:0 0 24px ${gradeColorAlpha(gradeColor)};
}
.grade-meta{display:flex;flex-direction:column;gap:4px;min-width:0}
.grade-meta .title{font-size:0.78em;color:var(--muted);letter-spacing:1.5px;text-transform:uppercase}
.grade-meta .label{font-size:1.05em;font-weight:600}
.pill{
  display:inline-flex;align-items:center;gap:6px;
  align-self:flex-start;
  padding:4px 10px;border-radius:999px;
  font-size:0.72em;font-weight:600;letter-spacing:1px;text-transform:uppercase;
  background:${zeroTrackingColorAlpha(zeroTrackingColor)};color:$zeroTrackingColor;
  border:1px solid ${zeroTrackingColorAlpha(zeroTrackingColor)};
}
.pill .dot{width:6px;height:6px;border-radius:50%;background:$zeroTrackingColor;
  box-shadow:0 0 6px $zeroTrackingColor;}

/* ===== Search ===== */
.search{position:relative}
.search input{
  width:100%;padding:14px 16px 14px 44px;border-radius:14px;
  border:1px solid var(--border);background:var(--card);color:var(--text);
  font-size:15px;outline:none;transition:border-color 0.15s ease, box-shadow 0.15s ease;
}
.search input::placeholder{color:var(--muted)}
.search input:focus{border-color:var(--accent2);box-shadow:0 0 0 3px rgba(79,195,247,0.18)}
.search .icon{
  position:absolute;left:14px;top:50%;transform:translateY(-50%);
  color:var(--muted);font-size:1.05em;pointer-events:none;
}

/* ===== Stat cards ===== */
.stats{display:grid;grid-template-columns:repeat(2,1fr);gap:10px}
.card{
  background:var(--card);border:1px solid var(--border);border-radius:14px;
  padding:12px 14px;display:flex;flex-direction:column;gap:4px;min-width:0;
  transition:border-color 0.15s ease;
}
.card:active{border-color:var(--border-hi)}
.card .k{font-size:0.7em;color:var(--muted);letter-spacing:1.2px;text-transform:uppercase;
  display:flex;align-items:center;gap:6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.card .v{font-size:1.4em;font-weight:700;color:var(--text);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.card .v.accent{color:var(--accent)}
.card .v.cyan{color:var(--accent2)}
.card .v.warn{color:var(--warn)}
.card .v.small{font-size:1.05em}

/* ===== Quick actions ===== */
.section-title{
  font-size:0.72em;color:var(--muted);letter-spacing:2px;text-transform:uppercase;
  margin-top:6px;padding-left:4px;
}
.actions{display:grid;grid-template-columns:repeat(2,1fr);gap:10px}
.action{
  background:var(--card);border:1px solid var(--border);border-radius:14px;
  padding:12px;display:flex;align-items:center;gap:10px;
  color:var(--text);text-decoration:none;cursor:pointer;text-align:left;
  font-size:0.92em;width:100%;font-family:inherit;
  transition:border-color 0.15s ease,background 0.15s ease;
}
.action:active{background:var(--card-hi);border-color:var(--border-hi)}
.action .ico{
  width:32px;height:32px;border-radius:10px;flex-shrink:0;
  display:flex;align-items:center;justify-content:center;
  font-size:1.05em;background:rgba(0,230,118,0.08);color:var(--accent);
  border:1px solid rgba(0,230,118,0.2);
}
.action.alt .ico{background:rgba(79,195,247,0.08);color:var(--accent2);border-color:rgba(79,195,247,0.2)}
.action .lbl{display:flex;flex-direction:column;gap:2px;min-width:0}
.action .lbl b{font-weight:600;font-size:0.95em}
.action .lbl span{font-size:0.72em;color:var(--muted)}

/* ===== Shortcuts ===== */
.shortcuts{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}
.shortcuts a{
  display:flex;flex-direction:column;align-items:center;gap:6px;
  padding:12px 8px;border-radius:14px;background:var(--card);border:1px solid var(--border);
  text-decoration:none;color:var(--text);font-size:0.78em;
}
.shortcuts a:active{background:var(--card-hi);border-color:var(--border-hi)}
.shortcuts .icon{font-size:1.5em;line-height:1}

/* ===== Footer ===== */
.foot{
  margin-top:6px;text-align:center;font-size:0.7em;color:var(--muted);letter-spacing:1px;
}
.foot b{color:var(--accent2)}
</style></head><body>
<div class="wrap">

  <div class="brand">
    <div class="name">🛡 نبض · Nabd Browser</div>
    <div class="sub">Zero Tracking Browser</div>
  </div>

  <div class="grade-card">
    <div class="grade">$grade</div>
    <div class="grade-meta">
      <div class="title">Privacy Grade</div>
      <div class="label">Overall protection</div>
      <div class="pill"><span class="dot"></span>Zero Tracking · $zeroTrackingState</div>
    </div>
  </div>

  <form class="search" onsubmit="go(event)" autocomplete="off">
    <span class="icon">🔍</span>
    <input type="text" id="q" placeholder="Search with $searchEngineName or enter address" autofocus
           autocapitalize="off" autocorrect="off" spellcheck="false" inputmode="url">
  </form>

  <div class="section-title">Protection</div>
  <div class="stats">
    <div class="card"><div class="k">🛡 Total blocked</div><div class="v accent">$totalStr</div></div>
    <div class="card"><div class="k">🚫 Ads blocked</div><div class="v">$adsStr</div></div>
    <div class="card"><div class="k">👁 Trackers blocked</div><div class="v">$trackersStr</div></div>
    <div class="card"><div class="k">🏢 Top tracker</div><div class="v small cyan">${escapeHtml(topCompany)}</div></div>
    <div class="card"><div class="k">⚡ Speed mode</div><div class="v small ${speedClass(mode)}">${escapeHtml(mode.name)}</div></div>
    <div class="card"><div class="k">🔒 HTTPS-Only</div><div class="v small accent">ON</div></div>
  </div>

  <div class="section-title">Quick actions</div>
  <div class="actions">
    <a class="action" href="ammar://action/protection-stats">
      <div class="ico">📊</div>
      <div class="lbl"><b>Protection Stats</b><span>Detailed breakdown</span></div>
    </a>
    <a class="action alt" href="ammar://action/settings">
      <div class="ico">⚙</div>
      <div class="lbl"><b>Settings</b><span>App preferences</span></div>
    </a>
    <a class="action" href="ammar://action/clear-data">
      <div class="ico">🧹</div>
      <div class="lbl"><b>Clear Data</b><span>History · cookies</span></div>
    </a>
    <a class="action alt" href="ammar://action/extreme-mode">
      <div class="ico">⚡</div>
      <div class="lbl"><b>Extreme Mode</b><span>Currently: ${escapeHtml(mode.name)}</span></div>
    </a>
  </div>

  <div class="section-title">Shortcuts</div>
  <div class="shortcuts">
    <a href="https://www.google.com"><span class="icon">🔍</span>Google</a>
    <a href="https://www.youtube.com"><span class="icon">▶️</span>YouTube</a>
    <a href="https://github.com"><span class="icon">💻</span>GitHub</a>
    <a href="https://www.xda-developers.com"><span class="icon">📱</span>XDA</a>
  </div>

  <div class="foot">
    AdBlock <b>$adBlockStatus</b> · No telemetry · Local-only
  </div>

</div>

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
  window.go=go;
})();
</script>
</body></html>"""
    }

    // --- helpers (private) -------------------------------------------------

    private fun formatCount(n: Int): String =
        // Use plain string formatting; no Locale dependency to keep output
        // deterministic regardless of device locale.
        if (n < 1000) n.toString() else buildString {
            val s = n.toString()
            for (i in s.indices) {
                if (i > 0 && (s.length - i) % 3 == 0) append(',')
                append(s[i])
            }
        }

    private fun escapeHtml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    /** Escape for embedding inside a JavaScript single-quoted string literal. */
    private fun escapeJsString(s: String): String =
        s.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("</", "<\\/")

    private fun speedClass(mode: SpeedMode): String = when (mode) {
        SpeedMode.EXTREME -> "accent"
        SpeedMode.BALANCED -> "cyan"
        SpeedMode.OFF -> "warn"
    }

    /** Returns the given hex color with ~24% alpha appended for soft glows. */
    private fun gradeColorAlpha(hex: String): String = "${hex}3d"

    private fun zeroTrackingColorAlpha(hex: String): String = "${hex}33"
}
