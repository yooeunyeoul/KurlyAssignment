package com.brady.kurly.domain.repository

import com.brady.kurly.domain.model.Product
import com.brady.kurly.domain.model.SectionsResult

interface SectionRepository {
    suspend fun getSections(page: Int): Result<SectionsResult>
    suspend fun getSectionProducts(sectionId: Int): Result<List<Product>>
}
