package pe.misfinanzas.app.ui.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.misfinanzas.app.data.Categorias
import pe.misfinanzas.app.ui.EstadoUi
import pe.misfinanzas.app.ui.Etiqueta
import pe.misfinanzas.app.ui.SelectorMes
import pe.misfinanzas.app.ui.Tarjeta
import pe.misfinanzas.app.ui.solesCorto
import pe.misfinanzas.app.ui.theme.ColoresFinanzas
import pe.misfinanzas.app.ui.theme.EstiloMonto

@Composable
fun PantallaPresupuestos(
    estado: EstadoUi,
    padding: PaddingValues,
    onCambiarMes: (Long) -> Unit,
    onGuardar: (String, Double) -> Unit,
) {
    val gastado = estado.gastoPorCategoria()
    val totalPres = Categorias.gastos.sumOf { estado.presupuestos[it] ?: 0.0 }
    val totalGast = gastado.values.sum()
    var editando by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SelectorMes(estado.mes, onCambiarMes) }
        item {
            val p = if (totalPres > 0) totalGast / totalPres else 0.0
            Tarjeta {
                Etiqueta("Gastado del presupuesto total")
                Text(
                    "${solesCorto(totalGast)} de ${solesCorto(totalPres)}",
                    style = MaterialTheme.typography.titleLarge.merge(EstiloMonto),
                )
                BarraProgreso(p)
                Text(
                    if (totalPres - totalGast >= 0) "Te quedan ${solesCorto(totalPres - totalGast)} este mes"
                    else "Te pasaste por ${solesCorto(totalGast - totalPres)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Text(
                "Toca una categoría para cambiar su límite mensual.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(Categorias.gastos) { cat ->
            val pres = estado.presupuestos[cat] ?: 0.0
            val gast = gastado[cat] ?: 0.0
            val p = if (pres > 0) gast / pres else if (gast > 0) 2.0 else 0.0
            Tarjeta(Modifier.clickable { editando = cat }) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(cat, style = MaterialTheme.typography.titleMedium)
                    EstadoChip(p, pres)
                }
                BarraProgreso(p)
                Text(
                    "${solesCorto(gast)} de ${solesCorto(pres)}",
                    style = MaterialTheme.typography.bodyMedium.merge(EstiloMonto),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    editando?.let { cat ->
        DialogoPresupuesto(
            categoria = cat,
            actual = estado.presupuestos[cat] ?: 0.0,
            onCerrar = { editando = null },
            onGuardar = { onGuardar(cat, it); editando = null },
        )
    }
}

@Composable
private fun colorProgreso(p: Double): Color = when {
    p > 1.0 -> ColoresFinanzas.gasto
    p >= 0.85 -> ColoresFinanzas.alerta
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun BarraProgreso(p: Double) {
    LinearProgressIndicator(
        progress = { p.toFloat().coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = colorProgreso(p),
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

@Composable
private fun EstadoChip(p: Double, pres: Double) {
    val (texto, color) = when {
        pres <= 0 && p > 0 -> "Sin límite" to ColoresFinanzas.alerta
        p > 1.0 -> "Excedido" to ColoresFinanzas.gasto
        p >= 0.85 -> "Cerca del límite" to ColoresFinanzas.alerta
        else -> "${(p * 100).toInt()} %" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(texto, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun DialogoPresupuesto(
    categoria: String,
    actual: Double,
    onCerrar: () -> Unit,
    onGuardar: (Double) -> Unit,
) {
    var texto by rememberSaveable { mutableStateOf(if (actual > 0) actual.toBigDecimal().stripTrailingZeros().toPlainString() else "") }
    val valor = texto.replace(",", ".").toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Presupuesto de $categoria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Cuánto quieres gastar como máximo al mes en esta categoría?")
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    prefix = { Text("S/ ") },
                    singleLine = true,
                    isError = texto.isNotEmpty() && (valor == null || valor < 0),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(valor ?: 0.0) }, enabled = texto.isEmpty() || (valor != null && valor >= 0)) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } },
    )
}
