package com.example.firestationops.domain.export

import com.example.firestationops.domain.model.DepartmentOperationsStore
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersonnelCsvExporterTest {

    private val sampleFirefighters = listOf(
        Firefighter(
            id = "ff_01",
            departmentId = "dept_calhoun",
            stationId = "sta_01",
            badgeNumber = "101",
            firstName = "John",
            lastName = "Gage",
            rank = "Captain",
            email = "jgage@calhounvfd.org",
            phone = "555-0201",
            certifications = listOf("FF2", "Paramedic", "Hazmat Ops"),
            isOfficer = true,
            isActive = true,
            status = PersonnelStatus.AVAILABLE
        ),
        Firefighter(
            id = "ff_02",
            departmentId = "dept_calhoun",
            stationId = "sta_01",
            badgeNumber = "102",
            firstName = "Roy",
            lastName = "DeSoto",
            rank = "Lieutenant",
            email = "rdesoto@calhounvfd.org",
            phone = "555-0202",
            certifications = listOf("FF2", "Paramedic", "Driver/Operator"),
            isOfficer = true,
            isActive = true,
            status = PersonnelStatus.RESPONDING
        ),
        Firefighter(
            id = "ff_03",
            departmentId = "dept_calhoun",
            stationId = "sta_02",
            badgeNumber = "103",
            firstName = "Chet",
            lastName = "Kelly",
            rank = "Firefighter",
            email = "ckelly@calhounvfd.org",
            phone = "555-0203",
            certifications = listOf("FF2", "EMT-B"),
            isOfficer = false,
            isActive = true,
            status = PersonnelStatus.TRAINING
        )
    )

    @Test
    fun export_generatesValidCsvHeaderAndRows() {
        val csv = PersonnelCsvExporter.export(
            firefighters = sampleFirefighters,
            departmentName = "Calhoun VFD",
            includeMetadataHeader = true
        )

        assertContains(csv, "Report Type,Firefighter Personnel Roster")
        assertContains(csv, "Department,Calhoun VFD")
        assertContains(csv, "Total Firefighters,3")
        assertContains(csv, "Badge #,First Name,Last Name,Full Name,Rank,Status,Readiness,Officer,Active,Certifications,Station ID,Email,Phone")

        // First firefighter (Captain John Gage - Available)
        assertContains(csv, "101,John,Gage,John Gage,Captain,Available / On Call,Ready for Duty,Yes,Yes,FF2; Paramedic; Hazmat Ops,sta_01,jgage@calhounvfd.org,555-0201")

        // Second firefighter (Lt. Roy DeSoto - Responding / Active)
        assertContains(csv, "102,Roy,DeSoto,Roy DeSoto,Lieutenant,Responding,Active / Engaged,Yes,Yes,FF2; Paramedic; Driver/Operator,sta_01,rdesoto@calhounvfd.org,555-0202")

        // Third firefighter (Chet Kelly - In Training)
        assertContains(csv, "103,Chet,Kelly,Chet Kelly,Firefighter,In Training,Active / Engaged,No,Yes,FF2; EMT-B,sta_02,ckelly@calhounvfd.org,555-0203")
    }

    @Test
    fun export_handlesSpecialCharactersAndQuotes() {
        val ff = Firefighter(
            id = "ff_special",
            departmentId = "dept_1",
            badgeNumber = "999",
            firstName = "Sam \"Chief\"",
            lastName = "O'Connor, Jr.",
            rank = "Safety Officer, Lead",
            certifications = listOf("FF1", "Instructor, Level 2"),
            status = PersonnelStatus.AVAILABLE
        )

        val csv = PersonnelCsvExporter.export(listOf(ff))

        // Commas and quotes must be properly escaped
        assertContains(csv, "\"Sam \"\"Chief\"\"\"")
        assertContains(csv, "\"O'Connor, Jr.\"")
        assertContains(csv, "\"Safety Officer, Lead\"")
        assertContains(csv, "\"FF1; Instructor, Level 2\"")
    }

    @Test
    fun escapeCsv_formatsProperly() {
        assertEquals("normalText", PersonnelCsvExporter.escapeCsv("normalText"))
        assertEquals("\"text, with comma\"", PersonnelCsvExporter.escapeCsv("text, with comma"))
        assertEquals("\"text with \"\"quotes\"\"\"", PersonnelCsvExporter.escapeCsv("text with \"quotes\""))
        assertEquals("\"multi\nline\"", PersonnelCsvExporter.escapeCsv("multi\nline"))
    }

    @Test
    fun departmentOperationsStore_exportRosterCsv() {
        val store = DepartmentOperationsStore("dept_calhoun")
        val csv = store.exportRosterCsv("Calhoun Fire Department")

        assertTrue(csv.isNotBlank())
        assertContains(csv, "Badge #,First Name,Last Name")
        assertContains(csv, "Gage")
        assertContains(csv, "DeSoto")
    }
}
