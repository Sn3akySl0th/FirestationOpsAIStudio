package com.example.firestationops.ui.personnel

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
import com.example.firestationops.model.PersonnelStatus

/**
 * Visual badge displaying the current operational status of a firefighter with high-contrast indicator.
 */
@Composable
fun PersonnelStatusBadge(
    status: PersonnelStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, textColor, dotColor) = when (status) {
        PersonnelStatus.AVAILABLE -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Color(0xFF2E7D32))
        PersonnelStatus.RESPONDING -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Color(0xFFEF6C00))
        PersonnelStatus.ON_SCENE -> Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), Color(0xFFD32F2F))
        PersonnelStatus.STATION_STANDBY -> Triple(Color(0xFFE3F2FD), Color(0xFF0D47A1), Color(0xFF1976D2))
        PersonnelStatus.TRAINING -> Triple(Color(0xFFF3E5F5), Color(0xFF4A148C), Color(0xFF7B1FA2))
        PersonnelStatus.UNAVAILABLE -> Triple(Color(0xFFEEEEEE), Color(0xFF424242), Color(0xFF757575))
        PersonnelStatus.LEAVE -> Triple(Color(0xFFECEFF1), Color(0xFF263238), Color(0xFF546E7A))
        PersonnelStatus.RETIRED -> Triple(Color(0xFFE0E0E0), Color(0xFF212121), Color(0xFF424242))
    }

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier.testTag("status_badge_${status.name}")
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
