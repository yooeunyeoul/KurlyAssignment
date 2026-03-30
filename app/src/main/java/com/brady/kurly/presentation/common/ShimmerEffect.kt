package com.brady.kurly.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ShimmerColors = listOf(
    Color(0xFFE0E0E0),
    Color(0xFFF5F5F5),
    Color(0xFFE0E0E0)
)

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = ShimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
fun ShimmerBox(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
    )
}

@Composable
fun SkeletonSectionList(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()

    Column(modifier = modifier) {
        repeat(3) {
            SkeletonSection(brush = brush)
            HorizontalDivider(thickness = 8.dp, color = Color(0xFFF5F5F5))
        }
    }
}

@Composable
private fun SkeletonSection(brush: Brush) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 타이틀 바
        ShimmerBox(
            brush = brush,
            modifier = Modifier
                .width(150.dp)
                .height(20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 카드 3개
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        brush = brush,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBox(
                        brush = brush,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        brush = brush,
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        brush = brush,
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                    )
                }
            }
        }
    }
}
