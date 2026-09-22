package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {

    @Query("SELECT * FROM user_accounts")
    fun getAllAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE provider = :provider LIMIT 1")
    suspend fun getAccountByProvider(provider: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: UserAccountEntity)

    @Update
    suspend fun update(account: UserAccountEntity)

    @Query("DELETE FROM user_accounts WHERE provider = :provider")
    suspend fun deleteByProvider(provider: String)
}
