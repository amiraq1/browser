# Nabd Browser / متصفح نبض

A privacy-focused Android web browser with built-in ad/tracker blocking. Designed with a modular architecture that allows swapping the rendering engine in the future (WebView → Chromium custom / GeckoView).

> The project was previously developed under the working name **AmmarBrowser**. Internal package and applicationId still use `com.ammar.browser` — the user-facing brand is now **Nabd Browser / نبض**.

## Current Status: v0.3-alpha

Built on top of Android WebView. All privacy and ad-blocking logic runs locally inside the app — no servers, no telemetry, no third-party SDKs.

Version: **0.3.0-alpha** (debug build, for testing only).

### v0.3-alpha highlights

- Search-only Home — minimal centered search bar, no Quick Actions cards.
- Download Manager via Android `DownloadManager` (saved to the device Downloads folder).
- Downloads surfaced in Settings and About so users know where files land.
- AdBlock request-path performance optimization — pre-computed dotted suffix and path rule fields, zero per-request string allocations on the hot path.
- Startup / NewTab cleanup — dead helper, dead parameter, and unused import removed.
- Nabd brand UI polish across Home, AdBlock Debug, and the main toolbar.

### Earlier alphas

- **v0.2-alpha**: Bookmarks, Search Engine Selector, Shield Dashboard, Native Quick Actions, rebrand to Nabd Browser.
- **v0.1-alpha**: MVP WebView browser, Tabs, History, AdBlock core, HTTPS-Only, Zero Tracking defaults.

## Documentation

- Arabic Project Review / تقرير مراجعة المشروع: [docs/PROJECT_REVIEW_AR.md](docs/PROJECT_REVIEW_AR.md)

## Features

### Core
- Android WebView-based browser
- URL / search bar with smart query detection
- Back / Forward / Reload navigation
- Bottom navigation toolbar
- Tabs + Tab Switcher
- Custom New Tab Page
- History

### Privacy
- Zero Tracking Defaults
- No telemetry
- No tracking SDKs
- Local-only protection (nothing leaves the device)
- Third-party cookies blocked
- Mixed content blocked
- File / content access disabled
- Geolocation disabled
- DNT and GPC headers
- HTTPS-Only Mode
- Suspicious Domain Warning (local heuristics)
- Cookie Banner Control (experimental)
- Clear Browsing Data

### Ad & Tracker Blocking
- Ad and tracker blocking
- Local blocklists shipped with the app
- ABP (Adblock Plus) subset filter parser
- Speed modes: OFF / BALANCED / EXTREME
- Per-site allowlist
- Protection Stats dashboard
- AdBlock Debug screen
- Privacy Grade (A–D) per site
- Tracker company classification

### Settings
- Speed Mode
- Cookie Banner Control
- Clear Browsing Data
- Protection Stats
- AdBlock Debug
- About page

## Architecture

```
com.ammar.browser/
├── engine/          — Browser engine abstraction (swappable)
├── tabs/            — Tab management + Tab Switcher
├── navigation/      — URL parsing, search detection
├── ui/              — Activities, New Tab Page, Protection Stats, AdBlock Debug
├── settings/        — User preferences
├── privacy/         — Privacy/security configuration
│   ├── adblock/     — Ad/tracker blocker, ABP parser, blocklists, stats
│   └── allowlist/   — Per-site allowlist
├── permissions/     — Runtime permission management
├── performance/     — Speed modes (OFF / BALANCED / EXTREME)
├── history/         — Browsing history (Room)
└── utils/           — Crash logger, startup tracker
```

## Security / Privacy Notice

- Nabd Browser / نبض لا يرسل URLs أو بيانات التصفح لأي سيرفر.
- لا يوجد Firebase / Analytics / Ads SDK.
- كل الحماية الحالية محلية داخل التطبيق.
- v0.3-alpha هي debug build للاختبار فقط.
- بعض المواقع قد تنكسر بسبب إعدادات الخصوصية الصارمة (يمكن إضافتها إلى الـ allowlist).

## Build

Requires Java 17 and Android SDK with build-tools and platform 34.

```sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
./gradlew assembleDebug --no-daemon
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Roadmap

### Completed
- [x] MVP WebView browser
- [x] Tabs + Tab Switcher
- [x] History
- [x] AdBlock core engine
- [x] Local blocklists
- [x] Speed modes (OFF / BALANCED / EXTREME)
- [x] Shield panel
- [x] Protection Stats dashboard
- [x] Settings
- [x] Custom New Tab Page
- [x] HTTPS-Only mode
- [x] Zero Tracking defaults
- [x] Suspicious Domain warning
- [x] Cookie Banner Control (experimental)
- [x] v0.1-alpha release prep
- [x] Bookmarks (v0.2-alpha)
- [x] Search Engine Selector (v0.2-alpha)
- [x] Rebrand to Nabd Browser (v0.2-alpha)
- [x] Search-only Home (v0.3-alpha)
- [x] Plus button opens Bookmarks (v0.3-alpha)
- [x] Download Manager via Android DownloadManager (v0.3-alpha)
- [x] AdBlock request-path performance optimization (v0.3-alpha)
- [x] Startup / NewTab cleanup (v0.3-alpha)

### Planned
- [ ] Bookmark folders + search + import/export
- [ ] Settings import / export
- [ ] Better EasyList support
- [ ] More tracker categories
- [ ] Better icon / branding
- [ ] Release signing
- [ ] GitHub Releases with attached APK
- [ ] Later: GeckoView or Chromium engine research

## License

MIT
