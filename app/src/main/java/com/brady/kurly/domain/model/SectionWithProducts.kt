package com.brady.kurly.domain.model

data class SectionWithProducts(
    val id: Int,
    val title: String,
    val type: SectionType,
    val products: List<Product>
)
