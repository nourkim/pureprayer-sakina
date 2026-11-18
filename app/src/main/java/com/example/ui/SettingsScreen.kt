package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.location.LocationZoneCatalog
import com.example.data.model.CalculationMethod
import com.example.data.model.CityZone
import com.example.data.model.LocationMode
import com.example.data.model.SakinaLanguage
import com.example.data.model.SakinaThemeId
import com.example.data.model.SakinaToneId
import com.example.util.AutostartHelper
import com.example.ui.theme.LocalSakinaPalette
import com.example.ui.theme.SakinaPalette
import com.example.ui.theme.SakinaPalettes
import com.example.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onBack: () -> Unit,
    onAlertsToggle: (Boolean) -> Unit,
    onSelectGpsMode: () -> Unit,
    onTriggerGpsCheck: () -> Unit,
    onSelectManualCity: (CityZone) -> Unit,
    onSelectMethod: (Int) -> Unit,
    onSelectLanguage: (SakinaLanguage) -> Unit,
    onSelectTheme: (SakinaThemeId) -> Unit,
    onSelectTone: (SakinaToneId) -> Unit,
    onChimeRepeatChange: (Int) -> Unit,
    onRefreshBgStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalSakinaPalette.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isArabic = LocaleHelper.isArabic(uiState.settings.language)

    var showCityPicker by remember { mutableStateOf(false) }
    var showMethodPicker by remember { mutableStateOf(false) }

    // Refresh battery/alarm/notification status whenever returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshBgStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onTriggerGpsCheck()
            onSelectGpsMode()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshBgStatus()
    }

    Box(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("back_button")
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceCard)
                            .border(1.dp, palette.borderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button),
                            tint = palette.textPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                }

                // 1. Single-Beep Prayer Alert Toggle
                SettingsSectionCard(
                    title = stringResource(R.string.section_alerts),
                    icon = Icons.Rounded.NotificationsActive,
                    palette = palette
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(R.string.alerts_enabled_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.alerts_enabled_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }
                        Switch(
                            checked = uiState.settings.alertsEnabled,
                            onCheckedChange = onAlertsToggle,
                            modifier = Modifier.testTag("alerts_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.onPrimary,
                                checkedTrackColor = palette.primary
                            )
                        )
                    }
                }

                // 2. Region & Timezone (GPS Auto or Manual)
                SettingsSectionCard(
                    title = stringResource(R.string.section_region),
                    icon = Icons.Rounded.Public,
                    palette = palette
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModeChip(
                            label = stringResource(R.string.mode_gps),
                            icon = Icons.Rounded.MyLocation,
                            selected = uiState.settings.locationMode == LocationMode.GPS,
                            palette = palette,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_gps"),
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                onSelectGpsMode()
                            }
                        )
                        ModeChip(
                            label = stringResource(R.string.mode_manual),
                            icon = Icons.Rounded.LocationOn,
                            selected = uiState.settings.locationMode == LocationMode.MANUAL,
                            palette = palette,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_manual"),
                            onClick = { showCityPicker = true }
                        )
                    }

                    HorizontalDivider(color = palette.borderSubtle)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.current_region_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary
                            )
                            Text(
                                text = "${uiState.settings.cityName} • ${uiState.settings.timezoneId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.settings.locationMode == LocationMode.GPS) {
                            ActionPillButton(
                                label = stringResource(R.string.sync_gps_now),
                                icon = Icons.Rounded.MyLocation,
                                palette = palette,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sync_gps_button"),
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                    onTriggerGpsCheck()
                                    onSelectGpsMode()
                                }
                            )
                        } else {
                            ActionPillButton(
                                label = stringResource(R.string.choose_city),
                                icon = Icons.Rounded.Search,
                                palette = palette,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("choose_city_button"),
                                onClick = { showCityPicker = true }
                            )
                        }

                        ActionPillButton(
                            label = stringResource(R.string.calc_method_label),
                            icon = Icons.Rounded.Tune,
                            palette = palette,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_method_button"),
                            onClick = { showMethodPicker = true }
                        )
                    }

                    val activeMethod = CalculationMethod.fromApiId(uiState.settings.methodId)
                    Text(
                        text = if (isArabic) activeMethod.labelAr else activeMethod.labelEn,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.accent
                    )
                }

                // 3. Background Presence & Battery Optimization
                SettingsSectionCard(
                    title = stringResource(R.string.section_background),
                    icon = Icons.Rounded.BatteryChargingFull,
                    palette = palette
                ) {
                    val bg = uiState.backgroundStatus

                    BackgroundStatusRow(
                        title = stringResource(R.string.bg_battery_title),
                        subtitle = stringResource(
                            if (bg.isBatteryUnrestricted) R.string.bg_battery_granted else R.string.bg_battery_denied
                        ),
                        isOk = bg.isBatteryUnrestricted,
                        icon = Icons.Rounded.BatteryChargingFull,
                        palette = palette,
                        modifier = Modifier.testTag("bg_battery_row"),
                        onClick = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            runCatching { context.startActivity(intent) }
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(color = palette.borderSubtle)
                        BackgroundStatusRow(
                            title = stringResource(R.string.bg_exact_alarm_title),
                            subtitle = stringResource(
                                if (bg.canExactAlarm) R.string.bg_exact_granted else R.string.bg_exact_denied
                            ),
                            isOk = bg.canExactAlarm,
                            icon = Icons.Rounded.Schedule,
                            palette = palette,
                            modifier = Modifier.testTag("bg_exact_alarm_row"),
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                                runCatching { context.startActivity(intent) }
                            }
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        HorizontalDivider(color = palette.borderSubtle)
                        BackgroundStatusRow(
                            title = stringResource(R.string.bg_notif_title),
                            subtitle = stringResource(
                                if (bg.hasNotifications) R.string.bg_notif_granted else R.string.bg_notif_denied
                            ),
                            isOk = bg.hasNotifications,
                            icon = Icons.Rounded.NotificationsActive,
                            palette = palette,
                            modifier = Modifier.testTag("bg_notif_row"),
                            onClick = {
                                if (!bg.hasNotifications) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    runCatching { context.startActivity(intent) }
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = palette.borderSubtle)
                    BackgroundStatusRow(
                        title = stringResource(R.string.bg_autostart_title),
                        subtitle = stringResource(R.string.bg_autostart_desc),
                        isOk = true,
                        icon = Icons.Rounded.PowerSettingsNew,
                        palette = palette,
                        modifier = Modifier.testTag("bg_autostart_row"),
                        onClick = {
                            AutostartHelper.openAutostartSettings(context)
                        }
                    )

                    Text(
                        text = stringResource(R.string.bg_footnote),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary
                    )
                }

                // 4. Language Selection
                SettingsSectionCard(
                    title = stringResource(R.string.section_language),
                    icon = Icons.Rounded.Language,
                    palette = palette
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SakinaLanguage.entries.forEach { lang ->
                            val selected = uiState.settings.language == lang
                            Box(
                                modifier = Modifier
                                    .testTag("lang_${lang.code}")
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) palette.primary else palette.surfaceElevated
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) palette.primary else palette.borderSubtle,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectLanguage(lang) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(lang.labelRes),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) palette.onPrimary else palette.textPrimary
                                )
                            }
                        }
                    }
                }

                // 5. Five Refined Islamic Themes (Light & Dark)
                SettingsSectionCard(
                    title = stringResource(R.string.section_themes),
                    icon = Icons.Rounded.Palette,
                    palette = palette
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SakinaThemeId.entries.forEach { theme ->
                            val swatch = SakinaPalettes.forId(theme)
                            val selected = uiState.settings.themeId == theme
                            Column(
                                modifier = Modifier
                                    .testTag("theme_${theme.id}")
                                    .width(124.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(swatch.gradientTop, swatch.gradientMid, swatch.gradientBottom)
                                        )
                                    )
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) palette.accent else swatch.borderSubtle,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { onSelectTheme(theme) }
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(swatch.primary)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(swatch.accent)
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = swatch.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(theme.labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = swatch.textPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 6. Three Rare Single-Ring Tones
                SettingsSectionCard(
                    title = stringResource(R.string.section_tones),
                    icon = Icons.Rounded.GraphicEq,
                    palette = palette
                ) {
                    SakinaToneId.entries.forEachIndexed { index, tone ->
                        if (index > 0) {
                            HorizontalDivider(color = palette.borderSubtle)
                        }
                        val selected = uiState.settings.toneId == tone
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tone_${tone.code}")
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectTone(tone) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { onSelectTone(tone) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = palette.accent,
                                        unselectedColor = palette.textSecondary
                                    )
                                )
                                Column {
                                    Text(
                                        text = stringResource(tone.titleRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = stringResource(tone.descRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onSelectTone(tone) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = stringResource(R.string.cd_preview_tone),
                                    tint = palette.accent
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = palette.borderSubtle)

                    // Chime Repeat Count (1 to 3 times)
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.chime_repeat_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.chime_repeat_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                1 to R.string.chime_repeat_1x,
                                2 to R.string.chime_repeat_2x,
                                3 to R.string.chime_repeat_3x
                            ).forEach { (count, labelRes) ->
                                val isSelected = uiState.settings.chimeRepeatCount == count
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) palette.primary else palette.surfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) palette.primary else palette.borderSubtle,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onChimeRepeatChange(count) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(labelRes),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) palette.onPrimary else palette.textPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. About Section (ALWAYS IN ENGLISH as explicitly requested)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SettingsSectionCard(
                        title = "About",
                        icon = Icons.Rounded.Info,
                        palette = palette
                    ) {
                        Text(
                            text = "Sakina Prayer Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "App idea by Nouri Hakim and developed with AI.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.accent,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Designed to consume minimal battery and memory—delivering one calm chime per prayer from cached local times.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Manual City Picker Bottom Sheet
    if (showCityPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var searchQuery by remember { mutableStateOf("") }
        val filteredCities = remember(searchQuery, isArabic) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) {
                LocationZoneCatalog.cities
            } else {
                LocationZoneCatalog.cities.filter {
                    it.nameEn.lowercase().contains(q) ||
                        it.nameAr.contains(q) ||
                        it.countryEn.lowercase().contains(q) ||
                        it.countryAr.contains(q) ||
                        it.timezoneId.lowercase().contains(q)
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showCityPicker = false },
            sheetState = sheetState,
            containerColor = palette.surfaceElevated,
            contentColor = palette.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.choose_city),
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_city_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = palette.textSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = palette.borderSubtle,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("city_search_input")
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCities, key = { it.id }) { city ->
                        val isSelected = uiState.settings.locationMode == LocationMode.MANUAL &&
                            uiState.settings.manualCityId == city.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) palette.primary.copy(alpha = 0.14f) else Color.Transparent)
                                .clickable {
                                    onSelectManualCity(city)
                                    showCityPicker = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = city.displayName(isArabic),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = city.timezoneId,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = palette.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Calculation Method Picker Bottom Sheet
    if (showMethodPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMethodPicker = false },
            sheetState = sheetState,
            containerColor = palette.surfaceElevated,
            contentColor = palette.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.calc_method_label),
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(CalculationMethod.entries, key = { it.apiId }) { method ->
                        val selected = uiState.settings.methodId == method.apiId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) palette.primary.copy(alpha = 0.14f) else Color.Transparent)
                                .clickable {
                                    onSelectMethod(method.apiId)
                                    showMethodPicker = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) method.labelAr else method.labelEn,
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = palette.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    palette: SakinaPalette,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.surfaceCard)
            .border(1.dp, palette.borderSubtle, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = palette.accent,
                fontWeight = FontWeight.SemiBold
            )
        }
        content()
    }
}

@Composable
private fun ModeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    palette: SakinaPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(if (selected) palette.primary else palette.surfaceElevated)
            .border(1.dp, if (selected) palette.primary else palette.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) palette.onPrimary else palette.textPrimary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) palette.onPrimary else palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionPillButton(
    label: String,
    icon: ImageVector,
    palette: SakinaPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(palette.surfaceElevated)
            .border(1.dp, palette.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BackgroundStatusRow(
    title: String,
    subtitle: String,
    isOk: Boolean,
    icon: ImageVector,
    palette: SakinaPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isOk) palette.successTint else palette.warningTint,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOk) palette.successTint else palette.warningTint
                )
            }
        }
        Icon(
            imageVector = if (isOk) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = if (isOk) palette.successTint else palette.warningTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
