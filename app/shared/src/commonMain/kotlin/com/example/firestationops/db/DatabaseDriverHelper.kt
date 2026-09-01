package com.example.firestationops.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Driver helper to initialize the SQLDelight SqlDriver and instantiate FirestationOpsDb / FirestationOpsDatabase
 * consistently across platforms (Android and Desktop/JVM).
 */
class DatabaseDriverHelper(
    private val driverFactory: DatabaseDriverFactory
) {
    /**
     * Creates and initializes the underlying SqlDriver for the current platform.
     */
    fun createDriver(): SqlDriver {
        return driverFactory.createDriver()
    }

    /**
     * Initializes a new FirestationOpsDb SQLDelight instance.
     */
    fun createSqlDelightDb(): FirestationOpsDb {
        return FirestationOpsDb(createDriver())
    }

    /**
     * Initializes and returns the domain-facing FirestationOpsDatabase wrapper.
     */
    fun createDatabase(): FirestationOpsDatabase {
        val driver = createDriver()
        return FirestationOpsDatabase(driver)
    }
}
