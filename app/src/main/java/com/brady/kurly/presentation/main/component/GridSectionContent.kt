package com.brady.kurly.presentation.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brady.kurly.domain.model.Product

@Composable
fun GridSectionContent(
    products: List<Product>,
    wishIds: Set<Long>,
    onToggleWish: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = remember(products) {
        products.take(6).chunked(3)
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowProducts.forEach { product ->
                    HorizontalProductCard(
                        product = product,
                        isWished = product.id in wishIds,
                        onToggleWish = onToggleWish,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 3개 미만이면 빈 공간 채우기
                repeat(3 - rowProducts.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
