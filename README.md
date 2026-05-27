# نبض (Nabd) - v1.0 Release

متصفح أندرويد احترافي، سريع، وآمن، مبني باستخدام Kotlin و Material Design 3. يتميز بنظام حظر إعلانات متطور وطبقات حماية للخصوصية.

## المميزات الرئيسية
- **مانع إعلانات ذكي:** يعتمد على EasyList Lite وحظر النطاقات والأنماط (Hybrid Engine).
- **تجاوز الـ Anti-Adblock:** تقنيات متطورة لمنع المواقع من كشف حظر الإعلانات.
- **إخفاء بصري (Cosmetic Filtering):** حذف المساحات الفارغة والعناصر المزعجة من الصفحة.
- **واجهة Material 3:** تصميم عصري، خفيف، ويدعم اللغة العربية بشكل كامل (RTL).
- **خصوصية مطلقة:** لا يوجد تتبع، لا توجد إعلانات مدمجة، ولا يتم جمع أي بيانات.

## طريقة البناء من Termux

### 1. المتطلبات
```bash
pkg install openjdk-17
```

### 2. بناء نسخة Debug
```bash
chmod +x gradlew
./gradlew assembleDebug
```
ستجد الملف في: `app/build/outputs/apk/debug/app-debug.apk`

### 3. بناء وتوقيع نسخة Release (من داخل Termux)

للحصول على نسخة نهائية قابلة للتثبيت، اتبع الخطوات التالية:

**أ- إنشاء مفتاح التوقيع (يتم مرة واحدة فقط):**
```bash
keytool -genkey -v -keystore nabd-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias nabd-alias
```

**ب- بناء نسخة الـ APK:**
```bash
./gradlew assembleRelease
```

**ج- محاذاة الملف (Zipalign):**
*(إذا كان متاحاً في أدوات البناء لديك)*
```bash
zipalign -v -p 4 app/build/outputs/apk/release/app-release-unsigned.apk Nabd-v1.0-release-aligned.apk
```

**د- التوقيع النهائي (Apksigner):**
```bash
apksigner sign --ks nabd-release-key.jks --out /sdcard/Download/Nabd-v1.0-release-signed.apk Nabd-v1.0-release-aligned.apk
```
*أو استخدم `jarsigner` إذا لم يتوفر `apksigner`:*
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore nabd-release-key.jks app/build/outputs/apk/release/app-release-unsigned.apk nabd-alias
```

## ملاحظات الخصوصية
- التطبيق مفتوح المصدر بالكامل.
- يتم حظر ملفات تعريف الارتباط للجهات الخارجية افتراضياً.
- لا يتواصل التطبيق مع أي خوادم خارجية بخلاف المواقع التي تزورها.
- **لا يحتوي التطبيق على أي أدوات تتبع (Trackers) أو إعلانات مدمجة.**
