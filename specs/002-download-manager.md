# Nabd Browser — Download Manager Spec

## 1. Problem
نبض حالياً متصفح خصوصية جيد، لكن لا يملك Download Manager واضح. المستخدم يحتاج تنزيل ملفات من WebView بطريقة آمنة، مفهومة، ومحلية.

## 2. Goals
- دعم تحميل الملفات من صفحات الويب.
- استخدام Android DownloadManager قدر الإمكان.
- عرض Toast أو إشعار واضح عند بدء التحميل.
- احترام الخصوصية: لا telemetry، لا إرسال روابط لأي سيرفر.
- حفظ الملفات في مجلد Downloads العام.
- التعامل مع اسم الملف ونوعه وحجمه إن توفر.
- منع crash عند روابط download غير مكتملة.
- الحفاظ على WebView وAdBlock وHTTPS-Only.

## 3. Non-goals
- لا Download UI كامل الآن.
- لا pause/resume مخصص.
- لا download queue داخل التطبيق.
- لا player أو preview.
- لا تغيير AdBlock logic.
- لا تغيير Room/database.
- لا إضافة dependencies.
- لا دعم torrent أو background custom service.

## 4. User Stories
- كمستخدم، عندما أضغط رابط PDF أو APK أو ZIP، يبدأ التحميل.
- كمستخدم، أرى Toast يقول إن التحميل بدأ.
- كمستخدم، أجد الملف في Downloads.
- كمستخدم، إذا فشل الرابط أو كان غير آمن، لا ينهار التطبيق.
- كمستخدم، التحميل لا يرسل بيانات خارجية من التطبيق.

## 5. Technical Plan
- استخدام WebView.setDownloadListener داخل WebViewEngine أو MainActivity حسب الأنسب.
- عند onDownloadStart:
  - استخرج url, userAgent, contentDisposition, mimeType, contentLength.
  - استخدم URLUtil.guessFileName.
  - استخدم DownloadManager.Request.
  - أضف headers الضرورية مثل User-Agent و Cookie إذا آمن ومطلوب.
  - setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED).
  - setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename).
  - enqueue عبر DownloadManager.
  - Toast: Download started.
- أضف permission إن كانت ضرورية فقط حسب target SDK.
- لا تطلب صلاحيات غير لازمة.
- تعامل مع exceptions بـ Toast آمن.

## 6. Privacy/Security
- لا telemetry.
- لا تسجيل URLs في logs.
- لا إرسال URLs لأي خدمة.
- لا تحميل schemes داخلية:
  - ammar://
  - about:
  - data:
  - file:
  - content:
- HTTPS-Only موجود للتنقل، لكن downloads القادمة من WebView قد تكون http/https حسب الموقع. يجب توثيق السلوك.
- لا تستخدم addJavascriptInterface.

## 7. Files likely affected
- app/src/main/java/com/ammar/browser/engine/WebViewEngine.kt
- app/src/main/java/com/ammar/browser/engine/BrowserEngine.kt إذا احتجنا callback
- app/src/main/java/com/ammar/browser/ui/MainActivity.kt إذا احتجنا Context/Toast
- app/src/main/AndroidManifest.xml إذا احتجنا permission
- app/src/main/res/values/strings.xml لإضافة نصوص Toast

## 8. Acceptance Tests
- BUILD SUCCESSFUL.
- فتح رابط PDF يبدأ تحميل.
- فتح رابط ZIP يبدأ تحميل.
- Toast يظهر.
- الملف يظهر في Downloads.
- لا crash مع URL فارغ أو data: أو about: أو ammar://.
- البحث والـ New Tab لا يتأثران.
- AdBlock وHTTPS-Only وSettings وBookmarks لا تتأثر.
- لا صلاحيات خطيرة غير مبررة.

## 9. Commit Plan
- commit 1: docs(specs): add download manager spec
- commit 2 لاحقاً: feat(downloads): handle WebView downloads with Android DownloadManager
