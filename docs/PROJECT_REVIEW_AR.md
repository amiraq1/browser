# تقرير مراجعة مشروع متصفح نبض (Nabd Browser)

> **ملاحظة على الاسم:** كان الاسم أثناء التطوير المبكر `AmmarBrowser`، بينما الاسم الظاهر للمستخدم الآن هو **نبض / Nabd Browser**. لا يزال `package` و `applicationId` داخلياً:
> `com.ammar.browser`
> وذلك لتجنب كسر التحديثات وبيانات المستخدم (التفضيلات، قاعدة بيانات Room، إلخ).

---

## 1. نظرة عامة

**نبض / Nabd Browser** هو متصفح ويب لأندرويد يركّز على الخصوصية، مع منع للإعلانات والمتعقّبات مدمج محلياً. صُمم بمعمارية مرنة تسمح باستبدال محرك العرض في المستقبل (WebView → Chromium مخصص / GeckoView).

- النوع: تطبيق Android Kotlin
- الحد الأدنى: Android 7.0 (API 24)
- المستهدف: API 34
- المحرك الحالي: Android WebView
- الترخيص: MIT
- الحماية: محلية فقط — لا يخرج أي شيء من الجهاز.

---

## 2. الحالة الحالية

| البند | الحالة |
|---|---|
| **v0.1-alpha** | مرفوعة على GitHub (https://github.com/amiraq1/Browser) |
| **v0.2-alpha** | مرفوعة على GitHub |
| **v0.3-alpha** | مرفوعة على GitHub |
| البناء | BUILD SUCCESSFUL على Java 17 / Gradle 8.5 |
| حجم APK (debug) | ~6.2 MB |
| التتبّع / التحليلات | لا شيء (Zero Telemetry) |
| تخزين بيانات خارجية | لا شيء |

### تحديث v0.3-alpha

- **صفحة بداية بسيطة بشريط بحث فقط** (Search-only Home).
- زر `+` على صفحة البداية يفتح Bookmarks.
- **دعم التنزيلات** عبر Android `DownloadManager` (تُحفظ الملفات في مجلد Downloads).
- إظهار توضيح في Settings و About بأن الملفات تُحفظ في Downloads.
- **تحسين أداء AdBlock**: إعادة هيكلة المسار الساخن لتتجنب تخصيص نصوص لكل request (precompute للـ dotted suffix و path rules).
- **تحسين startup**: إزالة helper غير مستخدم في `BrowserApp` و parameter ميت في `NewTabPage.generateHtml` و import ميت — بدون تغيير في السلوك.
- **لا telemetry ولا SDKs خارجية** — كما هو الحال منذ البداية.

### v0.2-alpha تتضمن:

1. **Bookmarks** — طبقة بيانات Room كاملة + شاشة عرض/حذف.
2. **Search Engine Selector** — اختيار محرك البحث، DuckDuckGo افتراضياً.
3. **Shield Dashboard** — إعادة تصميم New Tab Page كلوحة خصوصية.
4. **Native Quick Actions** — أزرار اللوحة تعمل عبر `ammar://action/*`.
5. **Rebrand to Nabd Browser** — تغيير الاسم الظاهر للمستخدم.

---

## 3. المعمارية

```
com.ammar.browser/
├── engine/          — تجريد محرك التصفح (قابل للاستبدال)
├── tabs/            — إدارة التبويبات + Tab Switcher
├── navigation/      — تحليل URL، اكتشاف البحث، ترقية HTTPS
├── search/          — اختيار محرك البحث (v0.2-alpha)
├── ui/              — Activities + Shield Dashboard
├── settings/        — تفضيلات المستخدم
├── privacy/         — إعدادات الخصوصية والأمان
│   ├── adblock/     — منع الإعلانات/المتعقّبات + ABP parser + إحصائيات
│   └── allowlist/   — قائمة المواقع المستثناة لكل موقع
├── permissions/     — إدارة الأذونات في الزمن الفعلي
├── performance/     — أوضاع السرعة (OFF / BALANCED / EXTREME)
├── history/         — سجل التصفح (Room) + BrowserDatabase
├── bookmarks/       — الإشارات المرجعية (Room) — v0.2-alpha
└── utils/           — Crash logger، Startup tracker
```

### نقاط معمارية مهمة

- **`BrowserEngine` interface** يفصل المتصفح عن WebView. يمكن استبدالها مستقبلاً بـ Chromium مخصص أو GeckoView بدون مس باقي الكود.
- **`EngineCallback`** يحمل `tabId` في كل نداء، فتُحدَّث الواجهة فقط للتبويب النشط، بينما تُحدَّث البيانات لكل التبويبات في الخلفية.
- **`Room` v2** يحتوي على جدولين: `history` و `bookmarks`. ترحيل (Migration) `1→2` يحافظ على بيانات المستخدم القائمة.

---

## 4. الميزات

### Core (الأساسيات)
- Android WebView-based browser
- شريط URL / بحث ذكي
- Back / Forward / Reload
- Tabs + Tab Switcher
- History
- **Bookmarks** (v0.2-alpha)
- **Search Engine Selector** (v0.2-alpha)
- **Shield Dashboard New Tab** (v0.2-alpha)
- **Native Quick Actions** (v0.2-alpha)
- Bottom Toolbar
- Custom New Tab Page

### Privacy (الخصوصية)
- Zero Tracking Defaults
- HTTPS-Only Mode (مع ترقية تلقائية للـ http→https)
- Suspicious Domain Warning (تحقق محلي)
- Cookie Banner Control (تجريبي)
- DNT / GPC headers
- No telemetry
- No tracking SDKs
- Local-only protection (نشاط الحماية لا يخرج من الجهاز)
- Third-party cookies blocked
- Mixed content blocked
- File / content access disabled
- Geolocation disabled

### AdBlock (منع الإعلانات والمتعقبات)
- Local blocklists (مرفقة مع التطبيق)
- ABP (Adblock Plus) subset filter parser
- Speed modes: **OFF / BALANCED / EXTREME**
- Per-site Allowlist
- Privacy Grade (A–D) لكل موقع
- Tracker Companies classification (Google / Meta / Amazon / Microsoft / TikTok / Other)
- Protection Stats dashboard
- AdBlock Debug screen
- Per-tab block stats

### Settings
- Speed Mode
- Search Engine (v0.2-alpha)
- Cookie Banner Control
- Clear Browsing Data (history / cookies / cache / all)
- Clear Allowlisted Sites
- Protection Stats
- AdBlock Debug
- About page

---

## 5. تفصيل ميزات v0.2-alpha

### Bookmarks
الحالة: **منفذة**.

- إضافة/تحديث بدون تكرار (مفتاح فريد على `url`).
- التحقق من URL: يُتجاهل blank / about:blank / data: / ammar://newtab / غير http(s).
- لا تُحفظ الإشارات في التبويبات الخاصة (Private tabs).
- `BookmarksActivity` يعرض العنوان والـ URL والوقت.
- النقر على إشارة يفتحها في التبويب الحالي عبر `navigateTo()` (ومن ثم تمر عبر HTTPS-Only و Suspicious Domain Warning).
- زر حذف لكل سطر مع تحديث فوري للقائمة.
- حالة فارغة: "No bookmarks yet".
- **Room migration v1→v2**: تحفظ بيانات `history` القائمة دون فقد.

#### المستقبل (لم يُنفّذ بعد)
- Folders
- Bookmarks search
- Import / export

### Search Engine Selector
الحالة: **منفذة**.

- المحركات المدعومة:
  - **DuckDuckGo** — افتراضي
  - **Brave Search**
  - **Startpage**
  - **Google**
- الاختيار محفوظ في `SharedPreferences` (مفتاح `engine_id`).
- يُستخدم في **شريط URL** (`NavigationHelper.resolveInput`) و **Shield Dashboard search box** (محقون في JS بالقالب الحالي).
- قابل للتغيير من Settings → Search Engine، يُحفظ فوراً.
- لماذا DuckDuckGo افتراضياً: لتجنب صفحات Google sorry/captcha ومتعقبات Google التحليلية، بما يتسق مع هوية Zero Tracking.

### Shield Dashboard (New Tab الجديد)
الحالة: **منفذ**.

التصميم: Dark cyber/privacy، 100% محلي (لا صور خارجية، لا خطوط شبكة، لا APIs خارجية).

العناصر:
- **Header**: نبض · Nabd Browser + شعار "Zero Tracking Browser".
- **Privacy Grade card**: حرف A/B/C/D كبير مع تدرج لوني وحالة "Zero Tracking · ON / Partial / OFF".
- **Search bar**: يستخدم المحرك المختار، placeholder ديناميكي "Search with <Engine>...".
- **Stats cards (2×3)**:
  - 🛡 Total blocked
  - 🚫 Ads blocked
  - 👁 Trackers blocked (= trackers + analytics + social)
  - 🏢 Top tracker company
  - ⚡ Speed mode
  - 🔒 HTTPS-Only: ON
- **Quick Actions (2×2)**: تعمل فعلياً (انظر الفقرة التالية).
- **Shortcuts (4×1)**: Google · YouTube · GitHub · XDA.
- **Footer**: AdBlock status + No telemetry · Local-only.

### Native Quick Actions
الحالة: **منفذة**.

- آلية: **URL scheme داخلي** `ammar://action/<name>`.
- **لا** يوجد `addJavascriptInterface` ولا أي bridge JS↔Native — لا توجد سطح هجوم من JavaScript.
- اعتراض في `WebViewEngine.shouldOverrideUrlLoading` ثم Dispatch إلى `EngineCallback.onCustomAction(tabId, action)`.
- **بوابة المصدر (Origin gate)**: يُنفّذ الإجراء فقط إذا كان `currentPageUrl` يبدأ بـ `ammar://`. إذا حاول موقع خارجي تحريك التصفح إلى `ammar://action/...` (عبر redirect أو `location.href`)، يتم ابتلاع الـ URL دون تنفيذ.
- الإجراءات المدعومة:
  - `protection-stats` → فتح ProtectionStatsActivity
  - `settings` → فتح SettingsActivity
  - `clear-data` → فتح SettingsActivity (حيث Clear Browsing Data)
  - `extreme-mode` → ضبط SpeedMode = EXTREME، Toast، إعادة توليد Dashboard
- لا تنكسر الـ Shortcuts ولا الـ Search لأنهما يبقيان `https://` عاديين تمر عبر `shouldOverrideUrlLoading` بـ `false`.

---

## 6. القيود (Limitations)

- **Android WebView يحدّ من بعض تقنيات anti-fingerprinting** المتاحة في متصفحات مثل Brave أو Tor — لا يمكن السيطرة الكاملة على Canvas / WebGL / AudioContext من داخل WebView بدون استبدال المحرك.
- **Zero Tracking ≠ منع 100% من كل تقنيات التتبع** على الويب. هناك تقنيات (مثل fingerprinting المتقدّم، first-party tracking داخل المواقع، CNAME cloaking على بعض الإعدادات) لا يمكن منعها كلياً بقواعد محلية.
- **Cookie Banner Control تجريبية** — تعتمد على CSS hiding (`display:none`) للعناصر الشائعة فقط، وليست تطبيقاً كاملاً لـ I-Don't-Care-About-Cookies أو Consent-O-Matic.
- **Suspicious Domain Warning محلي وبسيط** — يستخدم heuristics محلية (TLD مشبوهة، typosquatting شائع) وليس بديلاً عن Google Safe Browsing أو SmartScreen.
- **Debug APK للاختبار فقط** — غير مُوقّع release، لا يمر عبر تحقق Play Protect كاملاً، وقد لا يُوقّع توقيعاً ثابتاً (لا تحديثات نظيفة بين أجهزة).
- **بعض المواقع قد تنكسر** بسبب صرامة الإعدادات (Mixed content blocked، third-party cookies blocked، etc) — يمكن إضافتها إلى Allowlist يدوياً.
- **Migration الـ Room v1→v2** اختُبر بمراجعة الكود ولكن لم يُختبر على جهاز فعلي بعد. ينصح بتجربة تحديث APK من v0.1-alpha إلى v0.2-alpha قبل الإصدار العام.

---

## 7. الإعدادات الأمنية الافتراضية (Zero Tracking Defaults)

| الإعداد | القيمة الافتراضية |
|---|---|
| Mixed content | `MIXED_CONTENT_NEVER_ALLOW` |
| Third-party cookies | محظورة |
| File access (`allowFileAccess`) | معطّل |
| Content access (`allowContentAccess`) | معطّل |
| File access from file URLs | معطّل |
| Universal access from file URLs | معطّل |
| Geolocation | معطّل |
| HTTPS upgrade | تلقائي للـ http:// |
| DNT header | يُرسل `1` |
| GPC header (`Sec-GPC`) | يُرسل `1` |
| App telemetry | لا شيء |
| Tracking SDKs | لا شيء |
| Speed mode الافتراضي | EXTREME |
| Search engine الافتراضي | DuckDuckGo |

---

## 8. الـ Roadmap المختصر

### مكتمل
- [x] MVP WebView browser
- [x] Tabs + Tab Switcher
- [x] History
- [x] AdBlock core engine
- [x] Local blocklists + ABP subset
- [x] Speed modes (OFF / BALANCED / EXTREME)
- [x] Shield panel + Protection Stats
- [x] Settings
- [x] Custom New Tab Page
- [x] HTTPS-Only mode
- [x] Zero Tracking defaults
- [x] Suspicious Domain warning
- [x] Cookie Banner Control (تجريبي)
- [x] v0.1-alpha release prep + push
- [x] Bookmarks (v0.2-alpha)
- [x] Search Engine Selector (v0.2-alpha)
- [x] Shield Dashboard (v0.2-alpha)
- [x] Native Quick Actions (v0.2-alpha)
- [x] Rebrand to Nabd Browser (v0.2-alpha)
- [x] Search-only Home (v0.3-alpha)
- [x] Plus button opens Bookmarks (v0.3-alpha)
- [x] Download Manager via Android DownloadManager (v0.3-alpha)
- [x] AdBlock request-path performance optimization (v0.3-alpha)
- [x] Startup / NewTab cleanup (v0.3-alpha)

### قيد التخطيط
- [ ] Bookmark folders + search + import/export
- [ ] Settings import/export
- [ ] Better EasyList support (تحديثات دورية)
- [ ] More tracker categories
- [ ] Better icon / branding (شعار رسمي)
- [ ] Release signing (مفتاح ثابت)
- [ ] GitHub Releases + APK مرفقة
- [ ] لاحقاً: استبدال WebView بـ GeckoView أو Chromium مخصص

---

## 9. ملاحظات للمراجِع

- **حماية المستخدم في تحديث 0.1 → 0.2**: لا تُحذف بيانات المستخدم. ترحيل Room v1→v2 يضيف جدول `bookmarks` فقط ولا يلمس `history`. اسم قاعدة البيانات `ammar_browser.db` لم يتغيّر للسبب نفسه.
- **`package` لم يتغيّر**: تغيير الاسم الظاهر للمستخدم فقط؛ التحديث من v0.1-alpha إلى v0.2-alpha يجب أن يعمل كتحديث طبيعي على نفس applicationId.
- **التراجع (Rollback)**: لو احتاج المستخدم العودة إلى v0.1-alpha فعليه إلغاء التثبيت وتثبيت v0.1 (Room downgrade غير مدعوم).
- **APK غير موقّع release**: لتثبيت debug APK يجب تفعيل "Install from Unknown Sources" مرة واحدة لمصدر التثبيت.
