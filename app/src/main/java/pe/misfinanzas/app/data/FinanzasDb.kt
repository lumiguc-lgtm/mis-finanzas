package pe.misfinanzas.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Base de datos local (SQLite). Todo queda guardado en el teléfono. */
class FinanzasDb(context: Context) : SQLiteOpenHelper(context, "finanzas.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE movimientos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,
                monto REAL NOT NULL,
                categoria TEXT NOT NULL,
                fecha TEXT NOT NULL,
                detalle TEXT NOT NULL DEFAULT '',
                creado INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_mov_fecha ON movimientos(fecha)")
        db.execSQL("CREATE TABLE presupuestos (categoria TEXT PRIMARY KEY, monto REAL NOT NULL)")
        Categorias.presupuestoInicial.forEach { (cat, monto) ->
            db.insert("presupuestos", null, ContentValues().apply {
                put("categoria", cat); put("monto", monto)
            })
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versión 1: sin migraciones todavía.
    }
}
