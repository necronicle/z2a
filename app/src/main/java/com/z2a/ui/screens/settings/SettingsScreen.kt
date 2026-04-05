package com.z2a.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.z2a.ui.theme.Z2aAmber
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aGreenSurface
import com.z2a.ui.theme.Z2aOnSurface
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aOutline
import com.z2a.ui.theme.Z2aRed
import com.z2a.ui.theme.Z2aSurfaceVariant

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val autostart by viewModel.autostart.collectAsState(initial = false)
    val silentFallback by viewModel.silentFallback.collectAsState(initial = false)
    val rstFilter by viewModel.rstFilter.collectAsState(initial = true)
    val austerusMode by viewModel.austerusMode.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // General section
        SettingsSection(title = "Общие") {
            SettingsToggle(
                title = "Автозапуск",
                subtitle = "Запускать защиту при загрузке устройства",
                checked = autostart,
                onCheckedChange = { viewModel.setAutostart(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Engine section
        SettingsSection(title = "Движок") {
            SettingsToggle(
                title = "RST-фильтр",
                subtitle = "Блокировать поддельные RST-пакеты от DPI",
                checked = rstFilter,
                onCheckedChange = { viewModel.setRstFilter(it) }
            )
            HorizontalDivider(color = Z2aOutline, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggle(
                title = "Silent Fallback",
                subtitle = "Ротация стратегий при тихих блокировках (без ответа от DPI)",
                checked = silentFallback,
                onCheckedChange = { viewModel.setSilentFallback(it) },
                accentColor = Z2aAmber
            )
            HorizontalDivider(color = Z2aOutline, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggle(
                title = "Режим Austerus",
                subtitle = "Обрабатывать весь TCP/443 трафик (не рекомендуется)",
                checked = austerusMode,
                onCheckedChange = { viewModel.setAusterusMode(it) },
                accentColor = Z2aRed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data section
        SettingsSection(title = "Данные") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Автоциркуляр",
                        style = MaterialTheme.typography.titleMedium,
                        color = Z2aOnSurface
                    )
                    Text(
                        text = "Очистить сохранённые стратегии per-domain",
                        style = MaterialTheme.typography.bodySmall,
                        color = Z2aOnSurfaceDim
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.clearAutocircularState() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Очистить", color = Z2aRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About section
        SettingsSection(title = "О приложении") {
            SettingsInfo("Версия", "1.0.0")
            HorizontalDivider(color = Z2aOutline, modifier = Modifier.padding(vertical = 8.dp))
            SettingsInfo("Движок", "nfqws2 + Lua autocircular")
            HorizontalDivider(color = Z2aOutline, modifier = Modifier.padding(vertical = 8.dp))
            SettingsInfo("GitHub", "github.com/necronicle/z2a")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Z2aOnSurfaceDim,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Z2aSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color = Z2aGreen
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Z2aOnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Z2aOnSurfaceDim,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = Z2aGreenSurface,
                uncheckedThumbColor = Z2aOnSurfaceDim,
                uncheckedTrackColor = Z2aOutline
            )
        )
    }
}

@Composable
private fun SettingsInfo(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Z2aOnSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Z2aOnSurfaceDim
        )
    }
}
