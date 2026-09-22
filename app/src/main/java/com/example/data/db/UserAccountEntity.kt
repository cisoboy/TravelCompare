package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RideProvider
import com.example.model.UserAccount

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val provider: String, // "UBER" or "BOLT"
    val isLoggedIn: Boolean,
    val userName: String,
    val userEmail: String,
    val membershipTier: String,
    val memberDiscountPercent: Double,
    val linkedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): UserAccount {
        val providerEnum = if (provider.equals("UBER", ignoreCase = true)) RideProvider.UBER else RideProvider.BOLT
        return UserAccount(
            provider = providerEnum,
            isLoggedIn = isLoggedIn,
            userName = userName,
            userEmail = userEmail,
            membershipTier = membershipTier,
            memberDiscountPercent = memberDiscountPercent,
            linkedAt = linkedAt
        )
    }

    companion object {
        fun fromDomain(account: UserAccount): UserAccountEntity {
            return UserAccountEntity(
                provider = account.provider.name,
                isLoggedIn = account.isLoggedIn,
                userName = account.userName,
                userEmail = account.userEmail,
                membershipTier = account.membershipTier,
                memberDiscountPercent = account.memberDiscountPercent,
                linkedAt = account.linkedAt
            )
        }
    }
}
