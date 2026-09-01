package com.example.firestationops.ui.equipment

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.EquipmentStatus

/**
 * Visual badge displaying the current operational status of equipment with high-contrast indicator.
 */
@Composable
fun EquipmentStatusBadge(
    status: EquipmentStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, textColor, dotColor) = when (status) {
        EquipmentStatus.IN_SERVICE -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Color(0xFF2E7D32))
        EquipmentStatus.OUT_OF_SERVICE -> Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), Color(0xFFD32F2F))
        EquipmentStatus.MAINTENANCE_REQUIRED -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Color(0xFFEF6C00))
        EquipmentStatus.RESERVE -> Triple(Color(0xFFE3F2FD), Color(0xFF0D47A1), Color(0xFF1976D2))
        EquipmentStatus.RETIRED -> Triple(Color(0xFFEEEEEE), Color(0xFF424242), Color(0xFF757575))
    }

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier.testTag("eq_status_badge_${status.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = dotColor,
                modifier = Modifier.size(8.dp)
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.label,
                color = textColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
