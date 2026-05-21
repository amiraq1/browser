# Nabd Browser — Lite Mode Spec

## 1. Problem

بعض المستخدمين يريدون تحميل صفحات أسرع واستهلاك بيانات أقل، خصوصاً على الشبكات الضعيفة أو الأجهزة محدودة الأداء. نبض حالياً يملك Speed Modes للحماية والحجب، لكنه لا يملك Lite Mode واضح يتحكم بسلوك تحميل الوسائط.

## 2. Goals

- إضافة Lite Mode اختياري لاحقاً.
- يكون OFF افتراضياً.
- يوضح للمستخدم أنه قد يكسر بعض المواقع أو يمنع ظهور الصور.
- يقلل استهلاك البيانات.
- يحسن سرعة تحميل الصفحات.
- يحافظ على الخصوصية الحالية.
- لا يرسل أي بيانات لأي سيرفر.
- لا يستخدم proxy خارجي.
- لا telemetry.

## 3. Non-goals

- لا proxy compression.
- لا cloud acceleration.
- لا VPN.
- لا server-side rendering.
- لا AI summarization.
- لا تعطيل AdBlock.
- لا تعطيل HTTPS-Only.
- لا تعطيل DNT/GPC.
- لا تغيير Search behavior.
- لا تغيير DownloadManager.

## 4. Possible Behaviors

Lite Mode قد يفعل بعض أو كل هذه الخيارات:

- Disable automatic image loading:
  `WebSettings.loadsImagesAutomatically = false`
- Reduce media autoplay if possible.
- Keep JavaScript enabled لأن تعطيله يكسر أغلب المواقع.
- Keep DOM Storage enabled لأن تعطيله يكسر تسجيل الدخول والمواقع الحديثة.
- Keep `cacheMode = LOAD_DEFAULT`.
- Do not use `LOAD_NO_CACHE`.
- Do not use `LOAD_CACHE_ELSE_NETWORK` unless there is explicit offline behavior.
- Show clear status in Settings and Shield panel.

## 5. User Stories

- كمستخدم، أريد وضعاً أخف عند ضعف الإنترنت.
- كمستخدم، أريد معرفة أن الصور قد لا تظهر.
- كمستخدم، أريد تشغيل/إيقاف Lite Mode بسهولة.
- كمستخدم، لا أريد أن تمر بياناتي عبر proxy خارجي.
- كمستخدم، إذا كسر Lite Mode موقعاً، أستطيع إيقافه.

## 6. Privacy/Security Rules

- Lite Mode local-only.
- لا proxy.
- لا telemetry.
- لا إرسال URLs.
- لا تغيير سياسة cookies.
- لا تغيير HTTPS-Only.
- لا تغيير mixed content blocking.
- لا تغيير file/content access.
- لا `addJavascriptInterface`.
- لا تعطيل AdBlock.

## 7. Technical Plan — Future

- إنشاء `LiteModeSettings` شبيه بـ `SpeedSettings`.
- حفظ الحالة في SharedPreferences.
- Default: OFF.
- إضافة toggle في Settings:
  Lite Mode
  Loads pages faster by blocking images. Some sites may look broken.
- عند التغيير:
  - يطبق على WebView الحالي.
  - يطبق على WebViews الجديدة.
- في `WebViewEngine`:
  - إذا Lite Mode ON:
    `settings.loadsImagesAutomatically = false`
  - إذا OFF:
    `settings.loadsImagesAutomatically = true`
- لا تغيّر JavaScript أو DOM Storage في المرحلة الأولى.
- لا تغيّر `cacheMode` في المرحلة الأولى.

## 8. UI Plan — Future

Settings:

- قسم Performance أو Browser.
- Toggle:
  Lite Mode
- وصف صغير:
  Blocks images to reduce data usage and speed up loading. Some sites may look incomplete.

Shield panel:

- Status row:
  Lite Mode: ON/OFF

## 9. Acceptance Tests — Future

- BUILD SUCCESSFUL.
- Lite Mode OFF by default.
- عند ON، الصور لا تُحمّل تلقائياً.
- عند OFF، الصور تعود.
- Search/Home لا يتأثر.
- Downloads لا تتأثر.
- AdBlock لا يتأثر.
- HTTPS-Only لا يتأثر.
- لا permissions جديدة.
- لا telemetry.
- لا crash.

## 10. Risks

- بعض المواقع تعتمد على الصور كأزرار أو محتوى أساسي.
- بعض المستخدمين قد يظنون أن الموقع مكسور.
- يجب أن يكون الوصف واضحاً.
- يجب أن يكون OFF افتراضياً.

## 11. Commit Plan

- commit 1:
  `docs(specs): add Lite Mode spec`
- commit 2 لاحقاً:
  `feat(performance): add Lite Mode setting`

## 12. Implementation Status

- **Implemented in v0.4 development.**
  - Spec landed in commit `af6fe32 docs(specs): add Lite Mode spec`.
  - Setting + WebView wiring landed in `d7efe44 feat(performance): add Lite Mode setting`.
  - Shield Panel status row landed in `215814c feat(shield): show Lite Mode status in Shield panel`.
- **Default OFF** — `LiteModeSettings.enabled` defaults to `false`; SharedPreferences read defaults to `false`.
- **Uses `WebSettings.loadsImagesAutomatically` only.** No other WebView setting is touched. JavaScript, DOM Storage, `cacheMode`, mixed-content policy, file/content/geolocation access, third-party cookie blocking, and DNT/GPC headers are all unchanged.
- **QA passed** — see Phase v0.4-6 final QA report on `main`. All 20 checklist items pass; no defects.
- **No new permissions** — the manifest still has only `INTERNET`, `ACCESS_NETWORK_STATE`, and the legacy `WRITE_EXTERNAL_STORAGE android:maxSdkVersion="28"`.
- **No telemetry / proxy / remote calls** — `LiteModeSettings` reads/writes one local SharedPreferences key. No network code path involved.
