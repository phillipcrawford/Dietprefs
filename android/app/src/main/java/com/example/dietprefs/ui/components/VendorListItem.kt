package com.example.dietprefs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.upvoteGreen
import com.example.dietprefs.viewmodel.DisplayVendor

/**
 * List item component for displaying vendor information in search results.
 */
@Composable
fun VendorListItem(
    vendor: DisplayVendor,
    isTwoUserMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        // Vendor Name and Rating with visual rating indicator
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .drawBehind {
                    // Calculate the split point based on rating ratio
                    val ratingRatio = vendor.querySpecificRatingValue.coerceIn(0f, 1f)
                    val greenWidth = size.width * ratingRatio

                    // Draw green section (upvotes)
                    drawRect(
                        color = upvoteGreen,
                        topLeft = Offset.Zero,
                        size = Size(greenWidth, size.height)
                    )

                    // Draw grey section (remaining votes)
                    drawRect(
                        color = dietprefsGrey,
                        topLeft = Offset(greenWidth, 0f),
                        size = Size(size.width - greenWidth, size.height)
                    )
                }
        ) {
            // Vendor Name - Aligned to TopStart
            Text(
                text = vendor.vendorName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 7.dp, bottom = 10.dp)
            )

            // Rating String - Aligned to TopEnd
            Text(
                text = vendor.querySpecificRatingString,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 15.dp, bottom = 10.dp)
            )
        }

        // Distance
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(dietprefsGrey),
            contentAlignment = Alignment.Center
        ) {
            Text(
                String.format("%.1f mi", vendor.distanceMiles),
                fontSize = 14.sp,
                color = Color.White
            )
        }

        // Menu Item Counts (adapts to user mode)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(dietprefsGrey),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isTwoUserMode) {
                Text(
                    text = "${vendor.user1Count} | ${vendor.user2Count}",
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "${vendor.combinedRelevantItemCount}",
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
