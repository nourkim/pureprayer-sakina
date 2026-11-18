package com.example.data.model

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.example.R

enum class PrayerType(
    val id: Int,
    @StringRes val labelRes: Int,
    val isObligatoryAlert: Boolean
) {
    FAJR(0, R.string.prayer_fajr, true),
    SUNRISE(1, R.string.prayer_sunrise, false),
    DHUHR(2, R.string.prayer_dhuhr, true),
    ASR(3, R.string.prayer_asr, true),
    MAGHRIB(4, R.string.prayer_maghrib, true),
    ISHA(5, R.string.prayer_isha, true)
}

enum class LocationMode(val code: String) {
    GPS("gps"),
    MANUAL("manual");

    companion object {
        fun fromCode(code: String?): LocationMode =
            entries.firstOrNull { it.code == code } ?: GPS
    }
}

enum class SakinaLanguage(
    val code: String,
    @StringRes val labelRes: Int
) {
    SYSTEM("system", R.string.lang_system),
    AR("ar", R.string.lang_ar),
    EN("en", R.string.lang_en),
    FR("fr", R.string.lang_fr),
    TR("tr", R.string.lang_tr);

    companion object {
        fun fromCode(code: String?): SakinaLanguage =
            entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}

enum class SakinaThemeId(
    val id: Int,
    @StringRes val labelRes: Int,
    val isDark: Boolean
) {
    NUR_AL_FAJR(0, R.string.theme_nur_fajr, false),
    RAWDAH_EMERALD(1, R.string.theme_rawdah_emerald, true),
    LAIL_AL_QADR(2, R.string.theme_lail_qadr, true),
    MISK_ALABASTER(3, R.string.theme_misk_alabaster, false),
    QUBBA_OBSIDIAN(4, R.string.theme_qubba_obsidian, true);

    companion object {
        fun fromId(id: Int): SakinaThemeId =
            entries.firstOrNull { it.id == id } ?: RAWDAH_EMERALD
    }
}

enum class SakinaToneId(
    val code: String,
    val channelId: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @StringRes val channelNameRes: Int,
    @RawRes val rawResId: Int,
    val rawName: String
) {
    NATURE_DEW(
        code = "nature_dew",
        channelId = "sakina_tone_nature_dew_v2",
        titleRes = R.string.tone_dew_title,
        descRes = R.string.tone_dew_desc,
        channelNameRes = R.string.notif_channel_dew,
        rawResId = R.raw.tone_nature_dew,
        rawName = "tone_nature_dew"
    ),
    NATURE_BIRDS(
        code = "nature_birds",
        channelId = "sakina_tone_nature_birds_v2",
        titleRes = R.string.tone_birds_title,
        descRes = R.string.tone_birds_desc,
        channelNameRes = R.string.notif_channel_birds,
        rawResId = R.raw.tone_nature_birds,
        rawName = "tone_nature_birds"
    ),
    NATURE_BREEZE(
        code = "nature_breeze",
        channelId = "sakina_tone_nature_breeze_v2",
        titleRes = R.string.tone_breeze_title,
        descRes = R.string.tone_breeze_desc,
        channelNameRes = R.string.notif_channel_breeze,
        rawResId = R.raw.tone_nature_breeze,
        rawName = "tone_nature_breeze"
    );

    companion object {
        fun fromCode(code: String?): SakinaToneId =
            entries.firstOrNull { it.code == code } ?: NATURE_DEW
    }
}

enum class CalculationMethod(val apiId: Int, val labelEn: String, val labelAr: String) {
    UMM_AL_QURA(4, "Umm Al-Qura University, Makkah", "جامعة أم القرى (مكة المكرمة)"),
    MWL(3, "Muslim World League (MWL)", "رابطة العالم الإسلامي"),
    EGYPTIAN(5, "Egyptian General Authority of Survey", "الهيئة المصرية العامة للمساحة"),
    MOROCCO(21, "Ministry of Habous & Islamic Affairs (Morocco)", "وزارة الأوقاف والشؤون الإسلامية (المغرب)"),
    JORDAN(23, "Ministry of Awqaf & Islamic Affairs (Jordan)", "وزارة الأوقاف والشؤون المقدسات (الأردن)"),
    DUBAI(16, "Dubai Islamic Affairs (IACAD)", "دائرة الشؤون الإسلامية (دبي)"),
    QATAR(10, "Ministry of Awqaf (Qatar)", "وزارة الأوقاف (قطر)"),
    KUWAIT(9, "Ministry of Awqaf (Kuwait)", "وزارة الأوقاف (الكويت)"),
    GULF(8, "Gulf Region", "منطقة الخليج"),
    DIYANET(13, "Diyanet İşleri Başkanlığı (Turkey)", "رئاسة الشؤون الدينية (تركيا)"),
    UOIF(12, "UOIF (France)", "اتحاد المنظمات الإسلامية (فرنسا)"),
    ISNA(2, "ISNA (North America)", "الجمعية الإسلامية لأمريكا الشمالية"),
    KARACHI(1, "Univ. of Islamic Sciences, Karachi", "جامعة العلوم الإسلامية (كراتشي)"),
    JAKIM(17, "JAKIM (Malaysia)", "إدارة التنمية الإسلامية (ماليزيا)"),
    KEMENAG(20, "Kemenag (Indonesia)", "وزارة الشؤون الدينية (إندونيسيا)"),
    MUIS(11, "MUIS (Singapore)", "المجلس الإسلامي (سنغافورة)"),
    MOONSIGHTING(15, "Moonsighting Committee Worldwide", "لجنة تحري الهلال العالمية");

    companion object {
        fun fromApiId(id: Int): CalculationMethod =
            entries.firstOrNull { it.apiId == id } ?: MWL
    }
}

data class CityZone(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val countryEn: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val defaultMethodId: Int
) {
    fun displayName(isArabic: Boolean): String =
        if (isArabic) "$nameAr، $countryAr" else "$nameEn, $countryEn"
}
