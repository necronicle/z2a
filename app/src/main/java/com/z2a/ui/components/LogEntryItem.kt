package com.z2a.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.z2a.data.models.LogEntry
import com.z2a.data.models.LogEvent
import com.z2a.ui.theme.Z2aAmber
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogEntryItem(
    entry: LogEntry,
    modifier: Modifier = Modifier
) {
    val dotColor = when (entry.event) {
        LogEvent.CONNECTION_OK, LogEvent.STRATEGY_APPLIED -> Z2aGreen
        LogEvent.STRATEGY_ROTATED, LogEvent.SILENT_FALLBACK -> Z2aAmber
        LogEvent.STRATEGY_FAILED, LogEvent.CONNECTION_TIMEOUT,
        LogEvent.RST_DETECTED, LogEvent.TLS_ALERT -> Z2aRed
    }

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = dotColor,
            modifier = Modifier
                .size(8.dp)
                .padding(top = 6.dp)
        ) {}

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.domain,
                    style = MaterialTheme.typography.bodyMedium,
                    color = dotColor
                )
                Text(
                    text = "  #${entry.strategyNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Z2aOnSurfaceDim
                )
            }
            Text(
                text = "[${entry.profileId}] ${entry.event.name}",
                style = MaterialTheme.typography.bodySmall,
                color = Z2aOnSurfaceDim
            )
            Text(
                text = timeFormat.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = Z2aOnSurfaceDim.copy(alpha = 0.5f)
            )
        }
    }
}
