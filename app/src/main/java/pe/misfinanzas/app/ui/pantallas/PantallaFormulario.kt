@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package pe.misfinanzas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.misfinanzas.app.data.Categorias
import pe.misfinanzas.app.data.Movimiento
import pe.misfinanzas.app.data.Tipo
import pe.misfinanzas.app.ui.Etiqueta
import pe.misfinanzas.app.ui.colorTipo
import pe.misfinanzas.app.ui.fechaBoton
import pe.misfinanzas.app.ui.theme.EstiloMonto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun PantallaFormulario(
    inicial: Movimiento?,
    onGuardar: (Movimiento) -> Unit,
    onEliminar: (Long) -> Unit,
    onCerrar: () -> Unit,
) {
    var tipo by rememberSaveable { mutableStateOf(inicial?.tipo ?: Tipo.GASTO) }
    var montoTxt by rememberSaveable {
        mutableStateOf(inicial?.monto?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: "")
    }
    var categoria by rememberSaveable { mutableStateOf(inicial?.categoria ?: Categorias.gastos.first()) }
    var fecha by rememberSaveable { mutableStateOf(inicial?.fecha ?: LocalDate.now()) }
    var detalle by rememberSaveable { mutableStateOf(inicial?.detalle ?: "") }
    var verCalendario by rememberSaveable { mutableStateOf(false) }
    var confirmarBorrar by rememberSaveable { mutableStateOf(false) }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    val monto = montoTxt.replace(",", ".").toDoubleOrNull()
    val montoValido = monto != null && monto > 0
    val color = colorTipo(tipo)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inicial == null) "Nuevo movimiento" else "Editar movimiento") },
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (inicial != null) {
                        IconButton(onClick = { confirmarBorrar = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Tipo.GASTO to "Gasto", Tipo.INGRESO to "Ingreso").forEach { (t, txt) ->
                    FilterChip(
                        selected = tipo == t,
                        onClick = {
                            tipo = t
                            if (categoria !in Categorias.de(t)) categoria = Categorias.de(t).first()
                        },
                        label = { Text(txt) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorTipo(t).copy(alpha = 0.16f),
                            selectedLabelColor = colorTipo(t),
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = montoTxt,
                onValueChange = { nuevo -> montoTxt = nuevo.filter { it.isDigit() || it == '.' || it == ',' } },
                label = { Text("Monto") },
                prefix = { Text("S/ ") },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.merge(EstiloMonto).copy(color = color),
                isError = intentoGuardar && !montoValido,
                supportingText = { if (intentoGuardar && !montoValido) Text("Ingresa un monto mayor a cero.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta("Categoría")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Categorias.de(tipo).forEach { c ->
                        FilterChip(
                            selected = categoria == c,
                            onClick = { categoria = c },
                            label = { Text(c) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta("Fecha")
                OutlinedButton(onClick = { verCalendario = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Text(fechaBoton(fecha), Modifier.padding(start = 8.dp))
                }
            }

            OutlinedTextField(
                value = detalle,
                onValueChange = { if (it.length <= 80) detalle = it },
                label = { Text("Detalle (opcional)") },
                placeholder = { Text("Ej. Mercado, taxi, recibo de luz") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    intentoGuardar = true
                    if (montoValido) {
                        onGuardar(
                            Movimiento(
                                id = inicial?.id ?: 0,
                                tipo = tipo,
                                monto = Math.round(monto!! * 100) / 100.0,
                                categoria = categoria,
                                fecha = fecha,
                                detalle = detalle.trim(),
                                creado = inicial?.creado ?: System.currentTimeMillis(),
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(if (inicial == null) "Guardar" else "Guardar cambios", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (verCalendario) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { verCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let {
                        fecha = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    verCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { verCalendario = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (confirmarBorrar && inicial != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrar = false },
            title = { Text("¿Eliminar este movimiento?") },
            text = { Text("No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirmarBorrar = false; onEliminar(inicial.id) }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrar = false }) { Text("Cancelar") } },
        )
    }
}
