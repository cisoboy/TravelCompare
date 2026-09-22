package com.example.model

data class UserAccount(
    val provider: RideProvider,
    val isLoggedIn: Boolean,
    val userName: String,
    val userEmail: String,
    val membershipTier: String = "",
    val memberDiscountPercent: Double = 0.0,
    val linkedAt: Long = System.currentTimeMillis()
)

data class UserPromotion(
    val id: String,
    val provider: RideProvider,
    val code: String,
    val title: String,
    val description: String,
    val discountPercent: Double = 0.0,
    val flatDiscount: Double = 0.0,
    val maxDiscountCap: Double = 0.0, // 0.0 means uncapped
    val isActive: Boolean = true,
    val applicableCategory: RideCategory? = null,
    val ridesRemaining: Int = 5
) {
    fun calculateDiscount(basePrice: Double, category: RideCategory): Double {
        if (!isActive) return 0.0
        if (applicableCategory != null && applicableCategory != category) return 0.0

        var discount = 0.0
        if (discountPercent > 0.0) {
            discount += basePrice * (discountPercent / 100.0)
        }
        if (flatDiscount > 0.0) {
            discount += flatDiscount
        }

        if (maxDiscountCap > 0.0 && discount > maxDiscountCap) {
            discount = maxDiscountCap
        }

        return discount.coerceAtMost(basePrice - 1.0).coerceAtLeast(0.0)
    }

    fun formattedDiscountText(currencySymbol: String): String {
        return when {
            discountPercent > 0.0 && flatDiscount > 0.0 ->
                "${discountPercent.toInt()}% + $currencySymbol${String.format("%.2f", flatDiscount)} OFF"
            discountPercent > 0.0 ->
                "${discountPercent.toInt()}% OFF" + (if (maxDiscountCap > 0.0) " (max $currencySymbol${maxDiscountCap.toInt()})" else "")
            flatDiscount > 0.0 ->
                "$currencySymbol${String.format("%.2f", flatDiscount)} OFF"
            else -> "Special Promo"
        }
    }
}
