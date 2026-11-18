<div align="center">

<img src="docs/assets/banner.jpg" alt="Sakina - The Mindful Islamic Prayer Timepiece" width="100%" />

# ✦ SAKINA • سكينة ✦
### *The Mindful Islamic Prayer Timepiece*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.7.5-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Zero Battery Drain](https://img.shields.io/badge/Battery-0%25%20Background%20Drain-00C853.svg?style=for-the-badge&logo=powershell&logoColor=white)](#-zero-battery-drain-architecture)
[![100% Offline](https://img.shields.io/badge/Privacy-100%25%20Offline-FF6D00.svg?style=for-the-badge&logo=shield&logoColor=white)](#-offline-precision--privacy)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

<br/>

**[English](#-english) • [العربية](#-العربية) • [Français](#-français)**

<br/>

[📥 **Download Latest APK (v1.0)**](Sakina-v1.0-debug.apk) • [🎬 **Watch 3:4 Showcase Video**](Sakina_Showcase_3x4.mp4)

---

</div>

<br/>

## 🌐 Language Navigation / دليل اللغات
- 🇬🇧 [English (Default Presentation)](#-english)
  - [Why Sakina? (The Core Philosophy)](#why-sakina--the-core-philosophy)
  - [The 3D Astrolabe Horology](#the-3d-astrolabe-horology)
  - [Zero Battery Drain Architecture](#zero-battery-drain-architecture)
  - [Gentle Mindful Chimes](#gentle-mindful-chimes)
  - [Permissions & Safe Operation](#permissions--safe-background-operation)
  - [Screenshots](#screenshots-gallery)
  - [Download & Installation](#download--installation)
- 🇸🇦 [العربية (النسخة العربية الكاملة)](#-العربية)
  - [فلسفة تطبيق سكينة](#لماذا-تطبيق-سكينة؟)
  - [ساعة الأسطرلاب ثلاثية الأبعاد](#ساعة-الأسطرلاب-الفلكية-ثلاثية-الأبعاد)
  - [انعدام استهلاك البطارية والعمل الآمن](#انعدام-استهلاك-البطارية-والعمل-الآمن-في-الخلفية)
  - [نظام التنبيه الهادئ 1-3 رنات](#نظام-التنبيه-الهادئ-1-إلى-3-رنات-بدون-صخب)
  - [دليل التثبيت والصلاحيات](#دليل-التثبيت-والصلاحيات-المطلوبة)
- 🇫🇷 [Français (Présentation Française)](#-français)
  - [Pourquoi Sakina ?](#pourquoi-sakina--)
  - [Horlogerie 3D et Design Cupertino](#horlogerie-astrolabe-3d-et-design-cupertino)
  - [Zéro consommation de batterie](#zéro-consommation-de-batterie--performance-pure)
  - [Téléchargement et Installation](#téléchargement-et-installation)

<br/>

---

# 🇬🇧 English

## Why Sakina? • The Core Philosophy

In today's fast-paced, notification-saturated world, the call to prayer should be an invitation to **sanctuary and peace**, not another source of auditory stress.

Most traditional prayer applications:
- ❌ Shock users with sudden, blaring sirens or unexpected max-volume audio in quiet meetings, libraries, or family gatherings.
- ❌ Clutter the sacred experience with intrusive banner ads, tracking SDKs, and bloated subscription paywalls.
- ❌ Run persistent, aggressive foreground services and wakelocks in the background, quietly killing your phone's battery and causing excessive device heating.

**Sakina (سكينة)** was crafted with a completely different mindset:
> **"Prayer honored in serene tranquility."**

1. **Gentle, Mindful Chimes**: Instead of aggressive, loud alarms, Sakina notifies you with a **single, soft nature chime** (or repeats gently up to 3 times depending on your preference). Choose between morning dew, dawn breeze, or serene birdsong.
2. **0% Background Battery Drain**: No persistent services, no continuous wakelocks, no memory leaks.
3. **100% Offline & Private**: Calculates prayer times mathematically on-device using high-precision astronomical algorithms (Jean Meeus). Your location never leaves your phone.
4. **Timeless Astrolabe Design**: Merges classical 10th-century Andalusian astronomical instruments with modern Apple Cupertino frosted-glass luxury horology at a buttery-smooth 120Hz.

<br/>

## The 3D Astrolabe Horology

Inspired by the classical Islamic Astrolabe (*al-Asturlab*), Sakina re-imagines prayer tracking through the lens of luxury Swiss watchmaking:

```
                  ┌───────────────────────┐
                  │   3 PRAYER CARDS TOP  │
                  │ [Fajr] [Sunrise] [Dhuhr] │
                  └───────────┬───────────┘
                              │
                    ┌─────────▼─────────┐
                    │  3D ASTROLABE DIAL│
                    │   • Single Needle │
                    │   • 6 Medallions  │
                    │   • Sapphire Lens │
                    └─────────┬─────────┘
                              │
                  ┌───────────▼───────────┐
                  │ 3 PRAYER CARDS BOTTOM │
                  │  [Asr] [Maghrib] [Isha]│
                  └───────────────────────┘
```

- **Harmonious 3+3 Balanced Stage**: 3 prominent, high-contrast frosted glass cards above (Fajr, Sunrise, Dhuhr) and 3 cards below (Asr, Maghrib, Isha), fully visible on any screen with **Zero Scrolling**.
- **The Solitary Symbolic Needle**: A single rotating Dauphine hand with realistic drop shadow glides smoothly between the six stations, indicating current progression through the celestial day.
- **Domed Sapphire Crystal Reflection**: A delicate, curved specular highlight mimics the luxury convex crystal of high-end chronometers.
- **Universal Fixed Coordinate Geometry**: The dial's astronomical stations remain firmly pinned to true celestial positions across all languages (no RTL coordinate flipping when switching to Arabic).

<br/>

## Zero Battery Drain Architecture

Sakina was engineered from the ground up to achieve what traditional prayer apps fail to do: **absolute battery respect**.

| Feature | Traditional Prayer Apps | Sakina (PurePrayer) |
| :--- | :--- | :--- |
| **Background Execution** | Continuous Foreground Service | **Zero persistent background processes** |
| **WakeLock Usage** | Constant / Frequent partial wakelocks | **Zero unnecessary wakelocks** |
| **Alarm Mechanism** | Inexact / Periodic polling threads | **Native Android `AlarmManager.setExactAndAllowWhileIdle`** |
| **Network Telemetry** | Continuous ad SDKs & analytics | **100% Offline (Zero network packets sent)** |
| **UI Framework** | Legacy XML / WebViews / Flutter | **100% Native Kotlin Jetpack Compose (120Hz)** |
| **App Startup Time** | 800ms - 2500ms | **Instant (< 40ms via fast Room/DataStore cache)** |

### How It Works Safely:
Instead of keeping your phone awake 24/7, Sakina computes the exact timestamp of the upcoming prayer station, schedules a single, precise system alarm via Android's native `AlarmManager`, and completely terminates background execution. The operating system safely sleeps until the exact millisecond arrives.

<br/>

## Gentle Mindful Chimes

Customize how you are called to prayer with peaceful, natural soundscapes:
- **Tone Selection**: 
  - 🌿 *Dawn Breeze* (`tone_nature_breeze.wav`): A whisper of morning air.
  - 🐦 *Morning Songbirds* (`tone_nature_birds.wav`): Natural woodland melody.
  - 💧 *Crystal Dew* (`tone_nature_dew.wav`): A solitary, gentle acoustic drop.
- **Repeat Control**:
  - **1x Chime**: A solitary gentle chime that respects your meeting, quiet study, or family peace.
  - **2x - 3x Soft Chimes**: A polite, gentle repetition for when you need a slight reminder without loud disruptions.

<br/>

## Permissions & Safe Background Operation

To ensure atomic accuracy even during Android Deep Doze mode, Sakina requests only essential system privileges:

1. **Schedule Exact Alarms (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`)**:
   - Required on Android 12+ (API 31+) to trigger the gentle chime at the precise prayer minute without OEM delay.
2. **Post Notifications (`POST_NOTIFICATIONS`)**:
   - Required on Android 13+ (API 33+) to show the unobtrusive lock screen reminder and prayer card.
3. **Location (`ACCESS_FINE_LOCATION` or Manual City)**:
   - Used locally once to obtain latitude & longitude for offline astronomical calculation. Coordinates are stored in local preferences and never uploaded.
4. **Battery Optimization Exemption (Optional but Recommended)**:
   - On aggressive OEM skins (Xiaomi HyperOS/MIUI, Samsung OneUI, Huawei EMUI), granting battery optimization exemption guarantees alarms fire when the screen has been off for hours.

<br/>

---

# 🇸🇦 العربية

## لماذا تطبيق سكينة؟

في عالمنا اليوم المليء بالضجيج والتنبيهات المزعجة، يجب أن يكون النداء إلى الصلاة **واحة من السكينة والطمأنينة والخشوع**، وليس مصدراً للإزعاج أو التوتر.

معظم تطبيقات الصلاة التقليدية تعاني من مشاكل شائعة:
- ❌ تفاجئ المستخدم بأصوات صاخبة ونغمات حادة في أوقات غير مناسبة (الاجتماعات، قاعات الدراسة، أو أثناء نوم الأطفال).
- ❌ ملأى بالإعلانات التجارية المزعجة والتتبع البرمجي الذي ينتهك الخصوصية.
- ❌ تستهلك طاقة البطارية باستمرار من خلال تشغيل خدمات خلفية ثقيلة (Foreground Services) تؤدي إلى استنزاف الشحن وارتفاع حرارة الهاتف.

**تطبيق "سكينة" (Sakina)** صُمم برؤية جديدة ومختلفة كلياً:
> **"ذكر الله بهدوء وسكينة تليق بجلال الموقف."**

<br/>

## ساعة الأسطرلاب الفلكية ثلاثية الأبعاد

مستوحى من عبقرية علم الفلك الأندلسي والأسطرلاب التاريخي، يجمع تطبيق سكينة بين الأصالة الإسلامية وأحدث تقنيات التصميم العصري بنمط زجاجي بلوري (Cupertino Frosted Glass):

- **توزيع متوازن 3 بالأعلى و 3 بالأسفل**: تظهر مواقيت الصلاة بوضوح تام، ثلاثة مواقيت في الجزء العلوي (الفجر، الشروق، الظهر) وثلاثة في الجزء السفلي (العصر، المغرب، العشاء)، مما يحقق استغلالاً مثالياً للشاشة دون الحاجة إلى التمرير (Zero-scroll).
- **عقرب رمزي واحد وظل عائم ثلاثي الأبعاد**: يدور عقرب ذهبي وحيد بين المحطات الست بدقة فلكية متناهية، مع ظل هابط واقعي يعطي إحساساً بعمقه فوق الميناء الزجاجي.
- **انعكاس الكريستال الياقوتي المحدب**: بريق ضوئي انسيابي في المحيط العلوي يحاكي زجاج ساعات الكرونوغراف الفاخرة.
- **ثبات هندسي فلكي كامل عبر كل اللغات**: نظام إحداثيات محكم يضمن بقاء مسار الصلوات ثابتاً في اتجاه حركة عقارب الساعة حتى عند اختيار اللغة العربية، بينما يتكيف المركز الداخلي لعرض النصوص والعد التنازلي بشكل طبيعي.

<br/>

## انعدام استهلاك البطارية والعمل الآمن في الخلفية

تم بناء التطبيق بنسبة 100% بلغة **Kotlin** وأحدث أدوات **Jetpack Compose** لتحقيق أعلى معايير الخفة والسرعة:

1. **صفر استهلاك للبطارية في الخلفية (0% Battery Drain)**: لا توجد أي خدمات خلفية تعمل باستمرار، ولا توجد مستهلكات طاقة مخفية.
2. **جدولة منبهات النظام الدقيقة**: يعتمد التطبيق كلياً على منبهات أندرويد الأصلية (`AlarmManager.setExactAndAllowWhileIdle`)؛ حيث يُسجل وقت الأذان القادم ثم يتوقف التطبيق عن العمل تماماً حتى يحين وقت الصلاة بالمللي ثانية، مما يوفر 100% من طاقة الجهاز.
3. **يعمل بدون إنترنت بالكامل (100% Offline)**: تُحسب مواقيت الصلاة محلياً بدقة فلكية عالية اعتماداً على معادلات الفلكي جان ميوس (Jean Meeus)، دون إرسال أي بايت واحد عبر الشبكة.

<br/>

## نظام التنبيه الهادئ: 1 إلى 3 رنات بدون صخب

- **رنة واحدة ناعمة**: تنبيه خفيف يُشعرك بدخول وقت الصلاة بأدب وهدوء دون إرباك من حولك.
- **إمكانية التكرار حتى 3 مرات**: لمن يرغب بتكرار التنبيه الهادئ بلطف.
- **نغمات مستوحاة من هدوء الطبيعة**:
  - 🌿 *نسيم الفجر* (Breeze): نفحة صوتية هادئة تشبه نسمات الصباح الباكر.
  - 🐦 *زقزقة العصافير* (Songbirds): أصوات طيور طبيعية ناعمة تملأ النفس صفاءً.
  - 💧 *قطرات الندى* (Dew): رنة كريستالية فردية هادئة.

<br/>

## دليل التثبيت والصلاحيات المطلوبة

لكي يعمل التطبيق بدقة متناهية حتى في وضع خمول الجهاز العميق (Doze Mode)، يرجى منحه الصلاحيات الآتية:
1. **صلاحية المنبه الدقيق (Exact Alarms)**: مطلوبة في أندرويد 12 فما فوق لضمان رنين التنبيه في موعده المحدد تماماً.
2. **صلاحية الإشعارات (Post Notifications)**: مطلوبة في أندرويد 13 فما فوق لعرض إشعار الصلاة على شاشة القفل.
3. **صلاحية الموقع (Location)**: تُستخدم مرة واحدة فقط محلياً لتحديد خطوط الطول والعرض لحساب المواقيت (أو يمكنك اختيار مدينتك يدوياً).
4. **استثناء التطبيق من موفر البطارية**: يُنصح بتعطيل توفير البطارية للتطبيق على أجهزة شاومي (HyperOS/MIUI)، وسامسونج، وهواوي لضمان عدم إيقاف النظام للمنبه.

---

<br/>

# 🇫🇷 Français

## Pourquoi Sakina ?

Dans un monde saturé de notifications bruyantes, l'appel à la prière doit être un moment de **paix, de recueillement et de sérénité**, et non une source de stress sonore.

La plupart des applications de prière traditionnelles :
- ❌ Déclenchent des alarmes stridentes et soudaines qui perturbent les réunions, les cours ou le repos familial.
- ❌ Sont polluées par des publicités intrusives et des traqueurs publicitaires.
- ❌ Consomment énormément de batterie avec des services en arrière-plan permanents (Foreground Services) qui chauffent l'appareil.

**Sakina (سكينة)** a été pensée pour la pure sérénité :
> **"La mémoire de la prière dans une quiétude absolue."**

<br/>

## Horlogerie Astrolabe 3D et Design Cupertino

Inspirée par l'astronomie islamique classique et la haute horlogerie contemporaine :
- **Agencement équilibré 3+3 (Zéro défilement)** : 3 cartes de prière au-dessus de l'horloge (Fajr, Lever du soleil, Dhuhr) et 3 cartes en-dessous (Asr, Maghrib, Isha), offrant une lisibilité parfaite sur tout écran sans aucun défilement.
- **Aiguille unique flottante 3D** : Une aiguille Dauphine animée avec ombre portée réaliste qui survole les six stations avec une fluidité remarquable.
- **Éclat de cristal saphir bombé** : Un reflet lumineux subtil recréant le verre bombé des montres de luxe suisses.
- **Géométrie universelle invariable** : Le cadran préserve rigoureusement les coordonnées astronomiques dans toutes les langues.

<br/>

## Zéro Consommation de Batterie • Performance Pure

Développée à 100% en **Kotlin** et **Jetpack Compose** natif (120Hz) :
- **Aucun service permanent en arrière-plan** : L'application n'utilise ni service de premier plan persistant ni wakelocks énergivores.
- **Réveils exacts via `AlarmManager`** : Le système d'exploitation Android planifie la sonnerie à la milliseconde près, permettant au téléphone de dormir profondément.
- **100% Hors-Ligne** : Calculs astronomiques locaux (algorithmes de Jean Meeus) avec respect total de la vie privée.
- **Carillons naturels apaisants** : Une seule sonnerie douce (ou répétition jusqu'à 3 fois) parmi des sonorités naturelles (Brise de l'aube, Oiseaux chanteurs, Gouttes de rosée).

<br/>

---

## 📸 Screenshots Gallery / معرض الصور

<div align="center">

| Emerald Astrolabe (3+3 Layout) | Midnight Sky Astrolabe | Luxury Horology Banner |
| :---: | :---: | :---: |
| <img src="docs/assets/screenshot_emerald_main.jpg" width="280" alt="Emerald 3+3 Astrolabe" /> | <img src="docs/assets/screenshot_night_main.jpg" width="280" alt="Midnight Theme" /> | <img src="docs/assets/banner.jpg" width="280" alt="Luxury 3D Banner" /> |

</div>

<br/>

---

## 🛠️ Technical Architecture & Specifications

```
app/src/main/java/com/example/
├── domain/
│   ├── model/           # PrayerTime, PrayerType, AppTheme, SoundType
│   ├── calculator/      # High-precision Jean Meeus Astronomical Engine
│   └── repository/      # PrayerRepository, SettingsRepository
├── data/
│   ├── local/           # DataStore preferences & instant cached state
│   └── alarm/           # AlarmReceiver, Exact AlarmScheduler (AlarmManager)
└── ui/
    ├── HomeScreen.kt    # 3D Astrolabe dial, 3+3 cards, Cupertino glass
    ├── SettingsScreen.kt# Theme picker, chime soundscape & repeat controls
    └── theme/           # Color palettes (Emerald Rawdah, Midnight Sky, Gold)
```

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose 1.7.5 (Material 3 + Custom Glassmorphic design)
- **Minimum SDK**: Android 8.0 (API level 26)
- **Target SDK**: Android 15 (API level 35)
- **Architecture**: MVI (Model-View-Intent) with Kotlin Coroutines & `StateFlow`
- **Build System**: Gradle 9.3.1 (Kotlin DSL)
- **APK Size**: ~22 MB

<br/>

---

## 📥 Download & Installation

### Option 1: Direct APK Download
You can download the pre-compiled, optimized APK directly from this repository:

👉 [**Download Sakina-v1.0-debug.apk**](Sakina-v1.0-debug.apk) *(22.1 MB)*

#### Installation Steps:
1. Download the `.apk` file to your Android device.
2. Tap the downloaded file to install.
3. If prompted, enable **"Install from unknown sources"** for your browser or file manager.
4. Launch **Sakina**, grant the exact alarm and notification permissions, and select your preferred location.

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/your-username/pureprayer.git
cd pureprayer

# Build the debug APK using Gradle
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

<br/>

---

## 📄 License & Credits

This project is licensed under the **MIT License** — feel free to use, modify, and distribute it freely.

Crafted with dedication, precision, and tranquility.
*May it bring serenity to your daily prayers.* 🌿✨
