package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.PrayerAlarmScheduler
import com.example.data.location.LocationZoneCatalog
import com.example.data.model.PrayerType
import com.example.data.repository.OfflinePrayerCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sakina", appName)
    }

    @Test
    fun `default city matches timezone offset across regions so Fajr is always morning`() {
        val timezones = listOf(
            "America/Los_Angeles",
            "America/New_York",
            "Europe/London",
            "Europe/Paris",
            "Africa/Algiers",
            "Africa/Casablanca",
            "Africa/Cairo",
            "Asia/Riyadh",
            "Asia/Dubai",
            "UTC"
        )
        for (tzId in timezones) {
            val city = LocationZoneCatalog.defaultCityForTimezone(tzId)
            val tz = TimeZone.getTimeZone(city.timezoneId)
            val cal = Calendar.getInstance(tz).apply {
                set(2026, Calendar.NOVEMBER, 14, 12, 0, 0)
            }
            val day = OfflinePrayerCalculator.computeDay(
                calendar = cal,
                latitude = city.latitude,
                longitude = city.longitude,
                timezoneId = city.timezoneId,
                locationLabel = city.nameEn,
                methodId = city.defaultMethodId
            )
            assertTrue("Fajr for $tzId (${city.nameEn}) was ${day.fajr}", day.fajr in "03:30".."07:00")
            assertTrue("Sunrise for $tzId was ${day.sunrise}", day.sunrise > day.fajr)
            assertTrue("Dhuhr for $tzId was ${day.dhuhr}", day.dhuhr in "11:00".."14:00")
            assertTrue("Maghrib for $tzId was ${day.maghrib}", day.maghrib in "15:30".."20:30")
            assertTrue("Isha for $tzId was ${day.isha}", day.isha > day.maghrib)
        }
    }

    @Test
    fun `alarm scheduler skips sunrise and picks next obligatory prayer`() {
        val tz = TimeZone.getTimeZone("Asia/Riyadh")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 14, 5, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day = OfflinePrayerCalculator.computeDay(
            calendar = cal,
            latitude = 21.4225,
            longitude = 39.8262,
            timezoneId = "Asia/Riyadh",
            locationLabel = "Makkah",
            methodId = 4
        )
        val next = PrayerAlarmScheduler.findNextUpcomingPrayer(listOf(day), "Asia/Riyadh", cal.timeInMillis)
        assertNotNull(next)
        assertEquals(PrayerType.DHUHR, next!!.prayerType)
    }

    @Test
    fun `launch MainActivity with Robolectric to verify UI render`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
        controller.setup()
        assertNotNull(controller.get())
    }
}
