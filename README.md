# Ammar Browser Android

A privacy-focused Android web browser with built-in ad/tracker blocking. Designed with a modular architecture that allows swapping the rendering engine in the future (WebView → Chromium custom / GeckoView).

## Current Status: v0.1-alpha

Built on top of Android WebView. All privacy and ad-blocking logic runs locally inside the app — no servers, no telemetry, no third-party SDKs.

Version: **0.1.0-alpha** (debug build, for testing only).

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

- AmmarBrowser لا يرسل URLs أو بيانات التصفح لأي سيرفر.
- لا يوجد Firebase / Analytics / Ads SDK.
- كل الحماية الحالية محلية داخل التطبيق.
- v0.1-alpha هي debug build للاختبار فقط.
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

### Planned
- [ ] Bookmarks
- [ ] Download Manager
- [ ] Import / export settings
- [ ] Better EasyList support
- [ ] More tracker categories
- [ ] Better icon / branding
- [ ] Release signing
- [ ] GitHub release
- [ ] Later: GeckoView or Chromium engine research

## License

MIT
