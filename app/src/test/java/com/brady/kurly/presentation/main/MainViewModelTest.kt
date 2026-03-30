package com.brady.kurly.presentation.main

import app.cash.turbine.test
import com.brady.kurly.data.repository.FakeSectionRepository
import com.brady.kurly.data.repository.FakeSectionRepository.Companion.createTestProduct
import com.brady.kurly.data.repository.FakeSectionRepository.Companion.createTestSection
import com.brady.kurly.data.repository.FakeWishRepository
import com.brady.kurly.domain.model.SectionType
import com.brady.kurly.domain.usecase.ToggleWishUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSectionRepository: FakeSectionRepository
    private lateinit var fakeWishRepository: FakeWishRepository
    private lateinit var toggleWishUseCase: ToggleWishUseCase
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeSectionRepository = FakeSectionRepository()
        fakeWishRepository = FakeWishRepository()
        toggleWishUseCase = ToggleWishUseCase(fakeWishRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MainViewModel {
        return MainViewModel(
            sectionRepository = fakeSectionRepository,
            wishRepository = fakeWishRepository,
            toggleWishUseCase = toggleWishUseCase
        )
    }

    private fun setupPage1(nextPage: Int? = 2) {
        val sections = listOf(
            createTestSection(1, SectionType.GRID),
            createTestSection(2, SectionType.HORIZONTAL)
        )
        fakeSectionRepository.addSectionsPage(1, sections, nextPage)
        fakeSectionRepository.addSectionProducts(1, listOf(createTestProduct(101), createTestProduct(102)))
        fakeSectionRepository.addSectionProducts(2, listOf(createTestProduct(201)))
    }

    private fun setupPage2(nextPage: Int? = null) {
        val sections = listOf(
            createTestSection(3, SectionType.VERTICAL)
        )
        fakeSectionRepository.addSectionsPage(2, sections, nextPage)
        fakeSectionRepository.addSectionProducts(3, listOf(createTestProduct(301)))
    }

    @Test
    fun `초기 로딩 후 섹션 데이터가 로드된다`() = runTest {
        setupPage1()
        viewModel = createViewModel()

        viewModel.uiState.test {
            // initialValue
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.sections.size)
            assertEquals("테스트 섹션 1", loaded.sections[0].title)
            assertEquals(SectionType.GRID, loaded.sections[0].type)
            assertEquals(2, loaded.sections[0].products.size)
            assertTrue(loaded.hasNextPage)
            assertNull(loaded.error)
        }
    }

    @Test
    fun `페이징 - 다음 페이지 로드 시 데이터가 누적된다`() = runTest {
        setupPage1(nextPage = 2)
        setupPage2(nextPage = null)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue
            advanceUntilIdle()

            val page1 = expectMostRecentItem()
            assertEquals(2, page1.sections.size)

            viewModel.loadNextPage()
            advanceUntilIdle()

            val page2 = expectMostRecentItem()
            assertEquals(3, page2.sections.size)
            assertFalse(page2.hasNextPage)
        }
    }

    @Test
    fun `마지막 페이지면 hasNextPage가 false`() = runTest {
        setupPage1(nextPage = null)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertFalse(state.hasNextPage)
        }
    }

    @Test
    fun `찜 토글 - 추가 후 제거`() = runTest {
        setupPage1()
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.toggleWish(101)
            advanceUntilIdle()

            val wished = expectMostRecentItem()
            assertTrue(wished.wishIds.contains(101))

            viewModel.toggleWish(101)
            advanceUntilIdle()

            val unwished = expectMostRecentItem()
            assertFalse(unwished.wishIds.contains(101))
        }
    }

    @Test
    fun `새로고침 - 데이터가 교체된다`() = runTest {
        setupPage1()
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.refresh()
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(2, state.sections.size)
            assertFalse(state.isRefreshing)
            assertNull(state.error)
        }
    }

    @Test
    fun `에러 발생 시 error 상태가 설정된다`() = runTest {
        fakeSectionRepository.shouldThrowError = true
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(state.sections.isEmpty())
            assertEquals("테스트 에러", state.error)
        }
    }

    @Test
    fun `에러 후 새로고침 성공하면 에러가 초기화된다`() = runTest {
        fakeSectionRepository.shouldThrowError = true
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue
            advanceUntilIdle()

            val errorState = expectMostRecentItem()
            assertEquals("테스트 에러", errorState.error)

            fakeSectionRepository.shouldThrowError = false
            setupPage1()

            viewModel.refresh()
            advanceUntilIdle()

            val successState = expectMostRecentItem()
            assertNull(successState.error)
            assertEquals(2, successState.sections.size)
        }
    }

    @Test
    fun `중복 로딩 방지 - 로딩 중 loadNextPage 호출 무시`() = runTest {
        setupPage1()
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initialValue

            viewModel.loadNextPage() // 이미 init에서 로딩 중 → 무시
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(2, state.sections.size) // 1번만 로드
        }
    }
}
