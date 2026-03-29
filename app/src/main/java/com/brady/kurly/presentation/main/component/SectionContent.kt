package com.brady.kurly.presentation.main.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brady.kurly.domain.model.SectionType
import com.brady.kurly.domain.model.SectionWithProducts

@Composable
fun SectionContent(
    section: SectionWithProducts,
    wishIds: Set<Long>,
    onToggleWish: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (section.type) {
        SectionType.GRID -> {
            GridSectionContent(
                products = section.products,
                wishIds = wishIds,
                onToggleWish = onToggleWish
            )
        }
        SectionType.HORIZONTAL -> {
            HorizontalSectionContent(
                products = section.products,
                wishIds = wishIds,
                onToggleWish = onToggleWish
            )
        }
        SectionType.VERTICAL -> {
            VerticalSectionContent(
                products = section.products,
                wishIds = wishIds,
                onToggleWish = onToggleWish
            )
        }
    }
}
