# Brand Identity — نبض / Nabd Browser

## Brand

| | |
|---|---|
| **Arabic name** | نبض |
| **English name** | Nabd Browser |
| **Tagline** | Zero Tracking Browser |
| **Internal package** | `com.ammar.browser` (kept stable so existing installs upgrade cleanly) |
| **Repo** | https://github.com/amiraq1/Browser |

## Tone & Keywords

- Privacy
- Zero Tracking
- Fast
- Secure
- Local-only
- Modern
- Cyber / dark

The brand should feel like a small, sharp, focused tool — not a corporate suite. Dark UI by default, subtle glow accents, no busy gradients.

## Color Palette

| Token | Hex | Use |
|---|---|---|
| Deep Navy | `#0B1020` | Primary background; icon background |
| Pulse Cyan | `#00E5FF` | Primary accent; the heartbeat line; focus rings |
| Teal | `#00C2B8` | Secondary accent; shield outline; success/active states |
| Neon Green | `#39FF88` | Beat / "Nabd" highlight; small status dots |
| Slate | `#1A2233` | Card surfaces; shield inner fill |

These are also exposed as Android color resources in `app/src/main/res/values/colors.xml`:

```xml
<color name="nabd_deep_navy">#0B1020</color>
<color name="nabd_pulse_cyan">#00E5FF</color>
<color name="nabd_teal">#00C2B8</color>
<color name="nabd_neon_green">#39FF88</color>
<color name="nabd_slate">#1A2233</color>
```

## App Icon

### Concept
- **Shield** — privacy / protection.
- **Heartbeat pulse line** — نبض (Nabd, "pulse / heartbeat") cutting across the shield.
- **Endpoint dot** — the live "beat" point in neon green.

### Implementation
The current icon is a hand-written **adaptive vector icon** (no rasterised PNGs):

| File | Purpose |
|---|---|
| `app/src/main/res/drawable/ic_nabd_foreground.xml` | Foreground: shield + pulse + dot |
| `app/src/main/res/drawable/ic_nabd_background.xml` | Background: deep navy fill |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | `<adaptive-icon>` for API 26+ |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` | Round variant of the above |
| `app/src/main/res/mipmap-anydpi/ic_launcher.xml` | Layer-list fallback for API 24-25 |
| `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml` | Round variant of the above |

Icon viewport is 108×108 dp following the Android adaptive-icon spec; all visible elements stay inside the central 66×66 dp safe zone so no element gets clipped under any system mask shape (circle, squircle, rounded square, teardrop).

### Notes for designers

This is a **first-pass vector icon**. It is intentionally simple so the build stays light and the project doesn't depend on any external asset pipeline. It can (and should) be replaced later with a polished design — for example a higher-detail PNG/WebP set or an SVG that has been hand-tuned in a vector tool. When that happens, swap the foreground/background drawables and the mipmap entries; nothing else in the app references the icon files by name.

## App Label

The Android launcher label is set to the Arabic **نبض** via `app_name` in `strings.xml`. The English name **Nabd Browser** is used in About, on the Shield Dashboard, and in documentation. The two never conflict because the launcher label is intentionally Arabic-first.

## Future Work

- Replace the alpha vector icon with a designed PNG/WebP set across density buckets.
- Add a dedicated splash screen using the same shield + pulse motif.
- Add an Android 12 themed-icon monochrome variant (`drawable/ic_nabd_monochrome.xml`).
- Apply `nabd_*` colors throughout the app to replace the legacy `primary/accent` tokens.
