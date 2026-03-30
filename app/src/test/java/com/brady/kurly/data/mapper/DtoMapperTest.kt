package com.brady.kurly.data.mapper

import com.brady.kurly.data.remote.dto.PagingDto
import com.brady.kurly.data.remote.dto.ProductDto
import com.brady.kurly.data.remote.dto.SectionDto
import com.brady.kurly.data.remote.dto.SectionResponseDto
import com.brady.kurly.domain.model.SectionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DtoMapperTest {

    @Test
    fun `SectionDto type grid은 SectionType GRID로 매핑된다`() {
        val dto = createSectionDto(type = "grid")
        assertEquals(SectionType.GRID, dto.toDomain().type)
    }

    @Test
    fun `SectionDto type horizontal은 SectionType HORIZONTAL로 매핑된다`() {
        val dto = createSectionDto(type = "horizontal")
        assertEquals(SectionType.HORIZONTAL, dto.toDomain().type)
    }

    @Test
    fun `SectionDto type vertical은 SectionType VERTICAL로 매핑된다`() {
        val dto = createSectionDto(type = "vertical")
        assertEquals(SectionType.VERTICAL, dto.toDomain().type)
    }

    @Test
    fun `SectionDto 알 수 없는 type은 SectionType VERTICAL로 매핑된다`() {
        val dto = createSectionDto(type = "unknown")
        assertEquals(SectionType.VERTICAL, dto.toDomain().type)
    }

    @Test
    fun `ProductDto 할인 있을 때 discountedPrice가 매핑된다`() {
        val dto = createProductDto(originalPrice = 10000, discountedPrice = 8000)
        val product = dto.toDomain()

        assertEquals(10000, product.originalPrice)
        assertEquals(8000, product.discountedPrice)
    }

    @Test
    fun `ProductDto 할인 없을 때 discountedPrice가 null이다`() {
        val dto = createProductDto(originalPrice = 10000, discountedPrice = null)
        val product = dto.toDomain()

        assertEquals(10000, product.originalPrice)
        assertNull(product.discountedPrice)
    }

    @Test
    fun `SectionResponseDto paging 있으면 nextPage가 매핑된다`() {
        val dto = SectionResponseDto(
            data = listOf(createSectionDto()),
            paging = PagingDto(nextPage = 2)
        )
        val result = dto.toDomain()

        assertEquals(2, result.nextPage)
        assertEquals(1, result.sections.size)
    }

    @Test
    fun `SectionResponseDto paging 없으면 nextPage가 null이다`() {
        val dto = SectionResponseDto(
            data = listOf(createSectionDto()),
            paging = null
        )
        val result = dto.toDomain()

        assertNull(result.nextPage)
    }

    private fun createSectionDto(
        id: Int = 1,
        title: String = "테스트 섹션",
        type: String = "grid",
        url: String = "section_products_1"
    ) = SectionDto(id = id, title = title, type = type, url = url)

    private fun createProductDto(
        id: Long = 1L,
        name: String = "테스트 상품",
        image: String = "https://test.com/image.jpg",
        originalPrice: Int = 10000,
        discountedPrice: Int? = null,
        isSoldOut: Boolean = false
    ) = ProductDto(
        id = id, name = name, image = image,
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        isSoldOut = isSoldOut
    )
}
