package com.stall.calculator.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStatus(status: ProductStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ProductStatus =
        ProductStatus.entries.firstOrNull { it.name == value } ?: ProductStatus.ON_SALE
}
