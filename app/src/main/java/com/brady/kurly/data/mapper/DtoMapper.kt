package com.brady.kurly.data.mapper

import com.brady.kurly.data.remote.dto.ProductDto
import com.brady.kurly.data.remote.dto.SectionDto
import com.brady.kurly.data.remote.dto.SectionResponseDto
import com.brady.kurly.domain.model.Product
import com.brady.kurly.domain.model.Section
import com.brady.kurly.domain.model.SectionType
import com.brady.kurly.domain.model.SectionsResult

fun SectionResponseDto.toDomain() = SectionsResult(
    sections = data.map { it.toDomain() },
    nextPage = paging?.nextPage
)

fun SectionDto.toDomain() = Section(
    id = id,
    title = title,
    type = when (type) {
        "horizontal" -> SectionType.HORIZONTAL
        "grid" -> SectionType.GRID
        else -> SectionType.VERTICAL
    }
)

fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    image = image,
    originalPrice = originalPrice,
    discountedPrice = discountedPrice,
    isSoldOut = isSoldOut
)
