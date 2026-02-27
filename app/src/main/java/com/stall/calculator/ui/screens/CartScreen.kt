package com.stall.calculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stall.calculator.ui.AppUiState
import com.stall.calculator.ui.components.ProductImage
import com.stall.calculator.ui.components.QuantityInputDialog
import com.stall.calculator.util.MoneyUtils

@Composable
fun CartScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onSetQty: (Long, Int) -> Unit,
    onClear: () -> Unit
) {
    val editingItem = remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("已选清单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (state.cartLines.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text("清空")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("总价")
                    Text(
                        MoneyUtils.formatCents(state.totalCents, state.settings.showCurrencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(onClick = onBack) {
                    Text("返回点单")
                }
            }
        }
    ) { padding ->
        if (state.cartLines.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("当前没有已选商品")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.cartLines, key = { it.productId }) { line ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductImage(
                                imagePath = line.imagePath,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(68.dp)
                            )
                            Column(modifier = Modifier.weight(0.8f)) {
                                Text(line.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "单价 ${MoneyUtils.formatCents(line.priceCents, state.settings.showCurrencySymbol)}"
                                )
                                Text(
                                    "小计 ${MoneyUtils.formatCents(line.subtotalCents, state.settings.showCurrencySymbol)}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    OutlinedButton(onClick = { onSetQty(line.productId, line.qty - 1) }) {
                                        Text("-")
                                    }
                                    TextButton(onClick = { editingItem.value = line.productId }) {
                                        Text("数量 ${line.qty}")
                                    }
                                    OutlinedButton(onClick = { onSetQty(line.productId, line.qty + 1) }) {
                                        Text("+")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val currentId = editingItem.value
    if (currentId != null) {
        val line = state.cartLines.firstOrNull { it.productId == currentId }
        if (line != null) {
            QuantityInputDialog(
                initialValue = line.qty,
                onDismiss = { editingItem.value = null },
                onConfirm = { qty ->
                    editingItem.value = null
                    onSetQty(line.productId, qty)
                }
            )
        } else {
            editingItem.value = null
        }
    }
}
