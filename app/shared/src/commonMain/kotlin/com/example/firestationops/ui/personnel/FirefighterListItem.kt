package com.example.firestationops.ui.personnel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.Firefighter

/**
 * List item card displaying a single firefighter's profile according to the data schema:
 * - Full name
 * - Role / rank and badge number
 * - Officer status
 * - Current operational status
 * - Certifications and contact details
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirefighterListItem(
    firefighter: Firefighter,
    onItemClick: () -> Unit,
    onChangeStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("firefighter_card_${firefighter.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Avatar Initials, Name & Role, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Initials Avatar
                    val initials = buildString {
                        if (firefighter.firstName.isNotBlank()) append(firefighter.firstName.first())
                        if (firefighter.lastName.isNotBlank()) append(firefighter.lastName.first())
                    }.ifEmpty { "FF" }

                    Surface(
                        shape = CircleShape,
                        color = if (firefighter.isOfficer) Color(0xFFB71C1C) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = firefighter.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("firefighter_name_${firefighter.id}")
                        )

                        // Role / Rank and Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val roleText = firefighter.rank ?: if (firefighter.isOfficer) "Officer" else "Firefighter"
                            Text(
                                text = roleText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("firefighter_role_${firefighter.id}")
                            )

                            if (firefighter.isOfficer) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFEBEE)
                                ) {
                                    Text(
                                        text = "OFFICER",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB71C1C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            firefighter.badgeNumber?.let { badge ->
                                if (badge.isNotBlank()) {
                                    Text(
                                        text = "• Badge #$badge",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Operational Status Badge
                PersonnelStatusBadge(
                    status = firefighter.status,
                    onClick = onChangeStatusClick,
                    modifier = Modifier.testTag("status_badge_${firefighter.id}")
                )
            }

            // Contact info if available
            val contactInfo = buildList {
                firefighter.phone?.takeIf { it.isNotBlank() }?.let { add("📞 $it") }
                firefighter.email?.takeIf { it.isNotBlank() }?.let { add("✉️ $it") }
            }.joinToString("   ")

            if (contactInfo.isNotEmpty()) {
                Text(
                    text = contactInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Certifications tags
            if (firefighter.certifications.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    firefighter.certifications.forEach { cert ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = cert,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Readiness indicator label
                Text(
                    text = if (firefighter.isReadyToRespond) "Ready for dispatch" else "Not responding",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (firefighter.isReadyToRespond) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onChangeStatusClick,
                    modifier = Modifier.testTag("change_status_btn_${firefighter.id}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Change Status", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
