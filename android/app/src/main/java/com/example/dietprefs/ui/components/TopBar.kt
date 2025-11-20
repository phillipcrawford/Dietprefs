package com.example.dietprefs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietprefs.model.Preference
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.user1Red
import com.example.dietprefs.ui.theme.user2Magenta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TopBar(
    user1Prefs: Set<Preference>,
    user2Prefs: Set<Preference>,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isBackEnabled by remember { mutableStateOf(true) }

    val user1Selected = user1Prefs.map { it.display }
    val user2Selected = user2Prefs.map { it.display }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(dietprefsGrey)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Back button (only shown if onBackClick is provided)
        if (onBackClick != null) {
            IconButton(
                onClick = {
                    if (isBackEnabled) {
                        isBackEnabled = false
                        onBackClick()
                        coroutineScope.launch {
                            delay(300)
                            isBackEnabled = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-16).dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Preferences display
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.85f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (user1Selected.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User 1",
                            tint = user1Red
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user1Selected.joinToString(", "),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = user1Red,
                        maxLines = if (user2Selected.isEmpty()) 4 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (user2Selected.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User 2",
                            tint = user2Magenta
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user2Selected.joinToString(", "),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = user2Magenta,
                        maxLines = if (user1Selected.isEmpty()) 4 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
