package com.example.firestationops.ui.equipment

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.EquipmentCategory

/**
 * Visual badge displaying the category of a piece of equipment.
 */
@Composable
fun EquipmentCategoryBadge(
    category: EquipmentCategory,
    modifier: Modifier = Modifier
) {
    val formattedName = when (category) {
        EquipmentCategory.SCBA -> "SCBA Air-Pak"
        EquipmentCategory.HOSE -> "Hose / Line"
        EquipmentCategory.HAND_TOOL -> "Hand Tool"
        EquipmentCategory.HYDRAULIC_RESCUE -> "Hydraulic Rescue"
        EquipmentCategory.MEDICAL -> "Medical / EMS"
        EquipmentCategory.RADIO -> "Radio / Comms"
        EquipmentCategory.PPE -> "PPE / Bunker Gear"
        EquipmentCategory.GENERATOR -> "Generator / Power"
        EquipmentCategory.THERMAL_IMAGING -> "Thermal Imaging"
        EquipmentCategory.NOZZLE -> "Nozzle / Appliance"
        EquipmentCategory.LADDER -> "Ladder"
        EquipmentCategory.VENTILATION -> "Ventilation / Fan"
        EquipmentCategory.OTHER -> "General Equipment"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.testTag("eq_category_badge_${category.name}")
    ) {
        Text(
            text = formattedName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
