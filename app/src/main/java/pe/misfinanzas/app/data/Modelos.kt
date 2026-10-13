package pe.misfinanzas.app.data

import java.time.LocalDate

enum class Tipo { INGRESO, GASTO }

data class Movimiento(
    val id: Long = 0,
    val tipo: Tipo,
    val monto: Double,
    val categoria: String,
    val fecha: LocalDate,
    val detalle: String = "",
    val creado: Long = System.currentTimeMillis(),
)

object Categorias {
    val gastos = listOf(
        "Alimentación", "Vivienda", "Servicios", "Transporte", "Salud",
        "Educación", "Entretenimiento", "Ropa", "Deudas", "Otros gastos",
    )
    val ingresos = listOf("Sueldo", "Negocio", "Freelance", "Otros ingresos")

    fun de(tipo: Tipo) = if (tipo == Tipo.GASTO) gastos else ingresos

    val presupuestoInicial = mapOf(
        "Alimentación" to 1200.0, "Vivienda" to 1500.0, "Servicios" to 350.0,
        "Transporte" to 400.0, "Salud" to 200.0, "Educación" to 300.0,
        "Entretenimiento" to 250.0, "Ropa" to 150.0, "Deudas" to 500.0,
        "Otros gastos" to 200.0,
    )
}
