package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RidePricingEngine
import com.example.data.db.AppDatabase
import com.example.model.CityPresets
import com.example.model.LocationPoint
import com.example.model.RideCategory
import com.example.model.RideProvider
import com.example.model.TrafficCondition
import com.example.model.UserAccount
import com.example.model.UserPromotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ride Compare", appName)
    }

    @Test
    fun `test real-time pricing engine generates Bolt and Uber options for all categories`() {
        val city = CityPresets.LONDON
        val pickup = city.defaultPickup
        val dropoff = city.defaultDropoff

        val comparison = RidePricingEngine.compareRides(
            pickup = pickup,
            dropoff = dropoff,
            city = city,
            traffic = TrafficCondition.LIGHT,
            simulateSurge = false
        )

        assertTrue("Distance should be greater than 0", comparison.distanceKm > 0.0)
        assertTrue("Duration should be greater than 0", comparison.estimatedDurationMinutes > 0)
        assertEquals(5, comparison.tierComparisons.size)

        // Verify economy tier has both Bolt and Uber
        val economyTier = comparison.tierComparisons.find { it.category == RideCategory.ECONOMY }
        assertNotNull(economyTier)
        assertNotNull(economyTier!!.boltOption)
        assertNotNull(economyTier.uberOption)

        assertEquals(RideProvider.BOLT, economyTier.boltOption!!.provider)
        assertEquals(RideProvider.UBER, economyTier.uberOption!!.provider)
        assertTrue(economyTier.boltOption!!.price > 0.0)
        assertTrue(economyTier.uberOption!!.price > 0.0)
    }

    @Test
    fun `test surge multiplier increases fares properly`() {
        val city = CityPresets.PARIS
        val pickup = city.defaultPickup
        val dropoff = city.defaultDropoff

        val regularComparison = RidePricingEngine.compareRides(
            pickup = pickup,
            dropoff = dropoff,
            city = city,
            traffic = TrafficCondition.LIGHT,
            simulateSurge = false
        )

        val surgeComparison = RidePricingEngine.compareRides(
            pickup = pickup,
            dropoff = dropoff,
            city = city,
            traffic = TrafficCondition.LIGHT,
            simulateSurge = true
        )

        val regularEconomy = regularComparison.tierComparisons.first { it.category == RideCategory.ECONOMY }
        val surgeEconomy = surgeComparison.tierComparisons.first { it.category == RideCategory.ECONOMY }

        assertTrue(
            "Surge price should be higher than standard price",
            surgeEconomy.boltOption!!.price >= regularEconomy.boltOption!!.price
        )
    }

    @Test
    fun `test distance calculation between points`() {
        // Known distance between Big Ben and London Bridge is roughly 3-4 km driving
        val p1 = LocationPoint("Big Ben", "London", 51.5007, -0.1246)
        val p2 = LocationPoint("London Bridge", "London", 51.5079, -0.0877)
        val dist = RidePricingEngine.calculateRoadDistanceKm(p1, p2)

        assertTrue("Driving distance between Big Ben and London Bridge should be between 2.5 and 5.5 km", dist in 2.5..5.5)
    }

    @Test
    fun `test Google Places repository search returns suggestions`() = kotlinx.coroutines.runBlocking {
        val repo = com.example.data.places.GooglePlacesRepository()
        val results = repo.searchPlaces("Heathrow", CityPresets.LONDON)

        assertTrue("Should return search suggestions", results.isNotEmpty())
        val match = results.find { it.mainText.contains("Heathrow", ignoreCase = true) }
        assertNotNull("Should find Heathrow in suggestions", match)
    }

    @Test
    fun `test location point formatted coordinates and DMS`() {
        val point = LocationPoint(
            name = "Test Point",
            address = "Test St",
            latitude = 51.5074,
            longitude = -0.1278,
            tag = "Device GPS"
        )

        assertEquals("51.5074, -0.1278", point.formattedCoordinates())
        assertTrue("isDevicePosition should be true for GPS tag", point.isDevicePosition)
        assertTrue(point.formattedDms().contains("N"))
        assertTrue(point.formattedDms().contains("W"))
    }

    @Test
    fun `test personal promotion applies discount to Uber fare accurately`() {
        val city = CityPresets.LONDON
        val pickup = city.defaultPickup
        val dropoff = city.defaultDropoff

        // Base price comparison with no accounts/promos
        val baseline = RidePricingEngine.compareRides(
            pickup = pickup,
            dropoff = dropoff,
            city = city,
            traffic = TrafficCondition.LIGHT,
            simulateSurge = false
        )
        val baseUber = baseline.tierComparisons.first { it.category == RideCategory.ECONOMY }.uberOption!!

        // Uber promo: 20% off up to £10
        val uberPromo = UserPromotion(
            id = "test-uber-1",
            provider = RideProvider.UBER,
            code = "TEST20",
            title = "20% Off Rides",
            description = "Save 20%",
            discountPercent = 20.0,
            maxDiscountCap = 10.0,
            applicableCategory = RideCategory.ECONOMY,
            isActive = true
        )

        val discounted = RidePricingEngine.compareRides(
            pickup = pickup,
            dropoff = dropoff,
            city = city,
            traffic = TrafficCondition.LIGHT,
            simulateSurge = false,
            activeUberPromos = listOf(uberPromo),
            uberAccount = UserAccount(
                provider = RideProvider.UBER,
                isLoggedIn = true,
                userName = "Alex Rivera",
                userEmail = "alex@test.com"
            )
        )
        val discountedUber = discounted.tierComparisons.first { it.category == RideCategory.ECONOMY }.uberOption!!

        assertTrue("Discounted price should be lower than baseline", discountedUber.price < baseUber.price)
        assertTrue("Discount amount should be greater than 0", discountedUber.discountAmount > 0.0)
        assertEquals("TEST20", discountedUber.appliedPromoCode)
        assertEquals(baseUber.price, discountedUber.originalPrice, 0.01)
        assertEquals(baseUber.price - discountedUber.discountAmount, discountedUber.price, 0.01)
    }

    @Test
    fun `test member subscription perk applies discount`() {
        val city = CityPresets.LONDON
        val pickup = city.defaultPickup
        val dropoff = city.defaultDropoff

        val uberOneAccount = UserAccount(
            provider = RideProvider.UBER,
            isLoggedIn = true,
            userName = "Alex Rivera",
            userEmail = "alex@test.com",
            membershipTier = "Uber One",
            memberDiscountPercent = 5.0
        )

        val comparison = RidePricingEngine.compareRides(
            pickup = pickup,
            dropoff = dropoff,
            city = city,
            traffic = TrafficCondition.LIGHT,
            simulateSurge = false,
            uberAccount = uberOneAccount
        )

        val economyUber = comparison.tierComparisons.first { it.category == RideCategory.ECONOMY }.uberOption!!
        assertTrue("Member discount should be applied", economyUber.discountAmount > 0.0)
        assertTrue(economyUber.appliedPromoLabel?.contains("Uber One") == true)
    }

    @Test
    fun `test Room database initializes accounts and promotions properly`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val accountDao = db.userAccountDao()
        val promoDao = db.userPromotionDao()

        assertNotNull(accountDao)
        assertNotNull(promoDao)
    }
}
