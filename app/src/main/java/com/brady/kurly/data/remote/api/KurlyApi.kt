package com.brady.kurly.data.remote.api

import com.brady.kurly.data.remote.dto.ProductResponseDto
import com.brady.kurly.data.remote.dto.SectionResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface KurlyApi {

    @GET("sections")
    suspend fun getSections(
        @Query("page") page: Int
    ): SectionResponseDto

    @GET("section/products")
    suspend fun getSectionProducts(
        @Query("sectionId") sectionId: Int
    ): ProductResponseDto
}
