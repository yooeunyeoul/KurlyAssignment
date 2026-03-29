package com.brady.kurly.presentation.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brady.kurly.domain.model.Product

@Composable
fun VerticalSectionContent(
    products: List<Product>,
    wishIds: Set<Long>,
    onToggleWish: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        products.forEach { product ->
            VerticalProductCard(
                product = product,
                isWished = product.id in wishIds,
                onToggleWish = onToggleWish
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )
        }
    }
}
