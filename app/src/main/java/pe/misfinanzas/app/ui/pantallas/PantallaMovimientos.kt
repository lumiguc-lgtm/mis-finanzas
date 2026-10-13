@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pe.misfinanzas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.misfinanzas.app.data.Movimiento
import pe.misfinanzas.app.data.Tipo
import pe.misfinanzas.app.ui.EstadoUi
import pe.misfinanzas.app.ui.EstadoVacio
import pe.misfinanzas.app.ui.FilaMovimiento
import pe.misfinanzas.app.ui.SelectorMes
import pe.misfinanzas.app.ui.fechaDia
import pe.misfinanzas.app.ui.soles
import pe.misfinanzas.app.ui.theme.EstiloMonto

private enum class Filtro(val texto: String) { TODOS("Todos"), GASTOS("Gastos"), INGRESOS("Ingresos") }

@Composable
fun PantallaMovimientos(
    estado: EstadoUi,
    padding: PaddingValues,
    onCambiarMes: (Long) -> Unit,
    onAbrir: (Movimiento) -> Unit,
) {
    var filtro by rememberSaveable { mutableStateOf(Filtro.TODOS) }
    val lista = estado.delMes().filter {
        when (filtro) {
            Filtro.TODOS -> true
            Filtro.GASTOS -> it.tipo == Tipo.GASTO
            Filtro.INGRESOS -> it.tipo == Tipo.INGRESO
        }
    }
    val porDia = lista.groupBy { it.fecha } // ya viene ordenado de más reciente a más antiguo

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SelectorMes(estado.mes, onCambiarMes)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Filtro.entries.forEach { f ->
                        FilterChip(selected = filtro == f, onClick = { filtro = f }, label = { Text(f.texto) })
                    }
                }
                Text(
                    "${lista.size} movimientos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (estado.cargado && lista.isEmpty()) {
            item { EstadoVacio("No hay movimientos", "Registra uno con el botón «Nuevo».") }
        }
        porDia.forEach { (dia, movs) ->
            item(key = "dia-$dia") {
                val neto = movs.sumOf { if (it.tipo == Tipo.INGRESO) it.monto else -it.monto }
                Column {
                    HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(fechaDia(dia), style = MaterialTheme.typography.labelLarge)
                        Text(
                            (if (neto >= 0) "+" else "−") + soles(kotlin.math.abs(neto)),
                            style = MaterialTheme.typography.labelLarge.merge(EstiloMonto),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(movs, key = { it.id }) { m ->
                FilaMovimiento(m, onClick = { onAbrir(m) }, mostrarFecha = false)
            }
        }
    }
}
