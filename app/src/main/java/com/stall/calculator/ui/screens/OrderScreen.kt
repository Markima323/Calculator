package com.stall.calculator.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stall.calculator.data.db.ProductStatus
import com.stall.calculator.ui.AppUiState
import com.stall.calculator.ui.components.ProductImage
import com.stall.calculator.ui.components.QuantityInputDialog
import com.stall.calculator.util.MoneyUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OrderScreen(
    state: AppUiState,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onAddOne: (Long) -> Unit,
    onSetQty: (Long, Int) -> Unit,
    onComplete: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showCompleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("摆摊点单") },
                actions = {
                    IconButton(onClick = onOpenProducts) {
                        Icon(Icons.Default.Inventory, contentDescription = "商品管理")
                    }
                    IconButton(onClick = onOpenCategories) {
                        Icon(Icons.Default.Category, contentDescription = "分类")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .combinedClickable(
                            onClick = onOpenCart,
                            onLongClick = onOpenCart
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "总价",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = MoneyUtils.formatCents(state.totalCents, state.settings.showCurrencySymbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Button(
                    onClick = {
                        if (state.settings.confirmBeforeComplete) {
                            showCompleteConfirm = true
                        } else {
                            onComplete()
                        }
                    },
                    enabled = state.totalCents > 0,
                    modifier = Modifier.height(58.dp)
                ) {
                    Text("完成")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            OutlinedTextField(
                value = state.orderQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索商品") },
                singleLine = true
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("全部") }
                    )
                }
                items(state.categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { onSelectCategory(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            if (state.orderProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有匹配商品")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 118.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = state.orderProducts, key = { it.id }) { product ->
                        OrderProductCard(
                            name = product.name,
                            priceText = MoneyUtils.formatCents(
                                product.priceCents,
                                state.settings.showCurrencySymbol
                            ),
                            imagePath = product.imagePath,
                            qty = product.qty,
                            soldOut = product.status == ProductStatus.SOLD_OUT,
                            onClick = { onAddOne(product.id) },
                            onSetQty = { qty -> onSetQty(product.id, qty) }
                        )
                    }
                }
            }
        }
    }

    if (showCompleteConfirm) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirm = false },
            title = { Text("确认完成") },
            text = { Text("将清空本单已选商品并归零总价") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteConfirm = false
                        onComplete()
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirm = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OrderProductCard(
    name: String,
    priceText: String,
    imagePath: String?,
    qty: Int,
    soldOut: Boolean,
    onClick: () -> Unit,
    onSetQty: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showQtyDialog by remember { mutableStateOf(false) }

    BadgedBox(
        badge = {
            if (qty > 0) {
                Badge {
                    Text(qty.toString())
                }
            }
        }
    ) {
        Card(
            modifier = Modifier.combinedClickable(
                onClick = {
                    if (!soldOut) onClick()
                },
                onLongClick = {
                    menuExpanded = true
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (soldOut) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProductImage(
                    imagePath = imagePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )
                Text(name, maxLines = 2, style = MaterialTheme.typography.titleSmall)
                Text(
                    priceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (soldOut) {
                    Text(
                        "已售完",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("修改数量") },
                    onClick = {
                        menuExpanded = false
                        showQtyDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("清零") },
                    onClick = {
                        menuExpanded = false
                        onSetQty(0)
                    }
                )
            }
        }
    }

    if (showQtyDialog) {
        QuantityInputDialog(
            initialValue = qty,
            onDismiss = { showQtyDialog = false },
            onConfirm = {
                showQtyDialog = false
                onSetQty(it)
            }
        )
    }
}
