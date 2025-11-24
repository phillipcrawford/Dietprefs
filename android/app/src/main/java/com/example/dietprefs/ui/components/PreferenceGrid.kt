package com.example.dietprefs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dietprefs.model.Preference

/**
 * Grid display for dietary preference selection (16 rows x 2 columns).
 * First 8 rows use grey theme, last 8 rows use teal theme.
 */
@Composable
fun PreferenceGrid(
    preferences: List<Preference> = Preference.orderedForUI.take(32),
    user1Prefs: Set<Preference>,
    user2Prefs: Set<Preference>,
    isUser2Active: Boolean,
    onTogglePref: (Preference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val rows = preferences.chunked(2).take(16)

        rows.forEachIndexed { rowIndex, rowPrefs ->
            val isTealRow = rowIndex >= 8
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                rowPrefs.forEach { pref ->
                    val isSelected = if (isUser2Active)
                        user2Prefs.contains(pref)
                    else
                        user1Prefs.contains(pref)

                    PreferenceTile(
                        preference = pref,
                        isSelected = isSelected,
                        isTealRow = isTealRow,
                        onClick = { onTogglePref(pref) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add spacer if row only has one preference
                if (rowPrefs.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
