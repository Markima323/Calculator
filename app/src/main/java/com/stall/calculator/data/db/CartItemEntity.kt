package com.stall.calculator.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cart_items",
    primaryKeys = ["product_id"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["product_id"])]
)
data class CartItemEntity(
    @ColumnInfo(name = "product_id")
    val productId: Long,
    val qty: Int,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
