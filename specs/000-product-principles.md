# Nabd Browser — Product Principles

> هذه الوثيقة هي المرجع الأساسي لاتخاذ القرارات في مشروع **Nabd Browser / متصفح نبض**.
> أي ميزة، أي PR، وأي قرار تصميمي يجب أن يكون متوافقاً مع هذه المبادئ.
>
> الأسلوب المتبع: **Spec-Driven Development** (مستوحى من [github/spec-kit](https://github.com/github/spec-kit))
> — لكن **بدون** تثبيت الأداة. نحن نطبّق الفكرة فقط: spec → plan → tasks → acceptance.

---

## 1. Privacy First

الخصوصية ليست ميزة — هي الأساس.

- ❌ لا telemetry من أي نوع.
- ❌ لا tracking SDKs (Firebase / Analytics / Crashlytics / Ads / …).
- ❌ لا إرسال URLs أو queries أو أي بيانات تصفح إلى أي سيرفر.
- ✅ كل الحماية محلية قدر الإمكان (blocklists, heuristics, parsing).
- ✅ أي اتصال بالشبكة يجب أن يكون مبرراً وموثقاً في الـ spec.
- ✅ إذا اضطررنا مستقبلاً لأي fetch خارجي (مثل تحديث blocklists)،
  يجب أن يكون **opt-in** صريح ومُعلن في واجهة المستخدم.

---

## 2. User Control

المستخدم سيد جلسة التصفح. لا توجد "صناديق سوداء".

- المستخدم يفهم ما يحدث: ما الذي تم حجبه، ولماذا، وعلى أي موقع.
- كل آلية حماية يجب أن تكون قابلة للفهم من خلال الـ Shield panel
  أو شاشة Protection Stats أو AdBlock Debug.
- أي ميزة قد تكسر موقعاً (HTTPS-Only, third-party cookies blocking,
  EXTREME mode, suspicious domain warning) **يجب** أن:
  - تُعرض بوضوح في الـ UI.
  - يكون لها مفتاح تعطيل أو allowlist لكل موقع.
  - تشرح للمستخدم سبب الكسر.
- لا "حماية صامتة" تكسر الموقع دون تفسير.

---

## 3. Security by Default

الإعدادات الافتراضية يجب أن تكون آمنة، حتى لو لم يلمس المستخدم أي إعداد.

- ✅ HTTPS-Only mode مفعّل افتراضياً.
- ✅ Third-party cookies محجوبة افتراضياً.
- ✅ Mixed content محجوب.
- ✅ File / content access معطّل في WebView.
- ✅ Geolocation معطّلة افتراضياً.
- ✅ DNT + GPC headers مرسلة.
- ✅ Suspicious domain warning نشط (heuristics محلية فقط).
- ❌ لا `addJavascriptInterface` غير آمن. أي JS bridge جديد يجب:
  - أن يمر عبر spec.
  - أن يُقيَّد بـ origin محدد.
  - أن لا يكشف أي API يسمح بقراءة الملفات أو تنفيذ كود.
- ❌ لا `setAllowFileAccessFromFileURLs` ولا `setAllowUniversalAccessFromFileURLs`.

---

## 4. Lightweight Android

نبقى صغاراً وسريعاً.

- اللغة: **Kotlin**.
- المحرك الحالي: **Android WebView** (مع abstraction layer للسماح بالتبديل
  لاحقاً إلى GeckoView أو Chromium custom).
- ❌ لا تُضاف dependency جديدة إلا عند الحاجة الفعلية، ويجب تبريرها في الـ spec.
- ❌ لا refactor ضخم بدون سبب واضح ومذكور في spec/plan.
- ✅ نُفضّل الحلول البسيطة على الحلول "النظيفة معمارياً" التي تضخّم الكود.
- ✅ APK size و cold start time مقاييس نتابعها.

---

## 5. Build Safety

كل commit في `main` يجب أن يكون قابلاً للبناء والتشغيل.

- ✅ كل commit يجب أن يبني (`./gradlew assembleDebug`) بنجاح.
- ✅ Test pass قبل الـ commit (حين تتوفر اختبارات للمنطقة المعدّلة).
- ❌ ممنوع `git add .` — نضيف ملفات محددة فقط.
- ❌ ممنوع رفع APK أو build artifacts (`app/build/`, `*.apk`, `*.aab`).
- ❌ ممنوع رفع secrets أو keystores أو `local.properties` أو `.env`.
- ❌ لا force push على `main`.
- ✅ commit messages واضحة وتشرح "لماذا" وليس فقط "ماذا".

---

## 6. Visual Identity

الهوية البصرية جزء من تجربة الخصوصية — ليست زخرفة.

- الاسم: **Nabd / نبض**.
- الأسلوب: **dark cyber privacy style**.
- الرمز: **shield + pulse** (الدرع = الحماية، النبض = الحياة/التتبّع المرئي).
- الألوان والـ tokens الرسمية: مرجعها `docs/BRAND_IDENTITY.md`.
- أي شاشة أو مكوّن جديد يجب أن يحترم هذه الهوية — لا ألوان عشوائية،
  ولا إعادة اختراع للـ palette.

---

## 7. Roadmap Process

أي ميزة جديدة "كبيرة" (تلمس أكثر من ملف، أو تضيف سطح هجوم، أو تغيّر سلوكاً افتراضياً)
يجب أن تمر بهذا المسار قبل كتابة كود الإنتاج:

1. **spec/** — وصف الميزة:
   - المشكلة.
   - السلوك المطلوب من منظور المستخدم.
   - ما هو خارج النطاق (non-goals).
2. **plan/** — التصميم التقني:
   - الملفات المتأثرة.
   - الـ APIs / الكلاسات الجديدة.
   - أثر الخصوصية والأمان.
3. **tasks/** — تقسيم العمل:
   - خطوات صغيرة قابلة للمراجعة.
   - ترتيب التنفيذ.
4. **acceptance tests** — كيف نعرف أنها انتهت:
   - اختبارات يدوية أو آلية.
   - حالات الفشل التي يجب أن لا تحدث.

الميزات الصغيرة (إصلاح bug، تعديل نص، تعديل لون) معفاة من هذا المسار،
لكن يجب أن تظل متوافقة مع المبادئ من 1 إلى 6.

---

## مرجع الالتزام

عند كل قرار، اسأل:

- هل يحترم **Privacy First**؟
- هل المستخدم يفهم ما يحدث (**User Control**)؟
- هل الإعداد الافتراضي آمن (**Security by Default**)؟
- هل التغيير صغير ومبرّر (**Lightweight Android**)؟
- هل الـ commit نظيف وقابل للبناء (**Build Safety**)؟
- هل الهوية البصرية محترمة (**Visual Identity**)؟
- هل الميزة الكبيرة لها spec/plan/tasks/acceptance (**Roadmap Process**)؟

إذا كان الجواب "لا" على أي منها — لا نمضي قُدماً قبل المعالجة.
