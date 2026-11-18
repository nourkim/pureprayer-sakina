package com.example

import android.Manifest
import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.LocationMode
import com.example.ui.HomeScreen
import com.example.ui.PrayerViewModel
import com.example.ui.SettingsScreen
import com.example.ui.theme.SakinaTheme
import com.example.util.LocaleHelper

class MainActivity : ComponentActivity() {

    companion object {
        const val ROUTE_HOME = "home"
        const val ROUTE_SETTINGS = "settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PrayerViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val baseContext = LocalContext.current

            val selectedLanguage = uiState.settings.language
            val localizedContext = remember(baseContext, selectedLanguage) {
                LocaleHelper.wrapContext(baseContext, selectedLanguage)
            }
            val localizedConfig = remember(baseContext, selectedLanguage) {
                Configuration(baseContext.resources.configuration).apply {
                    val loc = LocaleHelper.resolveLocale(selectedLanguage)
                    setLocale(loc)
                    setLayoutDirection(loc)
                }
            }
            val isArabic = remember(selectedLanguage) {
                LocaleHelper.isArabic(selectedLanguage)
            }
            val layoutDirection = remember(selectedLanguage) {
                LocaleHelper.layoutDirection(selectedLanguage)
            }

            // Resolution launcher for the Google Play Services "Turn on device location" dialog
            val gpsResolutionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.refreshSchedule(forceGpsRefresh = true)
                }
                viewModel.refreshBackgroundStatus()
            }

            fun triggerGpsCheck() {
                val app = baseContext.applicationContext as SakinaApp
                val locHelper = app.container.locationHelper
                val activity = (baseContext as? Activity)
                if (activity != null) {
                    locHelper.promptEnableLocationHardware(
                        activity = activity,
                        onResolvable = { resolvable ->
                            gpsResolutionLauncher.launch(
                                IntentSenderRequest.Builder(resolvable.resolution.intentSender).build()
                            )
                        },
                        onAlreadyEnabled = {
                            viewModel.refreshSchedule(forceGpsRefresh = true)
                        }
                    )
                } else {
                    viewModel.refreshSchedule(forceGpsRefresh = true)
                }
            }

            // Gentle one-time permission request on initial launch for precise GPS + notifications
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val locGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (locGranted) {
                    triggerGpsCheck()
                } else {
                    viewModel.refreshBackgroundStatus()
                }
            }

            LaunchedEffect(uiState.settings.hasRequestedInitialPermissions) {
                if (!uiState.settings.hasRequestedInitialPermissions) {
                    viewModel.markInitialPermissionsAsked()
                    val perms = mutableListOf<String>()
                    if (uiState.settings.locationMode == LocationMode.GPS) {
                        perms += Manifest.permission.ACCESS_FINE_LOCATION
                        perms += Manifest.permission.ACCESS_COARSE_LOCATION
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms += Manifest.permission.POST_NOTIFICATIONS
                    }
                    if (perms.isNotEmpty()) {
                        permissionLauncher.launch(perms.toTypedArray())
                    }
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig,
                LocalLayoutDirection provides layoutDirection
            ) {
                SakinaTheme(
                    themeId = uiState.settings.themeId,
                    isArabic = isArabic
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(260)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(260)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(260)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(260)
                            )
                        }
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                uiState = uiState,
                                onOpenSettings = {
                                    navController.navigate(ROUTE_SETTINGS) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(ROUTE_SETTINGS) {
                            SettingsScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onAlertsToggle = viewModel::setAlertsEnabled,
                                onSelectGpsMode = viewModel::setLocationModeGps,
                                onTriggerGpsCheck = ::triggerGpsCheck,
                                onSelectManualCity = viewModel::selectManualCity,
                                onSelectMethod = viewModel::setCalculationMethod,
                                onSelectLanguage = viewModel::setLanguage,
                                onSelectTheme = viewModel::setTheme,
                                onSelectTone = viewModel::selectAndPreviewTone,
                                onChimeRepeatChange = viewModel::setChimeRepeatCount,
                                onRefreshBgStatus = viewModel::refreshBackgroundStatus
                            )
                        }
                    }
                }
            }
        }
    }
}
