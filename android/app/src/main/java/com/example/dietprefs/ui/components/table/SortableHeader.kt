package com.example.dietprefs.ui.components.table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortDirection
import com.example.dietprefs.model.SortState

/**
 * Sortable table header component with sort direction indicator.
 */
@Composable
fun SortableHeader(
    text: String,
    column: SortColumn,
    currentSortState: SortState,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.Center) Arrangement.Center else Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = textAlign ?: TextAlign.Start
        )
        Spacer(Modifier.width(4.dp))
        if (currentSortState.column == column) {
            Icon(
                imageVector = if (currentSortState.direction == SortDirection.ASCENDING)
                    Icons.Default.ArrowUpward
                else
                    Icons.Default.ArrowDownward,
                contentDescription = "Sort Direction: ${currentSortState.direction}",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else {
            // Transparent spacer to keep alignment consistent
            Spacer(Modifier.size(16.dp))
        }
    }
}
