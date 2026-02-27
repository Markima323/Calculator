package com.stall.calculator.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.stall.calculator.data.db.ProductStatus
import com.stall.calculator.data.model.ProductDraft
import com.stall.calculator.ui.AppUiState
import com.stall.calculator.ui.components.ProductImage
import com.stall.calculator.util.ImageStorage
import com.stall.calculator.util.MoneyUtils
import kotlinx.coroutines.launch

@Composable
fun ProductFormScreen(
    state: AppUiState,
    productId: Long?,
    imageStorage: ImageStorage,
    onBack: () -> Unit,
    onSave: (ProductDraft) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val editing = remember(state.allProducts, productId) {
        productId?.let { id -> state.allProducts.firstOrNull { it.id == id } }
    }

    var name by remember(productId) { mutableStateOf("") }
    var price by remember(productId) { mutableStateOf("") }
    var weight by remember(productId) { mutableStateOf("0") }
    var imagePath by remember(productId) { mutableStateOf<String?>(null) }
    var status by remember(productId) { mutableStateOf(ProductStatus.ON_SALE) }
    var selectedCategoryId by remember(productId) { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(editing?.id) {
        if (editing != null) {
            name = editing.name
            price = (editing.priceCents / 100.0).toString()
            weight = editing.weight.toString()
            imagePath = editing.imagePath
            status = editing.status
            selectedCategoryId = editing.categoryId
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val stored = imageStorage.persistFromUri(uri)
                if (!stored.isNullOrBlank()) {
                    imagePath = stored
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            scope.launch {
                val stored = imageStorage.persistFromUri(uri)
                if (!stored.isNullOrBlank()) {
                    imagePath = stored
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = imageStorage.createCameraUri()
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            error = "未授予相机权限"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "新增商品" else "编辑商品") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val parsedPrice = MoneyUtils.parsePriceToCents(price)
                            val parsedWeight = weight.toIntOrNull() ?: 0
                            when {
                                name.trim().isEmpty() -> error = "商品名不能为空"
                                parsedPrice == null -> error = "价格格式错误"
                                parsedPrice < 0 -> error = "价格不能小于 0"
                                parsedPrice == 0L && !state.settings.allowFreeProduct -> {
                                    error = "当前设置不允许 0 元商品"
                                }
                                else -> {
                                    if (editing?.imagePath != null && editing.imagePath != imagePath) {
                                        imageStorage.deleteImage(editing.imagePath)
                                    }
                                    onSave(
                                        ProductDraft(
                                            id = productId,
                                            name = name,
                                            priceCents = parsedPrice,
                                            imagePath = imagePath,
                                            status = status,
                                            categoryId = selectedCategoryId,
                                            weight = parsedWeight
                                        )
                                    )
                                    onBack()
                                }
                            }
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("商品名称") },
                singleLine = true
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("售价") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { next ->
                    if (next.isEmpty() || next == "-" || next.toIntOrNull() != null) {
                        weight = next
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("排序权重") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("商品状态")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (status == ProductStatus.ON_SALE) "可售" else "已售完")
                    Switch(
                        checked = status == ProductStatus.ON_SALE,
                        onCheckedChange = { checked ->
                            status = if (checked) ProductStatus.ON_SALE else ProductStatus.SOLD_OUT
                        }
                    )
                }
            }

            Column {
                Text("分类")
                val categoryLabel = state.categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "未分类"
                TextButton(onClick = { categoryMenuExpanded = true }) {
                    Text(categoryLabel)
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("未分类") },
                        onClick = {
                            selectedCategoryId = null
                            categoryMenuExpanded = false
                        }
                    )
                    state.categories.forEach { category ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Text("商品图片")
            ProductImage(
                imagePath = imagePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    pickMediaLauncher.launch(
                        ActivityResultContracts.PickVisualMedia.Request(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }) {
                    Text("从相册选择")
                }
                Button(onClick = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            val uri = imageStorage.createCameraUri()
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        }

                        else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("拍照")
                }
                TextButton(onClick = { imagePath = null }) {
                    Text("移除")
                }
            }

            if (!error.isNullOrBlank()) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
