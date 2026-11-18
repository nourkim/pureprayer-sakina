package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PrayerType
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSakinaPalette
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SakinaPalette
import com.example.util.LocaleHelper
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * شاشة سكينة الفلكية (Sakina 3D Astrolabe Masterpiece):
 * ساعة إسلامية فلكية ثلاثية الأبعاد ذات عقرب رمزي واحد بحواف مشطوفة فاخرة،
 * مستوحاة من الأسطرلاب الأندلسي مع خلفية هندسية إسلامية غنية تملأ الفراغات بتناغم تام.
 */
@Composable
fun HomeScreen(
    uiState: MainUiState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalSakinaPalette.current
    val context = LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val isArabic = LocaleHelper.isArabic(uiState.settings.language)
    val textFont = if (isArabic) FontFamily.Default else PlusJakartaFontFamily

    // حركة العقرب الانسيابية الفلكية
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressToNext.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "astrolabe_hand_progress"
    )

    // حساب زاوية العقرب لحظياً بين المحطات الست
    val handAngle = calculateAstrolabeAngle(
        nextPrayerType = uiState.nextPrayerType,
        progress = animatedProgress
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.gradientTop,
                        palette.gradientMid,
                        palette.gradientBottom
                    )
                )
            )
    ) {
        val screenHeight = maxHeight
        val isTallScreen = screenHeight >= 640.dp
        val availableWidth = maxWidth

        // القطر الفلكي الكبير لساعة الأوقات: يملأ الشاشة بفخامة واتساع (325dp - 335dp) مع احتواء الشاشة بالكامل
        val maxDialHeight = (screenHeight.value - 295f).coerceAtMost(335f)
        val dialSize = min(availableWidth.value - 16f, maxDialHeight).coerceAtLeast(250f).dp

        // تقسيم الصلوات: 3 بالأعلى و 3 بالأسفل لحجم كبير وواضح وتصميم آيفون احترافي
        val topRows = uiState.rows.take(3)
        val bottomRows = uiState.rows.drop(3).take(3)

        // 1. خلفية الهندسة الإسلامية العميقة التي تملأ الفراغات بالروحانية والتفاصيل
        IslamicSacredGeometryBackdrop(
            ornamentColor = palette.ornamentColor,
            accentColor = palette.accent,
            primaryColor = palette.primary,
            modifier = Modifier.fillMaxSize()
        )

        // 2. الحاوية الرأسية الرئيسية
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .then(
                    if (!isTallScreen) Modifier.verticalScroll(rememberScrollState())
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isTallScreen) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp)
        ) {
            // رأس الصفحة: الموقع الفلكي، التاريخ، وزر الإعدادات المجسم
            AstrolabeHeader3D(
                uiState = uiState,
                palette = palette,
                textFont = textFont,
                onOpenSettings = onOpenSettings
            )

            // كبسولات الصلوات الثلاث العليا: الفجر، الشروق، الظهر (3 بالأعلى كبيرة وواضحة)
            PrayerCardsRowIOS(
                rows = topRows,
                palette = palette,
                textFont = textFont,
                is24Hour = is24Hour,
                isArabic = isArabic
            )

            // ساعة الأوقات الفلكية في الوسط تماماً (3D Metallic Glass Astrolabe Timepiece)
            AstrolabeDial3D(
                uiState = uiState,
                handAngle = handAngle,
                progress = animatedProgress,
                palette = palette,
                textFont = textFont,
                dialSize = dialSize,
                is24Hour = is24Hour,
                isArabic = isArabic
            )

            // كبسولات الصلوات الثلاث السفلى: العصر، المغرب، العشاء (3 بالأسفل كبيرة وواضحة)
            PrayerCardsRowIOS(
                rows = bottomRows,
                palette = palette,
                textFont = textFont,
                is24Hour = is24Hour,
                isArabic = isArabic
            )

            // تذييل الحالة والمزامنة الزجاجي
            AstrolabeFooterPill(
                uiState = uiState,
                palette = palette,
                textFont = textFont
            )
        }
    }
}

/**
 * حساب زاوية العقرب بين محطات الصلاة الست (60 درجة لكل محطة)
 */
private fun calculateAstrolabeAngle(
    nextPrayerType: PrayerType,
    progress: Float
): Float {
    val prayerOrder = listOf(
        PrayerType.FAJR,
        PrayerType.SUNRISE,
        PrayerType.DHUHR,
        PrayerType.ASR,
        PrayerType.MAGHRIB,
        PrayerType.ISHA
    )
    val nextIndex = prayerOrder.indexOf(nextPrayerType).takeIf { it >= 0 } ?: 0
    val prevIndex = if (nextIndex == 0) 5 else nextIndex - 1

    val startAngle = prevIndex * 60f
    val currentAngle = (startAngle + progress * 60f) % 360f
    return currentAngle
}

/**
 * رأس الصفحة الزجاجي الفاخر ثلاثي الأبعاد
 */
@Composable
private fun AstrolabeHeader3D(
    uiState: MainUiState,
    palette: SakinaPalette,
    textFont: FontFamily,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // كبسولة موقع ذهبية مجسمة
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .shadow(4.dp, CircleShape, spotColor = palette.accent.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    palette.accent.copy(alpha = 0.50f),
                                    palette.surfaceElevated.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(1.2.dp, palette.accent.copy(alpha = 0.75f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Text(
                    text = uiState.settings.cityName,
                    fontFamily = textFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val dateLine = buildString {
                if (uiState.hijriDateText.isNotBlank()) append(uiState.hijriDateText)
                if (uiState.gregorianDateText.isNotBlank()) {
                    if (isNotEmpty()) append("  •  ")
                    append(uiState.gregorianDateText)
                }
            }
            if (dateLine.isNotBlank()) {
                Text(
                    text = dateLine,
                    fontFamily = textFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = palette.textSecondary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // زر الإعدادات المعدني
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .testTag("settings_button")
                .minimumInteractiveComponentSize()
                .size(44.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = palette.primary.copy(alpha = 0.40f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.surfaceCardActive.copy(alpha = if (palette.isDark) 0.90f else 0.98f),
                            palette.surfaceCard.copy(alpha = if (palette.isDark) 0.65f else 0.85f)
                        )
                    )
                )
                .border(
                    1.4.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x99FFFFFF),
                            palette.borderSubtle,
                            Color(0x15000000)
                        )
                    ),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.cd_settings_button),
                tint = palette.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * ساعة الأسطرلاب الفلكية ثلاثية الأبعاد (3D Metallic Glass Astrolabe Dial):
 * تجمع بين إطار الشطب المعدني البارز، نقوش أشعة الساعات الفلكية،
 * محطات الصلوات الموزعة بالمللي دون أي قص، والعقرب الممتد بدقة للمسار المحيطي.
 */
@Composable
private fun AstrolabeDial3D(
    uiState: MainUiState,
    handAngle: Float,
    progress: Float,
    palette: SakinaPalette,
    textFont: FontFamily,
    dialSize: Dp,
    is24Hour: Boolean,
    isArabic: Boolean
) {
    val dialRadiusDp = dialSize / 2
    // نصف قطر المحطات الفلكية: موضوعة بدقة على مسار الحلقة لتبدو كميداليات ساعة سويسرية فاخرة
    val stationsRadiusDp = dialRadiusDp - 26.dp
    // قطر قلب الساعة الداخلي الكبير والواضح
    val hubDiameterDp = dialSize * 0.49f
    val systemLayoutDirection = LocalLayoutDirection.current

    // ساعة المواقيت الفلكية ثابتة جغرافياً وهندسياً بنظام LTR لمنع انعكاس الأيقونات نهائياً عند تغيير اللغة
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .size(dialSize)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    spotColor = palette.primary.copy(alpha = if (palette.isDark) 0.60f else 0.35f),
                    ambientColor = Color(0x40000000)
                )
                .clip(CircleShape)
                // تدرج زجاجي ثلاثي الأبعاد محدب يعكس لون الثيم الفاخر
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.surfaceElevated.copy(alpha = if (palette.isDark) 0.90f else 0.98f),
                            palette.surfaceCard.copy(alpha = if (palette.isDark) 0.70f else 0.90f),
                            palette.surfaceCardActive.copy(alpha = if (palette.isDark) 0.85f else 0.95f),
                            palette.gradientMid.copy(alpha = if (palette.isDark) 0.95f else 0.90f)
                        )
                    )
                )
                // إطار الشطب المعدني الخارجي المحدب (3D Beveled Rim)
                .border(
                    2.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xE6FFFFFF),
                            palette.accent.copy(alpha = 0.80f),
                            palette.primary.copy(alpha = 0.60f),
                            Color(0x30000000)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // 1. رسم شبكة الأسطرلاب، الأشعة الفلكية، التدريجات، القوس المضيء، والعقرب الفاخر
            AstrolabeCanvas3D(
                handAngle = handAngle,
                progress = progress,
                nextPrayerType = uiState.nextPrayerType,
                stationsRadius = stationsRadiusDp,
                palette = palette,
                modifier = Modifier.fillMaxSize()
            )

            // 2. قلب الساعة الداخلي ثلاثي الأبعاد (The Inner Timepiece Chronometer)
            Box(
                modifier = Modifier
                    .size(hubDiameterDp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        spotColor = palette.accent.copy(alpha = 0.45f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                palette.surfaceCardActive.copy(alpha = if (palette.isDark) 0.96f else 1.0f),
                                palette.surfaceElevated.copy(alpha = if (palette.isDark) 0.88f else 0.96f),
                                palette.surfaceCard.copy(alpha = if (palette.isDark) 0.75f else 0.88f)
                            )
                        )
                    )
                    .border(
                        1.6.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xAAFFFFFF),
                                palette.accent.copy(alpha = 0.70f),
                                palette.primary.copy(alpha = 0.40f)
                            )
                        ),
                        CircleShape
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // استعادة اتجاه لغة النظام للنصوص والمعلومات بالوسط
                CompositionLocalProvider(LocalLayoutDirection provides systemLayoutDirection) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // اسم الصلاة القادمة
                        Text(
                            text = stringResource(uiState.nextPrayerType.labelRes),
                            fontFamily = textFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = palette.accent
                        )

                        // العد التنازلي البارز والواضح جداً
                        Text(
                            text = uiState.countdownText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = 1.3.sp,
                            color = palette.textPrimary
                        )

                        // التوقيت المجدول للصلاة القادمة
                        val formattedNextTime = formatPrayerTimeDisplay(
                            hhMm = uiState.nextPrayerTimeText,
                            is24Hour = is24Hour,
                            isArabic = isArabic
                        )
                        val timeDisplay = if (uiState.nextPrayerIsTomorrow) {
                            stringResource(R.string.tomorrow_fajr_prefix, formattedNextTime)
                        } else {
                            formattedNextTime
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(palette.primary.copy(alpha = 0.25f))
                                .border(1.2.dp, palette.primary.copy(alpha = 0.60f), RoundedCornerShape(9.dp))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = timeDisplay,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = palette.primary
                            )
                        }

                        // التسمية
                        Row(
                            modifier = Modifier.padding(top = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = palette.textSecondary.copy(alpha = 0.85f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = stringResource(R.string.remaining_label),
                                fontFamily = textFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.5.sp,
                                color = palette.textSecondary.copy(alpha = 0.90f)
                            )
                        }
                    }
                }
            }

            // 3. المحطات الست الفلكية المستقرة بدقة رياضية داخل القرص
            val prayerOrder = listOf(
                PrayerType.FAJR,
                PrayerType.SUNRISE,
                PrayerType.DHUHR,
                PrayerType.ASR,
                PrayerType.MAGHRIB,
                PrayerType.ISHA
            )

            prayerOrder.forEachIndexed { index, prayerType ->
                val angleDeg = index * 60f
                // 0° في الأعلى (12 o'clock)
                val rad = (angleDeg - 90.0) * (PI / 180.0)
                val offsetX = (stationsRadiusDp.value * cos(rad)).dp
                val offsetY = (stationsRadiusDp.value * sin(rad)).dp

                val rowData = uiState.rows.find { it.type == prayerType }
                val isNext = prayerType == uiState.nextPrayerType

                Box(
                    modifier = Modifier.offset(x = offsetX, y = offsetY),
                    contentAlignment = Alignment.Center
                ) {
                    AstrolabeStationNode3D(
                        type = prayerType,
                        isNext = isNext,
                        isPassed = rowData?.isPassed ?: false,
                        palette = palette
                    )
                }
            }
        }
    }
}

/**
 * ميدالية المحطة الفلكية على حافة ساعة الأسطرلاب:
 * كبسولة دائرية فخمة ثلاثية الأبعاد تحتوي على أيقونة الصلاة بمظهر ساعات اليد السويسرية والآيفون الفاخرة
 */
@Composable
private fun AstrolabeStationNode3D(
    type: PrayerType,
    isNext: Boolean,
    isPassed: Boolean,
    palette: SakinaPalette
) {
    val nodeSize = if (isNext) 34.dp else 26.dp
    val contentAlpha = if (isPassed && !isNext) 0.60f else 1f

    Box(
        modifier = Modifier
            .size(nodeSize)
            .shadow(
                elevation = if (isNext) 8.dp else 2.dp,
                shape = CircleShape,
                spotColor = if (isNext) palette.accent else Color.Black.copy(alpha = 0.25f)
            )
            .clip(CircleShape)
            .background(
                if (isNext) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.40f),
                            palette.accent,
                            palette.primary.copy(alpha = 0.65f)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            palette.surfaceElevated.copy(alpha = 0.98f),
                            palette.surfaceCard.copy(alpha = 0.85f),
                            palette.surfaceCardActive.copy(alpha = 0.70f)
                        )
                    )
                }
            )
            .border(
                width = if (isNext) 2.0.dp else 1.2.dp,
                brush = if (isNext) {
                    Brush.linearGradient(listOf(Color.White, palette.accent, palette.primary))
                } else {
                    Brush.linearGradient(listOf(Color(0xAAFFFFFF), palette.borderSubtle.copy(alpha = 0.50f), Color(0x20000000)))
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = prayerIcon(type),
            contentDescription = null,
            tint = if (isNext) Color.White else palette.textSecondary.copy(alpha = contentAlpha),
            modifier = Modifier.size(if (isNext) 18.dp else 14.dp)
        )
    }
}

/**
 * رسم خطوط الأسطرلاب، التدريجات، الأشعة الفلكية، القوس المضيء، والعقرب الممتد بدقة
 */
@Composable
private fun AstrolabeCanvas3D(
    handAngle: Float,
    progress: Float,
    nextPrayerType: PrayerType,
    stationsRadius: Dp,
    palette: SakinaPalette,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val outerRadius = width / 2f
        val trackRadius = outerRadius - 14.dp.toPx()
        val stationsRadiusPx = stationsRadius.toPx()

        // 1. حلقة التدريجات الفلكية الخارجية المشطوفة
        drawCircle(
            color = palette.borderSubtle.copy(alpha = 0.40f),
            radius = trackRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = Color(0x30FFFFFF),
            radius = trackRadius - 4.dp.toPx(),
            center = center,
            style = Stroke(width = 0.8.dp.toPx())
        )

        // انعكاس بلورة الزجاج المقببة ثلاثية الأبعاد (3D Domed Sapphire Crystal Highlight)
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                )
            ),
            startAngle = 195f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(center.x - trackRadius + 2.dp.toPx(), center.y - trackRadius + 2.dp.toPx()),
            size = Size((trackRadius - 2.dp.toPx()) * 2f, (trackRadius - 2.dp.toPx()) * 2f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // 2. نقوش أشعة الساعات الفلكية (Astrolabe Radial Ray Engravings):
        // تملأ الفراغ بين المحطات والمركز بأناقة كتحفة أندلسية
        for (i in 0 until 12) {
            val angle = i * 30f
            val isStation = i % 2 == 0 // عند كل محطة من المحطات الست
            val rayColor = if (isStation) palette.accent.copy(alpha = 0.35f) else palette.borderSubtle.copy(alpha = 0.20f)
            val strokeW = if (isStation) 1.2f else 0.7f

            rotate(degrees = angle, pivot = center) {
                drawLine(
                    color = rayColor,
                    start = Offset(center.x, center.y - (width * 0.25f)),
                    end = Offset(center.x, center.y - stationsRadiusPx + 18.dp.toPx()),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }

        // 3. تدريجات الدقائق والساعات الدقيقة على المسار (72 تدريجة = كل 5 درجات)
        for (i in 0 until 72) {
            val angle = i * 5f
            val isMajor = i % 12 == 0 // عند كل محطة رئيسية (60 درجة)
            val isMid = i % 6 == 0 // كل 30 درجة
            val tickLength = if (isMajor) 10.dp.toPx() else if (isMid) 6.dp.toPx() else 3.5.dp.toPx()
            val tickColor = if (isMajor) palette.accent else if (isMid) palette.primary.copy(alpha = 0.7f) else palette.borderSubtle.copy(alpha = 0.35f)
            val strokeW = if (isMajor) 2.2f else if (isMid) 1.4f else 0.8f

            rotate(degrees = angle, pivot = center) {
                drawLine(
                    color = tickColor,
                    start = Offset(center.x, center.y - trackRadius),
                    end = Offset(center.x, center.y - trackRadius + tickLength),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }

        // 4. القوس المضيء للفترة الحالية (Active Progress Arc)
        val prayerOrder = listOf(
            PrayerType.FAJR,
            PrayerType.SUNRISE,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )
        val nextIdx = prayerOrder.indexOf(nextPrayerType).takeIf { it >= 0 } ?: 0
        val prevIdx = if (nextIdx == 0) 5 else nextIdx - 1
        val startArcAngle = prevIdx * 60f - 90f // -90° لأن القمة في الأعلى
        val sweepAngle = progress * 60f

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    palette.primary.copy(alpha = 0.25f),
                    palette.accent
                ),
                center = center
            ),
            startAngle = startArcAngle,
            sweepAngle = sweepAngle.coerceAtLeast(1.5f),
            useCenter = false,
            topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
            size = Size(trackRadius * 2f, trackRadius * 2f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // 5. العقرب الفلكي الرمزي الممتد (The Extended Single Astrolabe Hand):
        // يمتد بطول مدروس يصل مباشرة إلى دائرة المحطات والمسار ليكون واضحاً ودقيقاً كالساعة الفاخرة
        rotate(degrees = handAngle, pivot = center) {
            val handLength = stationsRadiusPx + 2.dp.toPx()
            val handBaseWidth = 6.dp.toPx()
            val tailLength = 18.dp.toPx()

            // ظل العقرب ثلاثي الأبعاد المرتفع عن صفيحة الأسطرلاب
            val shadowOffset = 3.dp.toPx()
            val shadowPath = Path().apply {
                moveTo(center.x + shadowOffset, center.y - handLength + shadowOffset)
                lineTo(center.x + handBaseWidth + shadowOffset, center.y + shadowOffset)
                lineTo(center.x + shadowOffset, center.y + tailLength + shadowOffset)
                lineTo(center.x - handBaseWidth + shadowOffset, center.y + shadowOffset)
                close()
            }
            drawPath(
                path = shadowPath,
                color = Color.Black.copy(alpha = 0.35f)
            )

            // النصف الأيمن من العقرب (الوجه العاكس الفاتح)
            val rightFacet = Path().apply {
                moveTo(center.x, center.y - handLength)
                lineTo(center.x + handBaseWidth, center.y)
                lineTo(center.x, center.y)
                close()
            }
            drawPath(
                path = rightFacet,
                color = Color.White.copy(alpha = 0.95f)
            )

            // النصف الأيسر من العقرب (الوجه المعدني الذهبي المظلل)
            val leftFacet = Path().apply {
                moveTo(center.x, center.y - handLength)
                lineTo(center.x - handBaseWidth, center.y)
                lineTo(center.x, center.y)
                close()
            }
            drawPath(
                path = leftFacet,
                color = palette.accent
            )

            // ذيل الميزان التوازني للعقرب
            val tailPath = Path().apply {
                moveTo(center.x - handBaseWidth * 0.6f, center.y)
                lineTo(center.x + handBaseWidth * 0.6f, center.y)
                lineTo(center.x, center.y + tailLength)
                close()
            }
            drawPath(
                path = tailPath,
                color = palette.primary
            )

            // رأس السهم الفلكي المضيء الذي يشير للمحطة (Luminous Diamond Pointer)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, palette.accent),
                    center = Offset(center.x, center.y - handLength),
                    radius = 5.dp.toPx()
                ),
                radius = 4.5.dp.toPx(),
                center = Offset(center.x, center.y - handLength)
            )
            drawCircle(
                color = palette.accent.copy(alpha = 0.50f),
                radius = 8.dp.toPx(),
                center = Offset(center.x, center.y - handLength)
            )
        }

        // 6. محور ارتكاز العقرب الذهبي المجسم
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, palette.accent, palette.primary),
                center = center,
                radius = 12.dp.toPx()
            ),
            radius = 10.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color(0x99FFFFFF),
            radius = 10.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

/**
 * صف كبسولات الصلوات (3 كبسولات متساوية بتصميم iOS زجاجي احترافي):
 * يعرض 3 صلوات بحجم واضح وكبير مع الأيقونة والاسم والتوقيت بتناسق تام
 */
@Composable
private fun PrayerCardsRowIOS(
    rows: List<PrayerRowUi>,
    palette: SakinaPalette,
    textFont: FontFamily,
    is24Hour: Boolean,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        rows.forEach { row ->
            PrayerCardItemIOS(
                row = row,
                palette = palette,
                textFont = textFont,
                is24Hour = is24Hour,
                isArabic = isArabic,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * كبسولة الصلاة الواحدة بتصميم iOS زجاجي احترافي (Apple Frosted Glass):
 * حواف ناعمة، حدود مشطوفة متدرجة، إضاءة للصلاة القادمة، وأرقام واضحة وكبيرة وبارزة
 */
@Composable
private fun PrayerCardItemIOS(
    row: PrayerRowUi,
    palette: SakinaPalette,
    textFont: FontFamily,
    is24Hour: Boolean,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val isNext = row.isNext
    val shape = RoundedCornerShape(18.dp)
    val prayerName = stringResource(row.type.labelRes)
    val nameFontSize = if (prayerName.length > 9) 12.5.sp else 14.sp

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isNext) 10.dp else 3.dp,
                shape = shape,
                spotColor = if (isNext) palette.accent.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                if (isNext) {
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.accent.copy(alpha = 0.35f),
                            palette.surfaceElevated.copy(alpha = if (palette.isDark) 0.90f else 0.98f),
                            palette.surfaceCard.copy(alpha = if (palette.isDark) 0.80f else 0.92f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.surfaceCardActive.copy(alpha = if (palette.isDark) 0.75f else 0.92f),
                            palette.surfaceCard.copy(alpha = if (palette.isDark) 0.60f else 0.82f)
                        )
                    )
                }
            )
            .border(
                width = if (isNext) 1.8.dp else 1.2.dp,
                brush = if (isNext) {
                    Brush.linearGradient(
                        listOf(
                            Color.White,
                            palette.accent,
                            palette.primary.copy(alpha = 0.70f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            palette.borderSubtle.copy(alpha = 0.35f),
                            Color(0x10000000)
                        )
                    )
                },
                shape = shape
            )
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // شارة الأيقونة ومؤشر الحالة
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isNext) palette.accent.copy(alpha = 0.28f)
                        else palette.surfaceElevated.copy(alpha = 0.75f)
                    )
                    .border(
                        1.0.dp,
                        if (isNext) palette.accent.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.20f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = prayerIcon(row.type),
                    contentDescription = null,
                    tint = if (isNext) palette.accent else palette.textPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
            }

            // اسم الصلاة - بارز وكبير
            Text(
                text = prayerName,
                fontFamily = textFont,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = nameFontSize,
                color = if (isNext) palette.accent else palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // التوقيت الرقمي - كبير وعريض وفائق الوضوح داخل الإطار
            Text(
                text = formatPrayerTimeDisplay(row.timeText, is24Hour = is24Hour, isArabic = isArabic),
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = if (isNext) palette.primary else palette.textPrimary,
                maxLines = 1
            )
        }
    }
}

/**
 * تذييل الحالة والمزامنة الزجاجي
 */
@Composable
private fun AstrolabeFooterPill(
    uiState: MainUiState,
    palette: SakinaPalette,
    textFont: FontFamily
) {
    Box(
        modifier = Modifier
            .shadow(2.dp, CircleShape, spotColor = Color(0x20000000))
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        palette.surfaceCard.copy(alpha = if (palette.isDark) 0.55f else 0.78f),
                        palette.surfaceElevated.copy(alpha = if (palette.isDark) 0.38f else 0.62f)
                    )
                )
            )
            .border(
                0.8.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x40FFFFFF),
                        palette.borderSubtle.copy(alpha = 0.3f),
                        Color(0x10000000)
                    )
                ),
                CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (uiState.settings.alertsEnabled) {
                    Icons.Rounded.NotificationsActive
                } else {
                    Icons.Rounded.NotificationsOff
                },
                contentDescription = null,
                tint = palette.textSecondary.copy(alpha = 0.75f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            val statusLabel = when {
                uiState.isSyncing -> stringResource(R.string.syncing_times)
                uiState.todayPrayers?.isEstimate == true -> stringResource(R.string.offline_estimate)
                uiState.settings.hasSyncedOnline -> stringResource(R.string.synced_online)
                else -> stringResource(R.string.offline_cached)
            }
            Text(
                text = "$statusLabel • ${uiState.settings.timezoneId}",
                fontFamily = textFont,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp,
                color = palette.textSecondary.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatPrayerTimeDisplay(
    hhMm: String,
    is24Hour: Boolean,
    isArabic: Boolean
): String {
    val parts = hhMm.split(":")
    if (parts.size != 2) return hhMm
    val hour24 = parts[0].toIntOrNull() ?: return hhMm
    val minute = parts[1].toIntOrNull() ?: return hhMm
    if (is24Hour) {
        return String.format(Locale.US, "%02d:%02d", hour24, minute)
    }
    val isPm = hour24 >= 12
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val period = if (isArabic) {
        if (isPm) "م" else "ص"
    } else {
        if (isPm) "PM" else "AM"
    }
    return String.format(Locale.US, "%d:%02d %s", hour12, minute, period)
}

private fun prayerIcon(type: PrayerType): ImageVector = when (type) {
    PrayerType.FAJR -> Icons.Outlined.WbTwilight
    PrayerType.SUNRISE -> Icons.Outlined.Brightness5
    PrayerType.DHUHR -> Icons.Outlined.WbSunny
    PrayerType.ASR -> Icons.Outlined.WbSunny
    PrayerType.MAGHRIB -> Icons.Outlined.WbTwilight
    PrayerType.ISHA -> Icons.Outlined.NightsStay
}

/**
 * خلفية الهندسة الإسلامية العميقة التي تملأ الفراغات بالروحانية:
 * شبكة هندسية فلكية مستوحاة من شبكات الأسطرلاب الأندلسي ونقوش ربع الحزب الإسلامية،
 * موضوعة بحسابات دقيقة بعيداً عن أزرار التحكم لمنع أي تداخل بصري.
 */
@Composable
private fun IslamicSacredGeometryBackdrop(
    ornamentColor: Color,
    accentColor: Color,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. هالة الإضاءة الفلكية المركزية خلف القرص
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.20f),
                    accentColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.40f),
                radius = size.minDimension * 0.85f
            ),
            radius = size.minDimension * 0.85f,
            center = Offset(width * 0.5f, height * 0.40f)
        )

        // 2. هالة الأفق السفلية
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(width * 0.5f, height * 0.96f),
                radius = size.minDimension * 0.65f
            ),
            radius = size.minDimension * 0.65f,
            center = Offset(width * 0.5f, height * 0.96f)
        )

        // 3. نقش الأسطرلاب الفلكي الأندلسي المحفور في الزاوية العلوية (تم إبعاده عن زر الإعدادات)
        val astrolabeCenter = Offset(width * 0.86f, height * 0.06f)
        val outerR = size.minDimension * 0.22f

        // دوائر فلكية متحدة المركز
        drawCircle(
            color = ornamentColor.copy(alpha = 0.35f),
            radius = outerR,
            center = astrolabeCenter,
            style = Stroke(width = 1.0f)
        )
        drawCircle(
            color = ornamentColor.copy(alpha = 0.20f),
            radius = outerR * 0.70f,
            center = astrolabeCenter,
            style = Stroke(width = 0.8f)
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.15f),
            radius = outerR * 0.40f,
            center = astrolabeCenter,
            style = Stroke(width = 1.0f)
        )

        // 4. نقش نجمة ربع الحزب الإسلامية في الزاوية السفلية اليسرى لملء الفراغ بتناغم
        val starCenter = Offset(width * 0.14f, height * 0.82f)
        val side = size.minDimension * 0.28f
        val topLeft = Offset(starCenter.x - side / 2f, starCenter.y - side / 2f)
        val stroke = Stroke(width = 1.2f, cap = StrokeCap.Round)

        drawRect(
            color = ornamentColor.copy(alpha = 0.30f),
            topLeft = topLeft,
            size = Size(side, side),
            style = stroke
        )
        rotate(degrees = 45f, pivot = starCenter) {
            drawRect(
                color = ornamentColor.copy(alpha = 0.30f),
                topLeft = topLeft,
                size = Size(side, side),
                style = stroke
            )
        }
        drawCircle(
            color = ornamentColor.copy(alpha = 0.25f),
            radius = side * 0.60f,
            center = starCenter,
            style = Stroke(width = 0.8f)
        )
    }
}
