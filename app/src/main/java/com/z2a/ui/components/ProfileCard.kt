package com.z2a.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.z2a.data.models.Profile
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aGreenSurface
import com.z2a.ui.theme.Z2aOnSurface
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aSurfaceVariant
import com.z2a.ui.theme.Z2aOutline

@Composable
fun ProfileCard(
    profile: Profile,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.enabled) Z2aSurfaceVariant else Z2aSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (profile.enabled) Z2aOnSurface else Z2aOnSurfaceDim
                )
                Text(
                    text = "${profile.domainCount} доменов  |  ${profile.strategyCount} стратегий",
                    style = MaterialTheme.typography.bodySmall,
                    color = Z2aOnSurfaceDim,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "${profile.protocol.name} : ${profile.ports}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Z2aOnSurfaceDim.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (profile.enabled && profile.currentStrategy > 0) {
                    Text(
                        text = "Стратегия #${profile.currentStrategy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Z2aGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Switch(
                checked = profile.enabled,
                onCheckedChange = { onToggle(profile.id) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Z2aGreen,
                    checkedTrackColor = Z2aGreenSurface,
                    uncheckedThumbColor = Z2aOnSurfaceDim,
                    uncheckedTrackColor = Z2aOutline
                )
            )
        }
    }
}
