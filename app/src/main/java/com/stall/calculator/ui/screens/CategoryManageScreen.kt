package com.stall.calculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stall.calculator.data.db.CategoryEntity
import com.stall.calculator.data.model.CategoryDraft
import com.stall.calculator.ui.AppUiState

@Composable
fun CategoryManageScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onUpsert: (CategoryDraft) -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleting by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryEditor(
                title = "新增分类",
                initialName = "",
                initialWeight = "0",
                onCancel = {},
                onConfirm = { name, weight ->
                    onUpsert(CategoryDraft(name = name, weight = weight))
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(category.name)
                            Text(
                                "权重 ${category.weight}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Row {
                            IconButton(onClick = { editing = category }) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑")
                            }
                            IconButton(onClick = { deleting = category }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { current ->
        CategoryEditorDialog(
            category = current,
            onDismiss = { editing = null },
            onConfirm = { name, weight ->
                onUpsert(CategoryDraft(id = current.id, name = name, weight = weight))
                editing = null
            }
        )
    }

    deleting?.let { category ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("删除分类") },
            text = { Text("删除后该分类下商品会变为未分类，确认继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(category)
                        deleting = null
                    }
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CategoryEditor(
    title: String,
    initialName: String,
    initialWeight: String,
    onCancel: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var weight by remember { mutableStateOf(initialWeight) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("名称") },
            singleLine = true
        )
        OutlinedTextField(
            value = weight,
            onValueChange = { input ->
                if (input.isEmpty() || input == "-" || input.toIntOrNull() != null) {
                    weight = input
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("权重") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onConfirm(name.trim(), weight.toIntOrNull() ?: 0)
                        name = ""
                        weight = "0"
                    }
                }
            ) {
                Text("保存")
            }
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember(category.id) { mutableStateOf(category.name) }
    var weight by remember(category.id) { mutableStateOf(category.weight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { input ->
                        if (input.isEmpty() || input == "-" || input.toIntOrNull() != null) {
                            weight = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("权重") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onConfirm(name.trim(), weight.toIntOrNull() ?: 0)
                    }
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
