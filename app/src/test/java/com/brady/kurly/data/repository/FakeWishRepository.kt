package com.brady.kurly.data.repository

import com.brady.kurly.domain.repository.WishRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWishRepository : WishRepository {

    private val _wishIds = MutableStateFlow<Set<Long>>(emptySet())

    override fun observeWishIds(): Flow<Set<Long>> = _wishIds

    override suspend fun isWished(productId: Long): Boolean {
        return productId in _wishIds.value
    }

    override suspend fun addWish(productId: Long) {
        _wishIds.value = _wishIds.value + productId
    }

    override suspend fun removeWish(productId: Long) {
        _wishIds.value = _wishIds.value - productId
    }
}
