package com.brady.kurly.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brady.kurly.presentation.common.ErrorScreen
import com.brady.kurly.presentation.common.PageLoadingIndicator
import com.brady.kurly.presentation.common.SkeletonSectionList
import com.brady.kurly.presentation.main.component.SectionContent
import com.brady.kurly.presentation.main.component.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 2 && uiState.hasNextPage && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kurly") })
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                SkeletonSectionList(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null && uiState.sections.isEmpty() -> {
                ErrorScreen(
                    message = uiState.error!!,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.sections,
                            key = { it.id },
                            contentType = { it.type }
                        ) { section ->
                            Column {
                                SectionHeader(title = section.title)
                                SectionContent(
                                    section = section,
                                    wishIds = uiState.wishIds,
                                    onToggleWish = viewModel::toggleWish
                                )
                                HorizontalDivider(
                                    thickness = 8.dp,
                                    color = Color(0xFFF5F5F5)
                                )
                            }
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                PageLoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
