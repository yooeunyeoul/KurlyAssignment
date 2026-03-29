package com.brady.kurly.presentation.main

import com.brady.kurly.domain.model.SectionWithProducts

data class MainUiState(
    val sections: List<SectionWithProducts> = emptyList(),
    val wishIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasNextPage: Boolean = true,
    val error: String? = null
)
