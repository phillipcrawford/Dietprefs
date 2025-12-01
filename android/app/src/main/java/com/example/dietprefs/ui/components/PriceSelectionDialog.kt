package com.example.dietprefs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.selectedGrey

/**
 * Dialog for selecting maximum price filter.
 */
@Composable
fun PriceSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isUser2Active: Boolean,
    user1MaxPrice: Float?,
    user2MaxPrice: Float?,
    onSetPrice: (Float) -> Unit,
    onClearPrice: () -> Unit,
    priceOptions: List<Float>
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Set Maximum Price",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select maximum price:",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Scrollable list of price options (6 items visible at a time)
                    // Each item is ~44dp (12dp padding top + 16sp text + 12dp padding bottom + 4dp spacing)
                    // 6 items = 264dp (44dp * 6)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(264.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        priceOptions.forEach { price ->
                            val currentMaxPrice = if (isUser2Active) user2MaxPrice else user1MaxPrice
                            val isSelected = currentMaxPrice == price

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) selectedGrey else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        onSetPrice(price)
                                        onDismiss()
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "$${"%.0f".format(price)}",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearPrice()
                        onDismiss()
                    }
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = dietprefsGrey,
            textContentColor = Color.White
        )
    }
}
