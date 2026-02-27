package com.stall.calculator.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportBundle(
    @SerialName("version")
    val version: Int = 1,
    @SerialName("categories")
    val categories: List<ExportCategory> = emptyList(),
    @SerialName("products")
    val products: List<ExportProduct> = emptyList()
)

@Serializable
data class ExportCategory(
    val id: Long,
    val name: String,
    val weight: Int,
    @SerialName("created_at")
    val createdAt: Long
)

@Serializable
data class ExportProduct(
    val id: Long,
    val name: String,
    @SerialName("price_cents")
    val priceCents: Long,
    @SerialName("image_path")
    val imagePath: String? = null,
    val status: String,
    @SerialName("category_id")
    val categoryId: Long? = null,
    val weight: Int,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long
)
