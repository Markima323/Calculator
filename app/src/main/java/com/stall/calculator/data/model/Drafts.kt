package com.stall.calculator.data.model

import com.stall.calculator.data.db.ProductStatus

data class ProductDraft(
    val id: Long? = null,
    val name: String,
    val priceCents: Long,
    val imagePath: String? = null,
    val status: ProductStatus = ProductStatus.ON_SALE,
    val categoryId: Long? = null,
    val weight: Int = 0
)

data class CategoryDraft(
    val id: Long? = null,
    val name: String,
    val weight: Int = 0
)
