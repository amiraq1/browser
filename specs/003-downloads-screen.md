# Nabd Browser — Downloads Screen Spec

## 1. Problem

نبض يدعم تنزيل الملفات عبر Android DownloadManager، لكن لا توجد شاشة واضحة داخل التطبيق لشرح أو فتح التنزيلات. المستخدم يحتاج طريقة بسيطة للوصول إلى مجلد Downloads أو معرفة أين ذهبت الملفات.

## 2. Goals

- إضافة شاشة Downloads بسيطة داخل التطبيق.
- عرض رسالة واضحة:
  Downloads are saved to your device Downloads folder.
- إضافة زر:
  Open Downloads Folder
- إضافة زر أو نص:
  System Downloads
- فتح مجلد Downloads أو شاشة التنزيلات في النظام إذا أمكن.
- إبقاء المنطق بسيطاً وآمناً.
- لا telemetry.
- لا قراءة ملفات المستخدم.
- لا فهرسة مجلد Downloads.
- لا صلاحيات تخزين جديدة.
- لا database.

## 3. Non-goals

- لا download queue داخل التطبيق.
- لا list للملفات المحملة الآن.
- لا pause/resume/cancel.
- لا file manager كامل.
- لا قراءة محتويات Downloads.
- لا request permissions جديدة.
- لا tracking أو analytics.
- لا custom background service.

## 4. User Stories

- كمستخدم، أريد أن أعرف أين تُحفظ التنزيلات.
- كمستخدم، أريد زر يفتح مجلد Downloads أو تطبيق الملفات.
- كمستخدم، لا أريد أن يقرأ المتصفح ملفاتي بدون إذن.
- كمستخدم، إذا لم يستطع التطبيق فتح Downloads، أريد Toast واضح بدل crash.

## 5. Technical Plan

- إنشاء `DownloadsActivity`.
- إنشاء layout:
  `activity_downloads.xml`
- تسجيل Activity في `AndroidManifest.xml`.
- إضافة زر في Settings ضمن Browser card:
  Downloads
- عند الضغط يفتح `DownloadsActivity`.
- داخل `DownloadsActivity`:
  - عرض عنوان:
    Downloads
    التنزيلات
  - عرض شرح:
    Files downloaded by Nabd are saved to your device Downloads folder.
  - زر:
    Open Downloads Folder
  - عند الضغط:
    استخدم Intent آمن لفتح مجلد Downloads أو تطبيق الملفات إن أمكن.
    إذا فشل، Toast:
    Unable to open Downloads folder.
- لا تقرأ محتويات Downloads.
- لا تطلب permissions.
- لا تستخدم `MANAGE_EXTERNAL_STORAGE`.
- لا تستخدم `READ_EXTERNAL_STORAGE`.
- لا تستخدم `WRITE_EXTERNAL_STORAGE` جديد.

## 6. Privacy/Security

- لا telemetry.
- لا قراءة ملفات المستخدم.
- لا إرسال مسارات أو أسماء ملفات.
- لا أذونات تخزين جديدة.
- لا `addJavascriptInterface`.
- لا تغيير DownloadManager logic.
- الشاشة معلوماتية/تنقلية فقط.

## 7. Files likely affected

- `app/src/main/java/com/ammar/browser/ui/DownloadsActivity.kt`
- `app/src/main/res/layout/activity_downloads.xml`
- `app/src/main/res/layout/activity_settings.xml`
- `app/src/main/java/com/ammar/browser/settings/SettingsActivity.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`

## 8. Acceptance Tests

- BUILD SUCCESSFUL.
- Settings تفتح.
- زر Downloads يظهر في Settings.
- الضغط على Downloads يفتح `DownloadsActivity`.
- `DownloadsActivity` تفتح بدون crash.
- زر Open Downloads Folder لا يسبب crash.
- إذا تعذر فتح المجلد يظهر Toast واضح.
- لا permissions جديدة.
- لا قراءة ملفات Downloads.
- DownloadManager الحالي لا يتغير.
- Search/Home/AdBlock/Bookmarks/History لا تتأثر.

## 9. Commit Plan

- commit 1:
  `docs(specs): add downloads screen spec`
- commit 2 لاحقاً:
  `feat(downloads): add Downloads screen`
