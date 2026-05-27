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

### 3. بناء نسخة Release (احترافية)
لبناء نسخة Release موقعة، يجب عليك أولاً إنشاء مفتاح (Keystore) محلياً:

**إنشاء المفتاح:**
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```

**البناء:**
```bash
./gradlew assembleRelease
```
*ملاحظة: نسخة Release الناتجة ستكون غير موقعة (Unsigned) ما لم تقم بإعداد `signingConfigs` في ملف `build.gradle` بمفتاحك الخاص.*

## ملاحظات الخصوصية
- التطبيق مفتوح المصدر بالكامل.
- يتم حظر ملفات تعريف الارتباط للجهات الخارجية افتراضياً.
- لا يتواصل التطبيق مع أي خوادم خارجية بخلاف المواقع التي تزورها.
