package com.example.firestationops.domain.equipment

import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EquipmentTagMatcherTest {

    private val sampleList = listOf(
        Equipment(
            id = "eq_scba_101",
            departmentId = "dept_1",
            name = "Scott SCBA Pack #101",
            category = EquipmentCategory.SCBA,
            serialNumber = "SN-MSA-9921",
            barcode = "BC-SCBA-101",
            status = EquipmentStatus.IN_SERVICE
        ),
        Equipment(
            id = "eq_halligan_02",
            departmentId = "dept_1",
            name = "Pro-Bar 30\" Halligan Tool",
            category = EquipmentCategory.HAND_TOOL,
            serialNumber = "PB-30-002",
            barcode = "10029384",
            status = EquipmentStatus.IN_SERVICE
        ),
        Equipment(
            id = "eq_tic_01",
            departmentId = "dept_1",
            name = "FLIR K55 Thermal Imager",
            category = EquipmentCategory.THERMAL_IMAGING,
            serialNumber = "FLIR-55-901",
            barcode = "BC-TIC-01",
            status = EquipmentStatus.MAINTENANCE_REQUIRED
        )
    )

    @Test
    fun matchByExactId() {
        val result = EquipmentTagMatcher.matchEquipmentByTag("eq_scba_101", sampleList)
        assertNotNull(result)
        assertEquals("Scott SCBA Pack #101", result.name)
    }

    @Test
    fun matchByBarcode() {
        val result = EquipmentTagMatcher.matchEquipmentByTag("BC-SCBA-101", sampleList)
        assertNotNull(result)
        assertEquals("eq_scba_101", result.id)

        val resultNumeric = EquipmentTagMatcher.matchEquipmentByTag("10029384", sampleList)
        assertNotNull(resultNumeric)
        assertEquals("eq_halligan_02", resultNumeric.id)
    }

    @Test
    fun matchBySerialNumber() {
        val result = EquipmentTagMatcher.matchEquipmentByTag("SN-MSA-9921", sampleList)
        assertNotNull(result)
        assertEquals("eq_scba_101", result.id)
    }

    @Test
    fun matchByFormattedQrPayloads() {
        val uriResult = EquipmentTagMatcher.matchEquipmentByTag("firestationops://equipment/eq_scba_101", sampleList)
        assertNotNull(uriResult)
        assertEquals("eq_scba_101", uriResult.id)

        val colonResult = EquipmentTagMatcher.matchEquipmentByTag("fireops:equipment:eq_tic_01", sampleList)
        assertNotNull(colonResult)
        assertEquals("eq_tic_01", colonResult.id)

        val eqColonResult = EquipmentTagMatcher.matchEquipmentByTag("eq:eq_halligan_02", sampleList)
        assertNotNull(eqColonResult)
        assertEquals("eq_halligan_02", eqColonResult.id)

        val webUrlResult = EquipmentTagMatcher.matchEquipmentByTag("https://firestationops.app/equipment/eq_scba_101", sampleList)
        assertNotNull(webUrlResult)
        assertEquals("eq_scba_101", webUrlResult.id)
    }

    @Test
    fun returnsNullForNonMatchingTag() {
        val result = EquipmentTagMatcher.matchEquipmentByTag("UNKNOWN-TAG-9999", sampleList)
        assertNull(result)

        val blankResult = EquipmentTagMatcher.matchEquipmentByTag("   ", sampleList)
        assertNull(blankResult)
    }

    @Test
    fun generateEquipmentQrData_createsValidPayload() {
        val eq = sampleList.first()
        val qrData = EquipmentTagMatcher.generateEquipmentQrData(eq)
        assertEquals("fireops:equipment:eq_scba_101", qrData)

        // Verify roundtrip match
        val matched = EquipmentTagMatcher.matchEquipmentByTag(qrData, sampleList)
        assertNotNull(matched)
        assertEquals(eq.id, matched.id)
    }
}
