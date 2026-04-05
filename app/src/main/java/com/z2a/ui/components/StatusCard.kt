package com.z2a.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.z2a.ui.theme.Z2aSurfaceVariant
import com.z2a.ui.theme.Z2aOnSurface
import com.z2a.ui.theme.Z2aOnSurfaceDim

@Composable
fun StatusCard(
    title: String,
    value: String,
    accent: Color = Z2aOnSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Z2aSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Z2aOnSurfaceDim
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
