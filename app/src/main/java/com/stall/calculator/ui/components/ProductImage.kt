package com.stall.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stall.calculator.R

@Composable
fun ProductImage(
    imagePath: String?,
    modifier: Modifier = Modifier
) {
    if (imagePath.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        AsyncImage(
            model = imagePath,
            contentDescription = null,
            modifier = modifier.clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_product_placeholder),
            placeholder = painterResource(id = R.drawable.ic_product_placeholder)
        )
    }
}
