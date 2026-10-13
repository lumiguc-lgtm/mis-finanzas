package pe.misfinanzas.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.time.LocalDate

class Repositorio(context: Context) {
    private val helper = FinanzasDb(context.applicationContext)

    fun movimientos(): List<Movimiento> =
        helper.readableDatabase.rawQuery(
            "SELECT id, tipo, monto, categoria, fecha, detalle, creado FROM movimientos ORDER BY fecha DESC, creado DESC",
            null
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Movimiento(
                            id = c.getLong(0),
                            tipo = Tipo.valueOf(c.getString(1)),
                            monto = c.getDouble(2),
                            categoria = c.getString(3),
                            fecha = LocalDate.parse(c.getString(4)),
                            detalle = c.getString(5),
                            creado = c.getLong(6),
                        )
                    )
                }
            }
        }

    private fun valores(m: Movimiento) = ContentValues().apply {
        put("tipo", m.tipo.name)
        put("monto", m.monto)
        put("categoria", m.categoria)
        put("fecha", m.fecha.toString())
        put("detalle", m.detalle)
        put("creado", m.creado)
    }

    fun guardar(m: Movimiento) {
        val db = helper.writableDatabase
        if (m.id == 0L) db.insert("movimientos", null, valores(m))
        else db.update("movimientos", valores(m), "id = ?", arrayOf(m.id.toString()))
    }

    fun insertarVarios(lista: List<Movimiento>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            lista.forEach { db.insert("movimientos", null, valores(it)) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun eliminar(id: Long) {
        helper.writableDatabase.delete("movimientos", "id = ?", arrayOf(id.toString()))
    }

    fun borrarTodo() {
        helper.writableDatabase.delete("movimientos", null, null)
    }

    fun presupuestos(): Map<String, Double> =
        helper.readableDatabase.rawQuery("SELECT categoria, monto FROM presupuestos", null).use { c ->
            buildMap { while (c.moveToNext()) put(c.getString(0), c.getDouble(1)) }
        }

    fun guardarPresupuesto(categoria: String, monto: Double) {
        helper.writableDatabase.insertWithOnConflict(
            "presupuestos", null,
            ContentValues().apply { put("categoria", categoria); put("monto", monto) },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}
