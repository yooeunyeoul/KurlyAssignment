package com.brady.kurly.presentation.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DiscountColor = Color(0xFFFA622F)
private val OriginalPriceColor = Color.Gray

private fun formatPrice(price: Int): String {
    return String.format("%,d원", price)
}

private fun calculateDiscountRate(originalPrice: Int, discountedPrice: Int): Int {
    if (originalPrice == 0) return 0
    return ((originalPrice - discountedPrice) * 100) / originalPrice
}

/**
 * 2줄 가격 표시 (horizontal, grid 섹션용)
 * 1줄: 할인율 + 할인가
 * 2줄: 원가 (취소선)
 */
@Composable
fun TwoLinePriceDisplay(
    originalPrice: Int,
    discountedPrice: Int?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (discountedPrice != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${calculateDiscountRate(originalPrice, discountedPrice)}%",
                    color = DiscountColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(discountedPrice),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = formatPrice(originalPrice),
                color = OriginalPriceColor,
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough
            )
        } else {
            Text(
                text = formatPrice(originalPrice),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 1줄 가격 표시 (vertical 섹션용)
 * 할인율 + 할인가 + 원가 한 줄에
 */
@Composable
fun InlinePriceDisplay(
    originalPrice: Int,
    discountedPrice: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (discountedPrice != null) {
            Text(
                text = "${calculateDiscountRate(originalPrice, discountedPrice)}%",
                color = DiscountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatPrice(discountedPrice),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatPrice(originalPrice),
                color = OriginalPriceColor,
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough
            )
        } else {
            Text(
                text = formatPrice(originalPrice),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
