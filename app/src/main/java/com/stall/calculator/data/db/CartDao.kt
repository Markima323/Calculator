package com.stall.calculator.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query(
        "INSERT INTO cart_items(product_id, qty, updated_at) VALUES(:productId, 1, :updatedAt) " +
            "ON CONFLICT(product_id) DO UPDATE SET qty = qty + 1, updated_at = excluded.updated_at"
    )
    suspend fun addOneAtomic(productId: Long, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("UPDATE cart_items SET qty = :qty, updated_at = :updatedAt WHERE product_id = :productId")
    suspend fun updateQty(productId: Long, qty: Int, updatedAt: Long)

    @Query("DELETE FROM cart_items WHERE product_id = :productId")
    suspend fun remove(productId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()
}
