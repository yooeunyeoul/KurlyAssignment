package com.brady.kurly.data.repository

import com.brady.kurly.data.mapper.toDomain
import com.brady.kurly.data.remote.api.KurlyApi
import com.brady.kurly.domain.model.Product
import com.brady.kurly.domain.model.SectionsResult
import com.brady.kurly.domain.repository.SectionRepository
import javax.inject.Inject

class SectionRepositoryImpl @Inject constructor(
    private val api: KurlyApi
) : SectionRepository {

    override suspend fun getSections(page: Int): Result<SectionsResult> {
        return try {
            val response = api.getSections(page)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
        return try {
            val response = api.getSectionProducts(sectionId)
            Result.success(response.data.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
