package com.z2a.ui.screens.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.z2a.ui.components.LogEntryItem
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aSurfaceVariant

@Composable
fun LogsScreen(
    viewModel: LogsViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val filterProfile by viewModel.filterProfile.collectAsState()

    val profileFilters = listOf(
        null to "Все",
        "rkn_tcp" to "RKN",
        "yt_tcp" to "YouTube",
        "yt_quic" to "QUIC",
        "discord_udp" to "Discord"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Логи",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.clearLogs() }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Очистить",
                    tint = Z2aOnSurfaceDim
                )
            }
        }

        Text(
            text = "Автоциркуляр — ротация стратегий",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(profileFilters) { (id, label) ->
                FilterChip(
                    selected = filterProfile == id,
                    onClick = { viewModel.setFilter(id) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Z2aGreen.copy(alpha = 0.15f),
                        selectedLabelColor = Z2aGreen,
                        containerColor = Z2aSurfaceVariant,
                        labelColor = Z2aOnSurfaceDim
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Нет записей",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Z2aOnSurfaceDim
                )
                Text(
                    text = "Логи появятся при активном подключении",
                    style = MaterialTheme.typography.bodySmall,
                    color = Z2aOnSurfaceDim.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            val filteredLogs = viewModel.getFilteredLogs()
            LazyColumn {
                items(filteredLogs, key = { "${it.timestamp}_${it.domain}" }) { entry ->
                    LogEntryItem(entry = entry)
                }
            }
        }
    }
}
