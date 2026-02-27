package com.stall.calculator.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun QuantityInputDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    title: String = "修改数量"
) {
    val inputState = remember { mutableStateOf(initialValue.toString()) }
    val errorState = remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = inputState.value,
                onValueChange = { next ->
                    if (next.isEmpty() || next.all { it.isDigit() }) {
                        inputState.value = next
                        errorState.value = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                label = { Text("数量") },
                isError = !errorState.value.isNullOrBlank(),
                supportingText = {
                    if (!errorState.value.isNullOrBlank()) {
                        Text(errorState.value!!)
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = inputState.value.toIntOrNull()
                    if (parsed == null || parsed < 0) {
                        errorState.value = "请输入大于等于 0 的整数"
                    } else {
                        onConfirm(parsed)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
