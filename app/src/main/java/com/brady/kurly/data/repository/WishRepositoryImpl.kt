package com.brady.kurly.data.repository

import com.brady.kurly.data.local.dao.WishDao
import com.brady.kurly.data.local.entity.WishEntity
import com.brady.kurly.domain.repository.WishRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WishRepositoryImpl @Inject constructor(
    private val wishDao: WishDao
) : WishRepository {

    override fun observeWishIds(): Flow<Set<Long>> {
        return wishDao.observeWishIds().map { it.toSet() }
    }

    override suspend fun isWished(productId: Long): Boolean {
        return wishDao.isWished(productId)
    }

    override suspend fun addWish(productId: Long) {
        wishDao.insert(WishEntity(productId))
    }

    override suspend fun removeWish(productId: Long) {
        wishDao.delete(productId)
    }
}
