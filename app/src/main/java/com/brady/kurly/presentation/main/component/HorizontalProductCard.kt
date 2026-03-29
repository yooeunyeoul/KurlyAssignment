package com.brady.kurly.presentation.main.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brady.kurly.domain.model.Product

@Composable
fun HorizontalProductCard(
    product: Product,
    isWished: Boolean,
    onToggleWish: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box {
            ProductImage(
                imageUrl = product.image,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
            )
            WishButton(
                isWished = isWished,
                onClick = { onToggleWish(product.id) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            )
        }

        Text(
            text = product.name,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        TwoLinePriceDisplay(
            originalPrice = product.originalPrice,
            discountedPrice = product.discountedPrice,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
