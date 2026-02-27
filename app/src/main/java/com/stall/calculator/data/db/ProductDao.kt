package com.stall.calculator.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY weight DESC, updated_at DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllNow(): List<ProductEntity>

    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(products: List<ProductEntity>)

    @Query("UPDATE products SET category_id = NULL, updated_at = :updatedAt WHERE category_id = :categoryId")
    suspend fun clearCategory(categoryId: Long, updatedAt: Long)

    @Query("DELETE FROM products")
    suspend fun clearAll()
}
