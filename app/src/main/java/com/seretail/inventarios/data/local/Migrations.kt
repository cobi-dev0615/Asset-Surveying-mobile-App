package com.seretail.inventarios.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations for SER Inventarios.
 *
 * Current version: 6 (baseline — all prior versions used destructive migration).
 * All future schema changes MUST add a migration here and bump the version in AppDatabase.
 *
 * To add a migration:
 * 1. Bump `version` in @Database annotation in AppDatabase.kt
 * 2. Add a Migration(oldVersion, newVersion) object below
 * 3. Add it to the ALL array
 * 4. Write the ALTER TABLE / CREATE TABLE SQL in migrate()
 */
object Migrations {

    /**
     * Baseline schema creation from version 1 to 6.
     * Used when a fresh install encounters a database at version 1
     * (shouldn't happen normally, but provides safety).
     */
    val MIGRATION_1_6 = object : Migration(1, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createAllTables(db)
        }
    }

    // --- Future migrations go here ---
    // Example:
    // val MIGRATION_6_7 = object : Migration(6, 7) {
    //     override fun migrate(db: SupportSQLiteDatabase) {
    //         db.execSQL("ALTER TABLE productos ADD COLUMN nuevo_campo TEXT")
    //     }
    // }

    /** All migrations to register with Room. Add new migrations to this array. */
    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_6,
    )

    /**
     * Creates all 14 tables for the current schema (version 6).
     * Used by baseline migration and can be referenced for future migration validation.
     */
    private fun createAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `users` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `usuario` TEXT NOT NULL,
                `nombres` TEXT NOT NULL,
                `email` TEXT,
                `rol_id` INTEGER NOT NULL,
                `rol_nombre` TEXT,
                `empresa_ids` TEXT NOT NULL DEFAULT '[]',
                `acceso_app` INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `empresas` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `nombre` TEXT NOT NULL,
                `codigo` TEXT NOT NULL,
                `eliminado` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `sucursales` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `empresa_id` INTEGER NOT NULL,
                `nombre` TEXT NOT NULL,
                `codigo` TEXT,
                `direccion` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `productos` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `empresa_id` INTEGER NOT NULL,
                `codigo_barras` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `categoria` TEXT,
                `marca` TEXT,
                `modelo` TEXT,
                `color` TEXT,
                `serie` TEXT,
                `sucursal_id` INTEGER,
                `codigo_2` TEXT,
                `codigo_3` TEXT,
                `precio_venta` REAL,
                `cantidad_teorica` REAL,
                `unidad_medida` TEXT,
                `factor` REAL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `lotes` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `empresa_id` INTEGER NOT NULL,
                `producto_id` INTEGER,
                `codigo_barras` TEXT,
                `lote` TEXT NOT NULL,
                `caducidad` TEXT,
                `existencia` INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `inventarios` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `empresa_id` INTEGER NOT NULL,
                `sucursal_id` INTEGER NOT NULL,
                `nombre` TEXT NOT NULL,
                `tipo` TEXT,
                `estado` TEXT NOT NULL DEFAULT 'activo',
                `fecha_creacion` TEXT,
                `empresa_nombre` TEXT,
                `sucursal_nombre` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `activo_fijo_sessions` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `empresa_id` INTEGER NOT NULL,
                `sucursal_id` INTEGER NOT NULL,
                `nombre` TEXT NOT NULL,
                `estado` TEXT NOT NULL DEFAULT 'activo',
                `fecha_creacion` TEXT,
                `empresa_nombre` TEXT,
                `sucursal_nombre` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `inventario_registros` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `server_id` INTEGER,
                `session_id` INTEGER NOT NULL,
                `codigo_barras` TEXT NOT NULL,
                `descripcion` TEXT,
                `cantidad` INTEGER NOT NULL DEFAULT 1,
                `ubicacion` TEXT,
                `lote` TEXT,
                `caducidad` TEXT,
                `factor` INTEGER,
                `numero_serie` TEXT,
                `sincronizado` INTEGER NOT NULL DEFAULT 0,
                `fecha_captura` TEXT,
                `usuario_id` INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `activo_fijo_registros` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `server_id` INTEGER,
                `session_id` INTEGER NOT NULL,
                `codigo_barras` TEXT NOT NULL,
                `descripcion` TEXT,
                `categoria` TEXT,
                `marca` TEXT,
                `modelo` TEXT,
                `color` TEXT,
                `serie` TEXT,
                `ubicacion` TEXT,
                `comentarios` TEXT,
                `tag_nuevo` TEXT,
                `serie_revisado` TEXT,
                `status_id` INTEGER NOT NULL DEFAULT 1,
                `imagen1` TEXT,
                `imagen2` TEXT,
                `imagen3` TEXT,
                `latitud` REAL,
                `longitud` REAL,
                `sincronizado` INTEGER NOT NULL DEFAULT 0,
                `fecha_captura` TEXT,
                `usuario_id` INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `no_encontrados` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `server_id` INTEGER,
                `session_id` INTEGER NOT NULL,
                `activo_id` TEXT NOT NULL,
                `usuario_id` INTEGER NOT NULL,
                `latitud` REAL,
                `longitud` REAL,
                `sincronizado` INTEGER NOT NULL DEFAULT 0,
                `fecha_captura` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `traspasos` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `server_id` INTEGER,
                `registro_id` INTEGER NOT NULL,
                `sucursal_origen_id` INTEGER NOT NULL,
                `sucursal_destino_id` INTEGER NOT NULL,
                `sincronizado` INTEGER NOT NULL DEFAULT 0,
                `fecha_captura` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `statuses` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `status` TEXT NOT NULL,
                `nombre` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `sync_queue` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `entity_id` INTEGER NOT NULL,
                `payload` TEXT NOT NULL DEFAULT '',
                `status` TEXT NOT NULL DEFAULT 'pending',
                `error_message` TEXT,
                `retry_count` INTEGER NOT NULL DEFAULT 0,
                `created_at` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `rfid_tags` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `epc` TEXT NOT NULL,
                `rssi` INTEGER NOT NULL DEFAULT 0,
                `read_count` INTEGER NOT NULL DEFAULT 1,
                `session_id` INTEGER NOT NULL,
                `timestamp` TEXT NOT NULL,
                `matched` INTEGER NOT NULL DEFAULT 0,
                `matched_registro_id` INTEGER,
                `sincronizado` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}
