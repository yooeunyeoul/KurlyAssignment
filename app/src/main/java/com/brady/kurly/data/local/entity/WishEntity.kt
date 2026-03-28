package com.brady.kurly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishes")
data class WishEntity(
    @PrimaryKey
    val productId: Long
)
