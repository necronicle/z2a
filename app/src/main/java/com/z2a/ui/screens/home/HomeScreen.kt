package com.z2a.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.z2a.data.models.ConnectionState
import com.z2a.ui.components.ConnectButton
import com.z2a.ui.components.StatusCard
import com.z2a.ui.theme.Z2aAmber
import com.z2a.ui.theme.Z2aBlue
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aRed
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onToggleVpn: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val connectedSince by viewModel.connectedSince.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Session timer
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(connectedSince) {
        val since = connectedSince
        if (since != null) {
            while (true) {
                elapsed = System.currentTimeMillis() - since
                delay(1000)
            }
        } else {
            elapsed = 0L
        }
    }

    val statusText = when (connectionState) {
        ConnectionState.DISCONNECTED -> "Защита отключена"
        ConnectionState.CONNECTING -> "Подключение..."
        ConnectionState.CONNECTED -> "Защита активна"
        ConnectionState.ERROR -> errorMessage ?: "Ошибка"
    }

    val statusColor by animateColorAsState(
        targetValue = when (connectionState) {
            ConnectionState.CONNECTED -> Z2aGreen
            ConnectionState.CONNECTING -> Z2aAmber
            ConnectionState.ERROR -> Z2aRed
            ConnectionState.DISCONNECTED -> Z2aOnSurfaceDim
        },
        animationSpec = tween(500),
        label = "statusColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "z2a",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            color = statusColor,
            textAlign = TextAlign.Center
        )

        if (connectionState == ConnectionState.CONNECTED && elapsed > 0) {
            Text(
                text = formatDuration(elapsed),
                style = MaterialTheme.typography.bodyMedium,
                color = Z2aOnSurfaceDim,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ConnectButton(
            state = connectionState,
            onClick = onToggleVpn
        )

        Spacer(modifier = Modifier.weight(1f))

        // Status cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = "Профили",
                value = "${viewModel.getActiveProfileCount()} активных",
                accent = Z2aBlue,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "Стратегии",
                value = "${viewModel.getActiveStrategyCount()}",
                accent = Z2aGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatusCard(
            title = "Автоциркуляр",
            value = "${viewModel.getAutocircularEntryCount()} записей",
            accent = Z2aAmber
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
