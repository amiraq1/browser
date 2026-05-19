package com.ammar.browser.ui

import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.search.SearchSettings

/**
 * Generates the local HTML for the **Nabd Home / Shield Dashboard** new tab page.
 *
 * Phase Brand-3 redesign: a privacy-app style home with a status header,
 * a hero shield+pulse mark, a large search bar, a quick-actions grid, a
 * status card and a visual bottom nav. All assets are inline — no network
 * fetches, no external images, no remote fonts — keeping the new tab page
 * consistent with the Zero Tracking defaults of the rest of the app.
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

        // Zero Tracking state derived from speed mode.
        val zeroTrackingState = when (mode) {
            SpeedMode.EXTREME -> "ON"
            SpeedMode.BALANCED -> "Partial"
            SpeedMode.OFF -> "OFF"
        }
        val zeroTrackingPillClass = when (mode) {
            SpeedMode.EXTREME -> "pill-green"
            SpeedMode.BALANCED -> "pill-cyan"
            SpeedMode.OFF -> "pill-warn"
        }

        val adBlockStatus = if (mode == SpeedMode.OFF) "Disabled" else "Enabled"

        val searchEngine = SearchSettings.currentEngine
        val searchTemplateJs = escapeJsString(searchEngine.urlTemplate)
        val searchEngineName = escapeHtml(searchEngine.displayName)

        val totalStr = formatCount(total)
        val adsStr = formatCount(ads)
        val trackersStr = formatCount(trackers)

        // Subtitle for "Trackers Blocked" card — shows the live count.
        val trackersCardSub = if (total == 0)
            "View protection stats"
        else
            "$totalStr blocked · view stats"

        val speedModeName = escapeHtml(mode.name)
        val speedDotClass = when (mode) {
            SpeedMode.EXTREME -> "green"
            SpeedMode.BALANCED -> "cyan"
            SpeedMode.OFF -> "warn"
        }

        return """<!DOCTYPE html><html lang="en" dir="ltr"><head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,viewport-fit=cover">
<meta name="color-scheme" content="dark">
<title>Nabd Browser</title>
<style>
*{margin:0;padding:0;box-sizing:border-box;-webkit-tap-highlight-color:transparent}
:root{
  --bg:#0B1020;
  --bg-grad-1:#0B1020;
  --bg-grad-2:#0E1530;
  --card:#101729;
  --card-hi:#16213d;
  --border:#1e2a45;
  --border-hi:#2a3a60;
  --text:#E6EDF6;
  --text-soft:#A7B6CC;
  --muted:#6E7E96;
  --cyan:#00E5FF;
  --teal:#00C2B8;
  --green:#39FF88;
  --warn:#ffb74d;
  --danger:#ef5350;
}
html,body{
  background:var(--bg);color:var(--text);
  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;
  -webkit-font-smoothing:antialiased;
  overflow-x:hidden;             /* never horizontal scroll */
}
html{height:auto}                /* let document grow with content */
body{
  background:
    radial-gradient(900px 500px at 50% -120px, rgba(0,229,255,0.10), transparent 60%),
    radial-gradient(700px 400px at 100% 110%, rgba(57,255,136,0.05), transparent 60%),
    linear-gradient(180deg,var(--bg-grad-1),var(--bg-grad-2));
  /* extra bottom space so the last content clears the fixed bottom nav
     even on devices with gesture-nav safe-area inset (~24-30px). */
  padding:14px 14px calc(112px + env(safe-area-inset-bottom));
  min-height:100vh;
  min-height:100dvh;             /* dynamic viewport when supported */
  display:flex;flex-direction:column;align-items:center;
  overscroll-behavior-y:contain; /* no pull-to-refresh-like glow */
  touch-action:pan-y;            /* vertical scroll is the only gesture */
}
.page{width:100%;max-width:540px;display:flex;flex-direction:column;gap:16px}

/* ============== Top status bar ============== */
.topbar{display:flex;align-items:center;gap:10px;padding:2px 2px 0}
.topbar-left{display:flex;align-items:center;gap:8px;flex:1;min-width:0}
.shield-mini{width:22px;height:22px;color:var(--cyan);display:inline-flex;flex-shrink:0}
.shield-mini svg{width:100%;height:100%;display:block}
.topbar-title{font-size:14px;font-weight:600;color:var(--text);letter-spacing:0.3px}
.topbar-pill{
  display:inline-flex;align-items:center;gap:6px;
  padding:5px 10px;border-radius:999px;
  font-size:10.5px;font-weight:700;letter-spacing:1.2px;text-transform:uppercase;
  flex-shrink:0;
}
.topbar-pill .dot{width:6px;height:6px;border-radius:50%;flex-shrink:0}
.pill-green{background:rgba(57,255,136,0.10);color:var(--green);border:1px solid rgba(57,255,136,0.28)}
.pill-green .dot{background:var(--green);box-shadow:0 0 6px var(--green)}
.pill-cyan{background:rgba(0,229,255,0.10);color:var(--cyan);border:1px solid rgba(0,229,255,0.28)}
.pill-cyan .dot{background:var(--cyan);box-shadow:0 0 6px var(--cyan)}
.pill-warn{background:rgba(255,183,77,0.10);color:var(--warn);border:1px solid rgba(255,183,77,0.28)}
.pill-warn .dot{background:var(--warn);box-shadow:0 0 6px var(--warn)}

/* ============== Hero ============== */
.hero{display:flex;flex-direction:column;align-items:center;gap:4px;padding:6px 0 4px}
.hero-shield{width:84px;height:84px;display:block}
.hero-shield svg{width:100%;height:100%;display:block;filter:drop-shadow(0 0 14px rgba(0,229,255,0.35))}
.hero-name-ar{
  font-size:30px;font-weight:800;color:var(--cyan);
  letter-spacing:1px;line-height:1;margin-top:6px;
  text-shadow:0 0 18px rgba(0,229,255,0.45);
}
.hero-name-en{
  font-size:16px;font-weight:700;color:var(--text);
  letter-spacing:1.5px;margin-top:4px;
}
.hero-tagline{
  font-size:10.5px;color:var(--teal);letter-spacing:3px;
  text-transform:uppercase;margin-top:4px;font-weight:600;
}

/* ============== Search ============== */
.search{position:relative;width:100%}
.search input{
  width:100%;padding:15px 16px 15px 46px;border-radius:16px;
  background:var(--card);color:var(--text);border:1px solid var(--border);
  font-size:15px;outline:none;
  transition:border-color 0.15s ease, box-shadow 0.15s ease;
}
.search input::placeholder{color:var(--muted)}
.search input:focus{
  border-color:var(--cyan);
  box-shadow:0 0 0 3px rgba(0,229,255,0.18);
}
.search-icon{
  position:absolute;left:16px;top:50%;transform:translateY(-50%);
  color:var(--cyan);font-size:16px;pointer-events:none;
}
.search-hint{
  font-size:10.5px;color:var(--muted);letter-spacing:0.4px;
  text-align:center;margin-top:-6px;
}
.search-hint b{color:var(--teal);font-weight:600}

/* ============== Section labels ============== */
.section-label{
  font-size:10.5px;color:var(--muted);letter-spacing:2px;
  text-transform:uppercase;padding-left:4px;margin-top:4px;font-weight:600;
}

/* ============== Quick cards grid ============== */
.grid{display:grid;grid-template-columns:repeat(2,1fr);gap:10px}
.qcard{
  background:var(--card);border:1px solid var(--border);border-radius:16px;
  padding:14px;display:flex;flex-direction:column;gap:6px;
  text-decoration:none;color:var(--text);cursor:pointer;
  transition:border-color 0.15s ease, background 0.15s ease, transform 0.1s ease;
  min-height:96px;
}
.qcard:active{
  background:var(--card-hi);border-color:var(--border-hi);transform:scale(0.98);
}
.qcard-icon{
  width:36px;height:36px;border-radius:11px;
  display:flex;align-items:center;justify-content:center;
  font-size:16px;line-height:1;
}
.qicon-cyan {background:rgba(0,229,255,0.10);color:var(--cyan); border:1px solid rgba(0,229,255,0.22)}
.qicon-green{background:rgba(57,255,136,0.10);color:var(--green);border:1px solid rgba(57,255,136,0.22)}
.qicon-teal {background:rgba(0,194,184,0.10);color:var(--teal); border:1px solid rgba(0,194,184,0.22)}
.qcard-title{font-size:13.5px;font-weight:700;color:var(--text);letter-spacing:0.2px}
.qcard-sub{font-size:11px;color:var(--muted);line-height:1.4}

/* ============== Status card ============== */
.status-card{
  background:var(--card);border:1px solid var(--border);border-radius:16px;
  padding:14px;display:flex;flex-direction:column;gap:9px;
}
.status-row{display:flex;align-items:center;gap:10px;font-size:13px;color:var(--text-soft)}
.status-row b{color:var(--text);font-weight:600}
.status-dot{width:8px;height:8px;border-radius:50%;flex-shrink:0}
.status-dot.green{background:var(--green);box-shadow:0 0 6px rgba(57,255,136,0.6)}
.status-dot.cyan {background:var(--cyan); box-shadow:0 0 6px rgba(0,229,255,0.6)}
.status-dot.warn {background:var(--warn); box-shadow:0 0 6px rgba(255,183,77,0.6)}

/* ============== Footer ============== */
.foot{
  text-align:center;font-size:10.5px;color:var(--muted);
  letter-spacing:1px;margin-top:2px;padding:0 4px;
}
.foot b{color:var(--cyan);font-weight:700}

/* ============== Bottom nav (visual) ============== */
.bottomnav{
  position:fixed;left:0;right:0;bottom:0;
  display:flex;justify-content:space-around;align-items:stretch;
  background:rgba(11,16,32,0.92);
  -webkit-backdrop-filter:blur(14px);
  backdrop-filter:blur(14px);
  border-top:1px solid var(--border);
  padding:6px 4px calc(6px + env(safe-area-inset-bottom));
  z-index:100;
}
.nav-item{
  display:flex;flex-direction:column;align-items:center;justify-content:center;gap:3px;
  flex:1;text-decoration:none;color:var(--muted);cursor:pointer;
  padding:6px 0;border:none;background:transparent;font-family:inherit;
}
.nav-item .nav-icon{font-size:18px;line-height:1}
.nav-item .nav-label{font-size:10px;letter-spacing:0.5px;font-weight:600}
.nav-item.active{color:var(--cyan)}
.nav-item.active .nav-icon{text-shadow:0 0 10px rgba(0,229,255,0.7)}
.nav-item:active{color:var(--text)}
</style></head><body>
<div class="page">

  <!-- ============== Top status bar ============== -->
  <header class="topbar">
    <div class="topbar-left">
      <span class="shield-mini">
        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <path d="M12 2 L20 5 L20 12 C20 17 16.5 21 12 22.5 C7.5 21 4 17 4 12 L4 5 Z"
                fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          <path d="M7.5 12.5 L10 12.5 L11.5 15.5 L13.5 9.5 L15 12.5 L17 12.5"
                fill="none" stroke="currentColor" stroke-width="1.6"
                stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </span>
      <span class="topbar-title">Protected</span>
    </div>
    <span class="topbar-pill $zeroTrackingPillClass">
      <span class="dot"></span>Zero Tracking · $zeroTrackingState
    </span>
  </header>

  <!-- ============== Hero ============== -->
  <section class="hero">
    <div class="hero-shield">
      <svg viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
        <defs>
          <linearGradient id="shieldStroke" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stop-color="#00E5FF"/>
            <stop offset="1" stop-color="#00C2B8"/>
          </linearGradient>
        </defs>
        <path d="M32 4 L52 12 L52 30 C52 44 42 54 32 60 C22 54 12 44 12 30 L12 12 Z"
              fill="rgba(0,229,255,0.04)"
              stroke="url(#shieldStroke)" stroke-width="2.6" stroke-linejoin="round"/>
        <path d="M14 34 L22 34 L26 26 L32 42 L38 28 L42 34 L50 34"
              fill="none" stroke="#39FF88" stroke-width="2.4"
              stroke-linecap="round" stroke-linejoin="round"/>
        <circle cx="50" cy="34" r="2.6" fill="#39FF88"/>
      </svg>
    </div>
    <div class="hero-name-ar">نبض</div>
    <div class="hero-name-en">Nabd Browser</div>
    <div class="hero-tagline">Zero Tracking Browser</div>
  </section>

  <!-- ============== Search ============== -->
  <form class="search" onsubmit="go(event)" autocomplete="off">
    <span class="search-icon">🔍</span>
    <input type="text" id="q"
           placeholder="Search privately with $searchEngineName"
           autocapitalize="off" autocorrect="off" spellcheck="false" inputmode="url">
  </form>
  <div class="search-hint">or enter a website address — <b>HTTPS-Only</b> on</div>

  <!-- ============== Quick actions ============== -->
  <div class="section-label">Quick actions</div>
  <div class="grid">

    <a class="qcard" href="ammar://action/protection-stats">
      <div class="qcard-icon qicon-cyan">🛡</div>
      <div class="qcard-title">Privacy Shield</div>
      <div class="qcard-sub">Your activity stays private</div>
    </a>

    <a class="qcard" href="ammar://action/protection-stats">
      <div class="qcard-icon qicon-green">🚫</div>
      <div class="qcard-title">Trackers Blocked</div>
      <div class="qcard-sub">$trackersCardSub</div>
    </a>

    <a class="qcard" href="ammar://action/bookmarks">
      <div class="qcard-icon qicon-teal">★</div>
      <div class="qcard-title">Bookmarks</div>
      <div class="qcard-sub">Saved pages</div>
    </a>

    <a class="qcard" href="ammar://action/clear-data">
      <div class="qcard-icon qicon-cyan">🧹</div>
      <div class="qcard-title">Clear Data</div>
      <div class="qcard-sub">Remove browsing traces</div>
    </a>

    <a class="qcard" href="ammar://action/settings">
      <div class="qcard-icon qicon-teal">⚙</div>
      <div class="qcard-title">Settings</div>
      <div class="qcard-sub">Customize protection</div>
    </a>

    <a class="qcard" href="ammar://action/extreme-mode">
      <div class="qcard-icon qicon-green">⚡</div>
      <div class="qcard-title">Extreme Mode</div>
      <div class="qcard-sub">Strongest protection</div>
    </a>

  </div>

  <!-- ============== Status ============== -->
  <div class="section-label">Status</div>
  <div class="status-card">
    <div class="status-row">
      <span class="status-dot green"></span>
      <span>Zero Tracking · <b>$zeroTrackingState</b></span>
    </div>
    <div class="status-row">
      <span class="status-dot green"></span>
      <span>HTTPS-Only · <b>Enabled</b></span>
    </div>
    <div class="status-row">
      <span class="status-dot green"></span>
      <span>No telemetry</span>
    </div>
    <div class="status-row">
      <span class="status-dot green"></span>
      <span>Local-only protection</span>
    </div>
    <div class="status-row">
      <span class="status-dot $speedDotClass"></span>
      <span>Speed Mode · <b>$speedModeName</b></span>
    </div>
    <div class="status-row">
      <span class="status-dot cyan"></span>
      <span>Ads blocked: <b>$adsStr</b> · Trackers: <b>$trackersStr</b></span>
    </div>
  </div>

  <div class="foot">
    AdBlock <b>$adBlockStatus</b> · No telemetry · Local-only · Search via <b>$searchEngineName</b>
  </div>

</div>

<!-- ============== Bottom nav (visual + safe links) ============== -->
<nav class="bottomnav">
  <a class="nav-item active" href="ammar://newtab" aria-label="Home">
    <div class="nav-icon">⌂</div>
    <div class="nav-label">Home</div>
  </a>
  <button class="nav-item" type="button" onclick="window.history.back()" aria-label="Tabs">
    <div class="nav-icon">▦</div>
    <div class="nav-label">Tabs</div>
  </button>
  <a class="nav-item" href="ammar://action/protection-stats" aria-label="Shield">
    <div class="nav-icon">🛡</div>
    <div class="nav-label">Shield</div>
  </a>
  <a class="nav-item" href="ammar://action/settings" aria-label="Settings">
    <div class="nav-icon">⚙</div>
    <div class="nav-label">Settings</div>
  </a>
  <a class="nav-item" href="ammar://action/bookmarks" aria-label="Bookmarks">
    <div class="nav-icon">★</div>
    <div class="nav-label">Bookmarks</div>
  </a>
</nav>

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
}
