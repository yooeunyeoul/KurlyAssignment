package com.brady.kurly.presentation.main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.brady.kurly.R

@Composable
fun WishButton(
    isWished: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(
            id = if (isWished) R.drawable.ic_btn_heart_on
            else R.drawable.ic_btn_heart_off
        ),
        contentDescription = if (isWished) "찜 해제" else "찜하기",
        tint = Color.Unspecified,
        modifier = modifier
            .size(36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}
