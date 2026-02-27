package com.stall.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stall.calculator.AppContainer
import com.stall.calculator.data.db.CartItemEntity
import com.stall.calculator.data.db.CategoryEntity
import com.stall.calculator.data.db.ProductEntity
import com.stall.calculator.data.db.ProductStatus
import com.stall.calculator.data.model.AppSettings
import com.stall.calculator.data.model.CategoryDraft
import com.stall.calculator.data.model.ExportBundle
import com.stall.calculator.data.model.ProductDraft
import com.stall.calculator.data.repo.SettingsRepository
import com.stall.calculator.data.repo.StallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderProductUi(
    val id: Long,
    val name: String,
    val priceCents: Long,
    val imagePath: String?,
    val status: ProductStatus,
    val weight: Int,
    val qty: Int
)

data class CartLineUi(
    val productId: Long,
    val name: String,
    val priceCents: Long,
    val imagePath: String?,
    val qty: Int,
    val subtotalCents: Long
)

data class AppUiState(
    val settings: AppSettings = AppSettings(),
    val categories: List<CategoryEntity> = emptyList(),
    val allProducts: List<ProductEntity> = emptyList(),
    val orderProducts: List<OrderProductUi> = emptyList(),
    val cartLines: List<CartLineUi> = emptyList(),
    val orderQuery: String = "",
    val selectedCategoryId: Long? = null,
    val totalCents: Long = 0L
)

class AppViewModel(
    private val repository: StallRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val orderQuery = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<AppUiState> = combine(
        repository.productsFlow,
        repository.categoriesFlow,
        repository.cartFlow,
        repository.settingsFlow,
        orderQuery,
        selectedCategoryId
    ) { products, categories, cart, settings, query, selectedCategory ->
        buildUiState(
            products = products,
            categories = categories,
            cart = cart,
            settings = settings,
            query = query,
            selectedCategoryId = selectedCategory
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppUiState()
    )

    init {
        viewModelScope.launch {
            val settings = repository.settingsFlow.first()
            if (!settings.restoreCartOnLaunch) {
                repository.clearCart()
            }
        }
    }

    fun updateOrderQuery(value: String) {
        orderQuery.value = value
    }

    fun updateSelectedCategory(categoryId: Long?) {
        selectedCategoryId.value = categoryId
    }

    fun addToCart(productId: Long) {
        viewModelScope.launch {
            repository.addToCart(productId)
        }
    }

    fun setQty(productId: Long, qty: Int) {
        viewModelScope.launch {
            repository.setCartQty(productId, qty)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun upsertProduct(draft: ProductDraft, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.upsertProduct(draft)
            onDone(id)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun setProductStatus(productId: Long, status: ProductStatus) {
        viewModelScope.launch {
            repository.setProductStatus(productId, status)
        }
    }

    fun upsertCategory(draft: CategoryDraft) {
        viewModelScope.launch {
            repository.upsertCategory(draft)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateShowCurrency(value: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowCurrency(value) }
    }

    fun updateConfirmBeforeComplete(value: Boolean) {
        viewModelScope.launch { settingsRepository.updateConfirmBeforeComplete(value) }
    }

    fun updateAllowFreeProduct(value: Boolean) {
        viewModelScope.launch { settingsRepository.updateAllowFreeProduct(value) }
    }

    fun updateRestoreCart(value: Boolean) {
        viewModelScope.launch { settingsRepository.updateRestoreCart(value) }
    }

    fun updateShowSoldOut(value: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowSoldOut(value) }
    }

    suspend fun exportBundle(): ExportBundle = repository.exportBundle()

    suspend fun importBundle(bundle: ExportBundle, replaceExisting: Boolean = true) {
        repository.importBundle(bundle, replaceExisting)
    }

    private fun buildUiState(
        products: List<ProductEntity>,
        categories: List<CategoryEntity>,
        cart: List<CartItemEntity>,
        settings: AppSettings,
        query: String,
        selectedCategoryId: Long?
    ): AppUiState {
        val cartByProduct = cart.associateBy { it.productId }
        val productsById = products.associateBy { it.id }

        val normalizedQuery = query.trim().lowercase()
        val filtered = products
            .asSequence()
            .filter { product ->
                if (!settings.showSoldOutOnOrderPage && product.status == ProductStatus.SOLD_OUT) {
                    return@filter false
                }
                if (selectedCategoryId != null && product.categoryId != selectedCategoryId) {
                    return@filter false
                }
                if (normalizedQuery.isNotEmpty() && !product.name.lowercase().contains(normalizedQuery)) {
                    return@filter false
                }
                true
            }
            .sortedWith(compareByDescending<ProductEntity> { it.weight }.thenBy { it.name })
            .map { product ->
                OrderProductUi(
                    id = product.id,
                    name = product.name,
                    priceCents = product.priceCents,
                    imagePath = product.imagePath,
                    status = product.status,
                    weight = product.weight,
                    qty = cartByProduct[product.id]?.qty ?: 0
                )
            }
            .toList()

        val cartLines = cart.mapNotNull { item ->
            val product = productsById[item.productId] ?: return@mapNotNull null
            CartLineUi(
                productId = product.id,
                name = product.name,
                priceCents = product.priceCents,
                imagePath = product.imagePath,
                qty = item.qty,
                subtotalCents = product.priceCents * item.qty
            )
        }

        val totalCents = cartLines.sumOf { it.subtotalCents }

        return AppUiState(
            settings = settings,
            categories = categories,
            allProducts = products.sortedWith(compareByDescending<ProductEntity> { it.weight }.thenBy { it.name }),
            orderProducts = filtered,
            cartLines = cartLines,
            orderQuery = query,
            selectedCategoryId = selectedCategoryId,
            totalCents = totalCents
        )
    }
}

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(container.repository, container.settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
