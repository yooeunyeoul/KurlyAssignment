package com.brady.kurly.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SectionResponseDto(
    val data: List<SectionDto>,
    val paging: PagingDto?
)

data class SectionDto(
    val id: Int,
    val title: String,
    val type: String,
    val url: String
)

data class PagingDto(
    @SerializedName("next_page")
    val nextPage: Int?
)
