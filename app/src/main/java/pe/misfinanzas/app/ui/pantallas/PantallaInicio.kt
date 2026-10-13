package pe.misfinanzas.app.ui.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.misfinanzas.app.data.Movimiento
import pe.misfinanzas.app.data.Tipo
import pe.misfinanzas.app.ui.EstadoUi
import pe.misfinanzas.app.ui.EstadoVacio
import pe.misfinanzas.app.ui.Etiqueta
import pe.misfinanzas.app.ui.FilaMovimiento
import pe.misfinanzas.app.ui.SelectorMes
import pe.misfinanzas.app.ui.Tarjeta
import pe.misfinanzas.app.ui.mesCorto
import pe.misfinanzas.app.ui.mesTitulo
import pe.misfinanzas.app.ui.soles
import pe.misfinanzas.app.ui.solesCorto
import pe.misfinanzas.app.ui.theme.ColoresFinanzas
import pe.misfinanzas.app.ui.theme.EstiloMonto
import kotlin.math.max
import kotlin.math.min

@Composable
fun PantallaInicio(
    estado: EstadoUi,
    padding: PaddingValues,
    onCambiarMes: (Long) -> Unit,
    onAbrir: (Movimiento) -> Unit,
    onVerTodos: () -> Unit,
) {
    val ingresos = estado.total(Tipo.INGRESO)
    val gastos = estado.total(Tipo.GASTO)
    val delMes = estado.delMes()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SelectorMes(estado.mes, onCambiarMes) }
        item { TarjetaBalance(ingresos, gastos) }

        if (estado.cargado && delMes.isEmpty()) {
            item {
                Tarjeta {
                    EstadoVacio(
                        "Sin movimientos en ${mesTitulo(estado.mes)}",
                        "Toca «Nuevo» para registrar tu primer ingreso o gasto del mes.",
                    )
                }
            }
        } else {
            item { TarjetaCategorias(estado.gastoPorCategoria(), gastos) }
        }
        item { TarjetaTendencia(estado) }

        if (delMes.isNotEmpty()) {
            item {
                Tarjeta {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onVerTodos) { Text("Ver todos") }
                    }
                    Column {
                        delMes.take(5).forEach { FilaMovimiento(it, onClick = { onAbrir(it) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaBalance(ingresos: Double, gastos: Double) {
    val balance = ingresos - gastos
    val tasa = if (ingresos > 0) (balance / ingresos * 100) else 0.0
    val colorBalance = if (balance >= 0) ColoresFinanzas.ingreso else ColoresFinanzas.gasto
    Tarjeta {
        Etiqueta("Balance del mes")
        Text(
            soles(balance),
            style = MaterialTheme.typography.headlineMedium.merge(EstiloMonto),
            color = colorBalance, fontWeight = FontWeight.SemiBold,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Etiqueta("Ingresos")
                Text(soles(ingresos), style = EstiloMonto, color = ColoresFinanzas.ingreso)
            }
            Column(Modifier.weight(1f)) {
                Etiqueta("Gastos")
                Text(soles(gastos), style = EstiloMonto, color = ColoresFinanzas.gasto)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tasa de ahorro", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (ingresos > 0) String.format("%.1f %%", tasa) else "—",
                    style = EstiloMonto,
                    color = when {
                        ingresos <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                        tasa >= 20 -> ColoresFinanzas.ingreso
                        tasa >= 0 -> ColoresFinanzas.alerta
                        else -> ColoresFinanzas.gasto
                    },
                )
            }
            LinearProgressIndicator(
                progress = { (tasa / 20.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (tasa >= 20) ColoresFinanzas.ingreso else ColoresFinanzas.alerta,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            Text(
                "Meta recomendada: ahorrar al menos el 20 % de tus ingresos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TarjetaCategorias(porCategoria: Map<String, Double>, total: Double) {
    if (porCategoria.isEmpty()) return
    val orden = porCategoria.entries.sortedByDescending { it.value }.take(6)
    val maximo = orden.first().value
    val color = MaterialTheme.colorScheme.primary
    Tarjeta {
        Text("¿En qué se va tu dinero?", style = MaterialTheme.typography.titleMedium)
        orden.forEach { (cat, monto) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cat, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${solesCorto(monto)} · ${if (total > 0) (monto / total * 100).toInt() else 0} %",
                        style = MaterialTheme.typography.bodySmall.merge(EstiloMonto),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    Modifier.fillMaxWidth().height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        Modifier.fillMaxWidth((monto / maximo).toFloat().coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .background(color, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaTendencia(estado: EstadoUi) {
    val meses = (5 downTo 0).map { estado.mes.minusMonths(it.toLong()) }
    val serie = meses.map { Triple(it, estado.total(Tipo.INGRESO, it), estado.total(Tipo.GASTO, it)) }
    val maximo = max(serie.maxOf { max(it.second, it.third) }, 1.0)
    val cIng = ColoresFinanzas.ingreso
    val cGas = ColoresFinanzas.gasto
    val cLinea = MaterialTheme.colorScheme.outlineVariant

    Tarjeta {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Últimos 6 meses", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Leyenda(cIng, "Ingresos")
                Leyenda(cGas, "Gastos")
            }
        }
        Text(
            "Máximo: ${solesCorto(maximo)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            for (i in 0..3) {
                val y = size.height * i / 3f
                drawLine(cLinea, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            val slot = size.width / serie.size
            val ancho = min(slot / 3.2f, 16.dp.toPx())
            val separacion = 2.dp.toPx()
            serie.forEachIndexed { i, (_, ing, gas) ->
                val cx = slot * i + slot / 2
                val alfa = if (i == serie.lastIndex) 1f else 0.7f
                val hI = (ing / maximo * size.height).toFloat()
                val hG = (gas / maximo * size.height).toFloat()
                drawRoundRect(
                    cIng.copy(alpha = alfa), topLeft = Offset(cx - ancho - separacion, size.height - hI),
                    size = Size(ancho, hI), cornerRadius = CornerRadius(4.dp.toPx()),
                )
                drawRoundRect(
                    cGas.copy(alpha = alfa), topLeft = Offset(cx + separacion, size.height - hG),
                    size = Size(ancho, hG), cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            serie.forEachIndexed { i, (m, _, _) ->
                Text(
                    mesCorto(m), modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (i == serie.lastIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (i == serie.lastIndex) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
