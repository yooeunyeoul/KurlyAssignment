package com.brady.kurly.domain.model

data class SectionsResult(
    val sections: List<Section>,
    val nextPage: Int?
)
