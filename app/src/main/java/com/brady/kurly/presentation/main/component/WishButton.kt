package com.brady.kurly.presentation.main.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.brady.kurly.R

@Composable
fun WishButton(
    isWished: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isInitialComposition by remember { mutableStateOf(true) }
    var animateTrigger by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (animateTrigger) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "wish_scale"
    )

    LaunchedEffect(isWished) {
        if (isInitialComposition) {
            isInitialComposition = false
            return@LaunchedEffect
        }
        animateTrigger = true
        delay(200)
        animateTrigger = false
    }

    Icon(
        painter = painterResource(
            id = if (isWished) R.drawable.ic_btn_heart_on
            else R.drawable.ic_btn_heart_off
        ),
        contentDescription = if (isWished) "찜 해제" else "찜하기",
        tint = Color.Unspecified,
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}
