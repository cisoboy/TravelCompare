package com.example.data

import com.example.data.db.UserAccountDao
import com.example.data.db.UserAccountEntity
import com.example.data.db.UserPromotionDao
import com.example.data.db.UserPromotionEntity
import com.example.model.RideCategory
import com.example.model.RideProvider
import com.example.model.UserAccount
import com.example.model.UserPromotion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AccountPromotionRepository(
    private val accountDao: UserAccountDao,
    private val promotionDao: UserPromotionDao
) {

    val allAccounts: Flow<List<UserAccount>> = accountDao.getAllAccounts().map { entities ->
        entities.map { it.toDomain() }
    }

    val allPromotions: Flow<List<UserPromotion>> = promotionDao.getAllPromotions().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getAccount(provider: RideProvider): UserAccount? {
        return accountDao.getAccountByProvider(provider.name)?.toDomain()
    }

    suspend fun loginToUber(
        name: String,
        email: String,
        isUberOneMember: Boolean = true
    ) {
        val account = UserAccount(
            provider = RideProvider.UBER,
            isLoggedIn = true,
            userName = name.ifBlank { "Alex Rivera" },
            userEmail = email.ifBlank { "alex.rivera@example.com" },
            membershipTier = if (isUberOneMember) "Uber One" else "Standard",
            memberDiscountPercent = if (isUberOneMember) 5.0 else 0.0
        )
        accountDao.insertOrUpdate(UserAccountEntity.fromDomain(account))

        // Pre-populate default personal Uber promotions for the user
        val defaultUberPromos = listOf(
            UserPromotion(
                id = "uber_one_membership",
                provider = RideProvider.UBER,
                code = "UBERONE",
                title = "Uber One Member Discount",
                description = "5% off all Uber rides with your Uber One membership",
                discountPercent = 5.0,
                maxDiscountCap = 5.0,
                isActive = isUberOneMember,
                ridesRemaining = 99
            ),
            UserPromotion(
                id = "uber_promo_weekend",
                provider = RideProvider.UBER,
                code = "WEEKEND20",
                title = "Personal Weekend Perk",
                description = "20% off your next 3 weekend rides (max £5 off)",
                discountPercent = 20.0,
                maxDiscountCap = 5.0,
                isActive = true,
                ridesRemaining = 3
            ),
            UserPromotion(
                id = "uber_promo_airport",
                provider = RideProvider.UBER,
                code = "AIRPORT10",
                title = "Airport Transfer Voucher",
                description = "10% off rides to or from major airports",
                discountPercent = 10.0,
                maxDiscountCap = 8.0,
                isActive = false,
                ridesRemaining = 2
            )
        )
        promotionDao.insertAll(defaultUberPromos.map { UserPromotionEntity.fromDomain(it) })
    }

    suspend fun loginToBolt(
        name: String,
        email: String,
        isBoltPlusMember: Boolean = true
    ) {
        val account = UserAccount(
            provider = RideProvider.BOLT,
            isLoggedIn = true,
            userName = name.ifBlank { "Alex Rivera" },
            userEmail = email.ifBlank { "+44 7700 900123" },
            membershipTier = if (isBoltPlusMember) "Bolt Plus" else "Regular",
            memberDiscountPercent = if (isBoltPlusMember) 10.0 else 0.0
        )
        accountDao.insertOrUpdate(UserAccountEntity.fromDomain(account))

        // Pre-populate default personal Bolt promotions for the user
        val defaultBoltPromos = listOf(
            UserPromotion(
                id = "bolt_reward_tier",
                provider = RideProvider.BOLT,
                code = "BOLTPLUS10",
                title = "Bolt Plus 10% Discount",
                description = "10% off all Bolt rides with your Bolt Plus membership",
                discountPercent = 10.0,
                maxDiscountCap = 6.0,
                isActive = isBoltPlusMember,
                ridesRemaining = 99
            ),
            UserPromotion(
                id = "bolt_promo_save30",
                provider = RideProvider.BOLT,
                code = "BOLTSAVE30",
                title = "Special Ride Voucher",
                description = "30% off your next 2 rides (up to £6 off)",
                discountPercent = 30.0,
                maxDiscountCap = 6.0,
                isActive = true,
                ridesRemaining = 2
            ),
            UserPromotion(
                id = "bolt_promo_flat4",
                provider = RideProvider.BOLT,
                code = "RIDE4OFF",
                title = "Loyalty Credit Voucher",
                description = "£4.00 flat discount on your next trip",
                flatDiscount = 4.0,
                isActive = false,
                ridesRemaining = 1
            )
        )
        promotionDao.insertAll(defaultBoltPromos.map { UserPromotionEntity.fromDomain(it) })
    }

    suspend fun logoutProvider(provider: RideProvider) {
        accountDao.deleteByProvider(provider.name)
        promotionDao.deleteByProvider(provider.name)
    }

    suspend fun setPromotionActive(promoId: String, isActive: Boolean) {
        promotionDao.setPromotionActive(promoId, isActive)
    }

    suspend fun addCustomPromotion(
        provider: RideProvider,
        code: String,
        title: String,
        discountPercent: Double,
        flatDiscount: Double,
        maxCap: Double,
        applicableCategory: RideCategory?
    ) {
        val promo = UserPromotion(
            id = "${provider.name.lowercase()}_custom_${UUID.randomUUID().toString().take(8)}",
            provider = provider,
            code = code.uppercase().trim(),
            title = title.ifBlank { "${code.uppercase().trim()} Promo" },
            description = when {
                discountPercent > 0 && flatDiscount > 0 -> "${discountPercent.toInt()}% + £${String.format("%.2f", flatDiscount)} off"
                discountPercent > 0 -> "${discountPercent.toInt()}% off" + (if (maxCap > 0) " (max £${maxCap.toInt()})" else "")
                else -> "£${String.format("%.2f", flatDiscount)} off"
            },
            discountPercent = discountPercent,
            flatDiscount = flatDiscount,
            maxDiscountCap = maxCap,
            isActive = true,
            applicableCategory = applicableCategory,
            ridesRemaining = 5
        )
        promotionDao.insertOrUpdate(UserPromotionEntity.fromDomain(promo))
    }

    suspend fun deletePromotion(promoId: String) {
        promotionDao.deleteById(promoId)
    }
}
