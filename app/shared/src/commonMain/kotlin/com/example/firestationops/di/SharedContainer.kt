package com.example.firestationops.di

import com.example.firestationops.db.DatabaseDriverFactory
import com.example.firestationops.db.DatabaseDriverHelper
import com.example.firestationops.db.FirestationOpsDatabase
import com.example.firestationops.db.FirestationOpsDb
import com.example.firestationops.domain.repository.ApparatusRepository
import com.example.firestationops.domain.repository.AttachmentRepository
import com.example.firestationops.domain.repository.AuthRepository
import com.example.firestationops.domain.repository.CatalogAdminRepository
import com.example.firestationops.domain.repository.CatalogRepository
import com.example.firestationops.domain.repository.DeficiencyRepository
import com.example.firestationops.domain.repository.DepartmentRepository
import com.example.firestationops.domain.repository.IncidentRepository
import com.example.firestationops.domain.repository.InspectionRepository
import com.example.firestationops.domain.repository.MemberRosterRepository
import com.example.firestationops.domain.repository.SyncConflictRepository
import com.example.firestationops.domain.repository.persistent.PersistentApparatusRepository
import com.example.firestationops.domain.repository.persistent.PersistentAttachmentRepository
import com.example.firestationops.domain.repository.persistent.PersistentAuthRepository
import com.example.firestationops.domain.repository.persistent.PersistentCatalogAdminRepository
import com.example.firestationops.domain.repository.persistent.PersistentCatalogRepository
import com.example.firestationops.domain.repository.persistent.PersistentDeficiencyRepository
import com.example.firestationops.domain.repository.persistent.PersistentDepartmentRepository
import com.example.firestationops.domain.repository.persistent.PersistentIncidentRepository
import com.example.firestationops.domain.repository.persistent.PersistentInspectionRepository
import com.example.firestationops.domain.repository.persistent.PersistentMemberRosterRepository
import com.example.firestationops.domain.repository.persistent.PersistentSyncConflictRepository

/**
 * Dependency injection container for shared database, drivers, and local persistent repositories.
 * Compatible with Android and Desktop targets.
 */
class SharedContainer(
    val driverFactory: DatabaseDriverFactory
) {
    val databaseDriverHelper: DatabaseDriverHelper = DatabaseDriverHelper(driverFactory)

    val sqlDriver by lazy { databaseDriverHelper.createDriver() }

    val rawDb: FirestationOpsDb by lazy { FirestationOpsDb(sqlDriver) }

    val database: FirestationOpsDatabase by lazy { FirestationOpsDatabase(sqlDriver) }

    // Persistent Repositories
    val apparatusRepository: ApparatusRepository by lazy { PersistentApparatusRepository(database) }
    val inspectionRepository: InspectionRepository by lazy { PersistentInspectionRepository(database) }
    val deficiencyRepository: DeficiencyRepository by lazy { PersistentDeficiencyRepository(database) }
    val attachmentRepository: AttachmentRepository by lazy { PersistentAttachmentRepository(database) }
    val incidentRepository: IncidentRepository by lazy { PersistentIncidentRepository(database) }
    val departmentRepository: DepartmentRepository by lazy { PersistentDepartmentRepository(database) }
    val localAuthRepository: PersistentAuthRepository by lazy { PersistentAuthRepository(database) }
    val localMemberRosterRepository: MemberRosterRepository by lazy { PersistentMemberRosterRepository(database) }
    val syncConflictRepository: SyncConflictRepository by lazy { PersistentSyncConflictRepository(database) }

    val catalogRepository: CatalogRepository by lazy {
        PersistentCatalogRepository(database) {
            (apparatusRepository as? PersistentApparatusRepository)?.refreshCatalog()
            (inspectionRepository as? PersistentInspectionRepository)?.refreshCatalog()
        }
    }

    val localCatalogAdminRepository: CatalogAdminRepository by lazy {
        PersistentCatalogAdminRepository(database) {
            (apparatusRepository as? PersistentApparatusRepository)?.refreshCatalog()
            (inspectionRepository as? PersistentInspectionRepository)?.refreshCatalog()
        }
    }
}
