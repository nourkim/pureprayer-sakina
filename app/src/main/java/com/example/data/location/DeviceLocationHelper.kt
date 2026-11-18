package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

data class ResolvedDeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val timezoneId: String
)

class DeviceLocationHelper(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationHardwareEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun promptEnableLocationHardware(
        activity: Activity,
        onResolvable: (ResolvableApiException) -> Unit,
        onAlreadyEnabled: () -> Unit
    ) {
        if (isLocationHardwareEnabled()) {
            onAlreadyEnabled()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(activity)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                onAlreadyEnabled()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    onResolvable(exception)
                } else {
                    runCatching {
                        activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    suspend fun getSingleLowPowerLocation(): ResolvedDeviceLocation? {
        if (!hasLocationPermission()) return null

        // 1. Check immediate last known location from FusedProvider or LocationManager
        val fastCached = fetchLastKnownFast()

        // 2. Request a fresh GPS fix with 10s timeout
        val freshLocation: Location? = withTimeoutOrNull(10000L) {
            fetchViaFusedProvider()
        } ?: fetchViaLocationManager()

        val location = freshLocation ?: fastCached ?: return null

        val tzId = TimeZone.getDefault().id
        val cityName = resolveCityName(location.latitude, location.longitude)
            ?: LocationZoneCatalog.nearestCity(location.latitude, location.longitude).nameEn

        return ResolvedDeviceLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = cityName,
            timezoneId = tzId
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLastKnownFast(): Location? = suspendCancellableCoroutine { cont ->
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation
                .addOnSuccessListener { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchViaFusedProvider(): Location? = suspendCancellableCoroutine { cont ->
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            val priority = if (hasFineLocationPermission()) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            client.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        if (cont.isActive) cont.resume(loc)
                    } else {
                        client.lastLocation
                            .addOnSuccessListener { last ->
                                if (cont.isActive) cont.resume(last)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchViaLocationManager(): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            providers.firstNotNullOfOrNull { provider ->
                runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveCityName(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale.getDefault())
            val label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                withTimeoutOrNull(3000L) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lon, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            val l = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                            if (cont.isActive) cont.resume(l)
                        }
                    }
                }
            } else {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val addr = addresses?.firstOrNull()
                addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
            }
            label ?: LocationZoneCatalog.nearestCity(lat, lon).nameEn
        } catch (_: Exception) {
            LocationZoneCatalog.nearestCity(lat, lon).nameEn
        }
    }
}
