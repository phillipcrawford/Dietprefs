package com.example.dietprefs.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Small filter button component for filter selections.
 */
@Composable
fun FilterButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 2.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}
