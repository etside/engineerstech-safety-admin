package com.etsafety.admin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "premium_users")
data class PremiumUser(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identifier: String,
    val tier: String,
    val expiryTimestamp: Long,
    val createdAt: Long = System.currentTimeMillis()
)
