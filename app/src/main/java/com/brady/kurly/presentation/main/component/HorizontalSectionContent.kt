package com.brady.kurly.presentation.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brady.kurly.domain.model.Product

@Composable
fun HorizontalSectionContent(
    products: List<Product>,
    wishIds: Set<Long>,
    onToggleWish: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = products,
            key = { it.id }
        ) { product ->
            HorizontalProductCard(
                product = product,
                isWished = product.id in wishIds,
                onToggleWish = onToggleWish,
                modifier = Modifier.width(150.dp)
            )
        }
    }
}
