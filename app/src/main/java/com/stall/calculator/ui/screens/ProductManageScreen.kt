package com.stall.calculator.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stall.calculator.data.db.ProductEntity
import com.stall.calculator.data.db.ProductStatus
import com.stall.calculator.ui.AppUiState
import com.stall.calculator.ui.components.ProductImage
import com.stall.calculator.util.MoneyUtils
import kotlinx.coroutines.launch

@Composable
fun ProductManageScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onSetStatus: (Long, ProductStatus) -> Unit,
    onOpenCategories: () -> Unit,
    exportJson: suspend () -> String,
    importJson: suspend (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var search by remember { mutableStateOf("") }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val raw = exportJson()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                        writer.write(raw)
                    }
                }.onSuccess {
                    snackbarHostState.showSnackbar("导出完成")
                }.onFailure {
                    snackbarHostState.showSnackbar("导出失败: ${it.message}")
                }
            }
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val raw = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: error("文件为空")
                    importJson(raw)
                }.onSuccess {
                    snackbarHostState.showSnackbar("导入完成")
                }.onFailure {
                    snackbarHostState.showSnackbar("导入失败: ${it.message}")
                }
            }
        }
    }

    val filteredProducts = remember(state.allProducts, search) {
        val q = search.trim().lowercase()
        if (q.isBlank()) {
            state.allProducts
        } else {
            state.allProducts.filter { it.name.lowercase().contains(q) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商品管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { createDocLauncher.launch("stall-products.json") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "导出")
                    }
                    IconButton(onClick = { openDocLauncher.launch(arrayOf("application/json", "text/*")) }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "导入")
                    }
                    IconButton(onClick = onOpenCategories) {
                        Icon(Icons.Default.Category, contentDescription = "分类")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "新增商品")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("搜索商品") },
                singleLine = true
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    Surface(
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductImage(
                                imagePath = product.imagePath,
                                modifier = Modifier
                                    .size(72.dp)
                            )
                            Column(modifier = Modifier.weight(0.8f)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                Text(MoneyUtils.formatCents(product.priceCents, state.settings.showCurrencySymbol))
                                Text(
                                    if (product.status == ProductStatus.ON_SALE) "可售" else "已售完",
                                    color = if (product.status == ProductStatus.ON_SALE) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = product.status == ProductStatus.ON_SALE,
                                        onCheckedChange = { checked ->
                                            onSetStatus(
                                                product.id,
                                                if (checked) ProductStatus.ON_SALE else ProductStatus.SOLD_OUT
                                            )
                                        }
                                    )
                                    Text("可售")
                                    IconButton(onClick = { onEdit(product.id) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                                    }
                                    IconButton(onClick = { deletingProduct = product }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deletingProduct?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("删除商品") },
            text = { Text("确认删除 ${target.name} ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target)
                        deletingProduct = null
                    }
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) {
                    Text("取消")
                }
            }
        )
    }
}
