package com.brady.kurly.data.remote.dto

data class ProductResponseDto(
    val data: List<ProductDto>
)

data class ProductDto(
    val id: Long,
    val name: String,
    val image: String,
    val originalPrice: Int,
    val discountedPrice: Int?,
    val isSoldOut: Boolean
)
