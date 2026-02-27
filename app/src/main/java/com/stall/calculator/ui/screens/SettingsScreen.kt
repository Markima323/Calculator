package com.stall.calculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stall.calculator.data.model.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onUpdateShowCurrency: (Boolean) -> Unit,
    onUpdateConfirmBeforeComplete: (Boolean) -> Unit,
    onUpdateAllowFreeProduct: (Boolean) -> Unit,
    onUpdateRestoreCart: (Boolean) -> Unit,
    onUpdateShowSoldOut: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            SettingItem(
                title = "显示货币符号",
                desc = "总价和单价显示 ¥",
                checked = settings.showCurrencySymbol,
                onCheckedChange = onUpdateShowCurrency
            )
            SettingItem(
                title = "完成前确认",
                desc = "点击完成时弹出二次确认",
                checked = settings.confirmBeforeComplete,
                onCheckedChange = onUpdateConfirmBeforeComplete
            )
            SettingItem(
                title = "允许 0 元商品",
                desc = "商品可保存为 0.00（赠品）",
                checked = settings.allowFreeProduct,
                onCheckedChange = onUpdateAllowFreeProduct
            )
            SettingItem(
                title = "恢复上次未完成订单",
                desc = "重启 App 后保留购物车",
                checked = settings.restoreCartOnLaunch,
                onCheckedChange = onUpdateRestoreCart
            )
            SettingItem(
                title = "点单页显示已售完",
                desc = "显示并可识别已售完商品",
                checked = settings.showSoldOutOnOrderPage,
                onCheckedChange = onUpdateShowSoldOut
            )
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
