package dev.allancoding.gospellibrary.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun GospelLibraryTheme(
        content: @Composable () -> Unit
) {
    MaterialTheme(
            content = content
    )
}