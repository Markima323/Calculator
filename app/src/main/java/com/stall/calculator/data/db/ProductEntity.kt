package com.stall.calculator.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category_id", "status", "weight"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "price_cents")
    val priceCents: Long,
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    val status: ProductStatus = ProductStatus.ON_SALE,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    val weight: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
