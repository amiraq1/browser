# Spec 001 — Nabd Browser — Brand UI Polish

> **Phase:** Brand-2 — UI Brand Polish
> **Status:** Draft (spec only — no code yet)
> **Depends on:** [`000-product-principles.md`](000-product-principles.md), `docs/BRAND_IDENTITY.md`, Phase Brand-1 (app icon + brand identity, commit `08161a5`)

---

## 1. Title

**Nabd Browser — Brand UI Polish**

---

## 2. Problem

الواجهة فيها ميزات قوية (Shield panel, Protection Stats, AdBlock Debug, Settings),
لكن عدداً من الشاشات لا تزال غير موحّدة بصرياً مع هوية **Nabd / نبض**:

- ❌ بعض الشاشات لا تعكس أسلوب **dark cyber privacy**.
- ❌ رمزية **shield + pulse** غير حاضرة بصرياً خارج الـ Shield panel.
- ❌ الـ **cyan / teal / neon green accents** غير مستخدمة بشكل متّسق.
- ❌ شاشة Protection Stats لا تعطي إحساس **dashboard حماية** حقيقي.
- ❌ Settings تبدو كإعدادات Android عامة، لا كـ **لوحة Privacy**.
- ❌ About لا تبدو كـ **صفحة هوية** رسمية للمتصفح.
- ❌ Bookmarks و History بصرياً عاديتان، رغم أن السلوك مكتمل.

النتيجة: المستخدم لا يشعر فوراً أن هذا متصفح خصوصية بهوية مستقلة.

---

## 3. Goals

- ✅ توحيد ألوان الواجهات مع `docs/BRAND_IDENTITY.md`.
- ✅ جعل **Settings** تبدو كلوحة Privacy (أقسام واضحة، بطاقات Slate, عناوين Cyan/Teal).
- ✅ جعل **Protection Stats** تبدو كـ **Dashboard حماية** (KPI cards, accents, إحساس "لايف").
- ✅ جعل **About** تبدو كصفحة هوية رسمية (Logo + Name + tagline + version + privacy notice).
- ✅ تحسين **Bookmarks** و **History** بصرياً بدون تغيير السلوك (نفس الـ adapters, نفس الـ IDs).
- ✅ الحفاظ على **وضوح النصوص والتباين** (WCAG-friendly contrast على الخلفية الداكنة).
- ✅ كل التغييرات تكون **layout / styles / drawables / colors** قدر الإمكان — لا منطق.

---

## 4. Non-goals (خارج نطاق هذه المرحلة)

- ❌ لا تغيير `package` / `applicationId` (`com.ammar.browser` يبقى كما هو).
- ❌ لا تغيير Room schema أو DAOs أو Entities.
- ❌ لا تغيير AdBlock logic أو ABP parser أو blocklists.
- ❌ لا تغيير navigation behavior (Back/Forward/Reload, intents, deep links).
- ❌ لا تغيير `android:id` لأي View مستخدم في Kotlin.
- ❌ لا dependencies جديدة (لا Material3 جديد، لا Compose هنا، لا libs خارجية).
- ❌ لا إعادة تصميم **New Tab Page** — جاهزة وستبقى كما هي.
- ❌ لا تغيير menu IDs.
- ❌ لا تغيير string keys (نصوص العرض ممكن تتغير، لكن الـ keys في `strings.xml` تبقى).

---

## 5. Target screens & layouts

الشاشات المستهدفة في هذه المرحلة:

| # | Screen / Layout | Priority |
|---|---|---|
| 1 | `res/layout/activity_settings.xml` | عالية |
| 2 | `res/layout/activity_protection_stats.xml` | عالية |
| 3 | `res/layout/activity_about.xml` | عالية |
| 4 | `res/layout/activity_bookmarks.xml` | متوسطة |
| 5 | `res/layout/activity_history.xml` | متوسطة |
| 6 | `res/layout/item_bookmark.xml` | متوسطة |
| 7 | `res/layout/item_history.xml` | متوسطة |
| 8 | `res/layout/activity_main.xml` (toolbar / shield text فقط إن كان بسيطاً) | منخفضة |
| 9 | `res/layout/activity_adblock_debug.xml` (إن كان مناسباً ضمن نفس النمط) | منخفضة |

ملفات الستايل والألوان المتأثرة (للقراءة/التعديل المحدود فقط):

- `res/values/colors.xml` — tokens موجودة بالفعل (`nabd_deep_navy`, `nabd_pulse_cyan`, `nabd_teal`, `nabd_neon_green`, `nabd_slate`); يمكن إضافة aliases مساعدة فقط (مثل `nabd_text_primary`) إن لزم.
- `res/values/styles.xml` / `themes.xml` — قد نضيف styles جديدة للبطاقات والعناوين، **بدون** حذف styles مستخدمة.
- `res/drawable/` — ممكن إضافة backgrounds جديدة (`bg_card_slate.xml`, `bg_kpi_pulse.xml` …) **بدون** حذف موجود.

---

## 6. Visual rules (الهوية)

### Color tokens المستخدمة (من `colors.xml`)

- `@color/nabd_deep_navy` — الخلفية الرئيسية.
- `@color/nabd_slate` — البطاقات / containers.
- `@color/nabd_pulse_cyan` — العناوين الرئيسية / accents.
- `@color/nabd_teal` — العناوين الثانوية / icons / dividers مهمة.
- `@color/nabd_neon_green` — حالات النجاح / "Protected" / counters إيجابية.

### القواعد

- ✅ **الخلفية:** `nabd_deep_navy` لكل الـ root layouts المستهدفة.
- ✅ **البطاقات / Cards / Sections:** `nabd_slate` مع corner radius متّسق (12dp مقترح).
- ✅ **العناوين الرئيسية (H1/H2):** `nabd_pulse_cyan` أو `nabd_teal`.
- ✅ **حالات النجاح / "Protected" / counters:** `nabd_neon_green`.
- ✅ **النصوص الأساسية:** أبيض/فاتح كافٍ للتباين على Deep Navy.
- ✅ **النصوص الثانوية:** رمادي فاتح (يمكن تعريفه كـ alias مثل `nabd_text_secondary`).
- ❌ **لا تباين ضعيف** — كل نص يجب أن يقرأ بسهولة على الخلفية الداكنة.
- ❌ **لا ألوان عشوائية خارج الهوية** — استثناء وحيد: ألوان التحذيرات/الأخطاء (يمكن إبقاء أحمر/كهرماني للـ warnings).
- ✅ **رمزية shield + pulse:** تظهر في About (logo) وفي Protection Stats (header) كحدّ أدنى.

---

## 7. Safety requirements

هذه القيود غير قابلة للتفاوض في هذه المرحلة:

- ❌ **ممنوع تغيير `android:id`** لأي View موجود ومستخدم من Kotlin (أي ID مستهدف بـ `findViewById` / view binding).
- ❌ **ممنوع حذف أي View** مستخدم في Kotlin — يمكن تغيير شكله، لا وجوده.
- ❌ **ممنوع تغيير menu IDs** في `res/menu/*.xml`.
- ❌ **ممنوع كسر `findViewById`** — قبل تعديل أي layout، يُتحقق من جميع `findViewById` المرتبطة به.
- ❌ **ممنوع تغيير الـ root view type** إذا كان Kotlin يعتمد عليه (مثلاً `RecyclerView` يبقى `RecyclerView`).
- ✅ **كل commit يجب أن يبني** (`./gradlew assembleDebug` ينجح).
- ✅ كل تعديل layout يُختبر يدوياً بفتح الشاشة المعنية على الأقل مرة.

قبل تعديل أي layout:
1. ابحث عن كل `findViewById` و view binding يستهدف ذلك الـ layout.
2. سجّل قائمة الـ IDs التي يجب أن تبقى كما هي.
3. عدّل المظهر فقط، مع الحفاظ على تلك الـ IDs والتسلسل المنطقي.

---

## 8. Acceptance tests

التغيير مقبول إذا (وفقط إذا) تحقّقت كل البنود التالية:

- ✅ `./gradlew assembleDebug --no-daemon` → **BUILD SUCCESSFUL**.
- ✅ تشغيل التطبيق على جهاز/محاكي بدون **crash** عند البداية.
- ✅ **Settings** تفتح وتعرض كل الأقسام، ويمكن تبديل كل toggle بدون استثناء.
- ✅ **Protection Stats** تفتح وتعرض الأرقام (counters + charts/cards) بشكل صحيح.
- ✅ **About** تفتح وتعرض الاسم والإصدار وإشعار الخصوصية.
- ✅ **Bookmarks** تفتح، تعرض القائمة، يمكن **إضافة** و **حذف** عنصر — كل ذلك يعمل كما قبل التعديل.
- ✅ **History** تفتح وتعرض السجل بشكل صحيح.
- ✅ **Shield Panel** يعمل كما هو (toggle, allowlist, blocked counter).
- ✅ **New Tab Dashboard** لا يتغيّر بصرياً ولا سلوكياً.
- ✅ **Toolbar** الرئيسي يعمل كما هو (URL bar, back/forward/reload, tabs, menu).
- ✅ **AdBlock Debug** screen تفتح بدون crash (حتى لو لم نلمس مظهرها).
- ✅ التباين مقبول — لا نص "يضيع" على الخلفية.

---

## 9. Implementation plan

نمشي على الشاشات بالترتيب التالي. كل خطوة = commit مستقل قابل للبناء.

### Step 1 — Settings (أول شاشة، الأكثر ظهوراً)
- ملفات: `activity_settings.xml` (+ ربما styles لـ "PrivacyCard").
- النتيجة المتوقعة: أقسام داخل بطاقات Slate, عناوين Cyan, خلفية Deep Navy.
- لا تغيير في IDs أو ترتيب الإعدادات.

### Step 2 — Protection Stats (Dashboard look)
- ملفات: `activity_protection_stats.xml` (+ خلفيات KPI إن لزم).
- النتيجة: header بهوية shield+pulse, KPI cards بـ Slate و Neon Green للأرقام.

### Step 3 — About (صفحة الهوية الرسمية)
- ملفات: `activity_about.xml`.
- النتيجة: Logo + "Nabd / نبض" + tagline + version + privacy notice.

### Step 4 — Bookmarks & History
- ملفات: `activity_bookmarks.xml`, `activity_history.xml`, `item_bookmark.xml`, `item_history.xml`.
- النتيجة: items داخل بطاقات Slate, accents Cyan/Teal للعناوين, تواريخ ثانوية.
- **لا تغيير** في الـ adapter logic ولا IDs.

### Step 5 — Main toolbar / Shield text (تعديل بسيط فقط)
- ملف: `activity_main.xml` (إن كان التعديل layout/colors فقط).
- النتيجة: accents متّسقة، لا تغيير سلوكي.
- إن كان التعديل يتطلب لمس Kotlin → **يُؤجَّل** لمرحلة لاحقة.

### Step 6 — AdBlock Debug (اختياري في هذه المرحلة)
- ملف: `activity_adblock_debug.xml`.
- يُعدَّل فقط إذا كان التعديل بسيطاً ومتوافقاً مع نفس النمط، وإلا يُؤجَّل.

### Commit policy لهذه المرحلة
- كل Step = commit واحد على الأقل (يمكن أن يُقسَّم لأكثر إذا لزم).
- صيغة الـ commit messages المقترحة:
  - `style(settings): apply Nabd dark cyber palette`
  - `style(stats): turn Protection Stats into a privacy dashboard`
  - `style(about): redesign About as official identity page`
  - `style(bookmarks,history): unify list items with brand cards`
- لا `git add .` — الإضافة بأسماء ملفات صريحة فقط.
- لا push حتى ينتهي الـ Phase ويوافق المالك.

---

## 10. Open questions (للحسم قبل البدء بالكود)

- هل نضيف aliases جديدة في `colors.xml` (مثل `nabd_text_primary`, `nabd_text_secondary`, `nabd_divider`) أم نستخدم القائمة الحالية فقط؟
- هل نضيف styles مشتركة جديدة (`Nabd.Card`, `Nabd.Header`, `Nabd.Stat`) لتوحيد الشاشات؟
- corner radius موحّد للبطاقات: 12dp (مقترح) — مقبول؟
- في About: هل نستخدم نفس app icon أم variant خاص بالشاشة؟

تُجاب هذه الأسئلة في تعليق على الـ spec قبل فتح Step 1.
