package com.brady.kurly.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brady.kurly.domain.model.SectionWithProducts
import com.brady.kurly.domain.repository.SectionRepository
import com.brady.kurly.domain.repository.WishRepository
import com.brady.kurly.domain.usecase.ToggleWishUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sectionRepository: SectionRepository,
    private val wishRepository: WishRepository,
    private val toggleWishUseCase: ToggleWishUseCase
) : ViewModel() {

    private val _sections = MutableStateFlow<List<SectionWithProducts>>(emptyList())
    private val _pagingState = MutableStateFlow(PagingState())
    private val _error = MutableStateFlow<String?>(null)

    private var currentPage = 1
    private var hasNextPage = true
    private var isLoadingPage = false

    val uiState: StateFlow<MainUiState> = combine(
        _sections,
        wishRepository.observeWishIds(),
        _pagingState,
        _error
    ) { sections, wishIds, paging, error ->
        MainUiState(
            sections = sections,
            wishIds = wishIds,
            isLoading = paging.isLoading,
            isLoadingMore = paging.isLoadingMore,
            isRefreshing = paging.isRefreshing,
            hasNextPage = hasNextPage,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(isLoading = true)
    )

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoadingPage || !hasNextPage) return
        isLoadingPage = true

        viewModelScope.launch {
            val isInitialLoad = currentPage == 1
            _pagingState.update {
                it.copy(
                    isLoading = isInitialLoad,
                    isLoadingMore = !isInitialLoad
                )
            }

            try {
                val sectionsWithProducts = loadSectionsWithProducts()

                _sections.update { current -> current + sectionsWithProducts }
                currentPage++
                _error.update { null }
            } catch (e: Exception) {
                _error.update { e.message ?: "알 수 없는 에러가 발생했습니다" }
            } finally {
                _pagingState.update { PagingState() }
                isLoadingPage = false
            }
        }
    }

    fun refresh() {
        if (isLoadingPage) return
        isLoadingPage = true

        viewModelScope.launch {
            _pagingState.update { it.copy(isRefreshing = true) }

            currentPage = 1
            hasNextPage = true
            _error.update { null }

            try {
                val sectionsWithProducts = loadSectionsWithProducts()

                _sections.update { sectionsWithProducts }
                currentPage++
            } catch (e: Exception) {
                _error.update { e.message ?: "알 수 없는 에러가 발생했습니다" }
            } finally {
                _pagingState.update { PagingState() }
                isLoadingPage = false
            }
        }
    }

    private suspend fun loadSectionsWithProducts(): List<SectionWithProducts> {
        val result = sectionRepository.getSections(currentPage).getOrThrow()
        hasNextPage = result.nextPage != null

        return coroutineScope {
            result.sections.map { section ->
                async {
                    val products = sectionRepository.getSectionProducts(section.id)
                        .getOrDefault(emptyList())
                    SectionWithProducts(
                        id = section.id,
                        title = section.title,
                        type = section.type,
                        products = products
                    )
                }
            }.awaitAll()
        }
    }

    fun toggleWish(productId: Long) {
        viewModelScope.launch {
            toggleWishUseCase(productId)
        }
    }

    fun clearError() {
        _error.value = null
    }
}

private data class PagingState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false
)
