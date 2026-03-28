package com.brady.kurly.domain.usecase

import com.brady.kurly.domain.repository.WishRepository
import javax.inject.Inject

class ToggleWishUseCase @Inject constructor(
    private val wishRepository: WishRepository
) {
    suspend operator fun invoke(productId: Long) {
        if (wishRepository.isWished(productId)) {
            wishRepository.removeWish(productId)
        } else {
            wishRepository.addWish(productId)
        }
    }
}
