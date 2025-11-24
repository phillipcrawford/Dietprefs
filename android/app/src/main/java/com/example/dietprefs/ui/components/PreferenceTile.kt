package com.example.dietprefs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietprefs.model.Preference
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.dietprefsTeal
import com.example.dietprefs.ui.theme.selectedGrey
import com.example.dietprefs.ui.theme.selectedTeal

/**
 * Individual preference tile component for selection grid.
 * Note: This composable is designed to be used within a Row.
 */
@Composable
fun PreferenceTile(
    preference: Preference,
    isSelected: Boolean,
    isTealRow: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isTealRow && isSelected -> selectedTeal
        isTealRow && !isSelected -> dietprefsTeal
        isSelected -> selectedGrey
        else -> dietprefsGrey
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(start = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = preference.display,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
