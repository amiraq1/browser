# Nabd Browser — Download Manager Spec

## 1. Problem

Nabd Browser حالياً متصفح Privacy-first لكنه لا يحتوي Download Manager.
عند الضغط على روابط تحميل أو تنزيل ملفات، المستخدم يحتاج تجربة واضحة وآمنة:
- يعرف اسم الملف
- يعرف الحجم إن أمكن
- يوافق قبل التنزيل
- يعرف مكان الحفظ
- لا يتم تحميل شيء بصمت

## 2. Goals

- دعم تحميل الملفات من WebView.
- عرض dialog تأكيد قبل التحميل.
- إظهار:
  - اسم الملف
  - نوع الملف
  - الحجم إن توفر
  - مصدر الرابط / host
- حفظ الملفات في مجلد Downloads الخاص بالنظام.
- إرسال إشعار أو Toast عند بدء/نجاح/فشل التحميل.
- فتح الملف بعد التحميل إن أمكن.
- الحفاظ على مبادئ الخصوصية:
  - لا إرسال URLs لأي سيرفر
  - لا telemetry
  - لا tracking
  - كل شيء محلي

## 3. Non-goals

- لا download acceleration.
- لا parallel download chunks.
- لا background service معقد الآن.
- لا custom file picker في المرحلة الأولى.
- لا cloud sync.
- لا إرسال روابط التحميل لأي API خارجي.
- لا تغيير AdBlock logic.
- لا تغيير Search/NewTab/Bookmarks/History.

## 4. User Flow

1. المستخدم يضغط رابط تحميل داخل WebView.
2. WebViewEngine يلتقط download request.
3. MainActivity يستقبل request عبر callback.
4. يظهر dialog:
   - Download file?
   - filename
   - host
   - size if known
   - Cancel / Download
5. إذا وافق المستخدم:
   - يبدأ DownloadManager system service.
   - يتم الحفظ في public Downloads.
   - يظهر notification من Android DownloadManager.
   - يظهر Toast "Download started".
6. عند الفشل:
   - لا crash
   - يظهر Toast مناسب إن أمكن.

## 5. Technical Plan

الملفات المحتملة:

- engine/BrowserEngine.kt
  - أضف callback جديد مثل:
    onDownloadRequested(tabId, url, userAgent, contentDisposition, mimeType, contentLength)

- engine/WebViewEngine.kt
  - استخدم webView.setDownloadListener
  - مرر الطلب للـ callback
  - لا تبدأ التحميل مباشرة داخل engine

- ui/MainActivity.kt
  - نفذ onDownloadRequested
  - استخرج filename عبر URLUtil.guessFileName
  - اعرض AlertDialog تأكيد
  - استخدم Android DownloadManager
  - Request:
    - setTitle(filename)
    - setDescription(host)
    - setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    - setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
    - addRequestHeader("User-Agent", userAgent) إذا موجود
  - تعامل مع exceptions

- AndroidManifest.xml
  - افحص هل تحتاج WRITE_EXTERNAL_STORAGE للأجهزة القديمة.
  - للأندرويد الحديث DownloadManager public downloads غالباً لا يحتاج صلاحية خاصة، لكن تحقق.
  - لا تضف permission غير ضروري.

- strings.xml
  - أضف نصوص dialog/toasts.

## 6. Privacy/Security Requirements

- لا تنزيل تلقائي بدون موافقة المستخدم.
- لا تحميل روابط schemes غير http/https.
- لا إرسال URL لأي طرف خارجي.
- لا telemetry.
- اعرض host للمستخدم حتى يعرف المصدر.
- إذا filename مشبوه أو فارغ، استخدم اسم آمن من URLUtil.
- لا تنفذ الملف بعد التحميل تلقائياً بدون اختيار المستخدم.
- لا تكسر Suspicious Domain Warning.
- لا تكسر HTTPS-Only.

## 7. Edge Cases

- URL فارغ أو scheme غير مدعوم.
- filename فارغ.
- contentLength = -1.
- mimeType فارغ.
- DownloadManager غير متاح أو يرمي exception.
- storage غير متاح.
- نفس اسم الملف موجود سابقاً.
- userAgent null.
- رابط download من data: أو blob: لا ندعمه في المرحلة الأولى.

## 8. Acceptance Tests

- BUILD SUCCESSFUL.
- الضغط على رابط PDF/ZIP/APK يعرض dialog.
- Cancel لا يبدأ التحميل.
- Download يبدأ التحميل عبر Android DownloadManager.
- Toast يظهر.
- لا crash عند contentLength غير معروف.
- لا crash عند mimeType null.
- http/https فقط مسموحة.
- NewTab/Search/Bookmarks/History/AdBlock/Settings لا تتأثر.
- لا IDs مكسورة.
- لا permissions غير ضرورية.

## 9. Implementation Steps

Step 1:
- أضف download callback في BrowserEngine.

Step 2:
- اربط WebView.setDownloadListener في WebViewEngine.

Step 3:
- نفذ dialog + DownloadManager في MainActivity.

Step 4:
- أضف strings.

Step 5:
- build + manual test.

## 10. Commit Plan

إذا نجحت:
- commit واحد:
  feat(downloads): add basic download manager support
