@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pe.misfinanzas.app.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pe.misfinanzas.app.ui.pantallas.PantallaFormulario
import pe.misfinanzas.app.ui.pantallas.PantallaInicio
import pe.misfinanzas.app.ui.pantallas.PantallaMovimientos
import pe.misfinanzas.app.ui.pantallas.PantallaPresupuestos

enum class Seccion(val titulo: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Outlined.Home),
    MOVIMIENTOS("Movimientos", Icons.Outlined.Receipt),
    PRESUPUESTOS("Presupuestos", Icons.Outlined.PieChart),
}

/** Id especial: el formulario está cerrado. 0 = nuevo movimiento. */
private const val FORM_CERRADO = -1L

@Composable
fun AppFinanzas(vm: FinanzasViewModel = viewModel()) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var seccion by rememberSaveable { mutableStateOf(Seccion.INICIO) }
    var formId by rememberSaveable { mutableLongStateOf(FORM_CERRADO) }
    var menu by remember { mutableStateOf(false) }
    var confirmarBorrarTodo by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current

    fun aviso(texto: String) { scope.launch { snackbar.showSnackbar(texto) } }

    if (formId != FORM_CERRADO) {
        BackHandler { formId = FORM_CERRADO }
        PantallaFormulario(
            inicial = estado.movimientos.find { it.id == formId },
            onGuardar = { vm.guardar(it); formId = FORM_CERRADO; aviso("Movimiento guardado") },
            onEliminar = { vm.eliminar(it); formId = FORM_CERRADO; aviso("Movimiento eliminado") },
            onCerrar = { formId = FORM_CERRADO },
        )
        return
    }

    if (seccion != Seccion.INICIO) BackHandler { seccion = Seccion.INICIO }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (seccion == Seccion.INICIO) "Mis Finanzas" else seccion.titulo) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Exportar respaldo (CSV)") },
                            enabled = estado.movimientos.isNotEmpty(),
                            onClick = { menu = false; compartirCsv(contexto, vm.csv()) },
                        )
                        DropdownMenuItem(
                            text = { Text("Cargar datos de ejemplo") },
                            onClick = { menu = false; vm.cargarEjemplo(); aviso("Se cargaron 6 meses de ejemplo") },
                        )
                        DropdownMenuItem(
                            text = { Text("Borrar todos los movimientos") },
                            enabled = estado.movimientos.isNotEmpty(),
                            onClick = { menu = false; confirmarBorrarTodo = true },
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Seccion.entries.forEach { s ->
                    NavigationBarItem(
                        selected = seccion == s,
                        onClick = { seccion = s },
                        icon = { Icon(s.icono, contentDescription = null) },
                        label = { Text(s.titulo) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (seccion != Seccion.PRESUPUESTOS) {
                ExtendedFloatingActionButton(
                    onClick = { formId = 0L },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nuevo") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when (seccion) {
            Seccion.INICIO -> PantallaInicio(
                estado, padding,
                onCambiarMes = vm::cambiarMes,
                onAbrir = { formId = it.id },
                onVerTodos = { seccion = Seccion.MOVIMIENTOS },
            )
            Seccion.MOVIMIENTOS -> PantallaMovimientos(
                estado, padding,
                onCambiarMes = vm::cambiarMes,
                onAbrir = { formId = it.id },
            )
            Seccion.PRESUPUESTOS -> PantallaPresupuestos(
                estado, padding,
                onCambiarMes = vm::cambiarMes,
                onGuardar = { cat, monto -> vm.guardarPresupuesto(cat, monto); aviso("Presupuesto actualizado") },
            )
        }
    }

    if (confirmarBorrarTodo) {
        AlertDialog(
            onDismissRequest = { confirmarBorrarTodo = false },
            title = { Text("¿Borrar todos los movimientos?") },
            text = { Text("Se eliminarán ${estado.movimientos.size} movimientos. Exporta un respaldo antes si lo necesitas. Tus presupuestos se conservan.") },
            confirmButton = {
                TextButton(onClick = { confirmarBorrarTodo = false; vm.borrarTodo(); aviso("Movimientos borrados") }) {
                    Text("Borrar todo")
                }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrarTodo = false }) { Text("Cancelar") } },
        )
    }
}

private fun compartirCsv(contexto: Context, csv: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Respaldo Mis Finanzas (CSV)")
        putExtra(Intent.EXTRA_TEXT, csv)
    }
    contexto.startActivity(Intent.createChooser(intent, "Exportar movimientos"))
}
