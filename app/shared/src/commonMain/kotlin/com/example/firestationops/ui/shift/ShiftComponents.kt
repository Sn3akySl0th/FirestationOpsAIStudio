package com.example.firestationops.ui.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.AvailabilityPattern
import com.example.firestationops.model.ShiftStatus
import com.example.firestationops.model.ShiftType

/**
 * Visual badge for shift types and platoons.
 */
@Composable
fun ShiftTypeBadge(
    shiftType: ShiftType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, borderColor) = when (shiftType) {
        ShiftType.A_SHIFT -> Triple(Color(0xFFE3F2FD), Color(0xFF0D47A1), Color(0xFF90CAF9))
        ShiftType.B_SHIFT -> Triple(Color(0xFFEDE7F6), Color(0xFF4A148C), Color(0xFFCE93D8))
        ShiftType.C_SHIFT -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Color(0xFFA5D6A7))
        ShiftType.D_SHIFT -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Color(0xFFFFCC80))
        ShiftType.DAY_DUTY -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), Color(0xFFFFE082))
        ShiftType.NIGHT_STANDBY -> Triple(Color(0xFFECEFF1), Color(0xFF263238), Color(0xFFB0BEC5))
        ShiftType.WEEKEND_CREW -> Triple(Color(0xFFF3E5F5), Color(0xFF6A1B9A), Color(0xFFE1BEE7))
        ShiftType.VOLUNTEER_ON_CALL -> Triple(Color(0xFFE0F7FA), Color(0xFF006064), Color(0xFF80DEEA))
        ShiftType.TRAINING_DRILL -> Triple(Color(0xFFFBE9E7), Color(0xFFBF360C), Color(0xFFFFAB91))
        ShiftType.CUSTOM -> Triple(Color(0xFFF5F5F5), Color(0xFF424242), Color(0xFFE0E0E0))
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .testTag("shift_type_badge_${shiftType.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = shiftType.label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Visual badge for shift status lifecycle.
 */
@Composable
fun ShiftStatusBadge(
    status: ShiftStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor, borderCol) = when (status) {
        ShiftStatus.ACTIVE -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Color(0xFF81C784))
        ShiftStatus.SCHEDULED -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Color(0xFF90CAF9))
        ShiftStatus.COMPLETED -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), Color(0xFFBDBDBD))
        ShiftStatus.CANCELLED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Color(0xFFEF9A9A))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .testTag("shift_status_badge_${status.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(contentColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Staffing gauge / progress bar indicating current assignments vs minimum staffing.
 */
@Composable
fun StaffingProgressBar(
    assignedCount: Int,
    minimumStaffing: Int,
    modifier: Modifier = Modifier
) {
    val isStaffed = assignedCount >= minimumStaffing
    val shortfall = (minimumStaffing - assignedCount).coerceAtLeast(0)
    val progress = if (minimumStaffing <= 0) 1f else (assignedCount.toFloat() / minimumStaffing).coerceIn(0f, 1f)

    val progressColor = when {
        assignedCount >= minimumStaffing -> Color(0xFF2E7D32) // Green
        assignedCount >= minimumStaffing / 2 -> Color(0xFFF57C00) // Orange
        else -> Color(0xFFD32F2F) // Red
    }

    Column(modifier = modifier.testTag("staffing_progress_bar")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isStaffed) "Staffing: $assignedCount / $minimumStaffing (Met)" else "Staffing: $assignedCount / $minimumStaffing (Need $shortfall FF)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isStaffed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Visual badge for volunteer availability patterns.
 */
@Composable
fun AvailabilityPatternBadge(
    pattern: AvailabilityPattern,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (pattern) {
        AvailabilityPattern.ALWAYS_AVAILABLE -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Color(0xFFA5D6A7))
        AvailabilityPattern.WEEKDAY_EVENINGS -> Triple(Color(0xFFE3F2FD), Color(0xFF0D47A1), Color(0xFF90CAF9))
        AvailabilityPattern.WEEKENDS_ONLY -> Triple(Color(0xFFEDE7F6), Color(0xFF4A148C), Color(0xFFCE93D8))
        AvailabilityPattern.DAYTIME_ONLY -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), Color(0xFFFFE082))
        AvailabilityPattern.NIGHTS_ONLY -> Triple(Color(0xFFECEFF1), Color(0xFF263238), Color(0xFFB0BEC5))
        AvailabilityPattern.SCHEDULED_ROTATION -> Triple(Color(0xFFF3E5F5), Color(0xFF6A1B9A), Color(0xFFE1BEE7))
        AvailabilityPattern.ON_CALL_CUSTOM -> Triple(Color(0xFFE0F7FA), Color(0xFF006064), Color(0xFF80DEEA))
        AvailabilityPattern.UNAVAILABLE -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Color(0xFFEF9A9A))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .testTag("availability_pattern_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = pattern.label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
