package com.seretail.inventarios.util

object RbacHelper {
    // Role IDs matching server
    const val SUPER_ADMIN = 1
    const val SUPERVISOR = 2
    const val CAPTURISTA = 3
    const val SUPERVISOR_INVITADO = 4

    // Bottom bar tabs each role can see
    private val tabAccess = mapOf(
        SUPER_ADMIN to setOf("dashboard", "inventario_list", "activofijo_list", "rfid_capture", "settings"),
        SUPERVISOR to setOf("dashboard", "inventario_list", "activofijo_list", "rfid_capture", "settings"),
        CAPTURISTA to setOf("dashboard", "inventario_list", "activofijo_list", "rfid_capture"),
        SUPERVISOR_INVITADO to setOf("dashboard", "activofijo_list", "rfid_capture"),
    )

    fun allowedTabs(rolId: Int): Set<String> =
        tabAccess[rolId] ?: tabAccess[CAPTURISTA]!!

    fun canCreateSession(rolId: Int): Boolean =
        rolId in setOf(SUPER_ADMIN, SUPERVISOR)

    fun canAccessReports(rolId: Int): Boolean =
        rolId in setOf(SUPER_ADMIN, SUPERVISOR)

    fun canManageCatalog(rolId: Int): Boolean =
        rolId == SUPER_ADMIN

    fun canImportCatalog(rolId: Int): Boolean =
        rolId == SUPER_ADMIN

    fun canCompareSession(rolId: Int): Boolean =
        rolId in setOf(SUPER_ADMIN, SUPERVISOR)
}
