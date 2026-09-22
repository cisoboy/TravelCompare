package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RideCategory
import com.example.model.RideProvider
import com.example.model.UserPromotion

@Entity(tableName = "user_promotions")
data class UserPromotionEntity(
    @PrimaryKey
    val id: String,
    val provider: String, // "UBER" or "BOLT"
    val code: String,
    val title: String,
    val description: String,
    val discountPercent: Double,
    val flatDiscount: Double,
    val maxDiscountCap: Double,
    val isActive: Boolean,
    val applicableCategory: String? = null,
    val ridesRemaining: Int = 3
) {
    fun toDomain(): UserPromotion {
        val providerEnum = if (provider.equals("UBER", ignoreCase = true)) RideProvider.UBER else RideProvider.BOLT
        val categoryEnum = applicableCategory?.let { catStr ->
            RideCategory.entries.find { it.name.equals(catStr, ignoreCase = true) }
        }
        return UserPromotion(
            id = id,
            provider = providerEnum,
            code = code,
            title = title,
            description = description,
            discountPercent = discountPercent,
            flatDiscount = flatDiscount,
            maxDiscountCap = maxDiscountCap,
            isActive = isActive,
            applicableCategory = categoryEnum,
            ridesRemaining = ridesRemaining
        )
    }

    companion object {
        fun fromDomain(promo: UserPromotion): UserPromotionEntity {
            return UserPromotionEntity(
                id = promo.id,
                provider = promo.provider.name,
                code = promo.code,
                title = promo.title,
                description = promo.description,
                discountPercent = promo.discountPercent,
                flatDiscount = promo.flatDiscount,
                maxDiscountCap = promo.maxDiscountCap,
                isActive = promo.isActive,
                applicableCategory = promo.applicableCategory?.name,
                ridesRemaining = promo.ridesRemaining
            )
        }
    }
}
