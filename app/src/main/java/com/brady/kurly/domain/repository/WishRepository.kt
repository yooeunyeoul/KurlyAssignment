package com.brady.kurly.domain.repository

import kotlinx.coroutines.flow.Flow

interface WishRepository {
    fun observeWishIds(): Flow<Set<Long>>
    suspend fun isWished(productId: Long): Boolean
    suspend fun addWish(productId: Long)
    suspend fun removeWish(productId: Long)
}
