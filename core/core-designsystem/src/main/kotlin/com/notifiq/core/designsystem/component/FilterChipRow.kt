package com.notifiq.core.designsystem.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun FilterChipRow(
    filters: List<String>,
    selectedFilter: String?,
    counts: Map<String, Int>,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allFilters = listOf("All") + filters

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allFilters.forEach { filter ->
            val isSelected = (filter == "All" && selectedFilter == null) ||
                           (filter == selectedFilter)
            val count = if (filter == "All") {
                counts.values.sum()
            } else {
                counts[filter] ?: 0
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    onFilterSelected(if (filter == "All") null else filter)
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = filter)
                        if (count > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.semantics { }
            )
        }
    }
}