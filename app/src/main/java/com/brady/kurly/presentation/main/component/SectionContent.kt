package com.brady.kurly.presentation.main.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brady.kurly.domain.model.Product
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
            // Commit 8에서 구현
            Text("Horizontal: ${section.title} (${section.products.size}개)")
        }
        SectionType.VERTICAL -> {
            // Commit 9에서 구현
            Text("Vertical: ${section.title} (${section.products.size}개)")
        }
    }
}
