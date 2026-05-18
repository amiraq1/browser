package com.ammar.browser.ui

import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.adblock.AdBlocker

/**
 * Generates local HTML for the new tab page.
 */
object NewTabPage {

    const val URL = "ammar://newtab"

    fun isNewTabUrl(url: String?): Boolean =
        url.isNullOrBlank() || url == URL || url == "about:blank" || url.startsWith("data:")

    fun generateHtml(adBlocker: AdBlocker): String {
        val totalBlocked = adBlocker.stats.totalBlocked
        val mode = SpeedSettings.mode
        val adBlockStatus = if (mode == SpeedMode.OFF) "Disabled" else "Enabled"

        return """<!DOCTYPE html><html><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>New Tab</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:#1a1a2e;color:#eee;
display:flex;flex-direction:column;align-items:center;justify-content:center;
min-height:100vh;padding:20px}
h1{font-size:1.6em;margin-bottom:8px;color:#4fc3f7}
.stats{font-size:0.9em;color:#aaa;margin-bottom:24px;text-align:center;line-height:1.8}
.stats b{color:#4fc3f7}
form{width:100%;max-width:500px;margin-bottom:32px}
input[type=text]{width:100%;padding:14px 18px;border-radius:28px;border:1px solid #333;
background:#16213e;color:#fff;font-size:16px;outline:none}
input[type=text]:focus{border-color:#4fc3f7}
.shortcuts{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;max-width:400px}
.shortcuts a{display:flex;flex-direction:column;align-items:center;text-decoration:none;
color:#ccc;padding:12px 8px;border-radius:12px;background:#16213e;font-size:0.8em}
.shortcuts a:active{background:#1a3a5c}
.shortcuts .icon{font-size:1.6em;margin-bottom:4px}
</style></head><body>
<h1>AmmarBrowser</h1>
<div class="stats">
🛡 Total blocked: <b>$totalBlocked</b><br>
⚡ Speed: <b>${mode.name}</b> · AdBlock: <b>$adBlockStatus</b>
</div>
<form onsubmit="go(event)">
<input type="text" id="q" placeholder="Search or enter URL" autofocus>
</form>
<div class="shortcuts">
<a href="https://www.google.com"><span class="icon">🔍</span>Google</a>
<a href="https://www.youtube.com"><span class="icon">▶️</span>YouTube</a>
<a href="https://github.com"><span class="icon">💻</span>GitHub</a>
<a href="https://www.xda-developers.com"><span class="icon">📱</span>XDA</a>
</div>
<script>
function go(e){e.preventDefault();var q=document.getElementById('q').value.trim();
if(!q)return;
if(q.startsWith('http://')||q.startsWith('https://'))location.href=q;
else if(q.indexOf('.')!==-1&&q.indexOf(' ')===-1)location.href='https://'+q;
else location.href='https://www.google.com/search?q='+encodeURIComponent(q);}
</script></body></html>"""
    }
}
