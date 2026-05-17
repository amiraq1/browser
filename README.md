# Ammar Browser Android

A privacy-focused, high-performance Android web browser. Designed with a modular architecture that allows swapping the rendering engine (WebView → Chromium custom / GeckoView).

## Current Status: MVP Alpha

Using Android WebView as a temporary engine while building the architecture for a custom Chromium-based engine.

## Architecture

```
com.ammar.browser/
├── engine/          — Browser engine abstraction (swappable)
├── tabs/            — Tab management
├── navigation/      — URL parsing, search detection
├── ui/              — Activities, fragments, views
├── settings/        — User preferences
├── privacy/         — Privacy/security configuration
├── permissions/     — Runtime permission management
├── downloads/       — (planned) Download manager
├── bookmarks/       — (planned) Bookmark storage
└── history/         — (planned) Browsing history
```

## Build

Requires: Java 17+, Android SDK with build-tools and platform 34.

```sh
sh gradlew assembleDebug --no-daemon
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Roadmap

- [x] Skeleton MVP with WebView
- [x] URL bar with search detection
- [x] Navigation controls (back/forward/refresh)
- [x] Swipe to refresh
- [x] Engine abstraction layer
- [ ] Tab management
- [ ] Bookmark system
- [ ] History
- [ ] Download manager
- [ ] Ad/tracker blocking
- [ ] Custom Chromium engine integration
- [ ] Dark mode
- [ ] Custom new tab page

## License

MIT
