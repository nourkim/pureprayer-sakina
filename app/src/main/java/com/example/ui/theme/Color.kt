package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.data.model.SakinaThemeId

@Immutable
data class SakinaPalette(
    val themeId: SakinaThemeId,
    val isDark: Boolean,
    val gradientTop: Color,
    val gradientMid: Color,
    val gradientBottom: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val surfaceCard: Color,
    val surfaceCardActive: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderSubtle: Color,
    val borderActive: Color,
    val ornamentColor: Color,
    val successTint: Color,
    val warningTint: Color
)

object SakinaPalettes {

    val NurAlFajr = SakinaPalette(
        themeId = SakinaThemeId.NUR_AL_FAJR,
        isDark = false,
        gradientTop = Color(0xFFFAF6EC),
        gradientMid = Color(0xFFF3ECE0),
        gradientBottom = Color(0xFFE7EFEA),
        primary = Color(0xFF0E5A4F),
        onPrimary = Color(0xFFFFFFFF),
        accent = Color(0xFFB8862D),
        surfaceCard = Color(0xE6FFFFFF),
        surfaceCardActive = Color(0xFFFFFFFF),
        surfaceElevated = Color(0xFFF7F3EA),
        textPrimary = Color(0xFF152622),
        textSecondary = Color(0xFF536963),
        borderSubtle = Color(0x330E5A4F),
        borderActive = Color(0xCC0E5A4F),
        ornamentColor = Color(0x180E5A4F),
        successTint = Color(0xFF167C5A),
        warningTint = Color(0xFFB87018)
    )

    val RawdahEmerald = SakinaPalette(
        themeId = SakinaThemeId.RAWDAH_EMERALD,
        isDark = true,
        gradientTop = Color(0xFF071B17),
        gradientMid = Color(0xFF0C2B24),
        gradientBottom = Color(0xFF051210),
        primary = Color(0xFF5CE0C4),
        onPrimary = Color(0xFF04221C),
        accent = Color(0xFFE8C874),
        surfaceCard = Color(0x4D1B4A41),
        surfaceCardActive = Color(0x8025685B),
        surfaceElevated = Color(0xFF10332B),
        textPrimary = Color(0xFFF4F7F5),
        textSecondary = Color(0xFFB0D1C9),
        borderSubtle = Color(0x405CE0C4),
        borderActive = Color(0xFFE8C874),
        ornamentColor = Color(0x20E8C874),
        successTint = Color(0xFF5CE0C4),
        warningTint = Color(0xFFF0B454)
    )

    val LailAlQadr = SakinaPalette(
        themeId = SakinaThemeId.LAIL_AL_QADR,
        isDark = true,
        gradientTop = Color(0xFF081023),
        gradientMid = Color(0xFF112044),
        gradientBottom = Color(0xFF070D1D),
        primary = Color(0xFF88BCFF),
        onPrimary = Color(0xFF071730),
        accent = Color(0xFFF5CC73),
        surfaceCard = Color(0x4D1B2D56),
        surfaceCardActive = Color(0x8026407B),
        surfaceElevated = Color(0xFF132244),
        textPrimary = Color(0xFFF1F5FF),
        textSecondary = Color(0xFFB2C3E8),
        borderSubtle = Color(0x4088BCFF),
        borderActive = Color(0xFFF5CC73),
        ornamentColor = Color(0x20F5CC73),
        successTint = Color(0xFF6FE3B5),
        warningTint = Color(0xFFF2B95A)
    )

    val MiskAlabaster = SakinaPalette(
        themeId = SakinaThemeId.MISK_ALABASTER,
        isDark = false,
        gradientTop = Color(0xFFF6F7F9),
        gradientMid = Color(0xFFECEFF3),
        gradientBottom = Color(0xFFE6ECEC),
        primary = Color(0xFF1A4B63),
        onPrimary = Color(0xFFFFFFFF),
        accent = Color(0xFFB56A4C),
        surfaceCard = Color(0xE6FFFFFF),
        surfaceCardActive = Color(0xFFFFFFFF),
        surfaceElevated = Color(0xFFF2F5F7),
        textPrimary = Color(0xFF162129),
        textSecondary = Color(0xFF576670),
        borderSubtle = Color(0x301A4B63),
        borderActive = Color(0xCCB56A4C),
        ornamentColor = Color(0x181A4B63),
        successTint = Color(0xFF187A5E),
        warningTint = Color(0xFFB56A4C)
    )

    val QubbaObsidian = SakinaPalette(
        themeId = SakinaThemeId.QUBBA_OBSIDIAN,
        isDark = true,
        gradientTop = Color(0xFF120F0C),
        gradientMid = Color(0xFF1F1A14),
        gradientBottom = Color(0xFF0B0907),
        primary = Color(0xFFF1C16F),
        onPrimary = Color(0xFF231807),
        accent = Color(0xFFE2954F),
        surfaceCard = Color(0x5535281D),
        surfaceCardActive = Color(0x884D3B2B),
        surfaceElevated = Color(0xFF261E16),
        textPrimary = Color(0xFFFDFBF7),
        textSecondary = Color(0xFFD6C6B2),
        borderSubtle = Color(0x45F1C16F),
        borderActive = Color(0xFFF1C16F),
        ornamentColor = Color(0x24F1C16F),
        successTint = Color(0xFF72D99B),
        warningTint = Color(0xFFE8A156)
    )

    fun forId(themeId: SakinaThemeId): SakinaPalette = when (themeId) {
        SakinaThemeId.NUR_AL_FAJR -> NurAlFajr
        SakinaThemeId.RAWDAH_EMERALD -> RawdahEmerald
        SakinaThemeId.LAIL_AL_QADR -> LailAlQadr
        SakinaThemeId.MISK_ALABASTER -> MiskAlabaster
        SakinaThemeId.QUBBA_OBSIDIAN -> QubbaObsidian
    }
}
