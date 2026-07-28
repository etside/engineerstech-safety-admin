package com.etsafety.admin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PremiumUserDao {

    @Query("SELECT * FROM premium_users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<PremiumUser>>

    @Query("SELECT * FROM premium_users WHERE id = :id")
    suspend fun getUserById(id: Long): PremiumUser?

    @Query("SELECT * FROM premium_users WHERE identifier = :identifier LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): PremiumUser?

    @Query("SELECT COUNT(*) FROM premium_users WHERE expiryTimestamp > :currentTime")
    fun getActiveUserCount(currentTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM premium_users WHERE expiryTimestamp > :currentTime AND expiryTimestamp <= :expiryLimit")
    fun getExpiringSoonCount(currentTime: Long, expiryLimit: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM premium_users WHERE tier = :tier AND expiryTimestamp > :currentTime")
    fun getTierUserCount(tier: String, currentTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: PremiumUser): Long

    @Update
    suspend fun updateUser(user: PremiumUser)

    @Delete
    suspend fun deleteUser(user: PremiumUser)

    @Query("DELETE FROM premium_users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("SELECT * FROM premium_users WHERE identifier = :identifier ORDER BY createdAt DESC")
    suspend fun getUsersByIdentifier(identifier: String): List<PremiumUser>

    @Query("DELETE FROM premium_users WHERE id IN (:ids)")
    suspend fun deleteUsersByIds(ids: List<Long>)

    @Query("DELETE FROM premium_users")
    suspend fun deleteAllUsers()
}
