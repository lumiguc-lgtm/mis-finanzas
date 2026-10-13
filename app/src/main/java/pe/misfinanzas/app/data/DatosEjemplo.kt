package pe.misfinanzas.app.data

import java.time.LocalDate

/** Seis meses de movimientos ficticios para probar la app. */
object DatosEjemplo {
    fun generar(hoy: LocalDate = LocalDate.now()): List<Movimiento> {
        val lista = mutableListOf<Movimiento>()
        for (atras in 0L..5L) {
            val mes = hoy.withDayOfMonth(1).minusMonths(atras)
            val ultimoDia = if (atras == 0L) hoy.dayOfMonth else mes.lengthOfMonth()
            val k = 1 + (atras % 3) * 0.06
            fun add(dia: Int, tipo: Tipo, cat: String, monto: Double, detalle: String) {
                if (dia <= ultimoDia) lista += Movimiento(
                    tipo = tipo, monto = Math.round(monto * 100) / 100.0, categoria = cat,
                    fecha = mes.withDayOfMonth(dia), detalle = detalle,
                )
            }
            add(1, Tipo.INGRESO, "Sueldo", 4800.0, "Sueldo quincena 1")
            add(15, Tipo.INGRESO, "Sueldo", 4800.0, "Sueldo quincena 2")
            if (atras % 2 == 0L) add(20, Tipo.INGRESO, "Freelance", 900.0, "Consultoría")
            add(2, Tipo.GASTO, "Vivienda", 1500.0, "Alquiler departamento")
            add(5, Tipo.GASTO, "Servicios", 118 * k, "Recibo de luz")
            add(6, Tipo.GASTO, "Servicios", 99.0, "Internet")
            add(3, Tipo.GASTO, "Alimentación", 320 * k, "Mercado semanal")
            add(10, Tipo.GASTO, "Alimentación", 295 * k, "Supermercado")
            add(17, Tipo.GASTO, "Alimentación", 340 * k, "Mercado semanal")
            add(24, Tipo.GASTO, "Alimentación", 260 * k, "Supermercado")
            add(4, Tipo.GASTO, "Transporte", 150 * k, "Recarga tarjeta de transporte")
            add(12, Tipo.GASTO, "Transporte", 110 * k, "Taxi por app")
            add(8, Tipo.GASTO, "Deudas", 520.0, "Cuota tarjeta de crédito")
            add(14, Tipo.GASTO, "Entretenimiento", 180 * k, "Cena y cine")
            add(22, Tipo.GASTO, "Salud", 85 * k, "Farmacia")
            if (atras % 2 == 1L) add(18, Tipo.GASTO, "Ropa", 220.0, "Zapatillas")
            add(9, Tipo.GASTO, "Educación", 280.0, "Curso online")
        }
        return lista
    }
}
