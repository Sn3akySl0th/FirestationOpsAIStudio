package com.example.firestationops.db

import com.example.firestationops.di.SharedContainer
import kotlin.test.Test
import kotlin.test.assertNotNull

class DatabaseDriverHelperTest {

    @Test
    fun testDatabaseDriverHelperInitializesDb() {
        val helper = DatabaseDriverHelper(DatabaseDriverFactory())
        val driver = helper.createDriver()
        assertNotNull(driver)

        val db = helper.createSqlDelightDb()
        assertNotNull(db)
        assertNotNull(db.firestationOpsQueries)

        val firestationOpsDatabase = helper.createDatabase()
        assertNotNull(firestationOpsDatabase)
    }

    @Test
    fun testSharedContainerExposesSqlDelightAndRepositories() {
        val container = SharedContainer(DatabaseDriverFactory())
        assertNotNull(container.databaseDriverHelper)
        assertNotNull(container.sqlDriver)
        assertNotNull(container.rawDb)
        assertNotNull(container.database)
        assertNotNull(container.apparatusRepository)
        assertNotNull(container.inspectionRepository)
        assertNotNull(container.deficiencyRepository)
        assertNotNull(container.attachmentRepository)
        assertNotNull(container.incidentRepository)
        assertNotNull(container.departmentRepository)
        assertNotNull(container.localAuthRepository)
    }
}

