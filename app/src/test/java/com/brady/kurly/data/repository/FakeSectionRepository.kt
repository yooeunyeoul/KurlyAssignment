package com.brady.kurly.data.repository

import com.brady.kurly.domain.model.Product
import com.brady.kurly.domain.model.Section
import com.brady.kurly.domain.model.SectionType
import com.brady.kurly.domain.model.SectionsResult
import com.brady.kurly.domain.repository.SectionRepository

class FakeSectionRepository : SectionRepository {

    private val sectionsPages = mutableMapOf<Int, SectionsResult>()
    private val sectionProducts = mutableMapOf<Int, List<Product>>()
    var shouldThrowError = false
    var errorMessage = "테스트 에러"

    fun addSectionsPage(page: Int, sections: List<Section>, nextPage: Int?) {
        sectionsPages[page] = SectionsResult(sections = sections, nextPage = nextPage)
    }

    fun addSectionProducts(sectionId: Int, products: List<Product>) {
        sectionProducts[sectionId] = products
    }

    override suspend fun getSections(page: Int): Result<SectionsResult> {
        if (shouldThrowError) return Result.failure(Exception(errorMessage))
        val result = sectionsPages[page] ?: return Result.failure(Exception("페이지 없음"))
        return Result.success(result)
    }

    override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
        if (shouldThrowError) return Result.failure(Exception(errorMessage))
        val products = sectionProducts[sectionId] ?: return Result.success(emptyList())
        return Result.success(products)
    }

    companion object {
        fun createTestSection(id: Int, type: SectionType = SectionType.GRID) = Section(
            id = id,
            title = "테스트 섹션 $id",
            type = type
        )

        fun createTestProduct(id: Long, hasDiscount: Boolean = false) = Product(
            id = id,
            name = "테스트 상품 $id",
            image = "https://test.com/image_$id.jpg",
            originalPrice = 10000,
            discountedPrice = if (hasDiscount) 8000 else null,
            isSoldOut = false
        )
    }
}
