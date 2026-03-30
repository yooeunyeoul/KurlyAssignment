package com.brady.kurly.domain.usecase

import com.brady.kurly.data.repository.FakeWishRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleWishUseCaseTest {

    private lateinit var fakeWishRepository: FakeWishRepository
    private lateinit var toggleWishUseCase: ToggleWishUseCase

    @Before
    fun setup() {
        fakeWishRepository = FakeWishRepository()
        toggleWishUseCase = ToggleWishUseCase(fakeWishRepository)
    }

    @Test
    fun `찜 안 된 상품 토글하면 찜에 추가된다`() = runTest {
        assertFalse(fakeWishRepository.isWished(1L))

        toggleWishUseCase(1L)

        assertTrue(fakeWishRepository.isWished(1L))
    }

    @Test
    fun `찜 된 상품 토글하면 찜에서 제거된다`() = runTest {
        fakeWishRepository.addWish(1L)
        assertTrue(fakeWishRepository.isWished(1L))

        toggleWishUseCase(1L)

        assertFalse(fakeWishRepository.isWished(1L))
    }
}
