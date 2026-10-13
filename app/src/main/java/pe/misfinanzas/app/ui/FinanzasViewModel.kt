package pe.misfinanzas.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.misfinanzas.app.data.DatosEjemplo
import pe.misfinanzas.app.data.Movimiento
import pe.misfinanzas.app.data.Repositorio
import pe.misfinanzas.app.data.Tipo
import java.time.YearMonth

data class EstadoUi(
    val movimientos: List<Movimiento> = emptyList(),
    val presupuestos: Map<String, Double> = emptyMap(),
    val mes: YearMonth = YearMonth.now(),
    val cargado: Boolean = false,
) {
    fun delMes(m: YearMonth = mes) = movimientos.filter { YearMonth.from(it.fecha) == m }
    fun total(tipo: Tipo, m: YearMonth = mes) = delMes(m).filter { it.tipo == tipo }.sumOf { it.monto }
    fun gastoPorCategoria(m: YearMonth = mes): Map<String, Double> =
        delMes(m).filter { it.tipo == Tipo.GASTO }
            .groupBy { it.categoria }
            .mapValues { (_, l) -> l.sumOf { it.monto } }
}

class FinanzasViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(app)
    private val _estado = MutableStateFlow(EstadoUi())
    val estado: StateFlow<EstadoUi> = _estado.asStateFlow()

    init { recargar() }

    private fun recargar() {
        viewModelScope.launch {
            val (movs, pres) = withContext(Dispatchers.IO) { repo.movimientos() to repo.presupuestos() }
            _estado.update { it.copy(movimientos = movs, presupuestos = pres, cargado = true) }
        }
    }

    private fun enSegundoPlano(accion: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { accion() }
            recargar()
        }
    }

    fun cambiarMes(delta: Long) = _estado.update { it.copy(mes = it.mes.plusMonths(delta)) }
    fun irAlMes(m: YearMonth) = _estado.update { it.copy(mes = m) }

    fun guardar(m: Movimiento) {
        irAlMes(YearMonth.from(m.fecha))
        enSegundoPlano { repo.guardar(m) }
    }

    fun eliminar(id: Long) = enSegundoPlano { repo.eliminar(id) }
    fun guardarPresupuesto(cat: String, monto: Double) = enSegundoPlano { repo.guardarPresupuesto(cat, monto) }
    fun cargarEjemplo() = enSegundoPlano { repo.insertarVarios(DatosEjemplo.generar()) }
    fun borrarTodo() = enSegundoPlano { repo.borrarTodo() }

    fun csv(): String {
        val filas = _estado.value.movimientos.sortedBy { it.fecha }.map { m ->
            listOf(
                m.fecha.toString(),
                if (m.tipo == Tipo.INGRESO) "Ingreso" else "Gasto",
                m.categoria,
                m.detalle,
                String.format(java.util.Locale.US, "%.2f", if (m.tipo == Tipo.INGRESO) m.monto else -m.monto),
            ).joinToString(",") { "\"" + it.replace("\"", "\"\"") + "\"" }
        }
        return (listOf("Fecha,Tipo,Categoría,Detalle,Monto") + filas).joinToString("\n")
    }
}
