package com.stall.calculator.data.repo

import androidx.room.withTransaction
import com.stall.calculator.data.db.AppDatabase
import com.stall.calculator.data.db.CartItemEntity
import com.stall.calculator.data.db.CategoryEntity
import com.stall.calculator.data.db.ProductEntity
import com.stall.calculator.data.db.ProductStatus
import com.stall.calculator.data.model.CategoryDraft
import com.stall.calculator.data.model.ExportBundle
import com.stall.calculator.data.model.ExportCategory
import com.stall.calculator.data.model.ExportProduct
import com.stall.calculator.data.model.ProductDraft
import kotlinx.coroutines.flow.Flow

class StallRepository(
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository
) {
    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val cartDao = database.cartDao()

    val productsFlow: Flow<List<ProductEntity>> = productDao.observeAll()
    val categoriesFlow: Flow<List<CategoryEntity>> = categoryDao.observeAll()
    val cartFlow: Flow<List<CartItemEntity>> = cartDao.observeAll()
    val settingsFlow = settingsRepository.settingsFlow

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getById(id)

    suspend fun upsertProduct(draft: ProductDraft): Long {
        val now = System.currentTimeMillis()
        val cleanName = draft.name.trim()
        if (draft.id == null) {
            return productDao.insert(
                ProductEntity(
                    name = cleanName,
                    priceCents = draft.priceCents,
                    imagePath = draft.imagePath,
                    status = draft.status,
                    categoryId = draft.categoryId,
                    weight = draft.weight,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        val existing = productDao.getById(draft.id) ?: return 0
        productDao.update(
            existing.copy(
                name = cleanName,
                priceCents = draft.priceCents,
                imagePath = draft.imagePath,
                status = draft.status,
                categoryId = draft.categoryId,
                weight = draft.weight,
                updatedAt = now
            )
        )
        return existing.id
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
    }

    suspend fun setProductStatus(productId: Long, status: ProductStatus) {
        val product = productDao.getById(productId) ?: return
        productDao.update(product.copy(status = status, updatedAt = System.currentTimeMillis()))
    }

    suspend fun upsertCategory(draft: CategoryDraft): Long {
        val now = System.currentTimeMillis()
        val cleanName = draft.name.trim()
        if (draft.id == null) {
            return categoryDao.insert(
                CategoryEntity(
                    name = cleanName,
                    weight = draft.weight,
                    createdAt = now
                )
            )
        }

        val existing = categoryDao.getById(draft.id) ?: return 0
        categoryDao.update(existing.copy(name = cleanName, weight = draft.weight))
        return existing.id
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        val now = System.currentTimeMillis()
        productDao.clearCategory(category.id, now)
        categoryDao.delete(category)
    }

    suspend fun addToCart(productId: Long) {
        cartDao.addOneAtomic(productId, System.currentTimeMillis())
    }

    suspend fun setCartQty(productId: Long, qty: Int) {
        if (qty <= 0) {
            cartDao.remove(productId)
            return
        }
        cartDao.upsert(
            CartItemEntity(
                productId = productId,
                qty = qty,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearCart() {
        cartDao.clearAll()
    }

    suspend fun exportBundle(): ExportBundle {
        val categories = categoryDao.getAllNow().map {
            ExportCategory(
                id = it.id,
                name = it.name,
                weight = it.weight,
                createdAt = it.createdAt
            )
        }
        val products = productDao.getAllNow().map {
            ExportProduct(
                id = it.id,
                name = it.name,
                priceCents = it.priceCents,
                imagePath = it.imagePath,
                status = it.status.name,
                categoryId = it.categoryId,
                weight = it.weight,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        return ExportBundle(categories = categories, products = products)
    }

    suspend fun importBundle(bundle: ExportBundle, replaceExisting: Boolean = true) {
        database.withTransaction {
            if (replaceExisting) {
                cartDao.clearAll()
                productDao.clearAll()
                categoryDao.clearAll()
            }

            val categories = bundle.categories.map {
                CategoryEntity(
                    id = it.id,
                    name = it.name,
                    weight = it.weight,
                    createdAt = it.createdAt
                )
            }
            if (categories.isNotEmpty()) {
                categoryDao.insertAllReplace(categories)
            }

            val now = System.currentTimeMillis()
            val products = bundle.products.map {
                ProductEntity(
                    id = it.id,
                    name = it.name,
                    priceCents = it.priceCents,
                    imagePath = it.imagePath,
                    status = ProductStatus.entries.firstOrNull { status ->
                        status.name == it.status
                    } ?: ProductStatus.ON_SALE,
                    categoryId = it.categoryId,
                    weight = it.weight,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt.takeIf { ts -> ts > 0 } ?: now
                )
            }
            if (products.isNotEmpty()) {
                productDao.insertAllReplace(products)
            }
        }
    }
}
