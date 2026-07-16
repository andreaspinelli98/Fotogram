package com.example.fotogram.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Button(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(modifier = Modifier.height(40.dp), onClick = onClick) {
        Text(text)
    }
}
